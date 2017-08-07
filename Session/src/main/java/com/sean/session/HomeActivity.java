package com.sean.session;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ParseUser currentParseUser;

    User currentUser;

    LocalDataManager localDataManager;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        queue = VolleyManager.getInstance(this.getApplicationContext()).getRequestQueue();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        currentParseUser = ParseUser.getCurrentUser();

        CreateUserInfo();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch(item.getItemId())
                {
                    case R.id.action_home:
                        selectedFragment = HomeFragment.newInstance();
                        break;
                    case R.id.action_music:
                        selectedFragment = MusicFragment.newInstance();
                        break;
                    case R.id.action_notifications:
                        selectedFragment = NotificationsFragment.newInstance();
                        break;
                    case R.id.action_profile:
                        selectedFragment = ProfileFragment.newInstance();
                        break;
                }

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
                return true;
            }
        });



    }

    private void CreateUserInfo()
    {
        localDataManager = LocalDataManager.getInstance(this);

        currentUser = new User();

        currentUser.setName(currentParseUser.getUsername());

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");

        query.whereEqualTo("username", currentParseUser.getUsername());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null)
                {
                    if (objects.size() > 0)
                    {
                        Log.i("INFO", "exists");
                        ParseObject object = objects.get(0);
                        object.put("username", currentParseUser.getUsername());
                        List<String> tempList = object.getList("artists");
                        currentUser.setArtistList(tempList);
                        tempList = object.getList("genres");
                        currentUser.setGenres(tempList);
                        object.saveInBackground();

                        ProfileManager.getInstance().currentUser = currentUser;

                        new MyAsyncTask().execute();

                        //PopulateUserProfile();
                    }
                    else
                    {
                        Log.i("INFO", "doesn't exist");
                        ParseObject userInfo = new ParseObject("UserInfo");
                        userInfo.put("username", currentParseUser.getUsername());
                        userInfo.saveInBackground();
                    }
                }
            }
        });


    }

    private void PopulateUserProfile()
    {
        for(int i = 0; i < currentUser.getArtistList().size(); i++)
        {
            if (!LocalDataManager.sharedPreferences.contains(currentUser.getArtistList().get(i)))
            {
                String url = LastFmRest.getInstance().GetArtist(currentUser.getArtistList().get(i));

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

                queue.add(stringRequest);
            }
            else
            {
                Artist artist = LocalDataManager.getInstance(this).FetchArtistFromLocal(currentUser.getArtistList().get(i));

                ProfileManager.getInstance().currentUser.getArtists().add(artist);
            }
        }
    }

    public void TaskComplete(String result) {

        Log.i("REMOTE", "Result Found.");

        JSONObject jsonObject;

        final Artist artist;

        try {
            jsonObject = new JSONObject(result);

            if (jsonObject.has("artist"))
            {
                artist = LastFmJsonParser.getInstance().ParseArtistInfo(jsonObject);

                LocalDataManager.getInstance(this).SaveArtistToLocal(artist);

                final Bitmap bitmap = LocalDataManager.getInstance(this).FetchBitmapFromLocal(artist.getName());

                if (bitmap == null)
                {
                    ImageRequest ir = new ImageRequest(artist.getImageUrl(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            LocalDataManager.getInstance(getApplicationContext()).SaveBitmapToLocal(response, artist.getName());
                        }
                    }, 100, 100, null, null);

                    queue.add(ir);
                }

                if (!currentUser.getArtists().contains(artist))
                {
                    ProfileManager.getInstance().currentUser.getArtists().add(artist);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            PopulateUserProfile();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, HomeFragment.newInstance());
            transaction.commit();
        }
    }
}



























