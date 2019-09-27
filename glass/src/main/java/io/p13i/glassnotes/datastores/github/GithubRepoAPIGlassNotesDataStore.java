package io.p13i.glassnotes.datastores.github;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.GlassNotesDataStoreException;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.datastores.github.client.GitHubClient;
import io.p13i.glassnotes.datastores.github.client.TLSSocketFactory;
import io.p13i.glassnotes.datastores.github.client.models.GitHubAPIRepoItem;
import io.p13i.glassnotes.datastores.github.client.models.GithubAPIRepoItemCreateOrUpdateRequestBody;
import io.p13i.glassnotes.datastores.github.client.models.GithubAPIRepoItemCreateOrUpdateResponse;
import io.p13i.glassnotes.datastores.github.client.models.GithubAPIRepoItemDeleteRequestBody;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.utilities.StringUtilities;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GithubRepoAPIGlassNotesDataStore implements GlassNotesDataStore<Note> {

    private final GitHubClient mGitHubAPIClient;
    private String owner;
    private String repo;

    public GithubRepoAPIGlassNotesDataStore(String owner, String repo, String githubOAuthToken) {
        this.owner = owner;
        this.repo = repo;
        this.mGitHubAPIClient = ClientFactory.getGitHubClient(githubOAuthToken);
    }

    @Override
    public void createNote(String path, final Promise<Note> promise) {
        createOrUpdateNote(path, "", promise);
    }

    private void createOrUpdateNote(String path, final String content, final Promise<Note> promise) {
        mGitHubAPIClient.createOrUpdateFile(owner, repo, path, new GithubAPIRepoItemCreateOrUpdateRequestBody(
                /* message: */ "create note :: " + path,
                /* base64EncodedContent: */ StringUtilities.base64EncodeToString(content),
                /* sha: */ StringUtilities.sha(content)
        )).enqueue(new Callback<GithubAPIRepoItemCreateOrUpdateResponse>() {
            @Override
            public void onResponse(Call<GithubAPIRepoItemCreateOrUpdateResponse> call, retrofit2.Response<GithubAPIRepoItemCreateOrUpdateResponse> response) {
                GithubAPIRepoItemCreateOrUpdateResponse createOrUpdateResponse = response.body();
                if (createOrUpdateResponse == null) {
                    promise.rejected(new GlassNotesDataStoreException("Response body was null"));
                    return;
                }
                promise.resolved(new Note(createOrUpdateResponse.mContent.mPath, createOrUpdateResponse.mContent.mFilename, content));
            }

            @Override
            public void onFailure(Call<GithubAPIRepoItemCreateOrUpdateResponse> call, Throwable t) {
                promise.rejected(t);
            }
        });
    }

    @Override
    public void getNotes(final Promise<List<Note>> promise) {
        mGitHubAPIClient.getContents(owner, repo, "").enqueue(new Callback<List<GitHubAPIRepoItem>>() {
            @Override
            public void onResponse(Call<List<GitHubAPIRepoItem>> call, retrofit2.Response<List<GitHubAPIRepoItem>> response) {
                List<GitHubAPIRepoItem> repoItems = response.body();

                List<Note> notes = new ArrayList<Note>(repoItems.size());

                for (GitHubAPIRepoItem repoItem : repoItems) {
                    if (repoItem.mFilename.endsWith(Note.MARKDOWN_EXTENSION)) {
                        notes.add(new Note(repoItem.mPath, repoItem.mFilename, null));
                    }
                }

                promise.resolved(notes);
            }

            @Override
            public void onFailure(Call<List<GitHubAPIRepoItem>> call, Throwable t) {
                promise.rejected(t);
            }
        });
    }

    @Override
    public void getNote(String path, final Promise<Note> promise) {
        mGitHubAPIClient.getContent(owner, repo, path).enqueue(new Callback<GitHubAPIRepoItem>() {
            @Override
            public void onResponse(Call<GitHubAPIRepoItem> call, retrofit2.Response<GitHubAPIRepoItem> response) {
                GitHubAPIRepoItem item = response.body();
                promise.resolved(new Note(item.mPath, item.mFilename, StringUtilities.base64Decode(item.mBase64EncodedContent)));
            }

            @Override
            public void onFailure(Call<GitHubAPIRepoItem> call, Throwable throwable) {
                promise.rejected(throwable);
            }
        });
    }

    @Override
    public void saveNote(Note note, Promise<Note> promise) {
        createOrUpdateNote(note.getAbsoluteResourcePath(), note.getContent(), promise);
    }

    @Override
    public void deleteNote(Note note, final Promise<Boolean> promise) {
        mGitHubAPIClient.deleteFile(
                owner,  repo,  note.getAbsoluteResourcePath(),
                new GithubAPIRepoItemDeleteRequestBody(
                        "delete note :: " + note.getAbsoluteResourcePath(),
                        StringUtilities.sha(note.getContent())
                ))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                        promise.resolved(true);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        promise.rejected(t);
                    }
                });
    }


    /**
     * Support class for GitHub API
     */
    public static class ClientFactory {
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
}
