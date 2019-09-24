package io.p13i.glassnotes.datastores.github_repo;

import android.util.Log;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.DateUtilities;

public class GitHubRepoDataStore implements GlassNotesDataStore<GitHubRepoNote> {
    private static final String TAG = GitHubRepoDataStore.class.getName();
    private IOException mConstructorException;

    private GitHub mGitHub;
    private GHRepository mRepo;
    private String mOwnerAndRepo;

    public GitHubRepoDataStore(String githubToken, String ownerAndRepo) {
        try {
            this.mGitHub = GitHub.connectUsingOAuth(githubToken);
            this.mRepo = mGitHub.getRepository(mOwnerAndRepo);
        } catch (IOException e) {
            Log.e(TAG, "Failed to connectUsingOAuth or getRepository", e);
            this.mGitHub = null;
            this.mRepo = null;
            this.mConstructorException = e;
        }
        this.mOwnerAndRepo = ownerAndRepo;
    }

    @Override
    public String getShortName() {
        return "GitHubRepo";
    }

    @Override
    public void createNote(Note note, Promise<Note> promise) {
        if (this.mRepo == null) {
            promise.rejected(mConstructorException);
        }

        try {
            GHContent content = mRepo.createContent()
                    .message(DateUtilities.timestamp())
                    .path(note.getPath())
                    .content(note.getContent())
                    .commit()
                    .getContent();

            promise.resolved(new GitHubRepoNote(content));

        } catch (IOException e) {
            promise.rejected(e);
        }
    }

    @Override
    public void getNotes(Promise<List<Note>> promise) {
        if (this.mRepo == null) {
            promise.rejected(mConstructorException);
        }

        try {
            List<GHContent> contentList = mRepo.getDirectoryContent("");

            List<Note> result = new LinkedList<Note>();
            for (GHContent content : contentList) {
                result.add(new GitHubRepoNote(content));
            }

            promise.resolved(result);

        } catch (IOException e) {
            promise.rejected(e);
        }
    }

    @Override
    public void getNote(String path, Promise<Note> promise) {
        if (this.mRepo == null) {
            promise.rejected(mConstructorException);
        }

        try {
            GHContent content = mRepo.getFileContent(path);

            promise.resolved(new GitHubRepoNote(content));

        } catch (IOException e) {
            promise.rejected(e);
        }
    }

    @Override
    public void saveNote(Note note, Promise<Note> promise) {
        try {
            GHContent content = ((GitHubRepoNote) note).getGHContent()
                .update(note.getContent(), DateUtilities.timestamp())
                .getContent();
            promise.resolved(new GitHubRepoNote(content));
        } catch (IOException e) {
            promise.rejected(e);
        }
    }
}
