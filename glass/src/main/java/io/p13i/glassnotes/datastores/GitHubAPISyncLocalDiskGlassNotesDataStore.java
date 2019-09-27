package io.p13i.glassnotes.datastores;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

import io.p13i.glassnotes.datastores.github.GithubRepoAPIGlassNotesDataStore;
import io.p13i.glassnotes.datastores.localdisk.LocalDiskGlassNotesDataStore;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.ListUtils;

public class GitHubAPISyncLocalDiskGlassNotesDataStore implements GlassNotesDataStore<Note> {
    private LocalDiskGlassNotesDataStore localDiskGlassNotesDataStore;
    private GithubRepoAPIGlassNotesDataStore githubRepoAPIGlassNotesDataStore;

    public GitHubAPISyncLocalDiskGlassNotesDataStore(Context context, String owner, String repo, String githubOAuthToken) {
        localDiskGlassNotesDataStore = new LocalDiskGlassNotesDataStore(context);
        githubRepoAPIGlassNotesDataStore = new GithubRepoAPIGlassNotesDataStore(owner, repo, githubOAuthToken);
    }

    @Override
    public void createNote(final String path, final Promise<Note> promise) {
        githubRepoAPIGlassNotesDataStore.createNote(path, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                localDiskGlassNotesDataStore.createNote(path, new Promise<Note>() {
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
    public void getNote(final String path, final Promise<Note> promise) {
        githubRepoAPIGlassNotesDataStore.getNote(path, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                localDiskGlassNotesDataStore.getNote(path, new Promise<Note>() {
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
        githubRepoAPIGlassNotesDataStore.saveNote(note, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                localDiskGlassNotesDataStore.saveNote(new Note(
                        new File(localDiskGlassNotesDataStore.getStorageDirectory(),
                  "TEMP-" + note.getAbsoluteResourcePath()).getAbsolutePath(),
                        note.getFilename(),
                        note.getContent()), new Promise<Note>() {
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
    public void deleteNote(final Note note, final Promise promise) {
        githubRepoAPIGlassNotesDataStore.deleteNote(note, new Promise<Boolean>() {
            @Override
            public void resolved(final Boolean githubResponse) {
                localDiskGlassNotesDataStore.deleteNote(note, new Promise<Boolean>() {
                    @Override
                    public void resolved(Boolean localDiskResponse) {
                        promise.resolved(githubResponse || localDiskResponse);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        promise.rejected(t);
                    }
                });
            }

            @Override
            public void rejected(Throwable t) {
                localDiskGlassNotesDataStore.deleteNote(note, new Promise<Boolean>() {
                    @Override
                    public void resolved(Boolean localDiskResponse) {
                        promise.resolved(localDiskResponse);
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
        githubRepoAPIGlassNotesDataStore.getNotes(new Promise<List<Note>>() {
            @Override
            public void resolved(final List<Note> githubNotes) {
                localDiskGlassNotesDataStore.getNotes(new Promise<List<Note>>() {
                    @Override
                    public void resolved(List<Note> localDiskNotes) {
                        promise.resolved(ListUtils.join(githubNotes, localDiskNotes));
                    }

                    @Override
                    public void rejected(Throwable t) {
                        promise.resolved(githubNotes);
                    }
                });
            }

            @Override
            public void rejected(Throwable t) {
                localDiskGlassNotesDataStore.getNotes(new Promise<List<Note>>() {
                    @Override
                    public void resolved(List<Note> localDiskNotes) {
                        promise.resolved(localDiskNotes);
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
