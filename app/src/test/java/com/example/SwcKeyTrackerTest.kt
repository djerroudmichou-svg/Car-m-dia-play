package com.example

import android.view.KeyEvent
import com.example.swc.SwcConfig
import com.example.swc.SwcKeyTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwcKeyTrackerTest {

    @Test
    fun handleMediaNext_triggersNextCallbackAndConsumesEvent() {
        var nextTriggered = false
        val tracker = SwcKeyTracker(
            config = SwcConfig(),
            onPlayPauseTriggered = {},
            onNextTrack = { nextTriggered = true },
            onPrevTrack = {}
        )

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
        val consumed = tracker.handleKeyEvent(downEvent)

        assertTrue(consumed)
        assertTrue(nextTriggered)
        assertEquals(KeyEvent.KEYCODE_MEDIA_NEXT, tracker.liveKeyLog.value?.keyCode)
    }

    @Test
    fun handleMediaPrevious_triggersPreviousCallbackAndConsumesEvent() {
        var prevTriggered = false
        val tracker = SwcKeyTracker(
            config = SwcConfig(),
            onPlayPauseTriggered = {},
            onNextTrack = {},
            onPrevTrack = { prevTriggered = true }
        )

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        val consumed = tracker.handleKeyEvent(downEvent)

        assertTrue(consumed)
        assertTrue(prevTriggered)
        assertEquals(KeyEvent.KEYCODE_MEDIA_PREVIOUS, tracker.liveKeyLog.value?.keyCode)
    }

    @Test
    fun handleMediaPlayPause_triggersPlayPauseCallback() {
        var playPauseTriggered = false
        val tracker = SwcKeyTracker(
            config = SwcConfig(),
            onPlayPauseTriggered = { playPauseTriggered = true }
        )

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        val consumed = tracker.handleKeyEvent(downEvent)

        assertTrue(consumed)
        assertTrue(playPauseTriggered)
    }

    @Test
    fun handleNonMediaKey_passesThroughToSystem() {
        var callbackFired = false
        val tracker = SwcKeyTracker(
            config = SwcConfig(),
            onPlayPauseTriggered = { callbackFired = true },
            onNextTrack = { callbackFired = true }
        )

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A)
        val consumed = tracker.handleKeyEvent(event)

        assertFalse(consumed)
        assertFalse(callbackFired)
    }
}
