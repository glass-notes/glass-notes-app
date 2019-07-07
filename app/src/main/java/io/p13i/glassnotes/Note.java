package io.p13i.glassnotes;

import java.io.Serializable;

public class Note implements Serializable {
    public static final String EXTRA_TAG = Note.class.getName();

    String mTitle;
    String mContents;

    public Note(String title) {
        this(title, "");
    }

    public Note(String title, String contents) {
        mTitle = title;
        mContents = contents;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContents() {
        return mContents;
    }

    public void setContents(String contents) {
        mContents = contents;
    }
}
