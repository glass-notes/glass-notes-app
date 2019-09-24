package io.p13i.glassnotes.models;

import com.google.gson.annotations.SerializedName;

import org.kohsuke.github.GHContent;

import java.io.IOException;
import java.io.Serializable;

import io.p13i.glassnotes.utilities.StringUtilities;

public class Note implements Serializable {
    public static final String DEFAULT_CONTENT = "- ";
    public static final String EXTRA_TAG = Note.class.getName();

    @SerializedName("path")
    public String mPath;

    @SerializedName("name")
    public String mName;

    @SerializedName("content")
    public String mContent;

    public Note(String path, String name, String content) {
        mPath = path;
        mName = name;
        mContent = content;
    }

    public String getPath() {
        return mPath;
    }

    public String getName() { return mName; }

    public String getContent() {
        return mContent;
    }

    @Override
    public String toString() {
        return this.mPath;
    }
}
