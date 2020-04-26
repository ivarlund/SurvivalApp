package com.irl.survivalapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Compass class of the survival application.
 * Gets sensor data from Orientation sensor and renders a 2D representation
 * of a compass to guide the user to safety.
 */
public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor orientationSensor;

    private ImageView compass;
    private TextView bearingDisplay;

    private float originalBearing = 0f;

    /**
     * View call. Sets activity and variables.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        compass = findViewById(R.id.compass);
        bearingDisplay = findViewById(R.id.bearing);
    }

    /** Register this class as the sensor listener. */
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /** Unregisters this as a sensor listener. */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Updates the bearing data and compass rendering.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        float bearing = event.values[0];
        bearingDisplay.setText(Float.toString(bearing));

        Animation animation = new RotateAnimation(originalBearing, -bearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(300);
        compass.startAnimation(animation);
        originalBearing = -bearing;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
