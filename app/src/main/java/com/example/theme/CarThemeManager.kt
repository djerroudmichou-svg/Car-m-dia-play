package com.example.theme

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.ContentObserver
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

enum class ThemeMode {
    DARK,
    LIGHT,
    AUTO_SENSOR, // Uses ambient light sensor, with smart fallback (Time + Screen) if absent
    AUTO_TIME,   // Day: 6:30 AM to 6:30 PM, Night: 6:30 PM to 6:30 AM
    AUTO_SCREEN  // Headlight illumination / Screen brightness
}

/**
 * Manages Car Head Unit day/night themes, ambient light sensor auto-adjustment,
 * smart alternative modes for head units without light sensor (Time / Screen Brightness),
 * fullscreen mode, and dynamic song cover color theme integration.
 */
class CarThemeManager(private val context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "CarThemeManager"
        const val PREFS_NAME = "car_theme_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_album_color"
        private const val KEY_COMPACT_SCREEN = "compact_screen_mode"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on_mode"
        private const val KEY_FULLSCREEN_MODE = "fullscreen_mode"
        const val KEY_AUTO_LAUNCH_ON_USB = "auto_launch_on_usb"
        private const val KEY_SCREEN_SCALE = "screen_scale"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val LUX_DARK_THRESHOLD = 40.0f // Car tunnel / twilight / night threshold
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val mainHandler = Handler(Looper.getMainLooper())

    val isLightSensorAvailable: Boolean = lightSensor != null

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO_SENSOR.name) ?: ThemeMode.AUTO_SENSOR.name)
        } catch (e: Exception) {
            ThemeMode.AUTO_SENSOR
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isDynamicColorEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    )
    val isDynamicColorEnabled: StateFlow<Boolean> = _isDynamicColorEnabled.asStateFlow()

    private val _isCompactScreenMode = MutableStateFlow(
        prefs.getBoolean(KEY_COMPACT_SCREEN, false)
    )
    val isCompactScreenMode: StateFlow<Boolean> = _isCompactScreenMode.asStateFlow()

    // Keep Screen On preference: defaults to TRUE so car screen never turns off when entering the app
    private val _isKeepScreenOn = MutableStateFlow(
        prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
    )
    val isKeepScreenOn: StateFlow<Boolean> = _isKeepScreenOn.asStateFlow()

    // Immersive Fullscreen Mode: defaults to TRUE so car media player occupies 100% of display
    private val _isFullscreenMode = MutableStateFlow(
        prefs.getBoolean(KEY_FULLSCREEN_MODE, true)
    )
    val isFullscreenMode: StateFlow<Boolean> = _isFullscreenMode.asStateFlow()

    // Auto launch on USB insertion: defaults to true for automotive convenience
    private val _isAutoLaunchOnUsb = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_LAUNCH_ON_USB, true)
    )
    val isAutoLaunchOnUsb: StateFlow<Boolean> = _isAutoLaunchOnUsb.asStateFlow()

    private val _screenScale = MutableStateFlow(
        prefs.getFloat(KEY_SCREEN_SCALE, 1.0f)
    )
    val screenScale: StateFlow<Float> = _screenScale.asStateFlow()

    private val _fontSizeScale = MutableStateFlow(
        prefs.getFloat(KEY_FONT_SCALE, 1.0f)
    )
    val fontSizeScale: StateFlow<Float> = _fontSizeScale.asStateFlow()

    private val _currentLux = MutableStateFlow(100f)
    val currentLux: StateFlow<Float> = _currentLux.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(calculateIsDark())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val brightnessObserver = object : ContentObserver(mainHandler) {
        override fun onChange(selfChange: Boolean) {
            if (_themeMode.value == ThemeMode.AUTO_SCREEN || (_themeMode.value == ThemeMode.AUTO_SENSOR && lightSensor == null)) {
                val nextDark = calculateIsDark()
                if (_isDarkTheme.value != nextDark) {
                    _isDarkTheme.value = nextDark
                    Log.d(TAG, "Screen brightness changed, updated theme to dark=$nextDark")
                }
            }
        }
    }

    private val timeCheckRunnable = object : Runnable {
        override fun run() {
            if (_themeMode.value == ThemeMode.AUTO_TIME || (_themeMode.value == ThemeMode.AUTO_SENSOR && lightSensor == null)) {
                val nextDark = calculateIsDark()
                if (_isDarkTheme.value != nextDark) {
                    _isDarkTheme.value = nextDark
                    Log.d(TAG, "Scheduled time check updated theme to dark=$nextDark")
                }
            }
            mainHandler.postDelayed(this, 60000L) // check every minute
        }
    }

    init {
        updateSensorRegistration()
        registerBrightnessObserver()
        mainHandler.postDelayed(timeCheckRunnable, 60000L)
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        updateSensorRegistration()
        _isDarkTheme.value = calculateIsDark()
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        _isDynamicColorEnabled.value = enabled
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    fun setCompactScreenMode(enabled: Boolean) {
        _isCompactScreenMode.value = enabled
        prefs.edit().putBoolean(KEY_COMPACT_SCREEN, enabled).apply()
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _isKeepScreenOn.value = enabled
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
    }

    fun setFullscreenMode(enabled: Boolean) {
        _isFullscreenMode.value = enabled
        prefs.edit().putBoolean(KEY_FULLSCREEN_MODE, enabled).apply()
    }

    fun setAutoLaunchOnUsb(enabled: Boolean) {
        _isAutoLaunchOnUsb.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_LAUNCH_ON_USB, enabled).apply()
    }

    fun setScreenScale(scale: Float) {
        _screenScale.value = scale
        prefs.edit().putFloat(KEY_SCREEN_SCALE, scale).apply()
    }

    fun setFontSizeScale(scale: Float) {
        _fontSizeScale.value = scale
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    private fun updateSensorRegistration() {
        if (_themeMode.value == ThemeMode.AUTO_SENSOR && lightSensor != null) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            sensorManager?.unregisterListener(this)
        }
    }

    private fun registerBrightnessObserver() {
        try {
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
                brightnessObserver
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to register screen brightness ContentObserver", e)
        }
    }

    private fun isNightByTime(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute
        // Night: between 18:30 (6:30 PM = 1110) and 06:30 (6:30 AM = 390)
        return totalMinutes >= 1110 || totalMinutes < 390
    }

    private fun isNightByScreenBrightness(): Boolean {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
            // On car head units, headlights ON typically dims screen brightness to <= 85 (approx 33% of 255)
            brightness <= 85
        } catch (e: Exception) {
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightMode == Configuration.UI_MODE_NIGHT_YES
        }
    }

    private fun calculateIsDark(): Boolean {
        return when (_themeMode.value) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.AUTO_TIME -> isNightByTime()
            ThemeMode.AUTO_SCREEN -> isNightByScreenBrightness()
            ThemeMode.AUTO_SENSOR -> {
                if (lightSensor != null) {
                    _currentLux.value < LUX_DARK_THRESHOLD
                } else {
                    // Smart fallback for car head units without light sensor:
                    // If headlights dimmed screen OR it's nighttime by clock OR system night mode is ON
                    val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    val isSystemNight = nightMode == Configuration.UI_MODE_NIGHT_YES
                    isSystemNight || isNightByTime() || isNightByScreenBrightness()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values.firstOrNull() ?: return
            _currentLux.value = lux
            if (_themeMode.value == ThemeMode.AUTO_SENSOR) {
                val nextIsDark = lux < LUX_DARK_THRESHOLD
                if (_isDarkTheme.value != nextIsDark) {
                    _isDarkTheme.value = nextIsDark
                    Log.d(TAG, "Car Ambient Light Sensor triggered theme switch to: ${if (nextIsDark) "DARK" else "LIGHT"} ($lux lux)")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun cleanup() {
        sensorManager?.unregisterListener(this)
        mainHandler.removeCallbacks(timeCheckRunnable)
        try {
            context.contentResolver.unregisterContentObserver(brightnessObserver)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering brightness observer", e)
        }
    }
}
