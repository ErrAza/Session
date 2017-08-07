package com.parse.session;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Sean on 8/1/2017.
 */

public class MusicFragment extends Fragment {


    ViewFlipper viewFlipper;

    ListView artistsListView;
    List<String> artistList;
    ArrayAdapter artistsArrayAdapter;

    ListView similarArtistsListView;
    List<String> similarArtistsList = new ArrayList<>();
    ArrayAdapter similarArtistsArrayAdapter;

    Artist artist;
    ImageView artistImageView;
    TextView artistTextView;
    TextView tagTextView;
    Button addArtistButton;
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
                    Log.i("INFOaa", similarArtistsList.get(position));

                    FindArtist(similarArtistsList.get(position));
                }
            }
        });

        similarArtistsListView.setAdapter(similarArtistsArrayAdapter);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {

        /*viewFlipper = null;
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
        backButton = null;
        task = null;
        artistImage = null;*/

        super.onDestroyView();
    }

    public void Back(View view)
    {
        viewFlipper.showPrevious();
    }

    private void FindArtist(String artistName)
    {
        artist = LocalDataManager.getInstance(getActivity()).FetchArtistFromLocal(artistName);

        if (artist != null)
        {
            Log.i("LOCAL", "Found local data.");
            artistImage = LocalDataManager.getInstance(getActivity()).FetchBitmapFromLocal(artistName);
            if (artistImage == null)
            {
                FetchRemote(artistName);
            }
            UpdateView();
        }
        else
        {
            Log.i("LOCAL", "Local data not found.");
            FetchRemote(artistName);
        }

    }

    private void FetchRemote(String artistName)
    {
        Log.i("INFO", "Fetching from Remote");
        artistTextView.setText("Fetching...");
        tagTextView.setText("");

        String url = LastFmRest.getInstance().GetArtist(artistName);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TaskComplete(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        VolleyManager.getInstance(getContext()).getRequestQueue().add(stringRequest);
    }

    private void PopulateSimilarArtistsListView()
    {
        similarArtistsList.clear();

        similarArtistsList.addAll(artist.getSimilar());

        similarArtistsArrayAdapter.notifyDataSetChanged();
    }

    private void UpdateView()
    {
        artistImageView.setImageBitmap(artistImage);
        artistTextView.setText(artist.getName());
        tagTextView.setText(artist.getTag());
        similarArtistsList.clear();

        similarArtistsList.addAll(artist.getSimilar());

        Log.i("SA", String.valueOf(similarArtistsList.size()) + " asdsa");

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

    public void TaskComplete(String result) {

        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(result);

            if (jsonObject.has("artist"))
            {
                artist = LastFmJsonParser.getInstance().ParseArtistInfo(jsonObject);

                if (artist != null)
                {
                    PopulateSimilarArtistsListView();
                }

                if (artistImage == null) {

                    ImageRequest ir = new ImageRequest(artist.getImageUrl(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            LocalDataManager.getInstance(getContext()).SaveBitmapToLocal(response, artist.getName());

                            artistImageView.setImageBitmap(artistImage);

                            LocalDataManager.getInstance(getActivity()).SaveBitmapToLocal(artistImage, artist.getName());

                            /*byte[] byteArray = LocalDataManager.getInstance(getActivity()).ConvertBitmapToByteArray(artistImage);

                            Log.i("PARSE", "Saving a copy on parse.");
                            ParseFile imageFile = new ParseFile(java.util.UUID.randomUUID() + ".png", byteArray);
                            ParseObject artistImageObject = new ParseObject("ArtistImages");
                            artistImageObject.put("artist", artist.getName());
                            artistImageObject.put("image", imageFile);
                            artistImageObject.saveInBackground();*/
                        }
                    }, 100, 100, null, null);

                    VolleyManager.getInstance(getContext()).getRequestQueue().add(ir);
                }

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



}
