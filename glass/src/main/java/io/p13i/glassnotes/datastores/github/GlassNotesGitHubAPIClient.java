package io.p13i.glassnotes.datastores.github;

import java.util.ArrayList;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlassNotesGitHubAPIClient implements GlassNotesDataStore {
    public static final String TAG = GlassNotesGitHubAPIClient.class.getName();

    private GitHubClient mGitHubClient;

    public GlassNotesGitHubAPIClient(String authorizationTokenValue) {
        mGitHubClient = ClientFactory.getGitHubClient( authorizationTokenValue);
    }

    @Override
    public String getShortName() {
        return "GitHub";
    }

    @Override
    public void createNote(Note note, final Promise<Note> promise) {
        mGitHubClient.postGist(note.asGist()).enqueue(new Callback<Gist>() {
            @Override
            public void onResponse(Call<Gist> call, Response<Gist> response) {
                Gist gist = response.body();
                promise.resolved(gist.asNote());
            }

            @Override
            public void onFailure(Call<Gist> call, Throwable t) {
                promise.rejected(t);
            }
        });

    }

    public void getNotes(final Promise<List<Note>> promise) {
        mGitHubClient.getGists().enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                List<Note> notes = new ArrayList<Note>();
                List<Gist> gists = response.body();
                for (Gist gist : gists) {
                    notes.addAll(gist.asNotes());
                }

                promise.resolved(notes);
            }

            @Override
            public void onFailure(Call<List<Gist>> call, Throwable t) {
                promise.rejected(t);
            }
        });

    }

    public void getNote(String id, final Promise<Note> promise) {
        mGitHubClient.getGist(id).enqueue(new Callback<Gist>() {
            @Override
            public void onResponse(Call<Gist> call, Response<Gist> response) {
                Gist gist = response.body();
                File firstFile = gist.getFirstFile();
                promise.resolved(new Note(gist.mId, firstFile.mFilename, firstFile.mContent, gist.mCreatedAt, gist.mUpdatedAt));
            }

            @Override
            public void onFailure(Call<Gist> call, Throwable t) {
                promise.rejected(t);
            }
        });
    }

    public void saveNote(Note note, final Promise<Note> promise) {
        mGitHubClient.patchGist(note.getId(), note.asGist()).enqueue(new Callback<Note>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                promise.resolved(response.body());
            }

            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                promise.rejected(t);
            }
        });
    }
}
