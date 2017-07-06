package com.blogspot.afoxtutorials.filemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;


public class RootActivity extends AppCompatActivity implements CustomRecyclerView.ItemClickCallback, View.OnClickListener, AsyncResponse {
    private static CustomRecyclerView adapter;
    static private File mCurrentDir;
    final int CONTEXT_MENU_New_File = 1;
    final int CONTEXT_MENU_New_Directory = 2;
    final int CONTEXT_MENU_FILE_ALREADY_EXIST = -1;
    final int CONTEXT_MENU_FILE_CREATED = 1;
    final int CONTEXT_MENU_FILE_CANT_CREATE = 0;
    private final int CUT = 0, COPY = 1;
    String sdCardPath;
    SharedPreferences hiddensharedPreference, rootsharedPreference;
    boolean showHiddenFiles, rootenabled;
    int scrollPosition;
    List<File> mClipBoard;
    String defaultPath;
    String[] availablePath;
    OperationHandler mOPH;
    private RecyclerView recyclerView;
    private ArrayList<DataGetSetter> arrayList;
    private FloatingActionButton FabCancel, FabCopy, FabCut, FabDelete;
    private int operation = -1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                Display(mCurrentDir);
                if (operation == -1) {
                    mClipBoard = new ArrayList<>();
                }
                updateUI();
                break;
            case R.id.menu_item_settings:
                Intent SettingIntent = new Intent(RootActivity.this, Settings.class);
                startActivity(SettingIntent);
                finish();
                break;
            case R.id.menu_item_about_app:
                Intent aboutIntent = new Intent(RootActivity.this, AboutApp.class);
                startActivity(aboutIntent);
                break;
            case R.id.menu_item_update_app:
                Intent updateIntent = new Intent(RootActivity.this, Updater.class);
                startActivity(updateIntent);
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeID = new Themer(this).themeRes();
        setTheme(themeID);
        setContentView(R.layout.activity_root);
        mOPH = new OperationHandler(rootenabled);
        hiddensharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        rootsharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        sdCardPath = Environment.getExternalStorageDirectory().toString();
        FabCancel = (FloatingActionButton) findViewById(R.id.root_view_fab_cancel);
        FabCut = (FloatingActionButton) findViewById(R.id.root_view_fab_cut);
        FabCopy = (FloatingActionButton) findViewById(R.id.root_view_fab_copy);
        FabDelete = (FloatingActionButton) findViewById(R.id.root_view_fab_delete);
        recyclerView = (FastScrollRecyclerView) findViewById(R.id.recycler_view);
        arrayList = new ArrayList<>();
        adapter = new CustomRecyclerView(this, arrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.invalidate();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(adapter);
        adapter.setItemClickCallback(this);
        FabCancel.setOnClickListener(this);
        FabCopy.setOnClickListener(this);
        FabCut.setOnClickListener(this);
        FabDelete.setOnClickListener(this);
        FabCopy.setVisibility(View.GONE);
        FabCut.setVisibility(View.GONE);
        FabDelete.setVisibility(View.GONE);
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(this);
        String storageString = storage.getString(getString(R.string.key_storage), getString(R.string.value_storage));

        if (storageString.equals("External") && Environment.getExternalStorageState().equals("mounted") && new File("/storage", "sdcard1").list() != null) {
            defaultPath = Environment.getExternalStorageDirectory().toString();
        } else if (storageString.equals("sdcard0") && new File("/storage", "sdcard0").list() != null) {
            defaultPath = "/storage/sdcard0";
        } else if (storageString.equals("sdcard1") && new File("/storage", "sdcard1").list() != null) {
            defaultPath = "/storage/sdcard1";
        } else defaultPath = "/sdcard";
        Display(new File(defaultPath));
        availablePath = getStorageList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showHiddenFiles = hiddensharedPreference.getBoolean(getResources().getString(R.string.key_hidden), true);
        rootenabled = rootsharedPreference.getBoolean(getResources().getString(R.string.key_root), false);
        getLastPosition();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setLastPosition();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentDir.getParent() != null) {
            if (mCurrentDir.getParentFile().canWrite() || rootenabled)
                Display(mCurrentDir.getParentFile());
            else exitDialog();
        } else exitDialog();
    }

    public void exitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Exit");
        dialog.setMessage("Do you want to exit?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();

            }
        });
        dialog.setNegativeButton("No", null);
        dialog.show();
    }

    private void Display(File file) {
        String name = file.getName();
        String mimeType = mOPH.getMimeType(file);
        mimeType = mimeType == null ? "" : mimeType;
        if (file.isDirectory() && file.canRead()) {
            arrayList.clear();
            File[] f = mOPH.sorter(file.listFiles());
            if (file.getParent() != null) {
                arrayList.add(new DataGetSetter(new File(file.getParent()), name, rootenabled));
            }
            ArrayList<DataGetSetter> arrayList1 = new ArrayList<>();
            ArrayList<DataGetSetter> arrayList2 = new ArrayList<>();
            ArrayList<DataGetSetter> arrayList3 = new ArrayList<>();
            for (File x : f) {
                if (x.getName().startsWith(".")) arrayList1.add(new DataGetSetter(x));
                else if (x.isDirectory()) arrayList2.add(new DataGetSetter(x));
                else arrayList3.add(new DataGetSetter(x));
            }
            if (showHiddenFiles) arrayList.addAll(arrayList1);
            arrayList.addAll(arrayList2);
            arrayList.addAll(arrayList3);
            adapter.notifyDataSetChanged();
            mCurrentDir = file;
        } else if (file.isDirectory() && !file.canRead()) {
            arrayList.clear();
            List<DataGetSetter> list = new OperationHandler(rootenabled).openDirRoot(file);
            arrayList.addAll(list);
            adapter.notifyDataSetChanged();
            mCurrentDir = file;
        } else if (mimeType.startsWith("text") || name.endsWith(".txt") || name.endsWith(".c") || name.endsWith(".cpp") || name.endsWith(".java") || name.endsWith(".py") || name.endsWith(".rc") || name.endsWith(".prop")) {
            Intent intent = new Intent(RootActivity.this, TextEditor.class);
            intent.setData(Uri.parse(file.getPath()));
            startActivity(intent);
        } else if (mimeType.startsWith("audio") || file.getName().endsWith(".ogg") || file.getName().endsWith(".amr")) {
            Intent intent = new Intent(RootActivity.this, MusicPlayer.class);
            intent.setData(Uri.parse(file.getPath()));
            startActivity(intent);
        } else {
            setLastPosition();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mimeType);
            PackageManager packageManager = this.getPackageManager();
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Can't open " + name + "\n" + mimeType, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*private class Displasync extends  AsyncTask<File[],Void,Void>{

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(File[]... tmpFileArray) {
            File f[]=tmpFileArray[0];
            ArrayList<DataGetSetter> arrayList1=new ArrayList<>();
            ArrayList<DataGetSetter> arrayList2=new ArrayList<>();
            ArrayList<DataGetSetter> arrayList3=new ArrayList<>();
              for (int i=0;i<f.length;i++) {
                  if (f[i].getName().startsWith(".")) arrayList1.add(new DataGetSetter(f[i]));
                  else if (f[i].isDirectory()) arrayList2.add(new DataGetSetter(f[i]));
                  else arrayList3.add(new DataGetSetter(f[i]));
              }
          if(showHiddenFiles)arrayList.addAll(arrayList1);
            arrayList.addAll(arrayList2);
            arrayList.addAll(arrayList3);
            return null;
        }

    }
