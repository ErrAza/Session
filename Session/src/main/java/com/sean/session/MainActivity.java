/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.sean.session;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.parse.session.R;


public class MainActivity extends AppCompatActivity {

    public void RedirectUser()
    {
        if (ParseUser.getCurrentUser() != null)
        {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
    }

    // Using a quick and dirty dual login/signup system.
    public void SignUpLogin(View view)
    {
        final EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        final EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null)
                {
                    Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_SHORT).show();

                    RedirectUser();
                }
                else
                {
                    ParseUser parseUser = new ParseUser();

                    parseUser.setUsername(usernameEditText.getText().toString());
                    parseUser.setPassword(passwordEditText.getText().toString());

                    parseUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                            {
                                Toast.makeText(MainActivity.this, "Signed Up", Toast.LENGTH_SHORT).show();

                                RedirectUser();
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, e.getMessage().substring(e.getMessage().indexOf(" ")), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RedirectUser();

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}