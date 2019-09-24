package io.p13i.glassnotes.datastores.github_offline;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.datastores.github.GlassNotesGitHubAPIClient;
import io.p13i.glassnotes.datastores.localdisk.LocalDiskGlassNotesDataStore;
import io.p13i.glassnotes.models.Note;


/**
 * Enables any note taking at anytime by implementing syncing between GitHub and local disk notes
 */
public class GitHubOfflineSyncingDataStore implements GlassNotesDataStore {
    private final LocalDiskGlassNotesDataStore mLocalDiskGlassNotesDataStore;
    private final GlassNotesGitHubAPIClient mGlassNotesGitHubAPIClient;

    public GitHubOfflineSyncingDataStore(Context context, String githubToken) {
        this.mLocalDiskGlassNotesDataStore = new LocalDiskGlassNotesDataStore(context);
        this.mGlassNotesGitHubAPIClient = new GlassNotesGitHubAPIClient(githubToken);
    }

    @Override
    public String getShortName() {
        return "GitHubOffline";
    }

    @Override
    public void createNote(final Note note, final Promise<Note> promise) {
        this.mGlassNotesGitHubAPIClient.createNote(note, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                mLocalDiskGlassNotesDataStore.createNote(note, new Promise<Note>() {

                    @Override
                    public void resolved(Note data) {
                        promise.resolved(data);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        promise.rejected(t);
                    }
                });
            }
        });
    }

    @Override
    public void getNotes(final Promise<List<Note>> promise) {
        this.mGlassNotesGitHubAPIClient.getNotes(new Promise<List<Note>>() {
            @Override
            public void resolved(final List<Note> githubNotes) {
                mLocalDiskGlassNotesDataStore.getNotes(new Promise<List<Note>>() {
                    @Override
                    public void resolved(final List<Note> localdiskNotes) {
                        promise.resolved(new LinkedList<Note>() {{
                            addAll(githubNotes);
                            addAll(localdiskNotes);
                        }});
                    }

                    @Override
                    public void rejected(Throwable t) {
                        promise.resolved(githubNotes);
                    }
                });
            }

            @Override
            public void rejected(Throwable t) {
                mLocalDiskGlassNotesDataStore.getNotes(new Promise<List<Note>>() {
                    @Override
                    public void resolved(final List<Note> localdiskNotes) {
                        promise.resolved(localdiskNotes);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        promise.rejected(t);
                    }
                });
            }
        });
    }

    @Override
    public void getNote(final String id, final Promise<Note> promise) {
        this.mGlassNotesGitHubAPIClient.getNote(id, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                mLocalDiskGlassNotesDataStore.getNote(id, new Promise<Note>() {
                    @Override
                    public void resolved(Note data) {
                        promise.resolved(data);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        promise.rejected(t);
                    }
                });
            }
        });
    }

    @Override
    public void saveNote(final Note note, final Promise<Note> promise) {
        this.mGlassNotesGitHubAPIClient.saveNote(note, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                mLocalDiskGlassNotesDataStore.saveNote(note, new Promise<Note>() {
                    @Override
                    public void resolved(Note data) {
                        promise.resolved(data);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        promise.rejected(t);
                    }
                });
            }
        });
    }
}
