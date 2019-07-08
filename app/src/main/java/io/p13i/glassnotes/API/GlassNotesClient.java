package io.p13i.glassnotes.API;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.p13i.glassnotes.Note;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlassNotesClient {
    public static final String TAG = GlassNotesClient.class.getName();

    public static void getNotes(Promise<List<Note>> promise) {
        GitHubClient client = ClientFactory.getGitHubClient();
        client.getGists().enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                List<Note> notes = new ArrayList<>();
                List<Gist> gists = response.body();
                for (Gist gist : gists) {
                    if (gist.files.keySet().size() == 1) {
                        File firstFile = gist.getFirstFile();
                        if (firstFile.filename.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*")) {
                            notes.add(new Note(gist.id, firstFile.filename, null));
                        }
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

    public static void getNote(String id, Promise<Note> promise) {
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

    public static void saveNote(Note note, Promise<ResponseBody> promise) {
        GitHubClient client = ClientFactory.getGitHubClient();

        Gist gist = new Gist() {{
            id = note.getId();
            files = new HashMap<String, File>() {{
                put(note.getTitle(), new File() {{
                    filename = note.getTitle();
                    content = note.getContent();
                }});
            }};
        }};


        client.patchGist(gist.id, gist).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                promise.resolved(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                promise.failed(t);
            }
        });
    }


    public interface Promise<T> {
        public void resolved(T data);
        public void failed(Throwable t);
    }
}
