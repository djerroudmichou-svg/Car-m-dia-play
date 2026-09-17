package com.example.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.example.data.MediaDatabase
import com.example.data.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

data class UsbEvent(
    val isConnected: Boolean,
    val deviceName: String
)

class UsbMediaReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "UsbMediaReceiver"
        
        // Interactive live callback to trigger viewmodel reload immediately if active
        var onUsbChangedCallback: (() -> Unit)? = null
        var onUsbEventCallback: ((UsbEvent) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "UsbMediaReceiver action received: $action")

        val isConnected = action == Intent.ACTION_MEDIA_MOUNTED ||
                action == "android.hardware.usb.action.USB_DEVICE_ATTACHED"

        val isDisconnected = action == Intent.ACTION_MEDIA_UNMOUNTED ||
                action == Intent.ACTION_MEDIA_EJECT ||
                action == "android.hardware.usb.action.USB_DEVICE_DETACHED"

        if (isConnected || isDisconnected || action == Intent.ACTION_BOOT_COMPLETED) {
            // Extract USB / Storage device name
            var devName = "USB"
            try {
                @Suppress("DEPRECATION")
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (device != null) {
                    devName = device.productName ?: device.deviceName ?: "USB Drive"
                } else {
                    val path = intent.data?.path
                    if (!path.isNullOrEmpty()) {
                        val seg = File(path).name
                        if (seg.isNotBlank() && seg != "media") {
                            devName = seg
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error resolving USB name", e)
            }
            
            // Check if we should auto-launch (Connection event OR Boot with existing USB)
            val prefs = context.getSharedPreferences(
                com.example.theme.CarThemeManager.PREFS_NAME,
                Context.MODE_PRIVATE
            )
            val autoLaunchPref = prefs.getBoolean(
                com.example.theme.CarThemeManager.KEY_AUTO_LAUNCH_ON_USB,
                true
            )

            if (isConnected || (action == Intent.ACTION_BOOT_COMPLETED && autoLaunchPref)) {
                if (isConnected) {
                    onUsbEventCallback?.invoke(UsbEvent(isConnected = true, deviceName = devName))
                }

                // Auto launch app if preference is enabled
                if (autoLaunchPref) {
                    try {
                        val launchIntent = context.packageManager
                            .getLaunchIntentForPackage(context.packageName)
                            ?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            }
                        if (launchIntent != null) {
                            Log.i(TAG, "Auto-launching Car Media Player (Action: $action)")
                            context.startActivity(launchIntent)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to auto-launch app", e)
                    }
                }
            } else if (isDisconnected) {
                onUsbEventCallback?.invoke(UsbEvent(isConnected = false, deviceName = devName))
            }

            // Notify UI
            onUsbChangedCallback?.invoke()

            // Run database scanning on background coroutine
            val db = MediaDatabase.getDatabase(context.applicationContext)
            val repo = MediaRepository(db)
            val scanner = UsbMediaScanner(context.applicationContext, repo)
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Starting background volume sync from receiver action: $action")
                    scanner.syncAllMountedVolumes()
                    // Re-notify callback after sync finishes
                    onUsbChangedCallback?.invoke()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in UsbMediaReceiver sync", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
