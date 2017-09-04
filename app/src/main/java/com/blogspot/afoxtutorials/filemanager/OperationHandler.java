package com.blogspot.afoxtutorials.filemanager;

import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by neerajMalhotra on 18-06-2017.
 */

public class OperationHandler {
    /**
     * last error output for root,null if success
     */
    public static String stdErr;
    static boolean mRootEnabled;
    /**
     * last exit code for root
     */
    private static int exitValue;
    String newFolder = "new_folder_", newFile = "new_file_";

    OperationHandler(boolean rootEnabled) {
        mRootEnabled = rootEnabled;
    }

    static public String exec(String cmd) {
        String outString = "";
        try {
            char[] buff = new char[1024 * 10];
            Process process;
            if (mRootEnabled)
                process = Runtime.getRuntime().exec("su\n");

            else
                process = Runtime.getRuntime().exec("sh");
            OutputStreamWriter stdin = new OutputStreamWriter(process.getOutputStream());
            InputStreamReader stdout = new InputStreamReader(process.getInputStream());
            if (mRootEnabled) {
                stdin.write("mount -o rw,remount /system" + "\n");
                stdin.write("mount -o rw,remount / " + "\n");
                stdin.write("mount -o rw,remount /data" + "\n");
            }
            stdin.write(cmd + "\n");
            stdin.write("exit\n");
            stdin.flush();
            exitValue = process.waitFor();
            int __count = stdout.read(buff);
            if (__count > 0) {
                outString = new String(buff);
            }
            stdErr = null;
            int count = new InputStreamReader(process.getErrorStream()).read(buff);
            if (count > 0)
                stdErr = new String(buff);
        } catch (IOException e) {
            Log.e("OperationHandler.class", "IOException at Line 114");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e("OperationHandler.class", "InterruptedException at Line 114");
            e.printStackTrace();
        }
        return outString;
    }

    /**
     * open the directory as root through shell
     */
    static public List<DataGetSetter> openDirRoot(File file) {
        String path = file.getPath();
        List<DataGetSetter> dataGetSetters = new ArrayList<DataGetSetter>();
        dataGetSetters.add(new DataGetSetter(file.getParentFile(), file.getName(), true));
        if (!path.endsWith("/"))
            path += "/";
        String resultString = exec("ls -al " + path);
        if (resultString == null || stdErr != null || exitValue != 0) {
            return null;
        }
        List<String> permList = new ArrayList<String>();
        List<String> secList = new ArrayList<String>();
        Pattern permPattern = Pattern.compile("\n[a-z-]+");
        Pattern secPattern = Pattern.compile("\\d* [\\d]{4}(-[\\d]{2}){2} [\\d]{2}:[\\d]{2} .+\n");
        Matcher pmMatcher = permPattern.matcher(resultString);
        Matcher tmMatcher = secPattern.matcher(resultString);
        while (pmMatcher.find() && tmMatcher.find()) {
            permList.add(pmMatcher.group());
            secList.add(tmMatcher.group());
        }
        for (int i = 0; i < permList.size(); i++) {
            String perm = (permList.get(i)).substring(1);
            String[] sec = (secList.get(i)).split("\\s");
            String time = sec[1] + " " + sec[2], name = sec[3], size = sec[0];
            boolean dir = !size.isEmpty() || sec.length > 4 ? false : true;
            long sizeLong = size.isEmpty() ? 0 : Long.valueOf(size);
            dataGetSetters.add(new DataGetSetter(name, time, sizeLong, perm, file.getPath(), dir));
        }
        return dataGetSetters;
    }

    public boolean delete(File dir) {
        String path = dir.getPath();
        return deleteFile(path);
    }

