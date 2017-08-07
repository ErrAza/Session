package com.sean.session;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Sean on 8/3/2017.
 */

public class LastFmJsonParser {

    private static LastFmJsonParser instance = null;

    protected LastFmJsonParser() {}

    public static LastFmJsonParser getInstance()
    {
        if (instance == null)
        {
            instance = new LastFmJsonParser();
        }

        return instance;
    }

    public String[] ParseArtistSearchResults(JSONObject result)
    {
        if (result == null)
            return null;

        String[] searchResults = null;

        try {

            JSONObject resultsObject = result.getJSONObject("results");
            JSONObject artistMatches = resultsObject.getJSONObject(("artistmatches"));
            JSONArray artistsArray = artistMatches.getJSONArray("artist");

            searchResults = new String[artistsArray.length()];

            for(int i = 0; i < artistsArray.length(); i++)
            {
                JSONObject obj = artistsArray.getJSONObject(i);
                searchResults[i] = obj.getString("name");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return searchResults;
    }

    public Artist ParseArtistInfo(JSONObject result)
    {
        Artist artist = new Artist();

        try {

            JSONObject artistInfo = result.getJSONObject("artist");
            JSONArray imageInfoList = new JSONArray(artistInfo.getString("image"));
            JSONObject similarInfo = new JSONObject(artistInfo.getString("similar"));
            JSONArray similarArtists = new JSONArray(similarInfo.getString("artist"));

            JSONObject tagsInfo = artistInfo.getJSONObject("tags");
            JSONArray tagsArray = new JSONArray(tagsInfo.getString("tag"));
            JSONObject firstTag = new JSONObject(tagsArray.get(0).toString());

            String[] tagSplits = firstTag.getString("name").split(" ");
            String tag = "";
            for (String tagSplit : tagSplits)
            {
                tag += tagSplit.substring(0, 1).toUpperCase() + tagSplit.substring(1).toLowerCase() +  " ";
            }
            tag = tag.substring(0, tag.length() - 1);

            artist.setName(artistInfo.getString("name"));
            artist.setTag(tag);

            for(int i = 0; i < imageInfoList.length(); i++)
            {
                JSONObject object = new JSONObject(imageInfoList.get(i).toString());
                if (object.getString("size").equals("large"))
                {
                    artist.setImageUrl(object.getString("#text"));
                    Log.i("IMG", artist.getImageUrl());
                }
            }

            ArrayList<String> similarArtistsList = new ArrayList<>();

            for(int i = 0; i < similarArtists.length(); i++)
            {
                JSONObject object = new JSONObject(similarArtists.get(i).toString());

                similarArtistsList.add(object.getString("name"));
            }

            artist.setSimilar(similarArtistsList);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return artist;
    }
}
