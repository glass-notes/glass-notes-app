package io.p13i.glassnotes.datastores.github.client.models;

import com.google.gson.annotations.SerializedName;

public class GithubAPIRepoItemCreateOrUpdateResponse {
    @SerializedName("content")
    public GitHubAPIRepoItem mContent;
}
