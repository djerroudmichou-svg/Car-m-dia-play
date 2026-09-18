package com.example.telemetry

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.util.Scanner

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TempSource {
    AUTO,
    AMBIENT_HARDWARE,
    BATTERY_SENSOR,
    CPU_SYSFS,
    CAN_BUS,
    CUSTOM_SENSOR,
    CUSTOM_SYSFS,
    MANUAL
}

enum class SpeedSource {
    AUTO_GPS,
    GPS_HIGH_PRECISION,
    NETWORK_CELL,
    CAN_BUS,
    CUSTOM_SENSOR,
    SIMULATED
}

data class ScannedSensorInfo(
    val name: String,
    val vendor: String,
    val type: Int,
    val typeString: String,
    val power: Float,
    val resolution: Float,
    val maxRange: Float,
    val isPotentialTemp: Boolean,
    val isPotentialSpeed: Boolean,
    val currentValues: List<Float> = emptyList(),
    val isTemperatureSensor: Boolean = isPotentialTemp,
    val isSpeedOrMotionSensor: Boolean = isPotentialSpeed
)

data class ScannedSysfsThermalInfo(
    val path: String,
    val typeName: String,
    val currentTempCelsius: Float?
)

data class ScannedLocationProviderInfo(
    val name: String,
    val isEnabled: Boolean,
    val currentSpeedKmh: Int? = null,
    val lastFixTime: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null
)

data class ComprehensiveSensorScanReport(
    val hardwareSensors: List<ScannedSensorInfo> = emptyList(),
    val thermalZones: List<ScannedSysfsThermalInfo> = emptyList(),
    val locationProviders: List<ScannedLocationProviderInfo> = emptyList(),
    val batteryTempCelsius: Float? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val totalFoundCount: Int = 0
) {
    val sysfsThermalZones: List<ScannedSysfsThermalInfo> get() = thermalZones
}

/**
 * Robust Car Telemetry Manager:
 * Handles real hardware sensors, tablet battery thermistors, vehicle thermal sysfs, GPS/Network speed computation,
 * CAN-Bus integration values, calibration offsets, speed multipliers, and customizable sensor sources.
 */
class CarTelemetryManager(private val context: Context) : SensorEventListener, LocationListener {
    private val TAG = "CarTelemetryManager"

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val prefs = context.getSharedPreferences("car_telemetry_prefs", Context.MODE_PRIVATE)

    // Active Sensor Source Preferences
    private val _tempSource = MutableStateFlow(
        try { TempSource.valueOf(prefs.getString("temp_source", TempSource.AUTO.name) ?: TempSource.AUTO.name) }
        catch (e: Exception) { TempSource.AUTO }
    )
    val tempSource: StateFlow<TempSource> = _tempSource.asStateFlow()

    private val _speedSource = MutableStateFlow(
        try { SpeedSource.valueOf(prefs.getString("speed_source", SpeedSource.AUTO_GPS.name) ?: SpeedSource.AUTO_GPS.name) }
        catch (e: Exception) { SpeedSource.AUTO_GPS }
    )
    val speedSource: StateFlow<SpeedSource> = _speedSource.asStateFlow()

    // Preset & Multiplier States
    private val _manualTempValue = MutableStateFlow(prefs.getFloat("manual_temp_value", 24f))
    val manualTempValue: StateFlow<Float> = _manualTempValue.asStateFlow()

    private val _simulatedSpeedValue = MutableStateFlow(prefs.getInt("simulated_speed_value", 0))
    val simulatedSpeedValue: StateFlow<Int> = _simulatedSpeedValue.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(prefs.getFloat("speed_multiplier", 1.0f))
    val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

    // Custom Chosen Sensors
    private val _customTempSensorName = MutableStateFlow<String?>(prefs.getString("custom_temp_sensor_name", null))
    val customTempSensorName: StateFlow<String?> = _customTempSensorName.asStateFlow()

    private val _customSysfsTempPath = MutableStateFlow<String?>(prefs.getString("custom_sysfs_temp_path", null))
    val customSysfsTempPath: StateFlow<String?> = _customSysfsTempPath.asStateFlow()

    private val _customSpeedSensorName = MutableStateFlow<String?>(prefs.getString("custom_speed_sensor_name", null))
    val customSpeedSensorName: StateFlow<String?> = _customSpeedSensorName.asStateFlow()

