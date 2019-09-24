package io.p13i.glassnotes.datastores.github_repo;

import org.kohsuke.github.GHContent;

import java.io.IOException;

import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.StringUtilities;

public class GitHubRepoNote extends Note {

    private GHContent mGHContent;

    public GitHubRepoNote(String path, String name, String content) {
        super(path, name, content);
    }

    public static GitHubRepoNote read(GHContent ghContent) throws IOException {
        GitHubRepoNote note = new GitHubRepoNote(ghContent.getPath(), ghContent.getName(), StringUtilities.readInputStream(ghContent.read()));
        note.setGHContent(ghContent);
        return note;
    }

    public GHContent getGHContent() {
        return mGHContent;
    }

    public void setGHContent(GHContent content) {
        mGHContent = content;
    }
}
