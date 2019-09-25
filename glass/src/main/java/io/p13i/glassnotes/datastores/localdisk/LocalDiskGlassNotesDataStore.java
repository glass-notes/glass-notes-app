package io.p13i.glassnotes.datastores.localdisk;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.Assert;
import io.p13i.glassnotes.utilities.DateUtilities;
import io.p13i.glassnotes.utilities.FileIO;

public class LocalDiskGlassNotesDataStore implements GlassNotesDataStore<Note> {
    private static final String TAG = LocalDiskGlassNotesDataStore.class.getName();

    private Context mContext;

    public LocalDiskGlassNotesDataStore(Context context) {
        mContext = context;
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
        Log.i(TAG, file.mkdirs() ? "Directories created" : "Directories not created");
        return file;
    }

    @Override
    public Note generateNewNote(String title) {
        return new Note(getStorageDirectory() + File.separator + DateUtilities.timestamp() + Note.MARKDOWN_EXTENSION, title, "");
    }

    private void write(String toFile, String data) {
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

    @Override
    public String getShortName() {
        return "LocalDisk";
    }

    @Override
    public void initialize() {
        Assert.that(isExternalStorageWritable());
        Log.i(TAG, "Initialized " + LocalDiskGlassNotesDataStore.class.getSimpleName());
    }

    @Override
    public void createNote(Note note, Promise<Note> promise) {
        write(note.getPath(), note.getContent());
        promise.resolved(note);
    }

    @Override
    public void getNotes(Promise<List<Note>> promise) {

        List<Note> notes = new ArrayList<Note>();
        File[] files = getStorageDirectory().listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.lastModified() == rhs.lastModified()) {
                    return 0;
                }
                return lhs.lastModified() < rhs.lastModified() ? -1 : 1;
            }
        });
        for (File noteFile : files) {
            notes.add(new Note(noteFile));
        }

        promise.resolved(notes);
    }

    @Override
    public void getNote(String path, Promise<Note> promise) {
        File noteFile = new File(path);
        promise.resolved(new Note(noteFile.getAbsolutePath(), noteFile.getName(), FileIO.read(noteFile)));
    }

    @Override
    public void saveNote(Note note, Promise<Note> promise) {
        write(note.getPath(), note.getContent());
        promise.resolved(note);
    }
}
