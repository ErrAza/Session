package com.sean.session;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sean on 7/27/2017.
 */

public class User {

    public boolean IsReady = false;

    private String _name;
    private String _email;
    private List<String> _genres;
    private ArrayList<Artist> _artists;
    private List<String> _artistList;

    public User()
    {
        _artists = new ArrayList<>();
    }

    public String getName()
    {
        return _name;
    }

    public String getEmail()
    {
        return _email;
    }

    public List<String> getGenres() {
        return _genres;
    }

    public ArrayList<Artist> getArtists() { return _artists; }

    public List<String> getArtistList() {
        return _artistList;
    }

    public void setName(String name)
    {
        this._name = name;
    }

    public void setEmail(String email)
    {
        this._email = email;
    }

    public void setGenres(List<String> genres) {
        this._genres = genres;
    }

    public void setArtists(ArrayList<Artist> artists) { this._artists = artists; }

    public void setArtistList(List<String> artistList) {
        this._artistList = artistList;
    }
}
