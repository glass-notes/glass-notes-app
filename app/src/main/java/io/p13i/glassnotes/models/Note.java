package io.p13i.glassnotes.models;

import java.io.Serializable;
import java.util.HashMap;

import io.p13i.glassnotes.datastores.github.File;
import io.p13i.glassnotes.datastores.github.Gist;

public class Note implements Serializable {
    public static final String EXTRA_TAG = Note.class.getName();
    public static final String DEFAULT_CONTENT = "- ";

    String mId;
    String mTitle;
    String mContents;


    public Note(String id, String title, String contents) {
        mId = id;
        mTitle = title;
        mContents = contents;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContents;
    }

    public void setContent(String contents) {
        mContents = contents;
    }

    public Gist asGist() {
        return new Gist() {{
            id = mId;
            files = new HashMap<String, File>() {{
                put(mTitle, new File() {{
                    filename = mTitle;
                    content = mContents;
                }});
            }};
        }};
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
