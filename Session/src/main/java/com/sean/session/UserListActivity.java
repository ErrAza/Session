package com.sean.session;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.session.R;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    ArrayList<String> users = new ArrayList<>();

    ArrayAdapter arrayAdapter;

    ParseUser currentUser;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = new MenuInflater(this);

        menuInflater.inflate(R.menu.tweet_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.tweet)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Send a tweet");

            final EditText tweetContentEditText = new EditText(this);

            builder.setView(tweetContentEditText);

            builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ParseObject tweet = new ParseObject("Tweet");

                    tweet.put("username", currentUser.getUsername());
                    tweet.put("tweet", tweetContentEditText.getText().toString());

                    tweet.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                            {
                                Toast.makeText(UserListActivity.this, "Tweet Sent!", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(UserListActivity.this, "Tweet Failed! Please Try Again Later.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        else if (item.getItemId() == R.id.logout)
        {
            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.viewFeed)
        {
            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        setTitle("Following List");

        currentUser = ParseUser.getCurrentUser();

        if (currentUser.get("isFollowing") == null)
        {
            ArrayList<String> emptyList = new ArrayList<>();
            currentUser.put("isFollowing", emptyList);
        }

        final ListView listView = (ListView) findViewById(R.id.userListView);

        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, users);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckedTextView checkedTextView = (CheckedTextView) view;

                if (checkedTextView.isChecked())
                {
                    currentUser.getList("isFollowing").add(users.get(position));
                    List newList = currentUser.getList("isFollowing");
                    currentUser.remove("isFollowing");
                    currentUser.put("isFollowing", newList);
                    currentUser.saveInBackground();
                }
                else
                {
                    currentUser.getList("isFollowing").remove(users.get(position));
                    List newList = currentUser.getList("isFollowing");
                    currentUser.remove("isFollowing");
                    currentUser.put("isFollowing", newList);
                    currentUser.saveInBackground();
                }

            }
        });

        users.clear();

        ParseQuery<ParseUser> query = ParseUser.getQuery();

        query.whereNotEqualTo("username", currentUser.getUsername().toString());

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null)
                {
                    if (objects.size() > 0)
                    {
                        for (ParseUser user : objects)
                        {
                            users.add(user.getUsername());
                        }
                    }

                    arrayAdapter.notifyDataSetChanged();

                    for (String username : users)
                    {
                        if (currentUser.getList("isFollowing").contains(username))
                        {
                            listView.setItemChecked(users.indexOf(username), true);
                        }
                    }

                }
            }
        });
    }
}
