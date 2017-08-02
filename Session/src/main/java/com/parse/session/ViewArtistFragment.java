package com.parse.session;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewArtistFragment extends Fragment implements AsyncResponse {


    private ArrayList<String> ApiResultsList = new ArrayList<>();

    private ListView similarArtistsListView;

    private DownloadTask task;


    ArrayList<String> similarArtistsList = new ArrayList<>();

    ArrayAdapter arrayAdapter;

    ParseUser currentParseUser;

    Artist artist;

    ImageView artistImageView;

    TextView artistTextView;

    TextView tagTextView;

    Button addArtistButton;

    ProgressBar progressBar;

    public static ViewArtistFragment newInstance() {
        return new ViewArtistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_view_artist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        progressBar = (ProgressBar) getView().findViewById(R.id.progressBarImageView);

        artistImageView = (ImageView) getView().findViewById(R.id.artistImageView);

        artistTextView = (TextView) getView().findViewById(R.id.textViewArtistName);

        tagTextView = (TextView) getView().findViewById(R.id.textViewArtistTag);

        similarArtistsListView = (ListView) getView().findViewById(R.id.artistsListView);

        addArtistButton = (Button) getView().findViewById(R.id.addArtistButton);

        currentParseUser = ParseUser.getCurrentUser();



        String url = LastFmRest.getInstance().GetArtist("Erra");

        task = new DownloadTask();
        task.delegate = this;
        task.bar = progressBar;
        task.execute(url);

        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, similarArtistsList);

        similarArtistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!similarArtistsList.isEmpty())
                {
                    Log.i("INFO", similarArtistsList.get(position));

                    FindArtist(similarArtistsList.get(position));
                }
            }
        });

        similarArtistsListView.setAdapter(arrayAdapter);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    private void FindArtist(String artistName)
    {
        String url = LastFmRest.getInstance().GetArtist(artistName);

        task = new DownloadTask();
        task.delegate = this;
        task.execute(url);
    }

    private void PopulateSimilarArtistsView()
    {
        similarArtistsList.clear();

        for (Artist artiste : artist.getSimilar())
        {
            similarArtistsList.add(artiste.getName());
        }

        arrayAdapter.notifyDataSetChanged();
    }

    public void AddArtistToUser(View view)
    {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");

        query.whereEqualTo("username", currentParseUser.getUsername());

        final ArrayList<String> tempList = new ArrayList<>();
        tempList.add(artist.getName());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null)
                {
                    if (objects.size() > 0)
                    {
                        Log.i("INFO", "updating artists");
                        ParseObject object = objects.get(0);
                        object.addAllUnique("artists", tempList);
                        object.saveInBackground();
                    }
                }
            }
        });

        addArtistButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void TaskComplete(String result) {

        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(result);

            if (jsonObject.has("artist"))
            {
                ParseArtistInfo(jsonObject);

                ImageDownloader imageDownloader = new ImageDownloader();
                Bitmap artistImage = null;
                try {
                    artistImage = imageDownloader.execute(artist.getImageUrl()).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (artistImage != null)
                {
                    artistImageView.setImageBitmap(artistImage);
                }

                artistTextView.setText(artist.getName());
                tagTextView.setText(artist.getTag());

                ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");

                query.whereEqualTo("username", currentParseUser.getUsername());

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null)
                        {
                            if (objects.size() > 0)
                            {
                                Log.i("INFO", "updating artists");
                                ParseObject object = objects.get(0);
                                if (object.getList("artists").contains(artist.getName()))
                                {
                                    addArtistButton.setVisibility(View.INVISIBLE);
                                }
                                else
                                {
                                    addArtistButton.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void ParseArtistInfo(JSONObject result)
    {
        try {

            JSONObject artistInfo = result.getJSONObject("artist");
            JSONArray imageInfoList = new JSONArray(artistInfo.getString("image"));
            JSONObject similarInfo = new JSONObject(artistInfo.getString("similar"));
            JSONArray similarArtists = new JSONArray(similarInfo.getString("artist"));

            JSONObject tagsInfo = artistInfo.getJSONObject("tags");
            JSONArray tagsArray = new JSONArray(tagsInfo.getString("tag"));
            JSONObject firstTag = new JSONObject(tagsArray.get(0).toString());

            String tag = firstTag.getString("name");
            tag = tag.substring(0, 1).toUpperCase() + tag.substring(1).toLowerCase();

            artist = new Artist();

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

            ArrayList<Artist> similarArtistsList = new ArrayList<>();

            for(int i = 0; i < similarArtists.length(); i++)
            {
                JSONObject object = new JSONObject(similarArtists.get(i).toString());
                Artist similarArtist = new Artist();
                similarArtist.setName(object.getString("name"));

                similarArtistsList.add(similarArtist);
            }

            artist.setSimilar(similarArtistsList);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        PopulateSimilarArtistsView();
    }
}
