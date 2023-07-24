package com.example.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.math.sqrt

class MotionDetector(
    private val context: Context
) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val walkingThreshold = 1.5f
    private val runningThreshold = 3.5f

    private val alpha = 0.8f
    private var gravity: FloatArray? = null

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun startListening() {
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            processAccelerometerData(event.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun processAccelerometerData(values: FloatArray) {
        if (gravity == null) {
            gravity = FloatArray(3)
        }
        gravity?.let {
            it[0] = alpha * it[0] + (1 - alpha) * values[0]
            it[1] = alpha * it[1] + (1 - alpha) * values[1]
            it[2] = alpha * it[2] + (1 - alpha) * values[2]
        }

        val accelWithoutGravity = FloatArray(3)
        accelWithoutGravity[0] = values[0] - gravity!![0]
        accelWithoutGravity[1] = values[1] - gravity!![1]
        accelWithoutGravity[2] = values[2] - gravity!![2]

        val acceleration = calculateAcceleration(accelWithoutGravity)
        detectMotion(acceleration)
    }

    private fun calculateAcceleration(values: FloatArray): Float {
        val x = values[0]
        val y = values[1]
        val z = values[2]
        return sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    private fun detectMotion(
        acceleration: Float
    ) {
        val accelerationList: ArrayList<Float> = arrayListOf()
        accelerationList.add(acceleration)

        val accelVal = accelerationList.average()
        Log.d("accelVal", "$accelVal")

        if (accelVal < walkingThreshold) {
            Log.d("accelVal", "Still")
            //Toast.makeText(context, "Staying Still", Toast.LENGTH_SHORT).show()
        } else if (accelVal < runningThreshold) {
            Log.d("accelVal", "Walking")
            //Toast.makeText(context, "Walking", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("accelVal", "Running")
            // Toast.makeText(context, "Running", Toast.LENGTH_SHORT).show()
        }
        accelerationList.clear()
    }
}

