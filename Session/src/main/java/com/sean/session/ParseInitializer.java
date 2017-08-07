/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.sean.session;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;


public class ParseInitializer extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
            .applicationId("fd4514b64ef4389ab48dc161ebde7a5ba4666923")
            .clientKey("397663c699ff56bfbc13f7dc046ed0101157a62b")
            .server("http://ec2-35-176-11-160.eu-west-2.compute.amazonaws.com:80/parse/")
            .build()
    );


    //ParseUser.enableAutomaticUser();

    ParseACL defaultACL = new ParseACL();
    defaultACL.setPublicReadAccess(true);
    defaultACL.setPublicWriteAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

  }
}
