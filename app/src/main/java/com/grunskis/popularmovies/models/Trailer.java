package com.grunskis.popularmovies.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Trailer implements Parcelable {

    private final String mName;
    private final String mKey;

    public Trailer(String name, String key) {
        mName = name;
        mKey = key;
    }

    protected Trailer(Parcel in) {
        mName = in.readString();
        mKey = in.readString();
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mKey);
    }

    public Uri getYoutubeUri() {
        String YOUTUBE_VIDEO_URL_BASE = "https://www.youtube.com/watch";

        return Uri.parse(YOUTUBE_VIDEO_URL_BASE)
                .buildUpon()
                .appendQueryParameter("v", mKey)
                .build();
    }

    public String getName() {
        return mName;
    }
}
