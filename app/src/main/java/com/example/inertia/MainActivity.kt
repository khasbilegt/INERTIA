package com.example.inertia

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.inertia.databinding.ActivityMainBinding
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var binding: ActivityMainBinding
    private var sensor: Sensor? = null

    private val velocity = FloatArray(3)
    private val position = FloatArray(3)
    private var timestamp: Long = 0L
    private val NS2S = 1.0f / 1000000000.0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.layoutSensor.setOnClickListener { reset() }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        setSupportActionBar(findViewById(R.id.toolbar))


        when(intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                        Log.i("Text", it)
                        binding.webview.webViewClient = WebViewClient()
                        binding.webview.settings.domStorageEnabled = true
                        binding.webview.settings.javaScriptEnabled = true
                        binding.webview.loadUrl(it)
                    }
                }
            }
            else -> {
                Log.w("Warning", "Not Supported!!!")
                Toast.makeText(applicationContext, "Not supported content", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu).let {
            return true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_reset -> {
            // User chose the "Settings" item, show the app settings UI...
            reset()
            Toast.makeText(applicationContext, "View reset...", Toast.LENGTH_SHORT).show()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        calculate(event)
    }

    override fun onResume() {
        super.onResume()
        sensor?.also { acceleration ->
            sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun calculate(event: SensorEvent) {
//        Log.i("X", event.values[0].roundToInt().toString())
//        Log.i("Y", event.values[1].roundToInt().toString())
//        Log.i("Z", event.values[2].roundToInt().toString())

        if (timestamp != 0L) {
            val dt: Float = (event.timestamp - timestamp) * NS2S
            for (index in 0..2) {
                velocity[index] += event.values[index].roundToInt() * dt - 0.5F * velocity[index]
                position[index] += velocity[index] * 10000 * dt - 0.4F * position[index]
            }
//            binding.apply {
//                sensorXText.text = event.values[0].roundToInt().toString()
//                sensorYText.text = event.values[1].roundToInt().toString()
//                sensorZText.text = event.values[2].roundToInt().toString()
//
//                velocityXText.text = velocity[0].toString()
//                velocityYText.text = velocity[1].toString()
//                velocityZText.text = velocity[2].toString()
//
//                positionXText.text = position[0].toString()
//                positionYText.text = position[1].toString()
//                positionZText.text = position[2].toString()
//            }
        }

        timestamp = event.timestamp;

        binding.webview.translationX = -position[0];
        binding.webview.translationY = position[1];
    }

    private fun reset() {
        velocity[0] = 0F
        velocity[1] = 0F
        velocity[2] = 0F
        position[0] = 0F
        position[1] = 0F
        position[2] = 0F
        timestamp = 0L;

        binding.webview.translationX = 0F;
        binding.webview.translationY = 0F;
    }
}