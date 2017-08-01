package com.parse.session;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Sean on 7/31/2017.
 */

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(String... params) {

        try {
            URL url = new URL(params[0]);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.connect();

            InputStream inputStream = connection.getInputStream();

            Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

            return myBitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
