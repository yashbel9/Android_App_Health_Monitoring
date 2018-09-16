package com.example.android.group_23;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.android.group_23.helper.DatabaseHelper;
import com.example.android.group_23.interfaces.ServiceCallbacks;
import com.example.android.group_23.model.Patient;
import com.example.android.group_23.network.UploadDownload;
import com.example.android.group_23.service.SensorService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static com.example.android.group_23.helper.DatabaseHelper.DATABASE_FILE_PATH;
import static com.example.android.group_23.helper.DatabaseHelper.DATABASE_NAME;

public class MainActivity extends Activity implements ServiceCallbacks {

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series, series1, series2;
    private int xcoor = 0;
    private boolean flag1 = true, runFlag = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    File mFolderStructure;
    Boolean mSuccess = false;
    private SensorService mSensorService;
    private boolean bound = false;
//    private UploadDownload mUploadDownload;
//    public static final String DOWNLOADED_FILENAME = "group_23.db";
//    public static final String ENV_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
//    public static final String DOWNLOAD_FILE_PATH = "Android"+ File.separator + "CSE535_ASSIGNMENT2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * Pass context to asyncTask
         */
//        mUploadDownload = new UploadDownload(this);
        if(!isStoragePermissionGranted() && !isInternetPermissionGranted()) {
            Log.e(TAG,"Storage Permission Denied!");
            Toast.makeText(MainActivity.this, "Storage Permission Denied!", Toast.LENGTH_SHORT).show();
            return;
        }

        /**
         * TODO: Remove code. Only for testing
         */
        /*((EditText) findViewById(R.id.editTextPatientName)).setText("Saurabh");
        ((EditText) findViewById(R.id.editTextPatientID)).setText("1234");
        ((EditText) findViewById(R.id.editTextPatientAge)).setText("25");
        ((RadioGroup) findViewById(R.id.radioGroupGender)).check(R.id.radioButtonMale);
        Patient.getInstance("Saurabh","1234","M",25);*/

        final GraphView graph = (GraphView) findViewById(R.id.plotgraph);
        final GraphView temp = (GraphView) findViewById(R.id.plotgraph1);
        GridLabelRenderer gridlabel = graph.getGridLabelRenderer();
        gridlabel.setHorizontalAxisTitle("Time(sec)");
        gridlabel.setVerticalAxisTitle("Directional Acceleration");
        //StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        //staticLabelsFormatter.setHorizontalLabels(new String[] {"0", "Time (sec)", ""});
        //staticLabelsFormatter.setVerticalLabels(new String[] {"", "Acceleration ", ""});
        //graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        series = new LineGraphSeries<DataPoint>();
        series.setDrawDataPoints(true);
        series.setColor(Color.GREEN);
        series.setDrawBackground(true);
        graph.addSeries(series);
        series1 = new LineGraphSeries<DataPoint>();
        series1.setDrawDataPoints(true);
        series1.setColor(Color.RED);
        series1.setDrawBackground(true);
        graph.addSeries(series1);
        series2 = new LineGraphSeries<DataPoint>();
        series2.setDrawDataPoints(true);
        series2.setDrawBackground(true);
        graph.addSeries(series2);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(20);
        viewport.setScrollable(true);
        viewport.scrollToEnd();
        temp.setVisibility(View.INVISIBLE);
        final Button downloadButton = (Button) findViewById(R.id.buttonDownload);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Check if folder exists
                 */
                temp.setVisibility(View.INVISIBLE);
                graph.setVisibility(View.VISIBLE);
                mFolderStructure = new File(DATABASE_FILE_PATH);
                if (!mFolderStructure.exists()) {
                    mSuccess = mFolderStructure.mkdir();
                } else {
                    Log.e(TAG,"Directory already exists");
                    mSuccess = true;
                }
                if(!mSuccess) {
                    Log.e(TAG,"Cannot create Directory");
                }
                new UploadDownload(getApplicationContext()).execute();

