package com.blogspot.afoxtutorials.filemanager;

/**
 * Created by neerajMalhotra on 04-06-2017.
 * used by for Updater process by updater class
 */

public class PackageData {
    private String mPackageName, mLink, mThumb, mWhatsnew, mMessage;
    private Double mVersion;

    public PackageData(String mPackageName, Double mVersion, String mLink, String mThumb, String mWhatsnew, String mMessage) {
        this.mPackageName = mPackageName;
        this.mLink = mLink;
        this.mThumb = mThumb;
        this.mWhatsnew = mWhatsnew;
        this.mMessage = mMessage;
        this.mVersion = mVersion;
    }

    public String getmPackageName() {
        return mPackageName;
    }

    public String getmLink() {
        return mLink;
    }

    public String getmThumb() {
        return mThumb;
    }

    public String getmWhatsnew() {
        return mWhatsnew;
    }

    public String getmMessage() {
        return mMessage;
    }

    public Double getmVersion() {
        return mVersion;
    }

}

