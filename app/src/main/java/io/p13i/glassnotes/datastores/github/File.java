package io.p13i.glassnotes.datastores.github;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class File implements Serializable {
    @SerializedName("filename")
    public String mFilename;

    @SerializedName("size")
    public Integer size;

    @SerializedName("raw_url")
    public String rawUrl;

    @SerializedName("content")
    public String mContent;
}
