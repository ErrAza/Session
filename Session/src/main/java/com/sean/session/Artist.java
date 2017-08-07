package com.sean.session;

import java.util.List;

/**
 * Created by Sean on 7/27/2017.
 */

public class Artist {

    private String _name;
    private String _imageUrl;
    private String _tag;
    private List<String> _similar;
    private String _imageArrayString;

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public List<String> getSimilar()
    {
        return _similar;
    }

    public void setSimilar(List<String> similar)
    {
        _similar = similar;
    }

    public String getImageUrl() { return _imageUrl; }

    public void setImageUrl(String url) { _imageUrl = url; }

    public String getTag(){ return _tag; }

    public void setTag(String tag) { _tag = tag; }

    public String getImageArrayString() {
        return _imageArrayString;
    }

    public void setImageArrayString(String _imageArrayString) {
        this._imageArrayString = _imageArrayString;
    }
}
