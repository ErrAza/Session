package com.parse.session;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ParseUser currentParseUser;

    User currentUser;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        textView  = (TextView) findViewById(R.id.textView);

        currentParseUser = ParseUser.getCurrentUser();

        this.setTitle(currentParseUser.getUsername());

        CreateUserInfo();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.action_home:
                        textView.setText("Home");
                        break;
                    case R.id.action_music:
                        textView.setText("Music");
                        break;
                    case R.id.action_notifications:
                        textView.setText("Notifications");
                        break;
                }

                return true;
            }
        });

        textView.setText("Home");

    }

    private void CreateUserInfo()
    {

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

        currentUser = new User();
        currentUser.setName(currentParseUser.getUsername());
    }

}
