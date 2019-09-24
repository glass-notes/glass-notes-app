package io.p13i.glassnotes.datastores;

import java.util.List;

import io.p13i.glassnotes.models.Note;

public interface GlassNotesDataStore<T extends Note> {
    String getShortName();
    void initialize();
    void createNote(Note note, Promise<Note> promise);
    void getNotes(Promise<List<Note>> promise);
    void getNote(String id, Promise<Note> promise);
    void saveNote(Note note, Promise<Note> promise);
}
