package com.blogspot.afoxtutorials.filemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TextEditor extends AppCompatActivity {
    EditText mEditView;
    boolean rootEnabled, canWrite = true;
    File file, tmp;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        DrawableCompat.setTint(menu.findItem(R.id.editor_menu_save).getIcon(), new Themer(this).fetchColorActionBarWidget());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.editor_menu_save:
                saveDialog();
                return true;
            case R.id.editor_menu_delete:
                boolean b = new OperationHandler(rootEnabled).delete(file);
                if (b) Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "Can't delete", Toast.LENGTH_SHORT).show();
                finish();
                return true;
        }
        return false;
    }

    private void saveDialog() {
        new AlertDialog.Builder(this).setTitle("Save")
                .setMessage("Save this is file ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            fileWriter(file, mEditView.getText().toString());
                            if (!canWrite) {
                                new OperationHandler(rootEnabled).exec("mv -f " + file + " " + tmp);
                            }
                        } catch (IOException e) {
                            Log.e("TextEditor:", "71 IOExcep " + e);
                            Toast.makeText(TextEditor.this, "can't edit this file", Toast.LENGTH_SHORT).show();
                        } finally {
                            finish();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        saveDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeID = new Themer(this).themeRes();
        setTheme(themeID);
        setContentView(R.layout.activity_text_editor);
        mEditView = (EditText) findViewById(R.id.editor_edit_text);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        tmp = new File(uri.getPath());
        if (!tmp.canWrite()) {
            file = new File("/sdcard/android", tmp.getName());
            new OperationHandler(rootEnabled).exec("cp -f " + tmp + " " + file);
            canWrite = false;
        } else {
            file = tmp;
        }
        setTitle(file.getName());
        String textData = "";
        try {
            textData = fileReader(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mEditView.setText(textData);
        SharedPreferences rootSP = PreferenceManager.getDefaultSharedPreferences(this);
        rootEnabled = rootSP.getBoolean(getString(R.string.key_root), false);

    }

    private String fileReader(File file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String tmp = br.readLine();
        String textData = "";
        while (tmp != null) {
            textData += tmp + System.getProperty("line.separator");
            tmp = br.readLine();
        }
        br.close();
        return textData;
    }

    private void fileWriter(File file, String data) throws IOException {
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(data);
        bw.close();
    }
}
