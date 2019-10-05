package io.p13i.glassnotes.datastores.github.client.models;

import com.google.gson.annotations.SerializedName;

public class GithubAPIRepoItemDeleteRequestBody {
    @SerializedName("message")
    public String mMessage;

    @SerializedName("sha")
    public String mSha;

    public GithubAPIRepoItemDeleteRequestBody(String mMessage, String mSha) {
        this.mMessage = mMessage;
        this.mSha = mSha;
    }
}
