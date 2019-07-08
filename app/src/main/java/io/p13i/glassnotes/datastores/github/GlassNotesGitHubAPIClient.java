package io.p13i.glassnotes.datastores.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.models.Note;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlassNotesGitHubAPIClient implements GlassNotesDataStore {
    public static final String TAG = GlassNotesGitHubAPIClient.class.getName();

    @Override
    public void createNote(Note note, Promise<Note> promise) {
        ClientFactory.getGitHubClient().postGist(note.asGist()).enqueue(new Callback<Gist>() {
            @Override
            public void onResponse(Call<Gist> call, Response<Gist> response) {
                Gist gist = response.body();
                promise.resolved(gist.asNote());
            }

            @Override
            public void onFailure(Call<Gist> call, Throwable t) {
                promise.failed(t);
            }
        });

    }

    public void getNotes(Promise<List<Note>> promise) {
        GitHubClient client = ClientFactory.getGitHubClient();
        client.getGists().enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                List<Note> notes = new ArrayList<>();
                List<Gist> gists = response.body();
                for (Gist gist : gists) {
                    Note note = gist.asNote();
                    if (note != null) {
                        notes.add(note);
                    }
                }

                promise.resolved(notes);
            }

            @Override
            public void onFailure(Call<List<Gist>> call, Throwable t) {
                promise.failed(t);
            }
        });

    }

    public void getNote(String id, Promise<Note> promise) {
        GitHubClient client = ClientFactory.getGitHubClient();
        client.getGist(id).enqueue(new Callback<Gist>() {
            @Override
            public void onResponse(Call<Gist> call, Response<Gist> response) {
                Gist gist = response.body();
                File firstFile = gist.getFirstFile();
                promise.resolved(new Note(gist.id, firstFile.filename, firstFile.content));
            }

            @Override
            public void onFailure(Call<Gist> call, Throwable t) {
                promise.failed(t);
            }
        });
    }

    public void saveNote(Note note, Promise<Note> promise) {
        GitHubClient client = ClientFactory.getGitHubClient();

        client.patchGist(note.getId(), note.asGist()).enqueue(new Callback<Note>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                promise.resolved(response.body());
            }

            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                promise.failed(t);
            }
        });
    }
}
