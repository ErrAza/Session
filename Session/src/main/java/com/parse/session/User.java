package com.parse.session;

import java.util.ArrayList;

/**
 * Created by Sean on 7/27/2017.
 */

public class User {

    private String _name;
    private String _email;
    private ArrayList<String> _genres;
    private ArrayList<Artist> _artists;

    public String getName()
    {
        return _name;
    }

    public String getEmail()
    {
        return _email;
    }

    public ArrayList<String> getGenres() {
        return _genres;
    }

    public ArrayList<Artist> getArtists() { return _artists; }

    public void setName(String name)
    {
        this._name = name;
    }

    public void setEmail(String email)
    {
        this._email = email;
    }

    public void setGenres(ArrayList<String> _genres) {
        this._genres = _genres;
    }

    public void setArtists(ArrayList<Artist> artists) { this._artists = artists; }


}
