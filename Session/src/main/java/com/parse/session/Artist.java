package com.parse.session;

import java.util.ArrayList;

/**
 * Created by Sean on 7/27/2017.
 */

public class Artist {

    private String _name;
    private String _imageUrl;
    private String _tag;
    private ArrayList<Artist> _similar;

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public ArrayList<Artist> getSimilar()
    {
        return _similar;
    }

    public void setSimilar(ArrayList<Artist> similar)
    {
        _similar = similar;
    }

    public String getImageUrl() { return _imageUrl; }

    public void setImageUrl(String url) { _imageUrl = url; }

    public String getTag(){ return _tag; }

    public void setTag(String tag) { _tag = tag; }

}
