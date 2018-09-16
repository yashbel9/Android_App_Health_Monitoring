package com.example.android.group_23.network;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import static com.example.android.group_23.helper.DatabaseHelper.DATABASE_FILE_PATH;
import static com.example.android.group_23.helper.DatabaseHelper.DATABASE_NAME;

/**
 * Created by saurabh on 3/8/18.
 */

public class UploadDownload extends AsyncTask<String, Void, String> {

    private static final String TAG = UploadDownload.class.getSimpleName();
    private Context mContext;

    public UploadDownload(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected String doInBackground(String... params) {
            try
            {
                DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse("http://impact.asu.edu/CSE535Spring18Folder/"+DATABASE_NAME);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                Log.d(TAG,"URI : "+ uri.getLastPathSegment());
                request.setDestinationInExternalPublicDir("Android"+ File.separator + "CSE535_ASSIGNMENT2", uri.getLastPathSegment()).setTitle(uri.getLastPathSegment());
                downloadManager.enqueue(request);
                Log.d(TAG, "File downloaded successfully");

            } catch (Exception ex) {
                Log.d(TAG, "File download failed");
                ex.printStackTrace();
            }
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG,"Finished executing Upload/Download");
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}