    private val _customHardwareTemp = MutableStateFlow<Float?>(null)
    val customHardwareTemp: StateFlow<Float?> = _customHardwareTemp.asStateFlow()

    private val _customSysfsTemp = MutableStateFlow<Float?>(null)
    val customSysfsTemp: StateFlow<Float?> = _customSysfsTemp.asStateFlow()

    private val _customSpeed = MutableStateFlow<Int?>(null)
    val customSpeed: StateFlow<Int?> = _customSpeed.asStateFlow()

    // Comprehensive Scan & Live Audit State
    private val _scanReport = MutableStateFlow(ComprehensiveSensorScanReport())
    val scanReport: StateFlow<ComprehensiveSensorScanReport> = _scanReport.asStateFlow()

    private val _liveSensorValues = MutableStateFlow<Map<String, List<Float>>>(emptyMap())
    val liveSensorValues: StateFlow<Map<String, List<Float>>> = _liveSensorValues.asStateFlow()

    private var isAuditing = false
    private var customTempSensor: Sensor? = null
    private var customSpeedSensor: Sensor? = null

    // Individual Raw Sensor Readings for Diagnostics
    private val _ambientHardwareTemp = MutableStateFlow<Float?>(null)
    val ambientHardwareTemp: StateFlow<Float?> = _ambientHardwareTemp.asStateFlow()

    private val _batteryTemp = MutableStateFlow<Float?>(null)
    val batteryTemp: StateFlow<Float?> = _batteryTemp.asStateFlow()

    private val _cpuSysfsTemp = MutableStateFlow<Float?>(null)
    val cpuSysfsTemp: StateFlow<Float?> = _cpuSysfsTemp.asStateFlow()

    private val _canBusTemp = MutableStateFlow<Float?>(null)
    val canBusTemp: StateFlow<Float?> = _canBusTemp.asStateFlow()

    private val _gpsSpeed = MutableStateFlow<Int?>(null)
    val gpsSpeed: StateFlow<Int?> = _gpsSpeed.asStateFlow()

    private val _networkSpeed = MutableStateFlow<Int?>(null)
    val networkSpeed: StateFlow<Int?> = _networkSpeed.asStateFlow()

    private val _canBusSpeed = MutableStateFlow<Int?>(null)
    val canBusSpeed: StateFlow<Int?> = _canBusSpeed.asStateFlow()

    // Combined Active Output State Flows
    private val _rawAmbientTemp = MutableStateFlow(24f)
    val rawAmbientTemp: StateFlow<Float> = _rawAmbientTemp.asStateFlow()

    private val _ambientTemp = MutableStateFlow(24f)
    val ambientTemp: StateFlow<Float> = _ambientTemp.asStateFlow()

    private val _carSpeed = MutableStateFlow(0)
    val carSpeed: StateFlow<Int> = _carSpeed.asStateFlow()

    private val _isTempSensorAvailable = MutableStateFlow(true)
    val isTempSensorAvailable: StateFlow<Boolean> = _isTempSensorAvailable.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    // Configurable Temperature Calibration Offset (in °C, e.g. -10 to +10)
    private val _tempOffset = MutableStateFlow(prefs.getFloat("temp_offset", 0f))
    val tempOffset: StateFlow<Float> = _tempOffset.asStateFlow()

    // Unit toggle: true = km/h, false = mph
    private val _useMetricSpeed = MutableStateFlow(prefs.getBoolean("use_metric_speed", true))
    val useMetricSpeed: StateFlow<Boolean> = _useMetricSpeed.asStateFlow()

    // Unit toggle: true = °C, false = °F
    private val _useMetricTemp = MutableStateFlow(prefs.getBoolean("use_metric_temp", true))
    val useMetricTemp: StateFlow<Boolean> = _useMetricTemp.asStateFlow()

    private var tempSensor: Sensor? = null
    private var lastLocation: Location? = null
    private var lastLocationTimeMs: Long = 0L
    private var isTracking = false
    private val telemetryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var periodicPollJob: Job? = null

    init {
        // Look for ambient temperature or automotive cabin temperature sensor
        tempSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_TEMPERATURE)

