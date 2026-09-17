package com.example.swc

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SwcSettingsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "car_swc_settings"
        private const val KEY_PRESET = "swc_preset"
        private const val KEY_DUAL_PRESS_MODE = "swc_dual_press_mode"
        private const val KEY_DUAL_PRESS_WINDOW = "swc_dual_press_window_ms"
        private const val KEY_DEDICATED_PAUSE = "swc_dedicated_pause"
        private const val KEY_LONG_PRESS_MODE = "swc_long_press_mode"
        private const val KEY_LONG_PRESS_THRESHOLD = "swc_long_press_threshold_ms"
        private const val KEY_SEEK_STEP_SECONDS = "swc_seek_step_seconds"
        private const val KEY_ACCEPT_EXTENDED_KEYS = "swc_accept_extended_keys"
        private const val KEY_DEBOUNCE_MS = "swc_debounce_ms"
        private const val KEY_CUSTOM_BTN_1 = "swc_custom_btn_1"
        private const val KEY_CUSTOM_BTN_2 = "swc_custom_btn_2"
        private const val KEY_CUSTOM_PLAY_PAUSE = "swc_custom_play_pause"
        private const val KEY_CUSTOM_ACCEL = "swc_custom_accel"
        private const val KEY_CANBUS_WHEEL_MODE = "swc_canbus_wheel_mode"
        private const val KEY_CANBUS_PROTOCOL = "swc_canbus_protocol"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<SwcConfig> = _config.asStateFlow()

    private fun loadConfig(): SwcConfig {
        val preset = try {
            SwcPresetProfile.valueOf(
                prefs.getString(KEY_PRESET, SwcPresetProfile.DEDICATED_PAUSE_BTN.name) ?: SwcPresetProfile.DEDICATED_PAUSE_BTN.name
            )
        } catch (e: Exception) {
            SwcPresetProfile.DEDICATED_PAUSE_BTN
        }

        val dualPressMode = try {
            SwcDualPressMode.valueOf(
                prefs.getString(KEY_DUAL_PRESS_MODE, SwcDualPressMode.DISABLED.name)
                    ?: SwcDualPressMode.DISABLED.name
            )
        } catch (e: Exception) {
            SwcDualPressMode.DISABLED
        }

        val longPressMode = try {
            SwcLongPressMode.valueOf(
                prefs.getString(KEY_LONG_PRESS_MODE, SwcLongPressMode.DISABLED.name)
                    ?: SwcLongPressMode.DISABLED.name
            )
        } catch (e: Exception) {
            SwcLongPressMode.DISABLED
        }

        val canBusWheelMode = try {
            CanBusWheelMode.valueOf(
                prefs.getString(KEY_CANBUS_WHEEL_MODE, CanBusWheelMode.NORMAL.name) ?: CanBusWheelMode.NORMAL.name
            )
        } catch (e: Exception) {
            CanBusWheelMode.NORMAL
        }

        val canBusProtocol = try {
            CanBusProtocol.valueOf(
                prefs.getString(KEY_CANBUS_PROTOCOL, CanBusProtocol.GENERIC_KEYBOARD.name) ?: CanBusProtocol.GENERIC_KEYBOARD.name
            )
        } catch (e: Exception) {
            CanBusProtocol.GENERIC_KEYBOARD
        }

        return SwcConfig(
            presetProfile = preset,
            dualPressMode = dualPressMode,
            dualPressWindowMs = prefs.getLong(KEY_DUAL_PRESS_WINDOW, 650L),
            hasDedicatedPauseButton = prefs.getBoolean(KEY_DEDICATED_PAUSE, true),
            longPressMode = longPressMode,
            longPressThresholdMs = prefs.getLong(KEY_LONG_PRESS_THRESHOLD, 450L),
            seekStepSeconds = prefs.getInt(KEY_SEEK_STEP_SECONDS, 10),
            acceptExtendedKeys = prefs.getBoolean(KEY_ACCEPT_EXTENDED_KEYS, true),
            debounceMs = prefs.getLong(KEY_DEBOUNCE_MS, 200L),
            customButton1KeyCode = prefs.getInt(KEY_CUSTOM_BTN_1, android.view.KeyEvent.KEYCODE_MEDIA_NEXT),
            customButton2KeyCode = prefs.getInt(KEY_CUSTOM_BTN_2, android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS),
            customPlayPauseKeyCode = prefs.getInt(KEY_CUSTOM_PLAY_PAUSE, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
            customLongPressAcceleration = prefs.getInt(KEY_CUSTOM_ACCEL, 2),
            canBusWheelMode = canBusWheelMode,
            canBusProtocol = canBusProtocol
        )
    }

    fun applyPreset(preset: SwcPresetProfile) {
        val newConfig = when (preset) {
            SwcPresetProfile.SMART_AUTO -> SwcConfig(
                presetProfile = SwcPresetProfile.SMART_AUTO,
                dualPressMode = SwcDualPressMode.SIMULTANEOUS_AND_SEQUENTIAL,
                dualPressWindowMs = 650L,
                hasDedicatedPauseButton = false,
                longPressMode = SwcLongPressMode.CONTINUOUS_SEEK,
                longPressThresholdMs = 450L,
                seekStepSeconds = 10,
                acceptExtendedKeys = true,
                debounceMs = 200L
            )
            SwcPresetProfile.DEDICATED_PAUSE_BTN -> SwcConfig(
                presetProfile = SwcPresetProfile.DEDICATED_PAUSE_BTN,
                dualPressMode = SwcDualPressMode.DISABLED,
                dualPressWindowMs = 500L,
                hasDedicatedPauseButton = true,
                longPressMode = SwcLongPressMode.CONTINUOUS_SEEK,
                longPressThresholdMs = 450L,
                seekStepSeconds = 10,
                acceptExtendedKeys = true,
                debounceMs = 200L
            )
            SwcPresetProfile.SLOW_CANBUS -> SwcConfig(
                presetProfile = SwcPresetProfile.SLOW_CANBUS,
                dualPressMode = SwcDualPressMode.SEQUENTIAL_ONLY,
                dualPressWindowMs = 950L,
                hasDedicatedPauseButton = false,
                longPressMode = SwcLongPressMode.CONTINUOUS_SEEK,
                longPressThresholdMs = 650L,
                seekStepSeconds = 10,
                acceptExtendedKeys = true,
                debounceMs = 300L
            )
            SwcPresetProfile.PEUGEOT_PSA -> SwcConfig(
                presetProfile = SwcPresetProfile.PEUGEOT_PSA,
                dualPressMode = SwcDualPressMode.DISABLED,
                dualPressWindowMs = 500L,
                hasDedicatedPauseButton = true,
                longPressMode = SwcLongPressMode.DISABLED,
                longPressThresholdMs = 450L,
                seekStepSeconds = 10,
                acceptExtendedKeys = true,
                debounceMs = 120L,
                canBusWheelMode = CanBusWheelMode.REVERSED,
                canBusProtocol = CanBusProtocol.RAISE
            )
            SwcPresetProfile.CUSTOM -> _config.value.copy(presetProfile = SwcPresetProfile.CUSTOM)
        }
        saveConfig(newConfig)
    }

    fun setDualPressMode(mode: SwcDualPressMode) {
        val updated = _config.value.copy(
            dualPressMode = mode,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setDualPressWindowMs(windowMs: Long) {
        val updated = _config.value.copy(
            dualPressWindowMs = windowMs,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setHasDedicatedPauseButton(enabled: Boolean) {
        val updated = _config.value.copy(
            hasDedicatedPauseButton = enabled,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setLongPressMode(mode: SwcLongPressMode) {
        val updated = _config.value.copy(
            longPressMode = mode,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setLongPressThresholdMs(thresholdMs: Long) {
        val updated = _config.value.copy(
            longPressThresholdMs = thresholdMs,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setSeekStepSeconds(seconds: Int) {
        val updated = _config.value.copy(
            seekStepSeconds = seconds,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setAcceptExtendedKeys(enabled: Boolean) {
        val updated = _config.value.copy(
            acceptExtendedKeys = enabled,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setDebounceMs(debounceMs: Long) {
        val updated = _config.value.copy(
            debounceMs = debounceMs,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setCustomButton1KeyCode(keyCode: Int) {
        val updated = _config.value.copy(
            customButton1KeyCode = keyCode,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setCustomButton2KeyCode(keyCode: Int) {
        val updated = _config.value.copy(
            customButton2KeyCode = keyCode,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setCustomPlayPauseKeyCode(keyCode: Int) {
        val updated = _config.value.copy(
            customPlayPauseKeyCode = keyCode,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setCustomLongPressAcceleration(factor: Int) {
        val updated = _config.value.copy(
            customLongPressAcceleration = factor,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setCanBusWheelMode(mode: CanBusWheelMode) {
        val updated = _config.value.copy(
            canBusWheelMode = mode,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    fun setCanBusProtocol(protocol: CanBusProtocol) {
        val updated = _config.value.copy(
            canBusProtocol = protocol,
            presetProfile = SwcPresetProfile.CUSTOM
        )
        saveConfig(updated)
    }

    private fun saveConfig(newConfig: SwcConfig) {
        prefs.edit()
            .putString(KEY_PRESET, newConfig.presetProfile.name)
            .putString(KEY_DUAL_PRESS_MODE, newConfig.dualPressMode.name)
            .putLong(KEY_DUAL_PRESS_WINDOW, newConfig.dualPressWindowMs)
            .putBoolean(KEY_DEDICATED_PAUSE, newConfig.hasDedicatedPauseButton)
            .putString(KEY_LONG_PRESS_MODE, newConfig.longPressMode.name)
            .putLong(KEY_LONG_PRESS_THRESHOLD, newConfig.longPressThresholdMs)
            .putInt(KEY_SEEK_STEP_SECONDS, newConfig.seekStepSeconds)
            .putBoolean(KEY_ACCEPT_EXTENDED_KEYS, newConfig.acceptExtendedKeys)
            .putLong(KEY_DEBOUNCE_MS, newConfig.debounceMs)
            .putInt(KEY_CUSTOM_BTN_1, newConfig.customButton1KeyCode)
            .putInt(KEY_CUSTOM_BTN_2, newConfig.customButton2KeyCode)
            .putInt(KEY_CUSTOM_PLAY_PAUSE, newConfig.customPlayPauseKeyCode)
            .putInt(KEY_CUSTOM_ACCEL, newConfig.customLongPressAcceleration)
            .putString(KEY_CANBUS_WHEEL_MODE, newConfig.canBusWheelMode.name)
            .putString(KEY_CANBUS_PROTOCOL, newConfig.canBusProtocol.name)
            .apply()
        _config.value = newConfig
    }
}
