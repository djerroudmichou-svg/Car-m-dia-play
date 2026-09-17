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

class CarTelemetryManager(context: Context) : SensorEventListener, LocationListener {
    private val TAG = "CarTelemetryManager"
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    private val _ambientTemp = MutableStateFlow(25f)
    val ambientTemp: StateFlow<Float> = _ambientTemp.asStateFlow()
    
    private val _carSpeed = MutableStateFlow(0)
    val carSpeed: StateFlow<Int> = _carSpeed.asStateFlow()
    
    private var tempSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    fun startTracking() {
        // 1. Register Temperature Sensor
        if (tempSensor != null) {
            sensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(TAG, "Ambient Temperature sensor registered")
        } else {
            Log.w(TAG, "Ambient Temperature sensor not found, using last known or mock if needed")
        }
        
        // 2. Register Location Updates for Speed
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, this)
                Log.i(TAG, "GPS speed tracking registered")
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 2f, this)
                Log.i(TAG, "Network speed tracking registered (less accurate)")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing for speed tracking", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates", e)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    // --- SensorEventListener ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            _ambientTemp.value = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // --- LocationListener ---
    override fun onLocationChanged(location: Location) {
        if (location.hasSpeed()) {
            // Convert m/s to km/h (1 m/s = 3.6 km/h)
            val speedKmH = (location.speed * 3.6).toInt()
            _carSpeed.value = speedKmH
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
