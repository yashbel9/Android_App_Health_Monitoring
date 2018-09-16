package com.example.android.group_23.model;

import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by saurabh on 3/5/18.
 */


public final class Patient {

    private static Patient mPatient = null;
    private String mPatientName = "CSE512Assignmanet2";
    private String mPatientID = "CSE512";
    private String mPatientSex = "M";
    private Integer mPatientAge = 24;

    /**
     * Singleton class
     */
    private Patient() {}
    private static final String TAG = Patient.class.getSimpleName();

    public static Patient getInstance(String patientName, String patientID, String patientSex, Integer patientAge) {
        if (mPatient == null) {
            mPatient = new Patient();
            Log.d(TAG,"Created Patient object");
            mPatient.mPatientName = patientName;
            mPatient.mPatientID = patientID;
            mPatient.mPatientSex = patientSex;
            mPatient.mPatientAge = patientAge;
        }
        return(mPatient);
    }

    /* Inner class that defines the table contents */
    public static class TableSchema implements BaseColumns {
        public static final String TABLE_NAME = mPatient.mPatientName + "_" + mPatient.mPatientID + "_" + mPatient.mPatientAge + "_" + mPatient.mPatientSex;
        public static final String TIMESTAMP = "Timestamp";
        public static final String xValues = "X_values";
        public static final String yValues = "Y_values";
        public static final String zValues = "Z_values";
    }
}

