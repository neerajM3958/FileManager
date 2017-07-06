package com.blogspot.afoxtutorials.filemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


public class AboutApp extends AppCompatActivity {
    int i = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeID = new Themer(this).themeRes();
        setTheme(themeID);
        setContentView(R.layout.activity_about_app);
        ImageView gp = (ImageView) findViewById(R.id.gplus);
        gp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/107000791397343207223"));
                startActivity(gpIntent);
            }
        });
        ImageView fb = (ImageView) findViewById(R.id.fb);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/neeraj.malhotra.3958"));
                startActivity(fbIntent);
            }
        });
    }


    public void bugreport(View view) {
        Intent bugreport = new Intent(Intent.ACTION_SENDTO);
        bugreport.setData(Uri.parse("mailto:, neerajmalhotra3958@gmail.com"));
        bugreport.putExtra(Intent.EXTRA_TEXT, "#bug_report\npackage:" + getPackageName() + "\nversion: " + getResources().getString(R.string.app_version));
        bugreport.putExtra(Intent.EXTRA_SUBJECT, "bug report");
        if (bugreport.resolveActivity(getPackageManager()) != null) {
            startActivity(bugreport);
            finish();
        } else {
            Toast.makeText(this, "You don't have any email app :-(", Toast.LENGTH_SHORT).show();
        }
    }

    public void foxyIntent(View view) {
        if (i > 0) {
            i--;
        }
        if (i == 0) {
            Intent foxyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.afoxtutorials.blogspot.com"));
            startActivity(foxyIntent);
            i = 3;
        } else
            Toast.makeText(this, "Press " + i + " times more for visit blog", Toast.LENGTH_SHORT).show();
    }
}

