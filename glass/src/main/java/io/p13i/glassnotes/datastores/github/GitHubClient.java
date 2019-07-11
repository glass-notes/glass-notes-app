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
    @GET("/gists")
    Call<List<Gist>> getGists();

    @POST("/gists")
    Call<Gist> postGist(@Body Gist gist);

    @GET("/gists/{gistId}")
    Call<Gist> getGist(@Path("gistId") String gistId);

    @PATCH("/gists/{gistId}")
    Call<Note> patchGist(@Path("gistId") String gistId, @Body Gist gist);

}
