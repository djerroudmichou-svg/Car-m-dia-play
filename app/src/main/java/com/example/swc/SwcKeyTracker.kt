package com.example.swc

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import kotlin.math.abs

class SwcKeyTracker(
    private val onPlayPauseComboTriggered: () -> Unit,
    private val onNextTrack: () -> Unit = {},
    private val onPrevTrack: () -> Unit = {},
    private val onFastForwardStart: () -> Unit = {},
    private val onFastForwardEnd: () -> Unit = {},
    private val onRewindStart: () -> Unit = {},
    private val onRewindEnd: () -> Unit = {}
) {
    companion object {
        private const val TAG = "SwcKeyTracker"
        // Generous window for automotive steering wheel keys
        private const val SIMULTANEOUS_WINDOW_MS = 650L
        private const val LONG_PRESS_THRESHOLD_MS = 450L
        private const val COMBO_COOLDOWN_MS = 1000L
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private var isNextDown = false
    private var nextDownTime = 0L
    private var isNextSeeking = false

    private var isPrevDown = false
    private var prevDownTime = 0L
    private var isPrevSeeking = false

    private var isComboConsumed = false
    private var lastComboTimestamp = 0L

    private val nextHoldRunnable = Runnable {
        if (isNextDown && !isComboConsumed && !isNextSeeking && (System.currentTimeMillis() - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
            isNextSeeking = true
            Log.i(TAG, "SWC Next Long-Press detected! Continuous fast-forward >>")
            onFastForwardStart()
        }
    }

    private val prevHoldRunnable = Runnable {
        if (isPrevDown && !isComboConsumed && !isPrevSeeking && (System.currentTimeMillis() - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
            isPrevSeeking = true
            Log.i(TAG, "SWC Prev Long-Press detected! Continuous rewind <<")
            onRewindStart()
        }
    }

    private fun isNextKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_MEDIA_NEXT ||
               keyCode == KeyEvent.KEYCODE_CHANNEL_UP ||
               keyCode == KeyEvent.KEYCODE_NAVIGATE_NEXT
    }

    private fun isPrevKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS ||
               keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
               keyCode == KeyEvent.KEYCODE_NAVIGATE_PREVIOUS
    }

    private fun isPlayPauseKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
               keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
               keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
               keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
    }

    /**
     * Handles key events for steering wheel & car hardware controls:
     * 1. Next + Prev pressed together -> Play / Pause toggle
     * 2. Direct Play / Pause / HeadsetHook keys -> Play / Pause toggle
     * 3. Next held long-press -> Continuous Fast-Forward >> until released
     * 4. Prev held long-press -> Continuous Rewind << until released
     * 5. Single short tap -> Next / Previous track cleanly
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action
        val currentTime = System.currentTimeMillis()

        // 1. Dedicated Play / Pause key support on steering wheel or head unit
        if (isPlayPauseKey(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                Log.i(TAG, "SWC Direct Play/Pause key ($keyCode) pressed")
                onPlayPauseComboTriggered()
            }
            return true
        }

        // 2. Direct Fast-Forward & Rewind hardware buttons support
        if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_MEDIA_STEP_FORWARD) {
            if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onFastForwardStart()
            else if (action == KeyEvent.ACTION_UP) onFastForwardEnd()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD) {
            if (action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onRewindStart()
            else if (action == KeyEvent.ACTION_UP) onRewindEnd()
            return true
        }

        val isNext = isNextKey(keyCode)
        val isPrev = isPrevKey(keyCode)

        if (!isNext && !isPrev) {
            return false
        }

        if (action == KeyEvent.ACTION_DOWN) {
            if (isNext) {
                if (event.repeatCount == 0) {
                    isNextDown = true
                    nextDownTime = currentTime

                    // Check for simultaneous press / combo (both buttons held or pressed within window)
                    if (isPrevDown || (currentTime - prevDownTime <= SIMULTANEOUS_WINDOW_MS && prevDownTime > 0)) {
                        triggerComboPlayPause()
                        return true
                    }

                    isComboConsumed = false
                    isNextSeeking = false
                    mainHandler.removeCallbacks(nextHoldRunnable)
                    mainHandler.postDelayed(nextHoldRunnable, LONG_PRESS_THRESHOLD_MS)
                } else if (event.repeatCount > 0) {
                    // Hardware key repeat
                    if (!isComboConsumed && !isNextSeeking && (currentTime - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
                        isNextSeeking = true
                        mainHandler.removeCallbacks(nextHoldRunnable)
                        onFastForwardStart()
                    }
                }
                return true
            } else if (isPrev) {
                if (event.repeatCount == 0) {
                    isPrevDown = true
                    prevDownTime = currentTime

                    // Check for simultaneous press / combo (both buttons held or pressed within window)
                    if (isNextDown || (currentTime - nextDownTime <= SIMULTANEOUS_WINDOW_MS && nextDownTime > 0)) {
                        triggerComboPlayPause()
                        return true
                    }

                    isComboConsumed = false
                    isPrevSeeking = false
                    mainHandler.removeCallbacks(prevHoldRunnable)
                    mainHandler.postDelayed(prevHoldRunnable, LONG_PRESS_THRESHOLD_MS)
                } else if (event.repeatCount > 0) {
                    // Hardware key repeat
                    if (!isComboConsumed && !isPrevSeeking && (currentTime - lastComboTimestamp >= COMBO_COOLDOWN_MS)) {
                        isPrevSeeking = true
                        mainHandler.removeCallbacks(prevHoldRunnable)
                        onRewindStart()
                    }
                }
                return true
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (isNext) {
                isNextDown = false
                mainHandler.removeCallbacks(nextHoldRunnable)

                // If this was part of a combo or within combo cooldown, consume without track change
                if (isComboConsumed || (currentTime - lastComboTimestamp < COMBO_COOLDOWN_MS)) {
                    if (!isPrevDown) isComboConsumed = false
                    return true
                }

                if (isNextSeeking) {
                    isNextSeeking = false
                    Log.i(TAG, "SWC Next released! Stopping fast-forward.")
                    onFastForwardEnd()
                    return true
                }

                // Clean single short tap -> Next track
                if (currentTime - nextDownTime < LONG_PRESS_THRESHOLD_MS) {
                    Log.i(TAG, "SWC Next short tap -> Next track")
                    onNextTrack()
                }
                return true
            } else if (isPrev) {
                isPrevDown = false
                mainHandler.removeCallbacks(prevHoldRunnable)

                // If this was part of a combo or within combo cooldown, consume without track change
                if (isComboConsumed || (currentTime - lastComboTimestamp < COMBO_COOLDOWN_MS)) {
                    if (!isNextDown) isComboConsumed = false
                    return true
                }

                if (isPrevSeeking) {
                    isPrevSeeking = false
                    Log.i(TAG, "SWC Prev released! Stopping rewind.")
                    onRewindEnd()
                    return true
                }

                // Clean single short tap -> Previous track
                if (currentTime - prevDownTime < LONG_PRESS_THRESHOLD_MS) {
                    Log.i(TAG, "SWC Prev short tap -> Previous track")
                    onPrevTrack()
                }
                return true
            }
        }

        return false
    }

    private fun triggerComboPlayPause() {
        Log.i(TAG, "SWC Combo (NEXT + PREV simultaneous press) detected! Triggering Play/Pause toggle.")
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
