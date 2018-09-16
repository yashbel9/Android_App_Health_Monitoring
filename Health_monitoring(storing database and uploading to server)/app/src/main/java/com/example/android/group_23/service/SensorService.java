package com.example.android.group_23.service;

/**
 * Created by saurabh on 3/6/18.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.example.android.group_23.interfaces.ServiceCallbacks;
import com.example.android.group_23.model.Patient;
import com.example.android.group_23.helper.DatabaseHelper;

public class SensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    static int ACCELEROMETER_FREQUENCY = 1000;
    long mLastSaved = System.currentTimeMillis();
    private static final String TAG = SensorService.class.getSimpleName();
    private ServiceCallbacks serviceCallbacks;
    private final IBinder binder = new LocalBinder();



    public class LocalBinder extends Binder {
        public SensorService getService() {
            // Return this instance of MyService so clients can call public methods
            return SensorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI, new Handler());
        return START_NOT_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ((System.currentTimeMillis() - mLastSaved) > ACCELEROMETER_FREQUENCY) {
            mLastSaved = System.currentTimeMillis();
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            DatabaseHelper databaseHelper = new DatabaseHelper();
            SQLiteDatabase database = databaseHelper.getWritableDatabase();
            databaseHelper.updateTable(database, Patient.TableSchema.TABLE_NAME,x,y,z);
            if (serviceCallbacks != null) {
                serviceCallbacks.addEntry(x, y, z);
            }
        }
    }
    @Override
    public void onDestroy() {
        /**
         * Unregister the SensorManager Listener
         */
        mSensorManager.unregisterListener(this, mAccelerometer);
        setCallbacks(null);
        super.onDestroy();
    }

}