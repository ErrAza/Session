package com.sean.session;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


import java.util.List;

/**
 * Created by Sean on 8/2/2017.
 */

public class ProfileManager {

    private static ProfileManager instance = null;

    public User currentUser;

    protected ProfileManager() {}

    public static ProfileManager getInstance()
    {
        if (instance == null)
        {
            instance = new ProfileManager();
        }

        return instance;
    }

    public boolean UserHasArtistAdded(final Artist artist)
    {
        final boolean[] ret = {false};

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");

        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null)
                {
                    if (objects.size() > 0)
                    {
                        ParseObject object = objects.get(0);
                        ret[0] = object.getList("artists").contains(artist.getName());
                    }
                }
            }
        });

        return ret[0];
    }


}
