package com.blogspot.afoxtutorials.filemanager;


import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Updater extends AppCompatActivity implements LoaderCallbacks<ArrayList<PackageData>> {
    String mUrl;
    LinearLayout checkingUpdateView, upToDateView;
    RelativeLayout updateAvailableView;
    ListView moreAppsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(new Themer(this).themeRes());
        setContentView(R.layout.activity_updater);
        upToDateView = (LinearLayout) findViewById(R.id.updater_up_to_date);
        moreAppsList = (ListView) findViewById(R.id.updater_listView);
        upToDateView.setVisibility(View.INVISIBLE);
        moreAppsList.setVisibility(View.INVISIBLE);
        checkingUpdateView = (LinearLayout) findViewById(R.id.updater_checking_update_view);
        updateAvailableView = (RelativeLayout) findViewById(R.id.updater_update_available_view);
        updateAvailableView.setVisibility(View.INVISIBLE);
        if (new FecthJSON().acessNetworkState(this)) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(0, null, Updater.this).forceLoad();
        } else {
            checkingUpdateView.setVisibility(View.GONE);
            upToDateView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "No Internet Connection available", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public Loader<ArrayList<PackageData>> onCreateLoader(int id, Bundle args) {
        mUrl = getResources().getString(R.string.json_url);
        return new DataAsyncLoader(this, mUrl);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<PackageData>> loader, ArrayList<PackageData> data) {
        if (data.size() == 0 || data.isEmpty()) {
            return;
        }
        checkingUpdateView.setVisibility(View.GONE);
        int currrentPackage = 0;
        boolean updateAvailable = false;
        String appName = getResources().getString(R.string.app_name);
        Double appVersion = Double.parseDouble(getResources().getString(R.string.app_version));
        for (int i = 0; i < data.size(); i++) {
            String NappName = data.get(i).getmPackageName();
            Double NappVersion = data.get(i).getmVersion();
            if (NappName.equals(appName) && NappVersion > appVersion) {
                currrentPackage = i;
                updateAvailable = true;
                break;
            }
        }
        if (updateAvailable) {
            upToDateView.setVisibility(View.GONE);
            moreAppsList.setVisibility(View.GONE);
            updateAvailableView.setVisibility(View.VISIBLE);
            TextView newAppVersion = (TextView) findViewById(R.id.updater_update_available_version);
            TextView whatsNew = (TextView) findViewById(R.id.updater_update_available_whatsnew);
            newAppVersion.setText(data.get(currrentPackage).getmVersion().toString());
            whatsNew.setText(data.get(currrentPackage).getmWhatsnew());
            final String updateURL = data.get(currrentPackage).getmLink();
            Button updateButton = (Button) findViewById(R.id.updater_update_button);
            updateButton.setBackgroundColor(new Themer(this).fetchDarkerBackgroundColor());
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent updateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateURL));
                    startActivity(updateIntent);
                }
            });
            return;
        }
        updateAvailableView.setVisibility(View.GONE);
        upToDateView.setVisibility(View.VISIBLE);
        moreAppsList.setVisibility(View.VISIBLE);
        ArrayList<String> packageNameList = new ArrayList();
        final ArrayList<String> packageLinkList = new ArrayList();
        for (int i = 0; i < data.size(); i++) {
            packageNameList.add(data.get(i).getmPackageName());
            packageLinkList.add(data.get(i).getmLink());
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, packageNameList);
        moreAppsList.setAdapter(arrayAdapter);
        moreAppsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uri = packageLinkList.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<PackageData>> loader) {

    }
}
