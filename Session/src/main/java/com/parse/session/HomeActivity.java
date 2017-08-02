package com.parse.session;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ParseUser currentParseUser;

    User currentUser;

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
        currentUser = new User();

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

        currentUser.setName(currentParseUser.getUsername());

        ProfileManager.getInstance().currentUser = currentUser;
    }

    private void PopulateUserProfile()
    {

    }

}
