package com.blogspot.afoxtutorials.filemanager;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by neerajMalhotra on 04-06-2017.
 */

public class DataAsyncLoader extends AsyncTaskLoader<ArrayList<PackageData>> {
    String mUrl;
    Context mContext;

    public DataAsyncLoader(Context context, String Url) {
        super(context);
        mUrl = Url;
        mContext = context;
    }

    @Override
    public ArrayList<PackageData> loadInBackground() {
        String JSON = new FecthJSON().StringUrlToJSON(mUrl);
        return extractDataFromJSON(JSON);
    }

    private ArrayList<PackageData> extractDataFromJSON(String JSON) {
        if (JSON.isEmpty()) {
            return null;
        }
        ArrayList<PackageData> list = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(JSON);
            JSONArray pack = root.getJSONArray("Packages");
            int noOfPackages = pack.length();
            for (int i = 0; i < noOfPackages; i++) {
                JSONObject packages = pack.getJSONObject(i);
                String name = packages.getString("name");
                Double version = packages.getDouble("version");
                String link = packages.getString("link");
                String thumb = packages.getString("thumb");
                String whatsnew = packages.getString("new");
                String message = packages.getString("message");
                list.add(new PackageData(name, version, link, thumb, whatsnew, message));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