*/
    @Override
    public void onItemClick(int position, String action, boolean b) {
        if (action.equals("delete") && b) {
            removeItemFromList(position);
            return;
        } else if (action.equals("rename") && b) {
            adapter.notifyDataSetChanged();
            return;
        } else if (action.equals("move")) {
            mClipBoard = new ArrayList<>();
            mClipBoard.add(arrayList.get(position).getmFile());
            onClickCut();
        } else if (action.equals("copy")) {
            mClipBoard = new ArrayList<>();
            mClipBoard.add(arrayList.get(position).getmFile());
            onClickCopy();
        } else if (action.equals("longClicked")) {
            updateUI();
            return;
        } else if (position == 0 && !arrayList.get(0).getmFile().canWrite() && !rootenabled) {
            registerForContextMenu(recyclerView);
            openContextMenu(recyclerView);
        } else {
            File file = arrayList.get(position).getmFile();
            updateUI();
            Display(file);
        }
    }

    @Override
    public void onItemLongClick(int p, String action, boolean b) {
        if (action.equals("longClicked")) {
            updateUI();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.root_view_fab_cancel:
                if (arrayList.get(0).ismSelected() || operation != -1) {
                    operation = -1;
                    cancelSelection();
                    adapter.notifyDataSetChanged();
                    updateUI();
                } else {
                    arrayList.get(0).setSelected(false);
                    registerForContextMenu(FabCancel);
                    openContextMenu(FabCancel);
                }
                break;
            case R.id.root_view_fab_cut:
                onClickCut();
                break;
            case R.id.root_view_fab_copy:
                onClickCopy();
                break;
            case R.id.root_view_fab_delete:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Delete");
                dialog.setMessage("Do you want to delete Multiple files?");
                dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        operationHandler(1);
                    }
                });
                dialog.setCancelable(true);
                dialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
                break;
            default:
        }
    }

    void onClickCopy() {
        if (operation == -1) {
            operation = COPY;
            cancelSelection();
            adapter.notifyDataSetChanged();
            updateUI();
        } else operationHandler(2);
    }

    void onClickCut() {
        cancelSelection();
        adapter.notifyDataSetChanged();
        operation = CUT;
        updateUI();

    }

    void operationHandler(int option) {
        OperationHandler opH = new OperationHandler(rootenabled);
        File currentFile;
        for (int i = 0; i < mClipBoard.size(); i++) {
            currentFile = mClipBoard.get(i);
            switch (option) {
                case 1:
                    opH.delete(currentFile);
                    break;
                case 2:
                    if (operation == CUT)
                        opH.cut(currentFile, new File(mCurrentDir, currentFile.getName()));
                    else {
                        CopyTask task = new CopyTask();
                        task.delegate = this;
                        task.execute(currentFile, mCurrentDir);
                    }
                    break;
                default:
                    break;
            }
        }
        operation = -1;
        mClipBoard = new ArrayList<>();
        Display(mCurrentDir);
        updateUI();
    }

    void updateUI() {
        boolean b = arrayList.get(0).ismSelected();
        if (b) {
            FabCancel.setRotation(45);
            FabCopy.setVisibility(View.VISIBLE);
            FabCopy.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_content_copy_24dp));
            FabCut.setVisibility(View.VISIBLE);
            FabDelete.setVisibility(View.VISIBLE);

        } else {
            if (operation == -1) {
                FabCancel.setRotation(0);
                FabCopy.setVisibility(View.GONE);
            } else {
                FabCancel.setRotation(45);
                FabCopy.setVisibility(View.VISIBLE);
                FabCopy.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_content_paste_24dp));
            }
            FabCut.setVisibility(View.GONE);
            FabDelete.setVisibility(View.GONE);
        }

    }

    @Override
    public void importClipBoard(List<File> list) {
        mClipBoard = list;
    }

    void cancelSelection() {
        for (DataGetSetter dx : arrayList) {
            if (dx.ismSelected()) {
                dx.setSelected(false);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View
            v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.root_view_fab_cancel) {
            menu.setHeaderTitle("Create");
            menu.add(Menu.NONE, CONTEXT_MENU_New_File, Menu.NONE, "New File");
            menu.add(Menu.NONE, CONTEXT_MENU_New_Directory, Menu.NONE, "New Directory");
        } else {
            menu.setHeaderTitle("Storage");
            int i = 3;
            for (String x : availablePath) {
                menu.add(Menu.NONE, i, Menu.NONE, x);
                i++;
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean dir = false;
        if (item.getItemId() == CONTEXT_MENU_New_Directory) {
            dir = true;
        } else if (item.getItemId() != CONTEXT_MENU_New_File) {
            Display(new File("/storage", "" + item.getTitle()));
            return true;
        }
        final String tittle = dir ? "Folder" : "File";
        AlertDialog.Builder dialogBox = new AlertDialog.Builder(this);
        dialogBox.setTitle(tittle);
        final EditText name = new EditText(this);
        name.setText(mOPH.nameRandomizer(mCurrentDir, dir));
        name.setSelectAllOnFocus(true);
        dialogBox.setView(name);
        final boolean isDirectory = dir;
        dialogBox.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currentPath = mCurrentDir.getPath();
                String fileName = name.getText().toString();
                String msg = tittle + " " + fileName;
                int result = mOPH.createNew(currentPath, fileName, isDirectory);
                switch (result) {
                    case CONTEXT_MENU_FILE_CREATED:
                        addItemToList(new File(currentPath, fileName));
                        msg += " succesfully created";
                        break;
                    case CONTEXT_MENU_FILE_ALREADY_EXIST:
                        msg += " already exist";
                        break;
                    case CONTEXT_MENU_FILE_CANT_CREATE:
                        msg += " can't create that file";
                }
                Toast.makeText(RootActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        dialogBox.setNegativeButton(android.R.string.no, null);
        dialogBox.show();
        adapter.notifyDataSetChanged();
        return super.onContextItemSelected(item);
    }

    private String[] getStorageList() {
        File file = new File("/storage");
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                if (name.startsWith("sdcard") && file.list() != null) {
                    return true;
                } else if (!name.equals("emulated") && file.list() != null) {
                    return true;
                }
                return false;
            }
        });
    }

    private void addItemToList(File file) {
        DataGetSetter data = new DataGetSetter(file);
        arrayList.add(data);
        adapter.notifyDataSetChanged();
    }

    private void removeItemFromList(int p) {
        arrayList.remove(p);
        adapter.notifyDataSetChanged();
    }

    public void getLastPosition() {
        SharedPreferences lastPosition = PreferenceManager.getDefaultSharedPreferences(this);
        String path = lastPosition.getString("last_path", defaultPath);
        mCurrentDir = new File(path);
    }

    public void setLastPosition() {
        SharedPreferences lastPosition = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = lastPosition.edit();
        editor.putString("last_path", mCurrentDir.toString());
        editor.apply();
    }

    @Override
    public void processFinish(Integer integer) {
        Display(mCurrentDir);
    }


    private static class CopyTask extends AsyncTask<File, Integer, Integer> {
        public AsyncResponse delegate = null;

        @Override
        protected Integer doInBackground(File... params) {
            OperationHandler oph = new OperationHandler(true);
            File src = params[0];
            File des = new File(params[1], src.getName());
            if (src.isDirectory()) {
                oph.copyHelper(src, des);
            } else {
                oph.copyOp(src, des);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            delegate.processFinish(integer);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Integer integer) {
            super.onCancelled(integer);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}

