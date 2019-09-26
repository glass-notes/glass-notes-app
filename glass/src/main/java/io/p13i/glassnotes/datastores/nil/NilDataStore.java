package io.p13i.glassnotes.datastores.nil;

import java.util.ArrayList;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;


/**
 * Returns an empty list in getNotes. All other methods are rejected promises.
 */
public class NilDataStore implements GlassNotesDataStore<Note> {
    private static Throwable sCommonException = new RuntimeException("You're using the " + NilDataStore.class.getSimpleName());

    @Override
    public void createNote(String title, Promise<Note> promise) {
        promise.rejected(sCommonException);
    }

    @Override
    public void getNotes(Promise<List<Note>> promise) {
        promise.resolved(new ArrayList<Note>(0));
    }

    @Override
    public void getNote(String path, Promise<Note> promise) {
        promise.rejected(sCommonException);
    }

    @Override
    public void saveNote(Note note, Promise<Note> promise) {
        promise.rejected(sCommonException);
    }

    @Override
    public void deleteNote(Note note, Promise<Boolean> promise) {
        promise.rejected(sCommonException);
    }
}
