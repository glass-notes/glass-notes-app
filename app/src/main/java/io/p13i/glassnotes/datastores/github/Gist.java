package io.p13i.glassnotes.datastores.github;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.p13i.glassnotes.models.Note;

public class Gist implements Serializable {
    @SerializedName("id")
    public String mId;
    @SerializedName("created_at")
    public String mCreatedAt;
    @SerializedName("updated_at")
    public String mUpdatedAt;
    @SerializedName("files")
    public Map<String, File> mFiles;

    public File getFirstFile() {
        List<String> filenames = new ArrayList<>(mFiles.keySet());
        return mFiles.get(filenames.get(0));
    }

    public Note asNote() {
        if (mFiles.keySet().size() == 1) {
            File firstFile = getFirstFile();
            return new Note(mId, firstFile.mFilename, firstFile.mContent, mCreatedAt, mUpdatedAt);
        }
        return null;
    }

    public List<Note> asNotes() {
        List<Note> notes = new ArrayList<>();
        for (Map.Entry<String, File> kv : mFiles.entrySet()) {
            notes.add(new Note(mId, kv.getValue().mFilename, kv.getValue().mContent, mCreatedAt, mUpdatedAt));
        }
        return notes;
    }
}
