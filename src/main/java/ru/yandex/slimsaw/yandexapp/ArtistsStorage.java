package ru.yandex.slimsaw.yandexapp;

import java.util.ArrayList;

/** Хранилище исполнителей */
public class ArtistsStorage {
    private static ArtistsStorage instance;
    private ArrayList<Artist> artistList;

    private ArtistsStorage() {
        artistList = new ArrayList<Artist>();
    }

    public static ArtistsStorage getInstance() {
        if(instance == null)
            instance = new ArtistsStorage();
        return instance;
    }

    public Artist getArtist(String id) {
        for(Artist artist : artistList) {
            if(artist.getId().equals(id))
                return artist;
        }
        return null;
    }

    public void setArtistList(ArrayList<Artist> list) {
        artistList = list;
    }

    public ArrayList<Artist> getArtistList() {
        return artistList;
    }

    public int getArtistCount() {
        return artistList.size();
    }
}