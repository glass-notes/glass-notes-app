package io.p13i.glassnotes.datastores.localdisk;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.github.ClientFactory;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.Assert;

public class LocalDiskGlassNotesDataStore implements GlassNotesDataStore {
    private static final String TAG = LocalDiskGlassNotesDataStore.class.getName();

    private Context mContext;

    public LocalDiskGlassNotesDataStore(Context context) {
        mContext = context;

        Assert.that(isExternalStorageWritable());
    }

    /**
     * Checks if external storage is available for read and write
     * https://developer.android.com/training/data-storage/files#CheckExternalAvail
     *
     * @return
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * https://developer.android.com/training/data-storage/files#PrivateFiles
     *
     * @return
     */
    private File getStorageDirectory() {
        // Get the directory for the app's private pictures directory.
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "glass-notes");
        Log.i(TAG, file.mkdir() ? "Directory created" : "Directory not created");
        return file;
    }

    private void write(String toFile, String data) {
        File file = new File(getStorageDirectory(), toFile);

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file, /* append: */false);
            stream.write(data.getBytes());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String read(String fromFile) {
        File file = new File(getStorageDirectory(), fromFile);
        return read(file);
    }

    private String read(File file) {
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

    private static String generateTemporaryId() {
        return "TEMP-" + UUID.randomUUID().toString();
    }

    @Override
    public String getShortName() {
        return "LocalDisk";
    }

    @Override
    public void createNote(Note note, Promise<Note> promise) {
        Gson gson = ClientFactory.getGson();
        note.setId(generateTemporaryId());
        write(note.getId(), gson.toJson(note));
        promise.resolved(note);
    }

    @Override
    public void getNotes(Promise<List<Note>> promise) {
        Gson gson = ClientFactory.getGson();

        List<Note> notes = new ArrayList<>();
        File[] files = getStorageDirectory().listFiles();
        for (File noteFile : files) {
            notes.add(gson.fromJson(read(noteFile), Note.class));
        }

        promise.resolved(notes);
    }

    @Override
    public void getNote(String id, Promise<Note> promise) {
        String fileContents = read(id);
        if (fileContents == null) {
            promise.rejected(new Throwable());
        }
        Gson gson = ClientFactory.getGson();
        Note note = gson.fromJson(fileContents, Note.class);
        promise.resolved(note);
    }

    @Override
    public void saveNote(Note note, Promise<Note> promise) {
        Gson gson = ClientFactory.getGson();
        write(note.getId(), gson.toJson(note));
        promise.resolved(note);
    }
}
