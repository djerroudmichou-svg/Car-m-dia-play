package com.example.swc

/**
 * Steering Wheel Controls (SWC) Configuration and Presets
 */

enum class SwcDualPressMode {
    /**
     * Smart Auto Mode:
     * Detects both simultaneous hold (both keys held down together)
     * and rapid sequential press (one key pressed followed immediately by the second within the window).
     */
    SIMULTANEOUS_AND_SEQUENTIAL,

    /**
     * Strict Simultaneous Mode:
     * Only triggers Play/Pause if both buttons are down at the exact same time.
     */
    STRICT_SIMULTANEOUS,

    /**
     * Sequential Mode:
     * Triggers Play/Pause when the first button is pressed and released, followed by the second button within the time window.
     * Ideal for cars with analog resistive steering controls (Key1 / Key2 ADC).
     */
    SEQUENTIAL_ONLY,

    /**
     * Disabled:
     * Disables the Next+Prev combo entirely. Highly recommended if your car already has a dedicated Play/Pause/Mute button.
     */
    DISABLED
}

enum class SwcLongPressMode {
    /**
     * Continuous Fast-Forward / Rewind while holding the button.
     */
    CONTINUOUS_SEEK,

    /**
     * Jumps a fixed number of seconds (e.g. 10s) upon long-press.
     */
    STEP_SEEK,

    /**
     * Skips to the next / previous folder or album upon holding.
     */
    SKIP_FOLDER,

    /**
     * Disabled: No hold action; only single skips occur upon button release.
     */
    DISABLED
}

enum class SwcPresetProfile {
    /**
     * Recommended smart setup for modern CAN-Bus units.
     */
    SMART_AUTO,

    /**
     * For cars with a dedicated steering wheel Pause/Mute button. Disables dual-button combo to prevent accidental pause.
     */
    DEDICATED_PAUSE_BTN,

    /**
     * For analog resistive steering wheels or slower CAN-Bus decoders with relaxed timings.
     */
    SLOW_CANBUS,

    /**
     * Dedicated preset for Peugeot 207 / 307 / Citroen PSA stalk controls (Rotary wheel + Source button).
     */
    PEUGEOT_PSA,

    /**
     * Custom user-defined configuration.
     */
    CUSTOM
}

enum class CanBusWheelMode {
    /** Standard interpretation of rotary/wheel signals. */
    NORMAL,
    /** Swap Next/Prev for reversed rotary wheels. */
    REVERSED,
    /** Specialized mode for certain rotary dials that send combined navigation codes. */
    SMART_ROTARY
}

enum class CanBusProtocol {
    /** Standard Android KeyEvents (Most aftermarket units). */
    GENERIC_KEYBOARD,
    /** Common Chinese CAN-Bus box protocol. */
    HIWORLD,
    /** Raise CAN-Bus box protocol. */
    RAISE,
    /** Simple Soft CAN-Bus box protocol. */
    SIMPLE_SOFT,
    /** XP / Xinpu CAN-Bus box protocol. */
    XP_XINPU
}

data class SwcConfig(
    val presetProfile: SwcPresetProfile = SwcPresetProfile.DEDICATED_PAUSE_BTN,
    val dualPressMode: SwcDualPressMode = SwcDualPressMode.DISABLED,
    val dualPressWindowMs: Long = 650L,
    val hasDedicatedPauseButton: Boolean = true,
    val longPressMode: SwcLongPressMode = SwcLongPressMode.DISABLED,
    val longPressThresholdMs: Long = 450L,
    val seekStepSeconds: Int = 10,
    val acceptExtendedKeys: Boolean = true,
    val debounceMs: Long = 200L,
    val customButton1KeyCode: Int = android.view.KeyEvent.KEYCODE_MEDIA_NEXT,
    val customButton2KeyCode: Int = android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS,
    val customPlayPauseKeyCode: Int = android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
    val customLongPressAcceleration: Int = 2,
    val canBusWheelMode: CanBusWheelMode = CanBusWheelMode.NORMAL,
    val canBusProtocol: CanBusProtocol = CanBusProtocol.GENERIC_KEYBOARD
)

data class SwcLiveKeyLog(
    val keyCode: Int,
    val keyName: String,
    val action: String, // DOWN or UP
    val mappedFunction: String,
    val timestamp: Long = System.currentTimeMillis()
)
