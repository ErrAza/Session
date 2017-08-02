package com.parse.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Sean on 8/1/2017.
 */

public class MusicFragment extends Fragment implements AsyncResponse {

    SharedPreferences sharedPreferences;

    ViewFlipper viewFlipper;

    ListView artistsListView;
    List<String> artistList;
    ArrayAdapter artistsArrayAdapter;

    ListView similarArtistsListView;
    ArrayList<String> similarArtistsList = new ArrayList<>();
    ArrayAdapter similarArtistsArrayAdapter;

    Artist artist;
    ImageView artistImageView;
    TextView artistTextView;
    TextView tagTextView;
    Button addArtistButton;
    ProgressBar progressBar;
    Button backButton;
    Bitmap artistImage;

    DownloadTask task;


    public static MusicFragment newInstance() {
        return new MusicFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        artistList = ProfileManager.getInstance().currentUser.getArtistList();

        Collections.sort(artistList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        viewFlipper = (ViewFlipper) getView().findViewById(R.id.viewFlipper);

        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);

        viewFlipper.setInAnimation(in);
        viewFlipper.setOutAnimation(out);

        artistsListView = (ListView) getView().findViewById(R.id.artistsListView);
        artistsArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, artistList);
        artistsListView.setAdapter(artistsArrayAdapter);

        artistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!artistList.isEmpty())
                {
                    Log.i("INFO", artistList.get(position));

                    FindArtist(artistList.get(position));

                    viewFlipper.showNext();
                }
            }
        });

        progressBar = (ProgressBar) getView().findViewById(R.id.progressBarImageView);
        artistImageView = (ImageView) getView().findViewById(R.id.artistImageView);
        artistTextView = (TextView) getView().findViewById(R.id.textViewArtistName);
        tagTextView = (TextView) getView().findViewById(R.id.textViewArtistTag);
        similarArtistsListView = (ListView) getView().findViewById(R.id.similarArtistsListView);
        addArtistButton = (Button) getView().findViewById(R.id.addArtistButton);
        addArtistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddArtistToUser(v);
            }
        });
        backButton = (Button) getView().findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back(v);
            }
        });

        similarArtistsArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, similarArtistsList);

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

        similarArtistsListView.setAdapter(similarArtistsArrayAdapter);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {

        viewFlipper = null;
        artistsListView = null;
        artistList = null;
        artistsArrayAdapter = null;
        similarArtistsListView = null;
        similarArtistsList = null;
        similarArtistsArrayAdapter = null;
        artist = null;
        artistImageView = null;
        artistTextView = null;
        tagTextView = null;
        addArtistButton = null;
        progressBar = null;
        backButton = null;
        task = null;
        artistImage = null;

        super.onDestroyView();
    }

    public void Back(View view)
    {
        viewFlipper.showPrevious();
    }

    private void FindArtist(String artistName)
    {
        String url = LastFmRest.getInstance().GetArtist(artistName);

        task = new DownloadTask();
        task.delegate = this;
        task.bar = progressBar;
        task.execute(url);

        artistImage = AttemptToFetchFromLocal(artistName);

        artistTextView.setText("Fetching...");
        tagTextView.setText("");

        similarArtistsList.clear();
        similarArtistsArrayAdapter.notifyDataSetChanged();

        if (artistImage != null)
        {
            artistImageView.setImageBitmap(artistImage);
            artistTextView.setText(artistName);
        }
    }

    private void PopulateSimilarArtistsView()
    {
        similarArtistsList.clear();

        for (Artist artiste : artist.getSimilar())
        {
            similarArtistsList.add(artiste.getName());
        }

        similarArtistsArrayAdapter.notifyDataSetChanged();
    }

    public void AddArtistToUser(View view)
    {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");

        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

        final ArrayList<String> tempList = new ArrayList<>();
        tempList.add(artist.getName());

        final ArrayList<String> tagList = new ArrayList<>();
        tagList.add(artist.getTag());

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
                        object.addAllUnique("genres", tagList);
                        object.saveInBackground();
                        Toast.makeText(getActivity(), "Added " + artist.getName(), Toast.LENGTH_SHORT).show();
                        ProfileManager.getInstance().currentUser.getArtistList().add(artist.getName());
                        Collections.sort(artistList, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.compareTo(o2);
                            }
                        });
                        artistsArrayAdapter.notifyDataSetChanged();
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

                if (artistImage == null) {

                    ImageDownloader imageDownloader = new ImageDownloader();

                    try {
                        Log.i("LASTFM", "Fetching from LastFM.");
                        artistImage = imageDownloader.execute(artist.getImageUrl()).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (artistImage != null) {
                        artistImageView.setImageBitmap(artistImage);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        artistImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        String arrayString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                        sharedPreferences.edit().putString(artist.getName(), arrayString).apply();

                        Log.i("PARSE", "Saving a copy on parse.");
                        ParseFile imageFile = new ParseFile(java.util.UUID.randomUUID() + ".png", byteArray);
                        ParseObject artistImageObject = new ParseObject("ArtistImages");
                        artistImageObject.put("artist", artist.getName());
                        artistImageObject.put("image", imageFile);
                        artistImageObject.saveInBackground();
                    }
                }

                artistTextView.setText(artist.getName());
                tagTextView.setText(artist.getTag());

                ParseQuery<ParseObject> query2 = ParseQuery.getQuery("UserInfo");

                query2.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

                query2.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null)
                        {
                            if (objects.size() > 0)
                            {
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

            String[] tagSplits = firstTag.getString("name").split(" ");
            String tag = "";
            for (String tagSplit : tagSplits)
            {
                tag += tagSplit.substring(0, 1).toUpperCase() + tagSplit.substring(1).toLowerCase() +  " ";
            }
            tag = tag.substring(0, tag.length() - 1);

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

    private Bitmap AttemptToFetchFromLocal(String artistName)
    {
        Bitmap bitmap = null;

        String localByteArrayString = sharedPreferences.getString(artistName, "Nope");

        if (!localByteArrayString.equals("Nope"))
        {
            Log.i("LOCAL", "Found local data.");
            byte[] array = Base64.decode(localByteArrayString, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
        }

        return bitmap;
    }

}
