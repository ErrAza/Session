package com.parse.session;

/**
 * Created by Sean on 8/3/2017.
 */

public class ParseDataManager {

    private static ParseDataManager instance = null;


    protected ParseDataManager() {}

    public static ParseDataManager getInstance()
    {
        if (instance == null)
        {
            instance = new ParseDataManager();
        }

        return instance;
    }
}
