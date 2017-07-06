package com.blogspot.afoxtutorials.filemanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by neerajMalhotra on 04-06-2017.
 */

public class FecthJSON {
    public String StringUrlToJSON(String StringUrl) {
        URL Url = stringToUrl(StringUrl);
        HttpURLConnection UrlCOnnection = urlConnection(Url);
        String JSON = inputstreamToString(UrlCOnnection);
        return JSON;
    }

    public URL stringToUrl(String string) {
        URL url = null;
        try {
            url = new URL(string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public HttpURLConnection urlConnection(URL url) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(1000);
            urlConnection.setConnectTimeout(1500);
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlConnection;

    }

    public String inputstreamToString(HttpURLConnection urlConnection) {
        InputStream inputStream = null;
        String string = "", line;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = urlConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            line = bufferedReader.readLine();
            while (line != null) {
                string += line;
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }

    public boolean acessNetworkState(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}
