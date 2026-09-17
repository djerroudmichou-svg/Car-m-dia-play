package com.example.swc

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SwcKeyTracker(
    private var config: SwcConfig = SwcConfig(),
    private val onPlayPauseComboTriggered: () -> Unit,
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
        private const val COMBO_COOLDOWN_MS = 900L
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    // Live Key Log for UI Testing
    private val _liveKeyLog = MutableStateFlow<SwcLiveKeyLog?>(null)
    val liveKeyLog: StateFlow<SwcLiveKeyLog?> = _liveKeyLog.asStateFlow()

    fun updateConfig(newConfig: SwcConfig) {
        this.config = newConfig
    }

    private var isNextDown = false
    private var nextDownTime = 0L
    private var isNextSeeking = false
    private var isNextLongActionTriggered = false

    private var isPrevDown = false
    private var prevDownTime = 0L
    private var isPrevSeeking = false
    private var isPrevLongActionTriggered = false

    private var isComboConsumed = false
    private var lastComboTimestamp = 0L

    private var lastKeyActionTime = 0L
    private var lastKeyCode = -1
    private var lastKeyAction = -1

    private val nextHoldRunnable = Runnable {
        if (isNextDown && !isComboConsumed && (System.currentTimeMillis() - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
            when (config.longPressMode) {
                SwcLongPressMode.CONTINUOUS_SEEK -> {
                    isNextSeeking = true
                    Log.i(TAG, "SWC Next Long-Press -> Continuous Fast-Forward >>")
                    onFastForwardStart()
                }
                SwcLongPressMode.STEP_SEEK -> {
                    isNextLongActionTriggered = true
                    Log.i(TAG, "SWC Next Long-Press -> Jump +${config.seekStepSeconds}s")
                    onSeekStepForward(config.seekStepSeconds)
                }
                SwcLongPressMode.SKIP_FOLDER -> {
                    isNextLongActionTriggered = true
                    Log.i(TAG, "SWC Next Long-Press -> Skip to Next Folder")
                    onSkipNextFolder()
                }
                SwcLongPressMode.DISABLED -> {
                    // Do nothing on hold
                }
            }
        }
    }

    private val prevHoldRunnable = Runnable {
        if (isPrevDown && !isComboConsumed && (System.currentTimeMillis() - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
            when (config.longPressMode) {
                SwcLongPressMode.CONTINUOUS_SEEK -> {
                    isPrevSeeking = true
                    Log.i(TAG, "SWC Prev Long-Press -> Continuous Rewind <<")
                    onRewindStart()
                }
                SwcLongPressMode.STEP_SEEK -> {
                    isPrevLongActionTriggered = true
                    Log.i(TAG, "SWC Prev Long-Press -> Jump -${config.seekStepSeconds}s")
                    onSeekStepBackward(config.seekStepSeconds)
                }
                SwcLongPressMode.SKIP_FOLDER -> {
                    isPrevLongActionTriggered = true
                    Log.i(TAG, "SWC Prev Long-Press -> Skip to Prev Folder")
                    onSkipPrevFolder()
                }
                SwcLongPressMode.DISABLED -> {
                    // Do nothing on hold
                }
            }
        }
    }

    private fun isRawNextKey(keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) return true
        if (keyCode == config.customButton1KeyCode && config.customButton1KeyCode != 0) return true
        
        if (config.acceptExtendedKeys) {
            return keyCode == KeyEvent.KEYCODE_CHANNEL_UP ||
                   keyCode == KeyEvent.KEYCODE_NAVIGATE_NEXT ||
                   keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                   keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                   keyCode == KeyEvent.KEYCODE_PAGE_DOWN ||
                   keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ||
                   keyCode == KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD ||
                   keyCode == KeyEvent.KEYCODE_NAVIGATE_IN ||
                   keyCode == KeyEvent.KEYCODE_BUTTON_R1 ||
                   keyCode == KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK ||
                   keyCode == KeyEvent.KEYCODE_PLUS ||
                   keyCode == KeyEvent.KEYCODE_NUMPAD_ADD
        }
        return false
    }

    private fun isRawPrevKey(keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) return true
        if (keyCode == config.customButton2KeyCode && config.customButton2KeyCode != 0) return true

        if (config.acceptExtendedKeys) {
            return keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
                   keyCode == KeyEvent.KEYCODE_NAVIGATE_PREVIOUS ||
                   keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                   keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                   keyCode == KeyEvent.KEYCODE_PAGE_UP ||
                   keyCode == KeyEvent.KEYCODE_MEDIA_REWIND ||
                   keyCode == KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD ||
                   keyCode == KeyEvent.KEYCODE_NAVIGATE_OUT ||
                   keyCode == KeyEvent.KEYCODE_BUTTON_L1 ||
                   keyCode == KeyEvent.KEYCODE_MINUS ||
                   keyCode == KeyEvent.KEYCODE_NUMPAD_SUBTRACT
        }
        return false
    }

    private fun isNextKey(keyCode: Int): Boolean {
        return when (config.canBusWheelMode) {
            CanBusWheelMode.REVERSED -> isRawPrevKey(keyCode)
            else -> isRawNextKey(keyCode)
        }
    }

    private fun isPrevKey(keyCode: Int): Boolean {
        return when (config.canBusWheelMode) {
            CanBusWheelMode.REVERSED -> isRawNextKey(keyCode)
            else -> isRawPrevKey(keyCode)
        }
    }

    private fun isPlayPauseKey(keyCode: Int): Boolean {
        if (keyCode == config.customPlayPauseKeyCode && config.customPlayPauseKeyCode != 0) return true
        return keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
               keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
               keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
               keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE ||
               keyCode == KeyEvent.KEYCODE_MEDIA_STOP ||
               keyCode == KeyEvent.KEYCODE_VOLUME_MUTE ||
               keyCode == KeyEvent.KEYCODE_MUTE ||
               keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
               keyCode == KeyEvent.KEYCODE_ENTER ||
               keyCode == KeyEvent.KEYCODE_MEDIA_CLOSE ||
               keyCode == KeyEvent.KEYCODE_BUTTON_MODE ||
               keyCode == KeyEvent.KEYCODE_BUTTON_SELECT
    }

    /**
     * Intercepts steering wheel keys and dispatches according to vehicle configuration.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action
        val currentTime = System.currentTimeMillis()

        // 1. Debounce filter for aftermarket CAN-bus decoders producing double-taps
        if (event.repeatCount == 0 && keyCode == lastKeyCode && action == lastKeyAction) {
            if (currentTime - lastKeyActionTime < config.debounceMs) {
                Log.d(TAG, "SWC Ignored debounce duplicate key: $keyCode")
                return true
            }
        }
        lastKeyActionTime = currentTime
        lastKeyCode = keyCode
        lastKeyAction = action

        // --- GLOBAL KEY LOGGING (For Debugging & Identification) ---
        // Log EVERY key event to the UI log, even if we don't handle it yet.
        // This helps the user see what the car is actually sending.
        val isHandledKey = isPlayPauseKey(keyCode) || isNextKey(keyCode) || isPrevKey(keyCode) ||
                          keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || 
                          keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
        
        if (!isHandledKey && action == KeyEvent.ACTION_DOWN) {
            _liveKeyLog.value = SwcLiveKeyLog(
                keyCode = keyCode,
                keyName = KeyEvent.keyCodeToString(keyCode),
                action = "DOWN (Unknown)",
                mappedFunction = "Not Mapped - Click to identify"
            )
        }

        // 2. Dedicated Play / Pause / Mute key on vehicle
        if (isPlayPauseKey(keyCode)) {
            val keyStr = KeyEvent.keyCodeToString(keyCode)
            val actionStr = if (action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"
            _liveKeyLog.value = SwcLiveKeyLog(
                keyCode = keyCode,
                keyName = keyStr,
                action = actionStr,
                mappedFunction = "Play/Pause (Dedicated)"
            )

            if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                Log.i(TAG, "SWC Dedicated Play/Pause key ($keyStr) pressed")
                onPlayPauseComboTriggered()
            }
            return true
        }

        // 3. Direct Fast-Forward & Rewind hardware keys
        if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_MEDIA_STEP_FORWARD) {
            val keyStr = KeyEvent.keyCodeToString(keyCode)
            val actionStr = if (action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"
            _liveKeyLog.value = SwcLiveKeyLog(
                keyCode = keyCode,
                keyName = keyStr,
                action = actionStr,
                mappedFunction = "Fast-Forward >>"
            )
            if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onFastForwardStart()
            else if (action == KeyEvent.ACTION_UP) onFastForwardEnd()
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD) {
            val keyStr = KeyEvent.keyCodeToString(keyCode)
            val actionStr = if (action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"
            _liveKeyLog.value = SwcLiveKeyLog(
                keyCode = keyCode,
                keyName = keyStr,
                action = actionStr,
                mappedFunction = "Rewind <<"
            )
            if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onRewindStart()
            else if (action == KeyEvent.ACTION_UP) onRewindEnd()
            return true
        }

        val isNext = isNextKey(keyCode)
        val isPrev = isPrevKey(keyCode)

        if (!isNext && !isPrev) {
            return false
        }

        val keyLabel = if (isNext) "NEXT (التالي)" else "PREVIOUS (السابق)"
        val actionStr = if (action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"

        if (action == KeyEvent.ACTION_DOWN) {
            if (isNext) {
                if (event.repeatCount == 0) {
                    isNextDown = true
                    nextDownTime = currentTime
                    isNextLongActionTriggered = false
                    isComboConsumed = false
                    isNextSeeking = false

                    // If all custom features are disabled, trigger INSTANTLY on DOWN for best responsiveness
                    if (config.dualPressMode == SwcDualPressMode.DISABLED && config.longPressMode == SwcLongPressMode.DISABLED) {
                        Log.i(TAG, "SWC Next (Instant) -> Next track")
                        _liveKeyLog.value = SwcLiveKeyLog(
                            keyCode = keyCode,
                            keyName = KeyEvent.keyCodeToString(keyCode),
                            action = actionStr,
                            mappedFunction = "Next Track (Instant)"
                        )
                        onNextTrack()
                        return true
                    }

                    // Check Dual Press Combo based on user setting
                    if (checkDualPressCombo(isNextIncoming = true, currentTime = currentTime)) {
                        _liveKeyLog.value = SwcLiveKeyLog(
                            keyCode = keyCode,
                            keyName = KeyEvent.keyCodeToString(keyCode),
                            action = actionStr,
                            mappedFunction = "Play/Pause (Dual Button Combo)"
                        )
                        return true
                    }

                    _liveKeyLog.value = SwcLiveKeyLog(
                        keyCode = keyCode,
                        keyName = KeyEvent.keyCodeToString(keyCode),
                        action = actionStr,
                        mappedFunction = "$keyLabel - Press Started"
                    )

                    mainHandler.removeCallbacks(nextHoldRunnable)
                    mainHandler.postDelayed(nextHoldRunnable, config.longPressThresholdMs)
                } else if (event.repeatCount > 0) {
                    // Hardware repeat while held
                    if (!isComboConsumed && !isNextSeeking && !isNextLongActionTriggered && (currentTime - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
                        if (config.longPressMode == SwcLongPressMode.CONTINUOUS_SEEK) {
                            isNextSeeking = true
                            mainHandler.removeCallbacks(nextHoldRunnable)
                            onFastForwardStart()
                        }
                    }
                }
                return true
            } else if (isPrev) {
                if (event.repeatCount == 0) {
                    isPrevDown = true
                    prevDownTime = currentTime
                    isPrevLongActionTriggered = false
                    isComboConsumed = false
                    isPrevSeeking = false

                    // If all custom features are disabled, trigger INSTANTLY on DOWN for best responsiveness
                    if (config.dualPressMode == SwcDualPressMode.DISABLED && config.longPressMode == SwcLongPressMode.DISABLED) {
                        Log.i(TAG, "SWC Prev (Instant) -> Previous track")
                        _liveKeyLog.value = SwcLiveKeyLog(
                            keyCode = keyCode,
                            keyName = KeyEvent.keyCodeToString(keyCode),
                            action = actionStr,
                            mappedFunction = "Previous Track (Instant)"
                        )
                        onPrevTrack()
                        return true
                    }

                    // Check Dual Press Combo based on user setting
                    if (checkDualPressCombo(isNextIncoming = false, currentTime = currentTime)) {
                        _liveKeyLog.value = SwcLiveKeyLog(
                            keyCode = keyCode,
                            keyName = KeyEvent.keyCodeToString(keyCode),
                            action = actionStr,
                            mappedFunction = "Play/Pause (Dual Button Combo)"
                        )
                        return true
                    }

                    _liveKeyLog.value = SwcLiveKeyLog(
                        keyCode = keyCode,
                        keyName = KeyEvent.keyCodeToString(keyCode),
                        action = actionStr,
                        mappedFunction = "$keyLabel - Press Started"
                    )

                    mainHandler.removeCallbacks(prevHoldRunnable)
                    mainHandler.postDelayed(prevHoldRunnable, config.longPressThresholdMs)
                } else if (event.repeatCount > 0) {
                    // Hardware repeat while held
                    if (!isComboConsumed && !isPrevSeeking && !isPrevLongActionTriggered && (currentTime - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
                        if (config.longPressMode == SwcLongPressMode.CONTINUOUS_SEEK) {
                            isPrevSeeking = true
                            mainHandler.removeCallbacks(prevHoldRunnable)
                            onRewindStart()
                        }
                    }
                }
                return true
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (isNext) {
                isNextDown = false
                mainHandler.removeCallbacks(nextHoldRunnable)

                // If already triggered on DOWN (both features disabled), just swallow UP
                if (config.dualPressMode == SwcDualPressMode.DISABLED && config.longPressMode == SwcLongPressMode.DISABLED) {
                    return true
                }

                // If part of combo, swallow cleanly
                if (isComboConsumed || (currentTime - lastComboTimestamp < COMBO_COOLDOWN_MS)) {
                    if (!isPrevDown) isComboConsumed = false
                    return true
                }

                if (isNextSeeking) {
                    isNextSeeking = false
                    Log.i(TAG, "SWC Next released! Stopping fast-forward.")
                    onFastForwardEnd()
                    _liveKeyLog.value = SwcLiveKeyLog(
                        keyCode = keyCode,
                        keyName = KeyEvent.keyCodeToString(keyCode),
                        action = actionStr,
                        mappedFunction = "Fast-Forward Stopped"
                    )
                    return true
                }

                if (isNextLongActionTriggered) {
                    isNextLongActionTriggered = false
                    return true
                }

                // Clean single short tap -> Next track
                if (currentTime - nextDownTime < config.longPressThresholdMs) {
                    Log.i(TAG, "SWC Next short tap -> Next track")
                    _liveKeyLog.value = SwcLiveKeyLog(
                        keyCode = keyCode,
                        keyName = KeyEvent.keyCodeToString(keyCode),
                        action = actionStr,
                        mappedFunction = "Next Track (المقطع التالي)"
                    )
                    onNextTrack()
                }
                return true
            } else if (isPrev) {
                isPrevDown = false
                mainHandler.removeCallbacks(prevHoldRunnable)

                // If already triggered on DOWN (both features disabled), just swallow UP
                if (config.dualPressMode == SwcDualPressMode.DISABLED && config.longPressMode == SwcLongPressMode.DISABLED) {
                    return true
                }

                // If part of combo, swallow cleanly
                if (isComboConsumed || (currentTime - lastComboTimestamp < COMBO_COOLDOWN_MS)) {
                    if (!isNextDown) isComboConsumed = false
                    return true
                }

                if (isPrevSeeking) {
                    isPrevSeeking = false
                    Log.i(TAG, "SWC Prev released! Stopping rewind.")
                    onRewindEnd()
                    _liveKeyLog.value = SwcLiveKeyLog(
                        keyCode = keyCode,
                        keyName = KeyEvent.keyCodeToString(keyCode),
                        action = actionStr,
                        mappedFunction = "Rewind Stopped"
                    )
                    return true
                }

                if (isPrevLongActionTriggered) {
                    isPrevLongActionTriggered = false
                    return true
                }

                // Clean single short tap -> Previous track
                if (currentTime - prevDownTime < config.longPressThresholdMs) {
                    Log.i(TAG, "SWC Prev short tap -> Previous track")
                    _liveKeyLog.value = SwcLiveKeyLog(
                        keyCode = keyCode,
                        keyName = KeyEvent.keyCodeToString(keyCode),
                        action = actionStr,
                        mappedFunction = "Previous Track (المقطع السابق)"
                    )
                    onPrevTrack()
                }
                return true
            }
        }

        return false
    }

    private fun checkDualPressCombo(isNextIncoming: Boolean, currentTime: Long): Boolean {
        val mode = config.dualPressMode
        if (mode == SwcDualPressMode.DISABLED) {
            return false
        }

        val window = config.dualPressWindowMs

        val isSimultaneous = if (isNextIncoming) {
            isPrevDown
        } else {
            isNextDown
        }

        val isSequential = if (isNextIncoming) {
            prevDownTime > 0 && (currentTime - prevDownTime <= window)
        } else {
            nextDownTime > 0 && (currentTime - nextDownTime <= window)
        }

        val isTriggered = when (mode) {
            SwcDualPressMode.SIMULTANEOUS_AND_SEQUENTIAL -> isSimultaneous || isSequential
            SwcDualPressMode.STRICT_SIMULTANEOUS -> isSimultaneous
            SwcDualPressMode.SEQUENTIAL_ONLY -> isSequential
            SwcDualPressMode.DISABLED -> false
        }

        if (isTriggered) {
            triggerComboPlayPause()
            return true
        }

        return false
    }

    private fun triggerComboPlayPause() {
        Log.i(TAG, "SWC Combo (Dual Button Press) detected! Triggering Play/Pause toggle.")
        isComboConsumed = true
        lastComboTimestamp = System.currentTimeMillis()

        mainHandler.removeCallbacks(nextHoldRunnable)
        mainHandler.removeCallbacks(prevHoldRunnable)

        if (isNextSeeking) {
            isNextSeeking = false
            onFastForwardEnd()
        }
        if (isPrevSeeking) {
            isPrevSeeking = false
            onRewindEnd()
        }

        onPlayPauseComboTriggered()
    }
}
