package io.p13i.glassnotes.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

import io.p13i.glassnotes.datastores.github.File;
import io.p13i.glassnotes.datastores.github.Gist;
import io.p13i.glassnotes.utilities.DateUtilities;

public class Note implements Serializable {
    public static final String EXTRA_TAG = Note.class.getName();
    public static final String DEFAULT_CONTENT = "- ";

    @SerializedName("id")
    private String mId;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("mContent")
    private String mContent;

    @SerializedName("created_at")
    private String mCreatedAt;

    @SerializedName("updated_at")
    private String mUpdatedAt;

    public Note(String title) {
        this(null, title, DEFAULT_CONTENT, DateUtilities.timestamp(), DateUtilities.timestamp());
    }

    public Note(String id, String title, String content, String createdAt, String updatedAt) {
        mId = id;
        mTitle = title;
        mContent = content;
        mCreatedAt = createdAt;
        mUpdatedAt = updatedAt;
    }

    public String getId() {
        return this.mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setContent(String contents) {
        this.mContent = contents;
    }

    public Gist asGist() {
        return new Gist() {{
            mId = Note.this.mId;
            mFiles = new HashMap<String, File>() {{
                put(mTitle, new File() {{
                    mFilename = Note.this.mTitle;
                    mContent = Note.this.mContent;
                }});
            }};
            mCreatedAt = Note.this.mCreatedAt;
            mUpdatedAt = Note.this.mUpdatedAt;
        }};
    }

    @Override
    public String toString() {
        return this.mTitle;
    }
}
