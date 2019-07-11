package io.p13i.glassnotes.utilities;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIO {
    private static final String TAG = FileIO.class.getName();

    /**
     * https://developer.android.com/training/data-storage/files#PrivateFiles
     *
     * @return
     */
    public static File getStorageDirectory(Context context) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "glass-notes");
        Log.i(TAG, file.mkdir() ? "Directory created" : "Directory not created");
        return file;
    }

    public static File getFile(Context context, String filename) {
        return new File(getStorageDirectory(context), filename);
    }

    public static FileOutputStream getFileOutputStream(Context context, String filename, boolean createFileIfNeeded) {
        File file = getFile(context, filename);
        if (createFileIfNeeded) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to create new file.", e);
            }
        }
        return getFileOutputStream(file);
    }

    public static FileOutputStream getFileOutputStream(File file) {
        try {
            return new FileOutputStream(file, /* append: */ false);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found:", e);
            return null;
        }
    }

    public static void write(Context context, String toFile, String data) {
        File file = getFile(context, toFile);

        try {
            file.createNewFile();
            FileOutputStream stream = getFileOutputStream(file);
            stream.write(data.getBytes());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String read(Context context, String fromFile) {
        File file = new File(getStorageDirectory(context), fromFile);
        return read(file);
    }

    public static String read(File file) {
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (IOException e) {
            return null;
        }

        return new String(bytes);
    }
}
