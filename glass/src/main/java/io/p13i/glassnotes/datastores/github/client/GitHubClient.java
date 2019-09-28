package io.p13i.glassnotes.datastores.github.client;

import java.util.List;

import io.p13i.glassnotes.datastores.github.client.models.GitHubAPIRepoItem;
import io.p13i.glassnotes.datastores.github.client.models.GithubAPIRepoItemCreateOrUpdateRequestBody;
import io.p13i.glassnotes.datastores.github.client.models.GithubAPIRepoItemCreateOrUpdateResponse;
import io.p13i.glassnotes.datastores.github.client.models.GithubAPIRepoItemDeleteRequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface GitHubClient {
    @PUT("/repos/{owner}/{repo}/contents/{path}")
    Call<GithubAPIRepoItemCreateOrUpdateResponse> createOrUpdateFile(@Path("owner") String owner, @Path("repo") String repo, @Path("path") String path, @Body GithubAPIRepoItemCreateOrUpdateRequestBody requestBody);

    @GET("/repos/{owner}/{repo}/contents/{path}")
    Call<List<GitHubAPIRepoItem>> getContents(@Path("owner") String owner, @Path("repo") String repo, @Path("path") String path);

    @GET("/repos/{owner}/{repo}/contents/{path}")
    Call<GitHubAPIRepoItem> getContent(@Path("owner") String owner, @Path("repo") String repo, @Path("path") String path);

    @HTTP(method = "DELETE", path = "/repos/{owner}/{repo}/contents/{path}", hasBody = true)
    Call<Void> deleteFile(@Path("owner") String owner, @Path("repo") String repo, @Path("path") String path, @Body GithubAPIRepoItemDeleteRequestBody requestBody);
}
