package com.sean.session;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.session.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Sean on 8/1/2017.
 */

public class MusicFragment extends Fragment implements View.OnKeyListener {


    ViewFlipper viewFlipper;

    ListView artistsListView;
    List<String> artistList;
    ArrayAdapter artistsArrayAdapter;

    ListView similarArtistsListView;
    List<String> similarArtistsList = new ArrayList<>();
    ArrayAdapter similarArtistsArrayAdapter;

    ListView searchResultsListView;
    List<String> searchResultsList = new ArrayList<>();
    ArrayAdapter searchResultsArrayAdapter;

    Artist artist;
    ImageView artistImageView;
    TextView artistTextView;
    TextView tagTextView;
    Button addArtistButton;
    Button backButton;
    Bitmap artistImage;
    EditText searchText;


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
        searchText = (EditText) getView().findViewById(R.id.artistSearchText);
        searchResultsListView = (ListView) getView().findViewById(R.id.foundArtistsListView);

        similarArtistsArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, similarArtistsList);

        similarArtistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!similarArtistsList.isEmpty())
                {
                    FindArtist(similarArtistsList.get(position));
                }
            }
        });

        similarArtistsListView.setAdapter(similarArtistsArrayAdapter);

        searchResultsArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, searchResultsList);

        searchResultsListView.setAdapter(searchResultsArrayAdapter);

        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!searchResultsList.isEmpty())
                {
                    FindArtist(searchResultsList.get(position));

                    viewFlipper.setDisplayedChild(1);
                }
            }
        });

        searchText.setOnKeyListener(this);

        super.onActivityCreated(savedInstanceState);
    }

    public void DismissKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus()
                        .getWindowToken(), 0);
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
        backButton = null;
        artistImage = null;

        super.onDestroyView();
    }

    public void Back(View view)
    {
        viewFlipper.showPrevious();

        searchText.setText("");
        searchText.clearFocus();
    }

    private void SearchArtist()
    {
        String artistName = searchText.getText().toString();

        String url = LastFmRest.getInstance().SearchArtist(artistName);

        viewFlipper.setDisplayedChild(2);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject object = null;

                        try {
                            object = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String[] results = LastFmJsonParser.getInstance().ParseArtistSearchResults(object);

                        if (results.length > 0)
                        {
                            searchResultsList.clear();
                            searchResultsList.addAll(Arrays.asList(results));
                            searchResultsArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        VolleyManager.getInstance(getContext()).getRequestQueue().add(stringRequest);
    }

    private void FindArtist(String artistName)
    {
        artist = LocalDataManager.getInstance(getActivity()).FetchArtistFromLocal(artistName);

        if (artist != null)
        {
            Log.i("LOCAL", "Retrieving local data.");
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
        Log.i("REMOTE", "Fetching from Remote");
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

        similarArtistsArrayAdapter.notifyDataSetChanged();

        if (ProfileManager.getInstance().UserHasArtistAdded(artist))
        {
            addArtistButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            addArtistButton.setVisibility(View.VISIBLE);
        }

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
                        Log.i("PARSE", "Updating User Artist List.");
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

                    ImageRequest ir = new ImageRequest(artist.getImageUrl(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            artistImage = response;

                            artistImageView.setImageBitmap(artistImage);

                            artist.setImageArrayString(LocalDataManager.getInstance(getActivity()).ConvertBitmapToStringArrayOfBytes(artistImage));

                            LocalDataManager.getInstance(getActivity()).SaveArtistToLocal(artist);

                            LocalDataManager.getInstance(getActivity()).SaveBitmapToLocal(artistImage, artist.getName());

                        }
                    }, 0, 0, null, null);

                    VolleyManager.getInstance(getContext()).getRequestQueue().add(ir);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        UpdateView();

    }




    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_ENTER)
        {
            DismissKeyboard(getActivity());

            SearchArtist();
        }

        return false;
    }
}
