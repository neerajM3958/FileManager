package com.blogspot.afoxtutorials.filemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;


/**
 * Created by neerajMalhotra on 30-05-2017.
 */

public class Themer {
    SharedPreferences sharedPreferences;
    String currentTheme;
    private Context mContext;

    public Themer(Context context) {
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        currentTheme = sharedPreferences.getString("theme_list_prefrence", "");
    }

    public Themer() {
    }

    public int themeRes() {
        String[] allThemes;
        allThemes = mContext.getResources().getStringArray(R.array.theme_array_key);
        int resId = 0;
        if (currentTheme.equals(allThemes[1])) {
            resId = R.style.AppTheme2;
        } else if (currentTheme.equals(allThemes[2])) {
            resId = R.style.AppTheme3;
        } else if (currentTheme.equals(allThemes[3])) {
            resId = R.style.AppTheme4;
        } else if (currentTheme.equals(allThemes[4])) {
            resId = R.style.AppTheme5;
        } else if (currentTheme.equals(allThemes[5])) {
            resId = R.style.AppTheme6;
        } else if (currentTheme.equals(allThemes[0])) {
            resId = R.style.AppTheme;
        } else if (currentTheme.equals(allThemes[7])) {
            resId = R.style.AppTheme8;
        } else {
            resId = R.style.AppTheme7;
        }
        return resId;
    }

    public int fetchBackgroundColor() {
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        int color = typedValue.data;
        return color;
    }

    public int fetchDarkerBackgroundColor() {
        int color = fetchBackgroundColor();
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f; // value component
        color = Color.HSVToColor(hsv);
        return color;
    }

    public int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;
        return color;
    }

    public int fetchAccentDarkerColor() {
        int color = fetchAccentColor();
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.94f; // value component
        color = Color.HSVToColor(hsv);
        return color;
    }

    public int fetchPrimaryColor() {
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;
        return color;
    }

    public int fetchPrimaryDarkColor() {
        TypedValue typedValue = new TypedValue();

        mContext.getTheme().resolveAttribute(android.R.attr.colorPrimaryDark, typedValue, true);
        int color = typedValue.data;
        return color;
    }

    public int fetchColorActionBarWidget() {
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data, newColor = mContext.getResources().getColor(android.R.color.white);
        if (color == ContextCompat.getColor(mContext, R.color.Nmaterial_grey_300)) {
            newColor = ContextCompat.getColor(mContext, R.color.Black);
        }
        return newColor;
    }

    public int fetchColorListItemBackground() {
        if (currentTheme.indexOf("Dark") != -1 || currentTheme.indexOf("Black") != -1) {
            return ContextCompat.getColor(mContext, R.color.Nmaterial_grey_800);
        }
        return ContextCompat.getColor(mContext, R.color.White);
    }
}
