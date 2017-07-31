package com.parse.session;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sean on 7/27/2017.
 */

public class DownloadTask extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... urls) {

        String result = "";
        URL url;
        HttpURLConnection urlConnection;

        try {
            url = new URL(urls[0]);

            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);

            int data = reader.read();

            while (data != -1) {
                char current = (char) data;
                result += current;

                data = reader.read();
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            delegate.TaskComplete(result);
        }
        else {
            Log.e("ERROR", "ONPOSTEXECUTE ISSUE");
        }
    }
}
