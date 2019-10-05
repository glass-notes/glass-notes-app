package io.p13i.glassnotes.datastores.github.client.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GitHubAPIRepoItem implements Serializable {

    @SerializedName("type")
    public String mItemType;

    @SerializedName("name")
    public String mFilename;

    @SerializedName("path")
    public String mPath;

    @SerializedName("sha")
    public String mSha;

    @SerializedName("content")
    public String mBase64EncodedContent;
}