                mFolderStructure = new File(DATABASE_FILE_PATH + File.separator + DATABASE_NAME);
                if (!mFolderStructure.exists()) {
                    Log.e(TAG,"Database file doesn't exists");
                    Toast.makeText(MainActivity.this, "Database file doesn't exist. Failed to show last 10 records.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Log.e(TAG,"Database file exists");
                    DatabaseHelper databaseHelper = new DatabaseHelper();
                    databaseHelper.getLastTenRecords();
                }
            }
        });

        final Button uploadButton = (Button) findViewById(R.id.buttonUpload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Check if folder exists
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //creating new thread to handle Http Operations
                        uploadFile();
                    }
                }).start();
                mFolderStructure = new File(DATABASE_FILE_PATH + File.separator + DATABASE_NAME);
                if (!mFolderStructure.exists()) {
                    Log.e(TAG,"Database file doesn't exists");
                    Toast.makeText(MainActivity.this, "Database file does'nt exist. Nothing to upload.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Log.e(TAG,"Database file exists");
                }
//                new UploadDownload(getApplicationContext()).execute("Upload");
            }
        });

        final Button run = findViewById(R.id.buttonRun);
        final Button stop = findViewById(R.id.buttonStop);

        /**
         * Handling RUN button onClick event
         */
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Code to close the virtual keyboard on screen
                 */
                InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String patientSex;
                String patientName = ((EditText) findViewById(R.id.editTextPatientName)).getText().toString();
                String patientID = ((EditText) findViewById(R.id.editTextPatientID)).getText().toString();
                String patientAge = ((EditText) findViewById(R.id.editTextPatientAge)).getText().toString();



                if (patientID.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please enter Patient's ID.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (patientAge.matches("")){
                    Toast.makeText(getApplicationContext(), "Please enter Patient's Age.", Toast.LENGTH_SHORT).show();
                    return;
                } else if(patientName.matches("")){
                    Toast.makeText(getApplicationContext(), "Please enter Patient's Name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioGroup radioButtonGroup = ((RadioGroup) findViewById(R.id.radioGroupGender));
                int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1)
                {
                    Toast.makeText(getApplicationContext(), "Please select Patient's Gender.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    View radioButton = radioButtonGroup.findViewById(radioButtonID);
                    int idx = radioButtonGroup.indexOfChild(radioButton);
                    if(idx == 0)
                        patientSex = "M";
                    else
                        patientSex = "F";
                }
                Patient.getInstance(patientName,patientID,patientSex, new Integer(patientAge));

                DatabaseHelper databaseHelper = new DatabaseHelper();
                databaseHelper.createDatabase();
                Log.d(TAG,"Database created");

                /**
                 * Start service to update accelerometer values into the database
                 */
                Intent intent = new Intent(getApplicationContext(), SensorService.class);
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                startService(intent);

                graph.setVisibility(View.VISIBLE);
                temp.setVisibility(View.INVISIBLE);
                flag1=true;
                /**
                 * Used runFlag to stop multiple thread from spawning
                 */
                if(!runFlag) {
                    /**
                     * Spawning a new thread to keep updating the GraphView
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                /**
                                 * Runs the specified action on the UI thread.
                                 * Here the current thread is not the UI thread, the action is posted to the event queue of the UI thread.
                                 */
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        /**
                                         * The graph is paused and a temporary graph is overlapped when the STOP button onClick event occurs.
                                         */
                                        stop.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                flag1 = false;
                                                graph.setVisibility(View.INVISIBLE);
                                                temp.setVisibility(View.VISIBLE);
                                                /**
                                                 * Stop service
                                                 */
                                                if (bound) {
                                                    mSensorService.setCallbacks(null); // unregister
                                                    unbindService(serviceConnection);
                                                    bound = false;
                                                }
                                                Intent intent = new Intent(getApplicationContext(), SensorService.class);
                                                stopService(intent);
                                            }
                                        });
                                        if (flag1) {
                                            //addEntry();
                                        }
                                    }
                                });

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                    runFlag = true;
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        // bind to Service
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from service
        if (bound) {
            mSensorService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    /** Callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get SensorService instance
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            mSensorService = binder.getService();
            bound = true;
            mSensorService.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    /* Defined by ServiceCallbacks interface */
    @Override
    public void addEntry(double x, double y, double z) {
        series.appendData(new DataPoint(xcoor++, x), false, 10);
        series1.appendData(new DataPoint(xcoor++, y), false, 10);
        series2.appendData(new DataPoint(xcoor++, z), false, 10);
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"WRITE_EXTERNAL_STORAGE Permission is granted");
                return true;
            } else {

                Log.d(TAG,"WRITE_EXTERNAL_STORAGE Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.d(TAG,"WRITE_EXTERNAL_STORAGE Permission is granted");
            return true;
        }
    }
    public  boolean isInternetPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"INTERNET Permission is granted");
                return true;
            } else {

                Log.d(TAG,"INTERNET Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
                return false;
            }
        }
        else {
            Log.d(TAG,"INTERNET Permission is granted");
            return true;
        }
    }
//    private void addEntry() {
//
//    }

    public int uploadFile(){

        int serverResponseCode = 0;

        String selectedFilePath = DATABASE_FILE_PATH + File.separator + DATABASE_NAME;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);

        String[] parts = selectedFilePath.split("/");
        String fileName = parts[parts.length-1];
        Log.d(TAG, "PATH: "+ selectedFilePath);
        Log.d(TAG, "FILENAME: "+fileName);
        Log.d(TAG, "SELECETED FILE: "+ selectedFile.isFile());
        if (!selectedFile.isFile()){
            Log.e(TAG, "File does not exists");
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL("http://impact.asu.edu/CSE535Spring18Folder/UploadToServer.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                while (bytesRead > 0){
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();


                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"File upload successful",Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.d(TAG, "File upload successful. Response: " + serverResponseMessage + ": " + serverResponseCode);
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"File upload failed",Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e(TAG, "File upload failed. Response: " + serverResponseMessage + ": " + serverResponseCode);
                }

                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            return serverResponseCode;
        }
    }


}