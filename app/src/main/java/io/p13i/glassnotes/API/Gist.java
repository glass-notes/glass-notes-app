package io.p13i.glassnotes.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Gist implements Serializable {
    @SerializedName("id")
    public String id;
    @SerializedName("files")
    public Map<String, File> files;

    public File getFirstFile() {
        List<String> filenames = new ArrayList<>(files.keySet());
        return files.get(filenames.get(0));
    }
}
