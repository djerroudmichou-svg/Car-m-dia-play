package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MediaDatabase
import com.example.data.MediaRepository
import com.example.localization.LocalAppStrings
import com.example.scanner.UsbMediaScanner
import com.example.scanner.UsbMediaReceiver
import com.example.service.PlaybackService
import com.example.swc.SwcKeyTracker
import com.example.theme.LocalCarColors
import com.example.theme.getCarColors
import com.example.ui.CarMediaPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var database: MediaDatabase
    private lateinit var repository: MediaRepository
    private lateinit var scanner: UsbMediaScanner
    private var swcKeyTracker: SwcKeyTracker? = null

    private val viewModel: MediaViewModel by viewModels {
        MediaViewModel.Factory(application, repository, scanner)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if launched by USB and if auto-launch is disabled
        if (intent?.action == "android.hardware.usb.action.USB_DEVICE_ATTACHED") {
            val prefs = getSharedPreferences(com.example.theme.CarThemeManager.PREFS_NAME, MODE_PRIVATE)
            val autoLaunch = prefs.getBoolean(com.example.theme.CarThemeManager.KEY_AUTO_LAUNCH_ON_USB, true)
            if (!autoLaunch) {
                Log.i(TAG, "USB auto-launch is disabled in settings, finishing MainActivity")
                finish()
                return
            }
        }

        // Keep screen on continuously when opening the app (prevents screen timeout/turn off in car)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        // 1. Initialize DB, Repo, and Scanner
        database = MediaDatabase.getDatabase(this)
        repository = MediaRepository(database)
        scanner = UsbMediaScanner(this, repository)

        // 2. Initialize Steering Wheel Controls Key Tracker from ViewModel
        swcKeyTracker = viewModel.swcKeyTracker

        // 3. Start Media3 Playback Service & connect SWC / Session callbacks
        startPlaybackService()
        PlaybackService.onNextTrackRequested = {
            runOnUiThread { viewModel.playNext() }
        }
        PlaybackService.onPrevTrackRequested = {
            runOnUiThread { viewModel.playPrevious() }
        }
        PlaybackService.onPlayPauseRequested = {
            runOnUiThread { viewModel.togglePlayPause() }
        }

        // 4. Register USB mount and change listeners to refresh ViewModel lists & show notifications
        UsbMediaReceiver.onUsbChangedCallback = {
            viewModel.triggerScan()
        }
        UsbMediaReceiver.onUsbEventCallback = { usbEvent ->
            runOnUiThread {
                val fmt = if (usbEvent.isConnected) {
                    viewModel.appStrings.value.usbConnectedToast
                } else {
                    viewModel.appStrings.value.usbDisconnectedToast
                }
                val msg = String.format(fmt, usbEvent.deviceName)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.handleUsbEvent(usbEvent)
            }
        }

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsStateWithLifecycle()
            val isFullscreenMode by viewModel.isFullscreenMode.collectAsStateWithLifecycle()
            val dynamicAccentColor by viewModel.dynamicAccentColor.collectAsStateWithLifecycle()
            val carColors = androidx.compose.runtime.remember(isDarkTheme, dynamicAccentColor) {
                getCarColors(isDarkTheme, dynamicAccentColor)
            }

            // Dynamically synchronize window keep-screen-on state with preference
            LaunchedEffect(isKeepScreenOn) {
                if (isKeepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // Dynamically synchronize immersive fullscreen mode
            LaunchedEffect(isFullscreenMode) {
                applyImmersiveFullscreen(isFullscreenMode)
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
                val appStrings by viewModel.appStrings.collectAsStateWithLifecycle()
                val screenScale by viewModel.screenScale.collectAsStateWithLifecycle()
                val fontSizeScale by viewModel.fontSizeScale.collectAsStateWithLifecycle()
                val currentDensity = androidx.compose.ui.platform.LocalDensity.current
                val customDensity = androidx.compose.runtime.remember(currentDensity, screenScale, fontSizeScale) {
                    androidx.compose.ui.unit.Density(
                        density = currentDensity.density * screenScale,
                        fontScale = currentDensity.fontScale * fontSizeScale
                    )
                }
                val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalDensity provides customDensity,
                    LocalLayoutDirection provides layoutDirection,
                    LocalAppStrings provides appStrings,
                    LocalCarColors provides carColors
                ) {
                    Surface(modifier = Modifier.fillMaxSize(), color = carColors.background) {
                        // Check and Request Permissions
                        val permissionsToRequest = mutableListOf<String>().apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                                add(Manifest.permission.READ_MEDIA_AUDIO)
                                add(Manifest.permission.READ_MEDIA_VIDEO)
                            } else {
                                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }

                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions ->
                            val allGranted = permissions.values.all { it }
                            if (allGranted) {
                                // Handled by the LaunchedEffect below
                            } else {
                                Log.w(TAG, "Not all storage/notification permissions were granted")
                            }
                        }

                        LaunchedEffect(Unit) {
                            // Request permissions dynamically
                            try {
                                launcher.launch(permissionsToRequest.toTypedArray())
                            } catch (e: Exception) {
                                Log.e(TAG, "Error launching permissions request", e)
                            }

                            // On Android 11+, special MANAGE_EXTERNAL_STORAGE might be required for random OTG directories
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    if (!Environment.isExternalStorageManager()) {
                                        try {
                                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                data = Uri.parse("package:$packageName")
                                            }
                                            startActivity(intent)
                                        } catch (e: Exception) {
                                            try {
                                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                                startActivity(intent)
                                            } catch (e2: Exception) {
                                                Log.w(TAG, "MANAGE_ALL_FILES_ACCESS_PERMISSION intent not supported on this vehicle unit", e2)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Storage manager check error", e)
                                }
                            }
                            
                            // Run scan automatically on load after a small delay to prioritize UI rendering
                            kotlinx.coroutines.delay(800)
                            viewModel.triggerScan()
                        }

                        CarMediaPlayerScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    /**
     * Intercepts and monitors Steering Wheel Controls (SWC) key events.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val tracker = swcKeyTracker
        if (tracker != null && tracker.handleKeyEvent(event)) {
            return true // Consume event
        }
        return super.dispatchKeyEvent(event)
    }

    private fun startPlaybackService() {
        try {
            val intent = Intent(this, PlaybackService::class.java)
            startService(intent)
            Log.d(TAG, "PlaybackService started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start PlaybackService", e)
        }
    }

    private fun applyImmersiveFullscreen(enable: Boolean) {
        try {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (enable) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying fullscreen mode", e)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && viewModel.isFullscreenMode.value) {
            applyImmersiveFullscreen(true)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == "android.hardware.usb.action.USB_DEVICE_ATTACHED") {
            val prefs = getSharedPreferences(com.example.theme.CarThemeManager.PREFS_NAME, MODE_PRIVATE)
            val autoLaunch = prefs.getBoolean(com.example.theme.CarThemeManager.KEY_AUTO_LAUNCH_ON_USB, true)
            if (!autoLaunch) {
                Log.i(TAG, "USB auto-launch is disabled in settings, ignoring onNewIntent for USB")
                return
            }
            Log.i(TAG, "USB Device attached via onNewIntent")
            viewModel.triggerScan()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.savePlaybackState()
    }

    override fun onDestroy() {
        viewModel.savePlaybackState()
        PlaybackService.onNextTrackRequested = null
        PlaybackService.onPrevTrackRequested = null
        PlaybackService.onPlayPauseRequested = null
        UsbMediaReceiver.onUsbChangedCallback = null
        UsbMediaReceiver.onUsbEventCallback = null
        super.onDestroy()
    }
}
