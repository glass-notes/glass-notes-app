package io.p13i.glassnotes.datastores.github.client.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.p13i.glassnotes.utilities.Assert;

public class GitHubAPIRepoItem implements Serializable {

    @SerializedName("name")
    public String mFilename;

    @SerializedName("path")
    public String mPath;

    @SerializedName("sha")
    public String mSha;

    @SerializedName("content")
    public String mBase64EncodedContent;

    public GitHubAPIRepoItem(String filename, String path, String sha, String base64EncodedContent) {
        Assert.that(path.endsWith(filename));

        mFilename = filename;
        mPath = path;
        mSha = sha;
        mBase64EncodedContent = base64EncodedContent;
    }
}
