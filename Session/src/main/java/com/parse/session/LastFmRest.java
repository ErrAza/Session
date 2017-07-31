package com.parse.session;

import java.util.List;

/**
 * Created by Sean on 7/27/2017.
 */

public class LastFmRest {

    private static LastFmRest instance = null;

    private String key = "8c98673bdbeeed85aa538c27326cb518";
    private String baseUrl = "http://ws.audioscrobbler.com/2.0/";

    protected LastFmRest() {}

    public static LastFmRest getInstance()
    {
        if (instance == null)
        {
            instance = new LastFmRest();
        }

        return instance;
    }

    public String GetArtist(String artistName)
    {
        if (artistName.length() <= 0)
            return null;

        String url = baseUrl + "?method=artist.getinfo&artist=" + artistName;
        url += "&api_key=" + key;
        url += "&format=json";

        return url;
    }

    public String GetTagsForArtist(String artistName)
    {
        if (artistName.length() <= 0)
            return null;

        String url = baseUrl + "?method=artist.gettoptags&artist=" + artistName;
        url += "&api_key=" + key;
        url += "&format=json";

        return url;
    }
}
