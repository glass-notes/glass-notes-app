package io.p13i.glassnotes.API;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface GitHubClient {
    public static final String AUTHORIZATION_HEADER = "Authorization: token 73807bedca727ef9a136c2dfb5835c0bd99233cd";


    @Headers(AUTHORIZATION_HEADER)
    @GET("/gists")
    Call<List<Gist>> getGists();

    @Headers(AUTHORIZATION_HEADER)
    @GET("/gists/{gistId}")
    Call<Gist> getGist(@Path("gistId") String gistId);

    @Headers(AUTHORIZATION_HEADER)
    @PATCH("/gists/{gistId}")
    Call<ResponseBody> patchGist(@Path("gistId") String gistId, @Body Gist gist);
}
