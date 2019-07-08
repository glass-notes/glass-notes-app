package io.p13i.glassnotes.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

import io.p13i.glassnotes.datastores.github.File;
import io.p13i.glassnotes.datastores.github.Gist;

public class Note implements Serializable {
    public static final String EXTRA_TAG = Note.class.getName();
    public static final String DEFAULT_CONTENT = "- ";

    @SerializedName("id")
    String mId;

    @SerializedName("title")
    String mTitle;

    @SerializedName("content")
    String mContent;


    public Note(String id, String title, String content) {
        mId = id;
        mTitle = title;
        mContent = content;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String contents) {
        mContent = contents;
    }

    public Gist asGist() {
        return new Gist() {{
            id = mId;
            files = new HashMap<String, File>() {{
                put(mTitle, new File() {{
                    filename = mTitle;
                    content = mContent;
                }});
            }};
        }};
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
