package io.p13i.glassnotes;

import java.io.Serializable;

public class Note implements Serializable {
    public static final String EXTRA_TAG = Note.class.getName();

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
}
