package io.p13i.glassnotes.datastores.github;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Represents a file in a {@code Gist}
 */
public class File implements Serializable {
    @SerializedName("filename")
    protected String mFilename;

    @SerializedName("size")
    protected Integer size;

    @SerializedName("raw_url")
    protected String rawUrl;

    @SerializedName("content")
    protected String mContent;
}
