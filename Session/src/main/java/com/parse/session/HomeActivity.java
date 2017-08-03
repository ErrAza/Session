package com.parse.session;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements AsyncResponse {

    ParseUser currentParseUser;

    User currentUser;

    LocalDataManager localDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, HomeFragment.newInstance());
        transaction.commit();

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

                        PopulateUserProfile();
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
                DownloadTask task = new DownloadTask();
                String url = LastFmRest.getInstance().GetArtist(currentUser.getArtistList().get(i));
                task.delegate = this;
                task.execute(url);
                Log.i("INFO", "Local Artist Data Not Found for " + currentUser.getArtistList().get(i));
            }
            else
            {
                Artist artist = LocalDataManager.getInstance(this).FetchArtistFromLocal(currentUser.getArtistList().get(i));

                ProfileManager.getInstance().currentUser.getArtists().add(artist);
            }
        }
    }

    @Override
    public void TaskComplete(String result) {

        Log.i("RESULT", "Result Found.");

        JSONObject jsonObject;

        Artist artist;

        try {
            jsonObject = new JSONObject(result);

            if (jsonObject.has("artist"))
            {
                artist = LastFmJsonParser.getInstance().ParseArtistInfo(jsonObject);

                LocalDataManager.getInstance(this).SaveArtistToLocal(artist);

                Bitmap bitmap = LocalDataManager.getInstance(this).FetchBitmapFromLocal(artist.getName());

                if (bitmap == null)
                {
                    ImageDownloader imageDownloader = new ImageDownloader();

                    try {
                        bitmap = imageDownloader.execute(artist.getImageUrl()).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (bitmap != null)
                {
                    LocalDataManager.getInstance(this).SaveBitmapToLocal(bitmap, artist.getName());
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
}



























