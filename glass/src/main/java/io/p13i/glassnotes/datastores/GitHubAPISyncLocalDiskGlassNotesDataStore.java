package io.p13i.glassnotes.datastores;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

import io.p13i.glassnotes.datastores.github.GithubRepoAPIGlassNotesDataStore;
import io.p13i.glassnotes.datastores.localdisk.LocalDiskGlassNotesDataStore;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.FileIO;
import io.p13i.glassnotes.utilities.ListUtils;

public class GitHubAPISyncLocalDiskGlassNotesDataStore implements GlassNotesDataStore {
    private static final String TAG = GithubRepoAPIGlassNotesDataStore.class.getName();

    private LocalDiskGlassNotesDataStore localDiskGlassNotesDataStore;
    private GithubRepoAPIGlassNotesDataStore githubRepoAPIGlassNotesDataStore;

    public GitHubAPISyncLocalDiskGlassNotesDataStore(Context context, String owner, String repo, String githubOAuthToken) {
        localDiskGlassNotesDataStore = new LocalDiskGlassNotesDataStore(context);
        githubRepoAPIGlassNotesDataStore = new GithubRepoAPIGlassNotesDataStore(owner, repo, githubOAuthToken);
    }

    @Override
    public String getName() {
        return "GitHub Sync";
    }

    @Override
    public void createNote(final String path, final Promise<Note> promise) {
        Log.i(TAG, "Creating note at path " + path);
        githubRepoAPIGlassNotesDataStore.createNote(path, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                Log.i(TAG, "Using GitHub, created note at path " + path);
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                Log.i(TAG, "Failed to create note on Github, trying local disk for path " + path, t);
                localDiskGlassNotesDataStore.createNote(path, new Promise<Note>() {
                    @Override
                    public void resolved(Note data) {
                        Log.i(TAG, "Created on local disk with path" + data.getAbsoluteResourcePath());
                        promise.resolved(data);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        Log.i(TAG, "Failed to create note on local disk with path" + path);
                        promise.rejected(t);
                    }
                });
            }
        });
    }

    @Override
    public void getNote(final String path, final Promise<Note> promise) {
        Log.i(TAG, "Getting note at path " + path);
        githubRepoAPIGlassNotesDataStore.getNote(path, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                Log.i(TAG, "Got note from GitHub at path " + data.getAbsoluteResourcePath());
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to get note from GitHub API, trying local disk for note with path " + path, t);
                localDiskGlassNotesDataStore.getNote(path, new Promise<Note>() {
                    @Override
                    public void resolved(Note data) {
                        Log.i(TAG, "Got note from local disk with path " + path);
                        promise.resolved(data);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        Log.e(TAG, "Failed to get note from local disk with path " + path);
                        promise.rejected(t);
                    }
                });
            }
        });
    }

    @Override
    public void saveNote(final Note note, final Promise<Note> promise) {
        Log.i(TAG, "Saving note with path " + note.getAbsoluteResourcePath());

        // Remove the local disk directory if it exists in the path
        String localDiskDirectory = localDiskGlassNotesDataStore.getStorageDirectory().getAbsolutePath();

        final Note noteToSave = new Note(
                note.getAbsoluteResourcePath().replace(localDiskDirectory, ""),
                note.getFilename(),
                note.getContent(),
                note.getSha()
        );

        githubRepoAPIGlassNotesDataStore.saveNote(note, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                Log.i(TAG, "Saved to GitHub for note with path " + data.getAbsoluteResourcePath());

                // Delete the local copy
                if (FileIO.delete(note.getAbsoluteResourcePath())) {
                    Log.i(TAG, "Deleted old note with name " + note.getFilename());
                } else {
                    Log.e(TAG, "Failed to delete old note with path " + note.getFilename());
                }

                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to save to GitHub, trying local disk for note with path " + noteToSave.getAbsoluteResourcePath(), t);
                // Delete the existing note
                String noteFilename = getLocalDiskNoteFallbackBaseFilename(noteToSave);
                String noteFilePath = new File(localDiskGlassNotesDataStore.getStorageDirectory(), noteFilename).getAbsolutePath();
                localDiskGlassNotesDataStore.saveNote(new Note(
                        noteFilePath,
                        noteFilename,
                        noteToSave.getContent(),
                        noteToSave.getSha()), new Promise<Note>() {
                    @Override
                    public void resolved(Note data) {
                        Log.i(TAG, "Saved note to local disk for note with path " + data.getAbsoluteResourcePath());

                        if (!note.getFilename().equals(data.getFilename())) {
                            if (FileIO.delete(note.getAbsoluteResourcePath())) {
                                Log.i(TAG, "Deleted old note with name " + note.getFilename());
                            } else {
                                Log.e(TAG, "Failed to delete old note with path " + note.getFilename());
                            }
                        }

                        promise.resolved(data);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        Log.e(TAG, "Failed to save note to local disk for note with path " + noteToSave.getAbsoluteResourcePath(), t);
                        promise.rejected(t);
                    }
                });
            }
        });
    }

    private String getLocalDiskNoteFallbackBaseFilename(Note note) {
        String baseFileName = FileIO.basename(note.getFilename());
        baseFileName = baseFileName.replace(Note.MARKDOWN_EXTENSION, "");
        baseFileName = baseFileName.replace(".local", "");
        return baseFileName + ".local" + Note.MARKDOWN_EXTENSION;
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
