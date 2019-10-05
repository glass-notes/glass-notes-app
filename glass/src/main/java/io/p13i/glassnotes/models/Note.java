package io.p13i.glassnotes.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Note implements Serializable {
    public static final String MARKDOWN_EXTENSION = ".glass-notes.md";
    public static final String EXTRA_TAG = Note.class.getName();

    @SerializedName("absoluteResourcePath")
    public String mAbsoluteResourcePath;

    @SerializedName("filename")
    public String mFilename;

    @SerializedName("content")
    public String mContent;

    @SerializedName("sha")
    public String mSha;

    public Note(String absoluteResourcePath, String filename, String content, String sha) {
        mAbsoluteResourcePath = absoluteResourcePath;
        mFilename = filename;
        mContent = content;
        mSha = sha;
    }

    public String getAbsoluteResourcePath() {
        return mAbsoluteResourcePath;
    }

    public String getFilename() {
        return mFilename;
    }

    public String getContent() {
        return mContent;
    }

    public String getSha() {
        return mSha;
    }

    @Override
    public String toString() {
        return this.mAbsoluteResourcePath;
    }
}
