package com.cjlab.taggedgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class Compass extends FragmentActivity {
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];


    private static float angle;

    public Compass(Context context) {
        createCompass(context);
    }

    public void createCompass(Context context) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener sensorEventListenerAccelerometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                //angle = (float) (floatOrientation[0] - 3.1415 / 1.8);
                angle = (float) (-floatOrientation[0] + 1.9);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                //angle = (float) (floatOrientation[0] - 3.1415 / 1.8);
                angle = (float) (-floatOrientation[0] + 2);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelerometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static float getAngle() {
        return angle;
    }
}
