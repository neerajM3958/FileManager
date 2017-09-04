package com.blogspot.afoxtutorials.filemanager;

import android.support.annotation.NonNull;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created by neerajMalhotra on 15-06-2017.
 */

public class DataGetSetter implements Comparable<DataGetSetter>{
    @Override
    public int compareTo(@NonNull DataGetSetter o) {
        return this.mFileName.compareTo(o.getmFileName());
    }

    int mFileType;
    long mFileSize;
    boolean mDirectory = false;
    private File mFile;
    private String mFileName, mLastModified, mFilePath;
    private boolean mSelected = false;

    public DataGetSetter(File file, String name, boolean rootEnabled) {
        this.mFile = file;
        mFileName = name;
        setUp(rootEnabled);

    }

    public DataGetSetter(File file) {
        mFile = file;
        mFileName = file.getName();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/LL/yy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        String dateToDisplay = dateFormatter.format(file.lastModified());
        String timeToDisplay = timeFormatter.format(file.lastModified());
        mLastModified = dateToDisplay + " " + timeToDisplay;
        setFileType(file.isDirectory());
        mFileSize = file.length();
        mFilePath = file.getPath();
        mDirectory = file.isDirectory();

    }

    public DataGetSetter(String nameString, String timeString, long size, String premString, String linkString, boolean dir) {
        mFile = new File(linkString, nameString);
        mFileName = nameString;
        mLastModified = timeString;
        setFileType(dir);
        mDirectory = dir;
        mFileSize = size;
        mFilePath = linkString;
    }

    public File getmFile() {
        return mFile;
    }

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String newFileName) {
        mFileName = newFileName;
    }

    public boolean getIsDirecotory() {
        return mDirectory;
    }

    public String getmLastModified() {
        return mLastModified;
    }

    public String getmFileSize() {
        String token = "KB";
        float f = (float) mFileSize / 1024;
        if (f > 1024) {
            f = f / 1024;
            token = "MB";
        } else if ((f / 1024) > 1024) {
            f = f / (1024 * 1024);
            token = "GB";
        }
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(f) + " " + token;
    }

    public int getmFileType() {
        return mFileType;
    }

    public void setUp(boolean rootEnabled) {
        if (rootEnabled) {
            mLastModified = "Parent Folder";
        } else {
            mLastModified = "Root disabled";
        }
        mFileType = R.drawable.ic_arrow_back_24dp;
        mDirectory = true;
    }

    public void setSelected(boolean b) {
        mSelected = b;
    }

    public boolean ismSelected() {
        return mSelected;
    }

    private void setFileType(boolean dir) {
        if (dir) {
            mFileType = R.drawable.ic_folder_24dp;
        } else if (mFileName.endsWith(".mp3") || mFileName.endsWith(".amr") || mFileName.endsWith(".wav") || mFileName.endsWith(".ogg") || mFileName.endsWith(".acc") || mFileName.endsWith(".mid")) {
            mFileType = R.drawable.ic_music_note_24dp;
        } else if (mFileName.endsWith(".mp4") || mFileName.endsWith(".3gp") || mFileName.endsWith(".avi") || mFileName.endsWith(".mkv") || mFileName.endsWith(".m4a")) {
            mFileType = R.drawable.ic_video_24dp;
        } else if (mFileName.endsWith(".apk")) {
            mFileType = R.drawable.ic_android_24dp;
        } else if (mFileName.endsWith(".jpg") || mFileName.endsWith(".jpeg") || mFileName.endsWith(".png") || mFileName.endsWith(".gif")) {
            mFileType = R.drawable.ic_image_24dp;
        } else if (mFileName.endsWith(".txt") || mFileName.endsWith(".c") || mFileName.endsWith(".cpp") || mFileName.endsWith(".java") || mFileName.endsWith(".py") || mFileName.endsWith(".rc") || mFileName.endsWith(".prop")) {
            mFileType = R.drawable.ic_text_24dp;
        } else mFileType = R.drawable.ic_note_24dp;
    }
}
