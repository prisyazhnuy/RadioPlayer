package com.prisyazhnuy.radioplayer.models;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public class Station {
    private long id;
    private String name;
    private String url;
    private boolean isFavourite;
    private int position;

    public Station(long id, String name, String url, int position, boolean isFavourite) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.position = position;
        this.isFavourite = isFavourite;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