        pollAllSensorsOnce()
    }

    fun setTempSource(source: TempSource) {
        _tempSource.value = source
        prefs.edit().putString("temp_source", source.name).apply()
        recalculateActiveOutputs()
    }

    fun setSpeedSource(source: SpeedSource) {
        _speedSource.value = source
        prefs.edit().putString("speed_source", source.name).apply()
        recalculateActiveOutputs()
    }

    fun setManualTempValue(tempC: Float) {
        _manualTempValue.value = tempC
        prefs.edit().putFloat("manual_temp_value", tempC).apply()
        if (_tempSource.value == TempSource.MANUAL) {
            recalculateActiveOutputs()
        }
    }

    fun setSimulatedSpeedValue(speedKmh: Int) {
        _simulatedSpeedValue.value = speedKmh
        prefs.edit().putInt("simulated_speed_value", speedKmh).apply()
        if (_speedSource.value == SpeedSource.SIMULATED) {
            recalculateActiveOutputs()
        }
    }

    fun setSpeedMultiplier(multiplier: Float) {
        _speedMultiplier.value = multiplier
        prefs.edit().putFloat("speed_multiplier", multiplier).apply()
        recalculateActiveOutputs()
    }

    fun setTempOffset(offset: Float) {
        _tempOffset.value = offset
        prefs.edit().putFloat("temp_offset", offset).apply()
        updateCalibratedTemp(_rawAmbientTemp.value)
    }

    fun setUseMetricSpeed(isMetric: Boolean) {
        _useMetricSpeed.value = isMetric
        prefs.edit().putBoolean("use_metric_speed", isMetric).apply()
    }

    fun setUseMetricTemp(isMetric: Boolean) {
        _useMetricTemp.value = isMetric
        prefs.edit().putBoolean("use_metric_temp", isMetric).apply()
    }

    private fun updateCalibratedTemp(raw: Float) {
        val calibrated = raw + _tempOffset.value
        _ambientTemp.value = calibrated
    }

    fun pollAllSensorsOnce() {
        // 1. Read Tablet Battery Thermistor
        try {
            val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, filter)
            val rawBatt = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            if (rawBatt > -1) {
                _batteryTemp.value = rawBatt / 10f
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not query battery temperature", e)
        }

        // 2. Read System Thermal Zone Sysfs
        val sysTemp = readThermalZoneTemp()
        if (sysTemp != null) {
            _cpuSysfsTemp.value = sysTemp
        }

        // 3. Read Custom Sysfs Path if specified
        val customPath = _customSysfsTempPath.value
        if (!customPath.isNullOrEmpty()) {
            try {
                val f = File(customPath)
                if (f.exists() && f.canRead()) {
                    val rawVal = f.readText().trim().toFloatOrNull()
                    if (rawVal != null) {
                        _customSysfsTemp.value = if (rawVal > 1000) rawVal / 1000f else rawVal
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Custom sysfs read failed: ${e.message}")
            }
        }

        recalculateActiveOutputs()
    }

    fun recalculateActiveOutputs() {
        // Temperature Source Selection logic
        val rawTemp = when (_tempSource.value) {
            TempSource.AUTO -> {
                _canBusTemp.value
                    ?: _customHardwareTemp.value
                    ?: _ambientHardwareTemp.value
                    ?: _batteryTemp.value
                    ?: _customSysfsTemp.value
                    ?: _cpuSysfsTemp.value
                    ?: _manualTempValue.value
            }
            TempSource.AMBIENT_HARDWARE -> {
                _ambientHardwareTemp.value
                    ?: _batteryTemp.value
                    ?: _cpuSysfsTemp.value
                    ?: _manualTempValue.value
            }
            TempSource.BATTERY_SENSOR -> {
                _batteryTemp.value ?: _manualTempValue.value
            }
            TempSource.CPU_SYSFS -> {
                _cpuSysfsTemp.value ?: _manualTempValue.value
            }
            TempSource.CAN_BUS -> {
                _canBusTemp.value ?: _manualTempValue.value
            }
            TempSource.CUSTOM_SENSOR -> {
                _customHardwareTemp.value
                    ?: _ambientHardwareTemp.value
                    ?: _batteryTemp.value
                    ?: _cpuSysfsTemp.value
                    ?: _manualTempValue.value
            }
            TempSource.CUSTOM_SYSFS -> {
                _customSysfsTemp.value
                    ?: _cpuSysfsTemp.value
                    ?: _batteryTemp.value
                    ?: _manualTempValue.value
            }
            TempSource.MANUAL -> {
                _manualTempValue.value
            }
        }

        _rawAmbientTemp.value = rawTemp
        updateCalibratedTemp(rawTemp)

        // Speed Source Selection logic
        val mult = _speedMultiplier.value
        val rawSpeedKmh = when (_speedSource.value) {
            SpeedSource.AUTO_GPS -> {
                _canBusSpeed.value
                    ?: _customSpeed.value
                    ?: _gpsSpeed.value
                    ?: _networkSpeed.value
                    ?: _simulatedSpeedValue.value
            }
            SpeedSource.GPS_HIGH_PRECISION -> {
                _gpsSpeed.value ?: 0
            }
            SpeedSource.NETWORK_CELL -> {
                _networkSpeed.value ?: 0
            }
            SpeedSource.CAN_BUS -> {
                _canBusSpeed.value ?: 0
            }
            SpeedSource.CUSTOM_SENSOR -> {
                _customSpeed.value ?: _gpsSpeed.value ?: _networkSpeed.value ?: 0
            }
            SpeedSource.SIMULATED -> {
                _simulatedSpeedValue.value
            }
        }

        val finalSpeed = (rawSpeedKmh * mult).toInt().coerceAtLeast(0)
        _carSpeed.value = finalSpeed
    }

    fun setCanBusSpeed(speedKmH: Int) {
        if (speedKmH >= 0) {
            _canBusSpeed.value = speedKmH
            recalculateActiveOutputs()
        }
    }

    fun setCanBusTemp(tempCelsius: Float) {
        _canBusTemp.value = tempCelsius
        _isTempSensorAvailable.value = true
        recalculateActiveOutputs()
    }

    fun selectCustomTempSensor(name: String) {
        _customTempSensorName.value = name
        prefs.edit().putString("custom_temp_sensor_name", name).apply()
        setTempSource(TempSource.CUSTOM_SENSOR)
        registerCustomTempSensorListener()
    }

    fun selectCustomSysfsThermal(path: String) {
        _customSysfsTempPath.value = path
        prefs.edit().putString("custom_sysfs_temp_path", path).apply()
        setTempSource(TempSource.CUSTOM_SYSFS)
        pollAllSensorsOnce()
    }

    fun selectCustomSpeedSensor(name: String) {
        _customSpeedSensorName.value = name
        prefs.edit().putString("custom_speed_sensor_name", name).apply()
        setSpeedSource(SpeedSource.CUSTOM_SENSOR)
        registerCustomSpeedSensorListener()
    }

    private fun registerCustomTempSensorListener() {
        val name = _customTempSensorName.value ?: return
        val sm = sensorManager ?: return
        val sensor = sm.getSensorList(Sensor.TYPE_ALL).find { it.name == name }
        if (sensor != null) {
            customTempSensor = sensor
            try {
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                Log.i(TAG, "Registered custom temperature sensor: ${sensor.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed registering custom temp sensor", e)
            }
        }
    }

    private fun registerCustomSpeedSensorListener() {
        val name = _customSpeedSensorName.value ?: return
        val sm = sensorManager ?: return
        val sensor = sm.getSensorList(Sensor.TYPE_ALL).find { it.name == name }
        if (sensor != null) {
            customSpeedSensor = sensor
            try {
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                Log.i(TAG, "Registered custom speed sensor: ${sensor.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed registering custom speed sensor", e)
            }
        }
    }

    fun startLiveAudit() {
        isAuditing = true
        val sm = sensorManager ?: return
        try {
            sm.getSensorList(Sensor.TYPE_ALL).forEach { s ->
                sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting live audit", e)
        }
        performComprehensiveScan()
    }

    fun stopLiveAudit() {
        isAuditing = false
        val sm = sensorManager ?: return
        try {
            sm.unregisterListener(this)
            // Restore persistent tracking listeners
            if (tempSensor != null) sm.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_UI)
            if (customTempSensor != null) sm.registerListener(this, customTempSensor, SensorManager.SENSOR_DELAY_UI)
            if (customSpeedSensor != null) sm.registerListener(this, customSpeedSensor, SensorManager.SENSOR_DELAY_UI)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping live audit", e)
        }
    }

    fun onLocationPermissionGranted() {
        Log.i(TAG, "Location permission callback triggered, restarting GPS listeners")
        if (isTracking) {
            stopTracking()
        }
        startTracking()
        performComprehensiveScan()
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        isTracking = true

        pollAllSensorsOnce()

        // 1. Register Default Ambient Temperature Sensor
        if (tempSensor != null) {
            try {
                sensorManager?.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_UI)
                Log.i(TAG, "Ambient Temperature sensor registered")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register default ambient temp sensor", e)
            }
        }

        // Register custom sensors if chosen
        registerCustomTempSensorListener()
        registerCustomSpeedSensorListener()

        // 2. Register Location Updates for Speed (Only if permission has been granted)
        val hasFineLoc = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLoc = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLoc || hasCoarseLoc) {
            try {
                var registeredAny = false
                val locMgr = locationManager
                if (locMgr != null) {
                    if (hasFineLoc && locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locMgr.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0L,
                            0f,
                            this
                        )
                        _isGpsActive.value = true
                        registeredAny = true
                        Log.i(TAG, "GPS speed tracking registered (high precision, 0ms/0m)")
                    }
                    if (locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locMgr.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            500L,
                            0f,
                            this
                        )
                        registeredAny = true
                    }
                    if (locMgr.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                        locMgr.requestLocationUpdates(
                            LocationManager.PASSIVE_PROVIDER,
                            1000L,
                            0f,
                            this
                        )
                        registeredAny = true
                    }
                }
                if (!registeredAny) {
                    Log.w(TAG, "No location providers enabled on device")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission missing for speed tracking", e)
                _isGpsActive.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error starting location updates", e)
            }
        } else {
            Log.i(TAG, "Location permission not yet granted; speed tracking will start once permission is granted")
            _isGpsActive.value = false
        }

        // 3. Periodic Poll for battery and thermal sysfs (Every 2 seconds)
        periodicPollJob?.cancel()
        periodicPollJob = telemetryScope.launch {
            while (isActive && isTracking) {
                try {
                    pollAllSensorsOnce()
                } catch (e: Exception) {
                    Log.d(TAG, "Periodic poll exception: ${e.message}")
                }
                delay(2000L)
            }
        }
    }

    fun stopTracking() {
        isTracking = false
        periodicPollJob?.cancel()
        periodicPollJob = null
        try {
            sensorManager?.unregisterListener(this)
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping telemetry tracking", e)
        }
    }

    // --- SensorEventListener ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.values.isEmpty()) return
        val sensorName = event.sensor.name

        // Live audit capture
        if (isAuditing) {
            val list = event.values.toList()
            _liveSensorValues.value = _liveSensorValues.value + (sensorName to list)
        }

        // Standard Ambient / Built-in Temperature
        if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE || event.sensor.type == Sensor.TYPE_TEMPERATURE) {
            val temp = event.values[0]
            if (temp in -50f..90f) {
                _ambientHardwareTemp.value = temp
                recalculateActiveOutputs()
            }
        }

        // Custom Selected Temp Sensor
        if (sensorName == _customTempSensorName.value) {
            val temp = event.values[0]
            if (temp in -50f..120f) {
                _customHardwareTemp.value = temp
                recalculateActiveOutputs()
            }
        }

        // Custom Selected Speed Sensor
        if (sensorName == _customSpeedSensorName.value) {
            val calculated = when {
                event.values.size >= 3 && (event.sensor.type == Sensor.TYPE_ACCELEROMETER || event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) -> {
                    val mag = kotlin.math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                    val delta = kotlin.math.abs(mag - 9.8f)
                    (delta * 4f).toInt()
                }
                else -> {
                    (event.values[0] * 3.6f).toInt()
                }
            }
            _customSpeed.value = calculated.coerceAtLeast(0)
            recalculateActiveOutputs()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // --- LocationListener (Multi-method Speed Calculation) ---
    override fun onLocationChanged(location: Location) {
        _isGpsActive.value = true
        var calculatedSpeedKmh = 0

        if (location.hasSpeed()) {
            val speedMps = location.speed
            calculatedSpeedKmh = (speedMps * 3.6f).toInt()
        } else {
            // Delta-distance fallback if hardware GPS does not populate hasSpeed()
            val prev = lastLocation
            val nowTime = location.time
            if (prev != null && lastLocationTimeMs > 0L && nowTime > lastLocationTimeMs) {
                val distanceMeters = prev.distanceTo(location)
                val timeDiffSeconds = (nowTime - lastLocationTimeMs) / 1000f
                if (timeDiffSeconds in 0.2f..10f) {
                    val speedMps = distanceMeters / timeDiffSeconds
                    calculatedSpeedKmh = (speedMps * 3.6f).toInt()
                }
            }
        }

        // Noise suppression & realistic bounds
        if (calculatedSpeedKmh < 2) {
            calculatedSpeedKmh = 0
        } else if (calculatedSpeedKmh > 350) {
            calculatedSpeedKmh = _gpsSpeed.value ?: 0
        }

        lastLocation = location
        lastLocationTimeMs = location.time

        if (location.provider == LocationManager.GPS_PROVIDER) {
            _gpsSpeed.value = calculatedSpeedKmh
        } else {
            _networkSpeed.value = calculatedSpeedKmh
        }

        recalculateActiveOutputs()
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {
        _isGpsActive.value = true
    }
    override fun onProviderDisabled(provider: String) {}

    fun performComprehensiveScan(): ComprehensiveSensorScanReport {
        val hardwareList = mutableListOf<ScannedSensorInfo>()
        val sm = sensorManager
        if (sm != null) {
            val all = sm.getSensorList(Sensor.TYPE_ALL)
            for (sensor in all) {
                val nameLower = sensor.name.lowercase()
                val typeName = getSensorTypeName(sensor.type)
                val isTemp = sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE ||
                        sensor.type == Sensor.TYPE_TEMPERATURE ||
                        nameLower.contains("temp") ||
                        nameLower.contains("therm") ||
                        nameLower.contains("heat") ||
                        nameLower.contains("cabin") ||
                        nameLower.contains("soc")
                val isSpeed = sensor.type == Sensor.TYPE_ACCELEROMETER ||
                        sensor.type == Sensor.TYPE_LINEAR_ACCELERATION ||
                        sensor.type == Sensor.TYPE_GYROSCOPE ||
                        nameLower.contains("speed") ||
                        nameLower.contains("motion") ||
                        nameLower.contains("velocity") ||
                        nameLower.contains("gps") ||
                        nameLower.contains("loc") ||
                        nameLower.contains("wheel")

                hardwareList.add(
                    ScannedSensorInfo(
                        name = sensor.name,
                        vendor = sensor.vendor,
                        type = sensor.type,
                        typeString = typeName,
                        power = sensor.power,
                        resolution = sensor.resolution,
                        maxRange = sensor.maximumRange,
                        isPotentialTemp = isTemp,
                        isPotentialSpeed = isSpeed,
                        currentValues = _liveSensorValues.value[sensor.name] ?: emptyList()
                    )
                )
            }
        }

        // Sysfs thermal scan
        val thermalList = mutableListOf<ScannedSysfsThermalInfo>()
        for (i in 0..25) {
            val zoneDir = File("/sys/class/thermal/thermal_zone$i")
            if (zoneDir.exists() && zoneDir.isDirectory) {
                val typeFile = File(zoneDir, "type")
                val tempFile = File(zoneDir, "temp")
                val typeName = if (typeFile.exists() && typeFile.canRead()) {
                    try { typeFile.readText().trim() } catch (e: Exception) { "thermal_zone$i" }
                } else "thermal_zone$i"

                var currentTemp: Float? = null
                if (tempFile.exists() && tempFile.canRead()) {
                    try {
                        val rawVal = tempFile.readText().trim().toFloatOrNull()
                        if (rawVal != null) {
                            currentTemp = if (rawVal > 1000) rawVal / 1000f else rawVal
                        }
                    } catch (e: Exception) {}
                }
                thermalList.add(
                    ScannedSysfsThermalInfo(
                        path = tempFile.absolutePath,
                        typeName = typeName,
                        currentTempCelsius = currentTemp
                    )
                )
            }
        }

        // Also scan /sys/class/hwmon
        try {
            val hwmonDir = File("/sys/class/hwmon")
            if (hwmonDir.exists() && hwmonDir.isDirectory) {
                hwmonDir.listFiles()?.forEach { hw ->
                    val nameFile = File(hw, "name")
                    val hwName = if (nameFile.exists() && nameFile.canRead()) {
                        try { nameFile.readText().trim() } catch (e: Exception) { hw.name }
                    } else hw.name

                    hw.listFiles()?.filter { it.name.startsWith("temp") && it.name.endsWith("_input") }?.forEach { tempInput ->
                        var currentTemp: Float? = null
                        try {
                            val rawVal = tempInput.readText().trim().toFloatOrNull()
                            if (rawVal != null) {
                                currentTemp = if (rawVal > 1000) rawVal / 1000f else rawVal
                            }
                        } catch (e: Exception) {}
                        thermalList.add(
                            ScannedSysfsThermalInfo(
                                path = tempInput.absolutePath,
                                typeName = "$hwName / ${tempInput.name}",
                                currentTempCelsius = currentTemp
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {}

        // Location Providers Scan
        val locList = mutableListOf<ScannedLocationProviderInfo>()
        val hasLocPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        var isGpsOn = false
        val locMgr = locationManager
        if (locMgr != null) {
            try {
                isGpsOn = locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (e: Exception) {}
            locMgr.allProviders.forEach { prov ->
                val enabled = try { locMgr.isProviderEnabled(prov) } catch (e: Exception) { false }
                var speedKmh: Int? = null
                var lat: Double? = null
                var lon: Double? = null
                var acc: Float? = null
                var lastTime: Long? = null
                if (hasLocPerm) {
                    try {
                        val loc = locMgr.getLastKnownLocation(prov)
                        if (loc != null) {
                            if (loc.hasSpeed()) speedKmh = (loc.speed * 3.6f).toInt()
                            lat = loc.latitude
                            lon = loc.longitude
                            acc = loc.accuracy
                            lastTime = loc.time
                        }
                    } catch (e: SecurityException) {} catch (e: Exception) {}
                }
                locList.add(
                    ScannedLocationProviderInfo(
                        name = prov,
                        isEnabled = enabled,
                        currentSpeedKmh = speedKmh,
                        lastFixTime = lastTime,
                        latitude = lat,
                        longitude = lon,
                        accuracy = acc
                    )
                )
            }
        }

        // Battery Thermistor
        var battTemp: Float? = null
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val status = context.registerReceiver(null, filter)
            val raw = status?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            if (raw > -1) {
                battTemp = raw / 10f
            }
        } catch (e: Exception) {}

        val report = ComprehensiveSensorScanReport(
            hardwareSensors = hardwareList,
            thermalZones = thermalList,
            locationProviders = locList,
            batteryTempCelsius = battTemp,
            isLocationPermissionGranted = hasLocPerm,
            isGpsEnabled = isGpsOn,
            totalFoundCount = hardwareList.size + thermalList.size + locList.size + (if (battTemp != null) 1 else 0)
        )
        _scanReport.value = report
        return report
    }

    /**
     * Smart Auto-Detection:
     * Scans and automatically binds the best working temperature and speed sensors.
     */
    fun autoDetectBestSensors(): String {
        pollAllSensorsOnce()
        val report = performComprehensiveScan()
        val summaryParts = mutableListOf<String>()

        // 1. Detect Temperature
        if (_canBusTemp.value != null) {
            setTempSource(TempSource.CAN_BUS)
            summaryParts.add("الحرارة: CAN-Bus (${_canBusTemp.value}°C)")
        } else {
            val hwAmbient = report.hardwareSensors.firstOrNull { it.type == Sensor.TYPE_AMBIENT_TEMPERATURE || it.type == Sensor.TYPE_TEMPERATURE }
            if (hwAmbient != null) {
                selectCustomTempSensor(hwAmbient.name)
                summaryParts.add("الحرارة: مستشعر العتاد (${hwAmbient.name})")
            } else {
                val potentialTempHw = report.hardwareSensors.firstOrNull { it.isPotentialTemp }
                if (potentialTempHw != null) {
                    selectCustomTempSensor(potentialTempHw.name)
                    summaryParts.add("الحرارة: مستشعر (${potentialTempHw.name})")
                } else {
                    val validSysfs = report.sysfsThermalZones.firstOrNull { it.currentTempCelsius != null && it.currentTempCelsius > 0f }
                    if (validSysfs != null) {
                        selectCustomSysfsThermal(validSysfs.path)
                        summaryParts.add("الحرارة: معالج النظام (${validSysfs.typeName} ${String.format(java.util.Locale.US, "%.1f", validSysfs.currentTempCelsius)}°C)")
                    } else if (report.batteryTempCelsius != null && report.batteryTempCelsius > 0f) {
                        setTempSource(TempSource.BATTERY_SENSOR)
                        summaryParts.add("الحرارة: ثيرمستور البطارية (${String.format(java.util.Locale.US, "%.1f", report.batteryTempCelsius)}°C)")
                    } else {
                        summaryParts.add("الحرارة: لم يتم العثور على مستشعر حرارة محيطية (استخدام اليدوي)")
                    }
                }
            }
        }

        // 2. Detect Speed
        val canSpeed = _canBusSpeed.value
        if (canSpeed != null && canSpeed > 0) {
            setSpeedSource(SpeedSource.CAN_BUS)
            summaryParts.add("السرعة: CAN-Bus ($canSpeed km/h)")
        } else if (report.isLocationPermissionGranted && report.isGpsEnabled) {
            setSpeedSource(SpeedSource.AUTO_GPS)
            summaryParts.add("السرعة: GPS فائق الدقة")
        } else if (report.locationProviders.any { it.isEnabled }) {
            setSpeedSource(SpeedSource.AUTO_GPS)
            summaryParts.add("السرعة: مزود الموقع (Network/Cell)")
        } else {
            val motionSensor = report.hardwareSensors.firstOrNull { it.isPotentialSpeed }
            if (motionSensor != null) {
                selectCustomSpeedSensor(motionSensor.name)
                summaryParts.add("السرعة: مستشعر الحركة (${motionSensor.name})")
            } else {
                summaryParts.add("السرعة: يرجى تفعيل إذن الموقع / GPS")
            }
        }

        pollAllSensorsOnce()
        recalculateActiveOutputs()
        return summaryParts.joinToString(" | ")
    }

    private fun getSensorTypeName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer (مستشعر التسارع)"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetic Field (المجال المغناطيسي)"
            Sensor.TYPE_ORIENTATION -> "Orientation (الاتجاه)"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope (الجيروسكوب)"
            Sensor.TYPE_LIGHT -> "Light (الإضاءة)"
            Sensor.TYPE_PRESSURE -> "Pressure / Barometer (الضغط الجوي)"
            Sensor.TYPE_TEMPERATURE -> "Temperature (درجة الحرارة)"
            Sensor.TYPE_PROXIMITY -> "Proximity (التقارب)"
            Sensor.TYPE_GRAVITY -> "Gravity (الجاذبية)"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration (التسارع الخطي)"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector (متجه الدوران)"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Relative Humidity (الرطوبة النسبية)"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient Temperature (الحرارة المحيطة)"
            18 -> "Step Detector (كاشف الخطوات)"
            19 -> "Step Counter (عداد الخطوات)"
            20 -> "Geomagnetic Rotation (الدوران المغناطيسي)"
            21 -> "Heart Rate (نبض القلب)"
            65536 -> "Vendor CAN / MCU Sensor"
            else -> "Type #$type"
        }
    }

    /**
     * Reads temperature from Linux/Android kernel thermal sysfs (/sys/class/thermal/thermal_zone*)
     * Common on car head units and automotive Android boards.
     */
    private fun hasThermalSysfs(): Boolean {
        val thermalDir = File("/sys/class/thermal")
        return thermalDir.exists() && thermalDir.isDirectory
    }

    private fun readThermalZoneTemp(): Float? {
        try {
            for (i in 0..25) {
                val file = File("/sys/class/thermal/thermal_zone$i/temp")
                if (file.exists() && file.canRead()) {
                    val content = file.readText().trim()
                    val rawVal = content.toFloatOrNull() ?: continue
                    val degC = if (rawVal > 1000) rawVal / 1000f else rawVal
                    if (degC in -20f..115f) {
                        return degC
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Thermal sysfs read not permitted or failed: ${e.message}")
        }
        return null
    }
}
