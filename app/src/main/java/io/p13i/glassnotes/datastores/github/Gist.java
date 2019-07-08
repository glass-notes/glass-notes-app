package io.p13i.glassnotes.datastores.github;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.p13i.glassnotes.models.Note;

public class Gist implements Serializable {
    @SerializedName("id")
    public String id;
    @SerializedName("files")
    public Map<String, File> files;

    public File getFirstFile() {
        List<String> filenames = new ArrayList<>(files.keySet());
        return files.get(filenames.get(0));
    }

    public Note asNote() {
        if (files.keySet().size() == 1) {
            File firstFile = getFirstFile();
            if (firstFile.filename.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*")) {
                return new Note(id, firstFile.filename, firstFile.content);
            }
        }
        return null;
    }
}
