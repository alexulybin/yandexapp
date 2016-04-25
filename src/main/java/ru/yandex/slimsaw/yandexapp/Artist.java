package ru.yandex.slimsaw.yandexapp;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by slimsaw on 19.04.2016.
 */
public class Artist {
    private String id;
    private String name;
    private Integer tracks;
    private Integer albums;
    private String link;
    private String description;
    private String smallCover;
    private String bigCover;

    public ArrayList<String> getGenres() {
        return genres;
    }

    public String getGenresStr() {
        StringBuffer buf = new StringBuffer();
        for(String genre : genres) {
            buf.append(genre);
            buf.append(", ");
        }
        // удаляем последнюю запятую
        if(buf.length() > 1)
            return buf.substring(0, buf.length() - 2);

        return null;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    private ArrayList<String> genres;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTracks() {
        return tracks;
    }

    public String getTracksStr() {
        StringBuffer sb = new StringBuffer();
        if (getTracks() > 0) {
            sb.append(getTracks());
            sb.append(" ");
            sb.append(pluralForm(getTracks(), "песня", "песни", "песен"));
        }
        return sb.toString();
    }

    public void setTracks(Integer tracks) {
        this.tracks = tracks;
    }

    public Integer getAlbums() {
        return albums;
    }

    public String getAlbumsStr() {
        StringBuffer sb = new StringBuffer();
        if (getAlbums() > 0) {
            sb.append(getAlbums());
            sb.append(" ");
            sb.append(pluralForm(getAlbums(), "альбом", "альбома", "альбомов"));
        }
        return sb.toString();
    }

    public void setAlbums(Integer albums) {
        this.albums = albums;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSmallCover() {
        return smallCover;
    }

    public void setSmallCover(String smallCover) {
        this.smallCover = smallCover;
    }

    public String getBigCover() { return bigCover; }

    public void setBigCover(String bigCover) {
        this.bigCover = bigCover;
    }

    public String toString() {
        return name;
    }

    /** Вычисляет правильное склонение существительных с числительными */
    private String pluralForm(Integer number, String form1, String form2, String form3){
        Long n = Math.abs(number.longValue()) % 100;
        Long n1 = n % 10;
        if (n > 10 && n < 20) return form3;
        if (n1 > 1 && n1 < 5) return form2;
        if (n1 == 1) return form1;
        return form3;
    }
}
