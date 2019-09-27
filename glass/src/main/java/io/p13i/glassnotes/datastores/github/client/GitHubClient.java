package io.p13i.glassnotes.datastores.github.client;

import java.util.List;

import io.p13i.glassnotes.datastores.github.client.models.GitHubAPIRepoItem;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GitHubClient {
//    @GET("/gists")
//    Call<List<GitHubAPIGist>> getGists();
//
//    @POST("/gists")
//    Call<GitHubAPIGist> postGist(@Body GitHubAPIGist gitHubAPIGist);
//
//    @GET("/gists/{gistId}")
//    Call<GitHubAPIGist> getGist(@Path("gistId") String gistId);
//
//    @PATCH("/gists/{gistId}")
//    Call<GitHubAPIGist> patchGist(@Path("gistId") String gistId, @Body GitHubAPIGist gitHubAPIGist);

    @GET("/repos/{owner}/{repo}/contents/{path}")
    Call<List<GitHubAPIRepoItem>> getContents(@Path("owner") String owner, @Path("repo") String repo, @Path("path") String path);

    @GET("/repos/{owner}/{repo}/contents/{path}")
    Call<GitHubAPIRepoItem> getContent(@Path("owner") String owner, @Path("repo") String repo, @Path("path") String path);

}
