package com.example.swc

import android.util.Log
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clean, lightweight Standard Android Media Key Handler for Steering Wheel Controls (SWC).
 * 
 * In modern Android head units and car tablets, physical steering wheel buttons are automatically
 * translated by the vehicle CAN-Bus decoder and tablet OS into standard system Media KeyEvents:
 * - KEYCODE_MEDIA_PLAY
 * - KEYCODE_MEDIA_PAUSE
 * - KEYCODE_MEDIA_PLAY_PAUSE
 * - KEYCODE_MEDIA_NEXT
 * - KEYCODE_MEDIA_PREVIOUS
 * - KEYCODE_HEADSETHOOK
 * - KEYCODE_MEDIA_STOP
 *
 * This handler eliminates fragile direct CAN-bus parsing and complex multi-button combos,
 * relying strictly on standard Android Media Key handling and MediaSession callbacks.
 */
class SwcKeyTracker(
    private var config: SwcConfig = SwcConfig(),
    private val onPlayPauseTriggered: () -> Unit,
    private val onNextTrack: () -> Unit = {},
    private val onPrevTrack: () -> Unit = {},
    private val onFastForwardStart: () -> Unit = {},
    private val onFastForwardEnd: () -> Unit = {},
    private val onRewindStart: () -> Unit = {},
    private val onRewindEnd: () -> Unit = {},
    private val onSeekStepForward: (seconds: Int) -> Unit = {},
    private val onSeekStepBackward: (seconds: Int) -> Unit = {},
    private val onSkipNextFolder: () -> Unit = {},
    private val onSkipPrevFolder: () -> Unit = {}
) {
    companion object {
        private const val TAG = "SwcKeyTracker"
    }

    // Live Key Log for on-screen user testing in Settings
    private val _liveKeyLog = MutableStateFlow<SwcLiveKeyLog?>(null)
    val liveKeyLog: StateFlow<SwcLiveKeyLog?> = _liveKeyLog.asStateFlow()

    fun updateConfig(newConfig: SwcConfig) {
        this.config = newConfig
    }

    /**
     * Handles key event if it is a standard Android media key.
     * Returns true if consumed, false if passed to the Android system.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action
        val actionStr = if (action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"
        val keyName = KeyEvent.keyCodeToString(keyCode)

        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Play / Pause")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    Log.i(TAG, "Standard SWC Media Play/Pause triggered ($keyName)")
                    onPlayPauseTriggered()
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Play")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    Log.i(TAG, "Standard SWC Media Play triggered ($keyName)")
                    onPlayPauseTriggered()
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Pause")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    Log.i(TAG, "Standard SWC Media Pause triggered ($keyName)")
                    onPlayPauseTriggered()
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Next Track")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    Log.i(TAG, "Standard SWC Media Next triggered ($keyName)")
                    onNextTrack()
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Previous Track")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    Log.i(TAG, "Standard SWC Media Previous triggered ($keyName)")
                    onPrevTrack()
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Fast-Forward >>")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onFastForwardStart()
                else if (action == KeyEvent.ACTION_UP) onFastForwardEnd()
                return true
            }

            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Rewind <<")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onRewindStart()
                else if (action == KeyEvent.ACTION_UP) onRewindEnd()
                return true
            }

            KeyEvent.KEYCODE_MEDIA_STOP -> {
                _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, actionStr, "Stop")
                if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    onPlayPauseTriggered()
                }
                return true
            }

            else -> {
                // Non-media key: Log for diagnostic testing if pressed, but do NOT intercept
                if (action == KeyEvent.ACTION_DOWN) {
                    _liveKeyLog.value = SwcLiveKeyLog(keyCode, keyName, "DOWN", "System Key (Pass-Through)")
                }
                return false
            }
        }
    }
}
