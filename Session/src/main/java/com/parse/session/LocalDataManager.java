package com.parse.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sean on 8/3/2017.
 */

public class LocalDataManager {

    private static LocalDataManager instance = null;

    public static SharedPreferences sharedPreferences;

    protected LocalDataManager() {}

    public static LocalDataManager getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new LocalDataManager();
        }

        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return instance;
    }

    public Bitmap FetchBitmapFromLocal(String artistName)
    {
        Artist artist = FetchArtistFromLocal(artistName);

        if (artist == null)
            return null;

        Bitmap bitmap = null;

        String localByteArrayString = artist.getImageArrayString();

        if (localByteArrayString == null)
            return null;

        if (!localByteArrayString.isEmpty())
        {
            Log.i("LOCAL", "Decoding Bitmap Bytes.");
            byte[] array = Base64.decode(localByteArrayString, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
        }

        return bitmap;
    }

    public void SaveBitmapToLocal(Bitmap bitmap, String artistName)
    {
        Artist artist = FetchArtistFromLocal(artistName);

        if (artist == null)
        {
            Log.i("ERROR", "Can't save Bitmap to a null artist.");
            return;
        }

        byte[] byteArray = ConvertBitmapToByteArray(bitmap);

        String arrayString = Base64.encodeToString(byteArray, Base64.DEFAULT);

        artist.setImageArrayString(arrayString);

        SaveArtistToLocal(artist);
    }

    public byte[] ConvertBitmapToByteArray(Bitmap bitmap)
    {
        byte[] byteArray;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();

        return byteArray;
    }

    public String ConvertBitmapToStringArrayOfBytes(Bitmap bitmap)
    {
        byte[] byteArray = ConvertBitmapToByteArray(bitmap);

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void SaveArtistToLocal(Artist artist)
    {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(artist);
        prefEditor.putString(artist.getName(), json);
        prefEditor.apply();
    }

    public Artist FetchArtistFromLocal(String artistName)
    {
        if (!sharedPreferences.contains(artistName))
            return null;

        Gson gson = new Gson();
        String json = sharedPreferences.getString(artistName, "");
        Artist artist = gson.fromJson(json, Artist.class);

        return artist;
    }
}
