package io.p13i.glassnotes.datastores.synced;

import android.content.Context;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.datastores.github_repo.GitHubRepoDataStore;
import io.p13i.glassnotes.datastores.github_repo.GitHubRepoNote;
import io.p13i.glassnotes.datastores.localdisk.LocalDiskGlassNotesDataStore;
import io.p13i.glassnotes.models.Note;


/**
 * Enables any note taking at anytime by implementing syncing between GitHub and local disk notes
 */
public class GitHubOfflineSyncingDataStore implements GlassNotesDataStore<GitHubRepoNote> {
    private LocalDiskGlassNotesDataStore mLocalDiskGlassNotesDataStore;
    private GitHubRepoDataStore mGitHubRepoDataStore;

    public GitHubOfflineSyncingDataStore(Context context, final String githubToken, final String ownerAndRepo) {
        this.mLocalDiskGlassNotesDataStore = new LocalDiskGlassNotesDataStore(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                GitHubOfflineSyncingDataStore.this.mGitHubRepoDataStore = new GitHubRepoDataStore(githubToken, ownerAndRepo);
            }
        }).run();
    }

    @Override
    public String getShortName() {
        return "GitHubOffline";
    }

    @Override
    public void createNote(final Note note, final Promise<Note> promise) {
        this.mGitHubRepoDataStore.createNote(note, new Promise<Note>() {
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
        this.mGitHubRepoDataStore.getNotes(new Promise<List<Note>>() {
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
        this.mGitHubRepoDataStore.getNote(id, new Promise<Note>() {
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
        this.mGitHubRepoDataStore.saveNote(note, new Promise<Note>() {
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