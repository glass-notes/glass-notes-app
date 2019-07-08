package io.p13i.glassnotes.datastores.github;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GitHubClient {
    public static final String AUTHORIZATION_HEADER = "Authorization: token bb49398803d7511b43b5cf3123a654ce784b34c0";

    @Headers(AUTHORIZATION_HEADER)
    @GET("/gists")
    Call<List<Gist>> getGists();

    @Headers(AUTHORIZATION_HEADER)
    @POST("/gists")
    Call<Gist> postGist(@Body Gist gist);

    @Headers(AUTHORIZATION_HEADER)
    @GET("/gists/{gistId}")
    Call<Gist> getGist(@Path("gistId") String gistId);

    @Headers(AUTHORIZATION_HEADER)
    @PATCH("/gists/{gistId}")
    Call<ResponseBody> patchGist(@Path("gistId") String gistId, @Body Gist gist);

}
