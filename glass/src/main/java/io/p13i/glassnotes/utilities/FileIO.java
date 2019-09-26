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

    public static void write(String toFile, String data) {
        File file = new File(toFile);

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file, /* append: */false);
            stream.write(data.getBytes());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean delete(String absoluteResourcePath) {
        File file = new File(absoluteResourcePath);
        return file.delete();
    }
}
