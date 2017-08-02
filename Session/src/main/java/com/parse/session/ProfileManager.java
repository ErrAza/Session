package com.parse.session;

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

}
