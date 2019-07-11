package io.p13i.glassnotes.datastores.github;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientFactory {
    public static OkHttpClient getOkHttpClientWithAuthorizationHeader(final String authorizationHeaderValue) {

        OkHttpClient httpClient = new OkHttpClient();
        try {
            httpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(new TLSSocketFactory())
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request()
                                    .newBuilder()
                                    .addHeader("Authorization", authorizationHeaderValue).build();
                            return chain.proceed(request);
                        }
                    })
                    .build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return httpClient;
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Gist.class, new JsonSerializer<Gist>() {
                    @Override
                    public JsonElement serialize(Gist src, Type typeOfSrc, JsonSerializationContext context) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("id", src.mId);
                        obj.add("files", new JsonObject());
                        for (Map.Entry<String, File> entry : src.mFiles.entrySet()) {
                            JsonObject fileObj = new JsonObject();
                            fileObj.addProperty("filename", entry.getValue().mFilename);
                            fileObj.addProperty("content", entry.getValue().mContent);
                            obj.getAsJsonObject("files").add(entry.getKey(), fileObj);
                        }
                        return obj;
                    }
                })
                .create();
    }

    public static GitHubClient getGitHubClient(String authorizationToken) {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .client(getOkHttpClientWithAuthorizationHeader("token " + authorizationToken))
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build()
                .create(GitHubClient.class);
    }
}
