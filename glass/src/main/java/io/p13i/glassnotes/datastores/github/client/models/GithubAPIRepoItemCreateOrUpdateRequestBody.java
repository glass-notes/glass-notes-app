package io.p13i.glassnotes.datastores.github.client.models;

import com.google.gson.annotations.SerializedName;

public class GithubAPIRepoItemCreateOrUpdateRequestBody {
    @SerializedName("message")
    public String mMessage;

    @SerializedName("content")
    public String mBase64EncodedContent;

    @SerializedName("sha")
    public String mSha;

    public GithubAPIRepoItemCreateOrUpdateRequestBody(String mMessage, String mBase64EncodedContent, String mSha) {
        this.mMessage = mMessage;
        this.mBase64EncodedContent = mBase64EncodedContent;
        this.mSha = mSha;
    }
}
