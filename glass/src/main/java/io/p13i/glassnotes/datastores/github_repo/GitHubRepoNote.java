package io.p13i.glassnotes.datastores.github_repo;

import org.kohsuke.github.GHContent;

import java.io.IOException;

import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.StringUtilities;

public class GitHubRepoNote extends Note {

    private final GHContent mGHContent;

    public GitHubRepoNote(GHContent content) throws IOException {
        super(content.getPath(), content.getName(), StringUtilities.readInputStream(content.read()));
        this.mGHContent = content;
    }

    public GHContent getGHContent() {
        return mGHContent;
    }
}
