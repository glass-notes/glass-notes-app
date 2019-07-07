package io.p13i.glassnotes.API;

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
    public static OkHttpClient getOkHttpClient() {

        OkHttpClient httpClient = new OkHttpClient();
        try {
            httpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(new TLSSocketFactory())
                    .build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return httpClient;
    }

    public static GitHubClient getGitHubClient() {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .registerTypeAdapter(Gist.class, new JsonSerializer<Gist>() {
                            @Override
                            public JsonElement serialize(Gist src, Type typeOfSrc, JsonSerializationContext context) {
                                JsonObject obj = new JsonObject();
                                obj.addProperty("id", src.id);
                                obj.add("files", new JsonObject());
                                for (Map.Entry<String, File> entry : src.files.entrySet()) {
                                    JsonObject fileObj = new JsonObject();
                                    fileObj.addProperty("filename", entry.getValue().filename);
                                    fileObj.addProperty("content", entry.getValue().content);
                                    obj.getAsJsonObject("files").add(entry.getKey(), fileObj);
                                }
                                return obj;
                            }
                        })
                        .create()))
                .build()
                .create(GitHubClient.class);
    }
}
