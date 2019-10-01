package io.p13i.glassnotes.datastores;

import java.util.List;

import io.p13i.glassnotes.models.Note;

public interface GlassNotesDataStore<T extends Note> {
    String getName();
    void createNote(String path, Promise<T> promise);
    void getNotes(Promise<List<T>> promise);
    void getNote(String path, Promise<T> promise);
    void saveNote(T note, Promise<T> promise);
    void deleteNote(T note, Promise<Boolean> promise);
}
