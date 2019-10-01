package io.p13i.glassnotes.datastores.github;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.UnknownHostException;
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

    private static final String TAG = GithubRepoAPIGlassNotesDataStore.class.getName();
    private final GitHubClient mGitHubAPIClient;
    private String owner;
    private String repo;

    public GithubRepoAPIGlassNotesDataStore(String owner, String repo, String githubOAuthToken) {
        this.owner = owner;
        this.repo = repo;
        this.mGitHubAPIClient = ClientFactory.getGitHubClient(githubOAuthToken);
    }

    @Override
    public String getName() {
        return "GitHub Repo";
    }

    @Override
    public void createNote(String path, final Promise<Note> promise) {
        Log.i(TAG, "Creating note at path " + path);
        createOrUpdateNote(true, path, "", null, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                Log.i(TAG, "Promise resolved");
                promise.resolved(data);
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Promise rejected", t);
                promise.rejected(t);
            }
        });
    }

    private void createOrUpdateNote(final boolean isCreate, final String path, final String content, final String priorContentsSha, final Promise<Note> promise) {
        String base64Encoded = StringUtilities.base64EncodeToString(content);
        Log.i(TAG, "Saving note:\n" + "path: " + path + "\ncontent: " + content + "\nprior sha: " + priorContentsSha);
        mGitHubAPIClient.createOrUpdateFile(owner, repo, path, new GithubAPIRepoItemCreateOrUpdateRequestBody(
                /* message: */ (isCreate ? "create" : "update") + " note :: " + path,
                /* base64EncodedContent: */ base64Encoded,
                /* priorContentsSha: */ priorContentsSha
        )).enqueue(new Callback<GithubAPIRepoItemCreateOrUpdateResponse>() {
            @Override
            public void onResponse(Call<GithubAPIRepoItemCreateOrUpdateResponse> call, retrofit2.Response<GithubAPIRepoItemCreateOrUpdateResponse> response) {
                GithubAPIRepoItemCreateOrUpdateResponse createOrUpdateResponse = response.body();
                if (createOrUpdateResponse == null) {
                    Throwable t = new GlassNotesDataStoreException(response);
                    Log.e(TAG, "Failed to create or update note at path " + path, t);
                    promise.rejected(t);
                    return;
                }
                Log.i(TAG, "Created or updated note at path " + path);
                promise.resolved(new Note(createOrUpdateResponse.mContent.mPath, createOrUpdateResponse.mContent.mFilename, content, createOrUpdateResponse.mContent.mSha));
            }

            @Override
            public void onFailure(Call<GithubAPIRepoItemCreateOrUpdateResponse> call, Throwable t) {
                if (t instanceof UnknownHostException) {
                    Log.e(TAG, "Failed to create or update note at path " + path + " due to:\n" + t.toString());
                } else {
                    Log.e(TAG, "Failed to create or update note at path " + path, t);
                }
                promise.rejected(t);
            }
        });
    }

    @Override
    public void getNotes(final Promise<List<Note>> promise) {
        final String path = "";
        mGitHubAPIClient.getContents(owner, repo, path).enqueue(new Callback<List<GitHubAPIRepoItem>>() {
            @Override
            public void onResponse(Call<List<GitHubAPIRepoItem>> call, retrofit2.Response<List<GitHubAPIRepoItem>> response) {
                List<GitHubAPIRepoItem> repoItems = response.body();

                List<Note> notes = new ArrayList<Note>(repoItems.size());

                for (GitHubAPIRepoItem repoItem : repoItems) {
                    if (repoItem.mItemType.equals("file")) {
                        if (repoItem.mFilename.endsWith(Note.MARKDOWN_EXTENSION)) {
                            notes.add(new Note(repoItem.mPath,
                                    repoItem.mFilename,
                                    StringUtilities.base64Decode(repoItem.mBase64EncodedContent),
                                    repoItem.mSha));
                        }
                    } else if (repoItem.mItemType.equals("dir")) {
                        Log.i(TAG, "Encountered directory " + repoItem.mFilename);
                    }
                }

                Log.i(TAG, "Got " + notes.size() + " notes from API at path " + path);
                promise.resolved(notes);
            }

            @Override
            public void onFailure(Call<List<GitHubAPIRepoItem>> call, Throwable t) {
                Log.e(TAG, "Failed to get notes from API at path " + path, t);
                promise.rejected(t);
            }
        });
    }

    @Override
    public void getNote(final String path, final Promise<Note> promise) {
        mGitHubAPIClient.getContent(owner, repo, path).enqueue(new Callback<GitHubAPIRepoItem>() {
            @Override
            public void onResponse(Call<GitHubAPIRepoItem> call, retrofit2.Response<GitHubAPIRepoItem> response) {
                GitHubAPIRepoItem item = response.body();
                if (item == null) {
                    Throwable t = new GlassNotesDataStoreException("Github response body was null");
                    Log.e(TAG, "Failed to get note at path " + path);
                    promise.rejected(t);
                    return;
                }

                Log.i(TAG, "Got note from API at path " + path);
                promise.resolved(new Note(item.mPath, item.mFilename, StringUtilities.base64Decode(item.mBase64EncodedContent), item.mSha));
            }

            @Override
            public void onFailure(Call<GitHubAPIRepoItem> call, Throwable throwable) {
                Log.e(TAG, "Failed to get note at path " + path, throwable);
                promise.rejected(throwable);
            }
        });
    }

    @Override
    public void saveNote(Note note, Promise<Note> promise) {
        Log.i(TAG, "Saving note at path " + note.getAbsoluteResourcePath());
        createOrUpdateNote(false, note.getAbsoluteResourcePath(), note.getContent(), note.getSha(), promise);
    }

    @Override
    public void deleteNote(final Note note, final Promise<Boolean> promise) {
        Log.i(TAG, "Delete note at path " + note.getAbsoluteResourcePath());
        mGitHubAPIClient.deleteFile(owner,  repo,  note.getAbsoluteResourcePath(),
                new GithubAPIRepoItemDeleteRequestBody(
                        /* message: */ "delete note :: " + note.getAbsoluteResourcePath(),
                        /* sha: */ note.getSha()
                ))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                        Log.i(TAG, "Deleted note at path " + note.getAbsoluteResourcePath());
                        promise.resolved(true);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Failed to delete note at path " + note.getAbsoluteResourcePath(), t);
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
