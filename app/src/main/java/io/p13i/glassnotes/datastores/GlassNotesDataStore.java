package io.p13i.glassnotes.datastores;

import java.util.List;

import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.datastores.github.GlassNotesGitHubAPIClient;
import okhttp3.ResponseBody;

public interface GlassNotesDataStore {
    void createNote(Note note, Promise<Note> promise);
    void getNotes(Promise<List<Note>> promise);
    void getNote(String id, Promise<Note> promise);
    void saveNote(Note note, Promise<Note> promise);

    interface Promise<T> {
        void resolved(T data);
        void failed(Throwable t);
    }
}
