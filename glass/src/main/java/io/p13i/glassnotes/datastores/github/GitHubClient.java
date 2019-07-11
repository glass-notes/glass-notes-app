package io.p13i.glassnotes.datastores.github;

import java.util.List;

import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.user.Preferences;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GitHubClient {
    @Headers(Preferences.GITHUB_OAUTH_AUTH_HEADER)
    @GET("/gists")
    Call<List<Gist>> getGists();

    @Headers(Preferences.GITHUB_OAUTH_AUTH_HEADER)
    @POST("/gists")
    Call<Gist> postGist(@Body Gist gist);

    @Headers(Preferences.GITHUB_OAUTH_AUTH_HEADER)
    @GET("/gists/{gistId}")
    Call<Gist> getGist(@Path("gistId") String gistId);

    @Headers(Preferences.GITHUB_OAUTH_AUTH_HEADER)
    @PATCH("/gists/{gistId}")
    Call<Note> patchGist(@Path("gistId") String gistId, @Body Gist gist);

}
