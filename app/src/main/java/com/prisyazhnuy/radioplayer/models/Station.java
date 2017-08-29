package com.prisyazhnuy.radioplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public class Station implements Parcelable {
    private long id;
    private String name;
    private String subname;
    private String url;
    private boolean isFavourite;
    private int position;

    public Station(long id, String name, String subname, String url, int position, boolean isFavourite) {
        this.id = id;
        this.name = name;
        this.subname = subname;
        this.url = url;
        this.position = position;
        this.isFavourite = isFavourite;
    }

    protected Station(Parcel in) {
        id = in.readLong();
        name = in.readString();
        subname = in.readString();
        url = in.readString();
        isFavourite = in.readByte() != 0;
        position = in.readInt();
    }

    public static final Creator<Station> CREATOR = new Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(subname);
        dest.writeString(url);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
        dest.writeInt(position);
    }
}
