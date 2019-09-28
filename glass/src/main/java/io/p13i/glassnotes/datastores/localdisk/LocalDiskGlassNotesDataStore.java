package io.p13i.glassnotes.datastores.localdisk;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.GlassNotesDataStoreException;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.FileIO;

public class LocalDiskGlassNotesDataStore implements GlassNotesDataStore<Note> {
    private static final String TAG = LocalDiskGlassNotesDataStore.class.getName();
    private static final String LOCAL_STORAGE_DIRECTORY = "glass-notes";

    private Context mContext;

    public LocalDiskGlassNotesDataStore(Context context) {
        if (!isExternalStorageWritable()) {
            throw new GlassNotesDataStoreException("External storage must be writable.");
        }
        mContext = context;
    }

    /**
     * Checks if external storage is available for read and write
     * https://developer.android.com/training/data-storage/files#CheckExternalAvail
     *
     * @return whether or not the storage is writable
     */
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * https://developer.android.com/training/data-storage/files#PrivateFiles
     *
     * @return the storage directory for this application
     */
    public File getStorageDirectory() {
        // Get the directory for the app's private pictures directory.
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), LOCAL_STORAGE_DIRECTORY);
        Log.i(TAG, (file.mkdirs() ? "Directories created" : "Directories not created") + " for path " + file.getAbsolutePath());
        return file;
    }

    @Override
    public void createNote(String path, Promise<Note> promise) {
        String filename = path;
        if (!path.endsWith(Note.MARKDOWN_EXTENSION)) {
            filename += Note.MARKDOWN_EXTENSION;
        }
        String absoluteFilePath = new File(getStorageDirectory(), filename).getAbsolutePath();
        String content = "";
        FileIO.write(absoluteFilePath, content);
        promise.resolved(new Note(absoluteFilePath, filename, content, null));
    }

    @Override
    public void getNotes(Promise<List<Note>> promise) {

        List<Note> notes = new ArrayList<Note>();

        File[] files = getStorageDirectory().listFiles();

        for (File noteFile : files) {
            if (noteFile.getName().endsWith(Note.MARKDOWN_EXTENSION)) {
                notes.add(new Note(noteFile.getAbsolutePath(), noteFile.getName(), FileIO.read(noteFile), null));
                Log.i(TAG, "Added file " + noteFile.getAbsolutePath());
            } else {
                Log.i(TAG, "Skipping file " + noteFile.getAbsolutePath());
            }
        }

        promise.resolved(notes);
    }

    @Override
    public void getNote(String path, Promise<Note> promise) {
        File noteFile = new File(path);
        promise.resolved(new Note(noteFile.getAbsolutePath(), noteFile.getName(), FileIO.read(noteFile), null));
    }

    @Override
    public void saveNote(Note note, Promise<Note> promise) {
        FileIO.write(note.getAbsoluteResourcePath(), note.getContent());
        promise.resolved(note);
    }

    @Override
    public void deleteNote(Note note, Promise<Boolean> promise) {
        promise.resolved(FileIO.delete(note.getAbsoluteResourcePath()));
    }
}