    public boolean deleteFile(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            String[] subFiles = file.list();
            String dirString;
            if (path.endsWith("/"))
                dirString = path;
            else {
                dirString = path + "/";
            }
            if (subFiles != null) {
                int count = subFiles.length;
                for (int i = 0; i < count; i++) {
                    deleteFile(dirString + subFiles[i]);
                }
            }
            file.delete();
        }
        if (!file.delete()) {
            exec("rm -rf  " + path);
            if (stdErr != null | exitValue != 0) return false;
            else return true;
        }
        return true;
    }

    public boolean cut(File source, File destination) {
        if (!source.renameTo(destination)) {
            String s = "mv \"" + source.getPath() + "\" \"" + destination.getPath() + "\"";
            exec(s);
            if (stdErr != null || exitValue != 0)
                return false;
        }
        return true;
    }

    /**
     * rename the file
     **/
    public boolean reName(File file, String newName) {
        File dstFile = new File(file.getParent() + "/" + newName);
        return cut(file, dstFile);
    }

    public boolean copyHelper(File src, File des) {
        boolean b = des.mkdir();
        if (!b && !des.exists()) {
            exec("cp -a ~" + src.getPath() + "/. ~" + des.getPath() + "/");
            if (stdErr != null | exitValue != 0) return false;
            else return true;
        }
        File[] file = src.listFiles();
        if (file == null) {
            return false;
        }
        for (File filex : file) {
            if (filex.isDirectory()) {
                copyHelper(filex, new File(des, filex.getName()));
            } else {
                b = copyOp(filex, new File(des, filex.getName()));
            }
        }
        return b;
    }

    public boolean copyOp(File filex, File des) {
        boolean b;
        FileChannel ic = null, oc = null;
        try {
            b = des.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            exec("cp -f " + filex.getPath() + " " + des.getPath());
            if (stdErr != null || exitValue != 0) return false;
            else return true;
        }
        try {
            ic = new FileInputStream(filex).getChannel();
            oc = new FileOutputStream(des).getChannel();
            oc.transferFrom(ic, 0, ic.size());
            b = true;
        } catch (IOException e) {
            e.printStackTrace();
            b = false;
        } finally {
            if (ic != null || oc != null)
                try {
                    ic.close();
                    oc.close();
                } catch (NullPointerException e) {
                    Log.e("OperationHandler>", "175 NullPointerException: " + e);
                } catch (IOException e) {
                    Log.e("OperationHandler>", "175 IOException: " + e);
                }
        }
        return b;
    }


    public String getMimeType(File file) {
        String s = getExt(file);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getMimeTypeFromExtension(s.toLowerCase());
    }

    public String getExt(File file) {
        StringBuilder sb = new StringBuilder(file.getName());
        sb.reverse();
        int i = sb.indexOf(".");
        if (i >= 0) {
            sb.setLength(i);
            sb.reverse();
        }
        return sb.toString();
    }

    public String nameRandomizer(File file, boolean dir) {
        String name, ext = ".txt";
        String[] s;
        if (dir) {
            name = newFolder;
            ext = "";
        } else {
            name = newFile;
        }
        if (file.list() == null) {
            String out = exec("ls " + file.getPath());
            s = out.split("\\s");
        } else s = file.list();
        int num = 0, tmp = 0;
        String group = "";
        for (String x : s) {
            if (x.startsWith(name)) {
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(x);
                while (m.find()) {
                    group = m.group();
                }
                tmp = group.isEmpty() ? 0 : Integer.valueOf(group) + 1;
                num = tmp > num ? tmp : num;
            }

        }
        return name + num + ext;
    }

    public int createNew(String path, String FileName, boolean dir) {
        File fileParent = new File(path);
        File file = new File(fileParent, FileName);
        boolean b = false;
        int result = 0;
        if (file.exists()) {
            result = -1;
        } else if (fileParent.canWrite()) {
            if (dir) b = file.mkdir();
            else {
                try {
                    b = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (b) {
                result = 1;
            }
        } else {
            if (dir) exec("mkdir " + file.getPath());
            else exec("touch  " + file.getPath());
            if (stdErr != null | exitValue != 0)
                return 0;
            else return 1;
        }
        return result;
    }

}
