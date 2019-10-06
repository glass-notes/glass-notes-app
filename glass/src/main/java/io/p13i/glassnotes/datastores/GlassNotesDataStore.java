package io.p13i.glassnotes.datastores;

import java.util.List;

import io.p13i.glassnotes.models.Note;

public interface GlassNotesDataStore {
    String getName();

    void createNote(String path, Promise<Note> promise);

    void getNotes(Promise<List<Note>> promise);

    void getNote(String path, Promise<Note> promise);

    void saveNote(Note note, Promise<Note> promise);

    void deleteNote(Note note, Promise<Boolean> promise);
}
