package io.p13i.glassnotes.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.p13i.glassnotes.R;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.github.GlassNotesGitHubAPIClient;
import io.p13i.glassnotes.ui.StatusTextView;
import io.p13i.glassnotes.user.Preferences;
import io.p13i.glassnotes.utilities.DateUtilities;
import io.p13i.glassnotes.utilities.LimitedViewItemManager;
import io.p13i.glassnotes.utilities.SelectableTextViewsManager;


public class MainActivity extends GlassNotesActivity implements
        SelectableTextViewsManager.OnTextViewSelectedListener {

    private final static String TAG = MainActivity.class.getName();

    @BindView(R.id.activity_edit_status)
    StatusTextView mStatusTextView;
    @BindView(R.id.activity_main_layout)
    LinearLayout mLinearLayout;
    GestureDetector mGestureDetector;
    SelectableTextViewsManager mSelectableTextViewsManager;
    GlassNotesDataStore mGlassNotesDataStore;
    LimitedViewItemManager<Note> mLimitedViewItemManager;
    List<Note> mNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // full screen, no app bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Bind to views
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Key the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGlassNotesDataStore = Preferences.getUserPreferredDataStore(this);

        mGestureDetector = createGestureDetector(this);

        // So that keyboard entry will be registered
        mLinearLayout.setFocusable(true);

        // Add views
        populateLayout();

        // Fetch from the data store and load into the UI
        reloadNotes();

        // Set status elements
        mStatusTextView.setPageTitle("Welcome to GlassNotes!");
        mStatusTextView.setStatus(mGlassNotesDataStore.getShortName());
    }

    void reloadNotes() {
        clearNotesFromView();

        mGlassNotesDataStore.getNotes(new GlassNotesGitHubAPIClient.Promise<List<Note>>() {
            @Override
            public void resolved(List<Note> data) {
                mNotes = data;
                mLimitedViewItemManager = new LimitedViewItemManager<Note>(mNotes, /* maximumCount: */5);
                setVisibleNotes();
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to get notes.", t);
            }
        });
    }

    void clearNotesFromView() {
        // Remove all children after the arrows
        while (mLinearLayout.getChildCount() > 7) {
            mSelectableTextViewsManager.removeViewChildAtIndex(7);
        }
    }

    void setVisibleNotes() {
        // Populate again
        List<Note> visibleNotes = mLimitedViewItemManager.getVisibleItems();
        for (final Note note : visibleNotes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSelectableTextViewsManager.addManagedTextViewChild(new TextView(MainActivity.this) {{
                        setId(View.generateViewId());
                        setText(note.getTitle());
                        setTextViewCommonStyles(MainActivity.this, this);
                    }});
                }
            });
        }
    }

    static void setTextViewCommonStyles(Context context, TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
    }

    void populateLayout() {
        // Add the controls
        mSelectableTextViewsManager = new SelectableTextViewsManager(mLinearLayout, this) {{
            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.create_new_note);
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.add_new_todo);
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("");
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.add_existing);
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("▲");
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("▼");
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            init();
        }};

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mSelectableTextViewsManager.handleDownRequest()) {
                    playSound(Sounds.TAP);
                    return true;
                } else {
                    playSound(Sounds.ERROR);
                    return false;
                }
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mSelectableTextViewsManager.handleUpRequest()) {
                    playSound(Sounds.TAP);
                    return true;
                } else {
                    playSound(Sounds.ERROR);
                    return false;
                }
            case KeyEvent.KEYCODE_ENTER:
                if (mSelectableTextViewsManager.handleEnter()) {
                    playSound(Sounds.SUCCESS);
                    return true;
                } else {
                    playSound(Sounds.ERROR);
                    return false;
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    Note getNoteWithTitle(String title) {
        for (Note note : mNotes) {
            if (note.getTitle().equals(title)) {
                return note;
            }
        }
        return null;
    }

    @Override
    public boolean onTextViewSelected(TextView textView) {
        String selectedText = textView.getText().toString();
        if (selectedText.equals(getResources().getString(R.string.create_new_note))) {
            Date now = DateUtilities.now();
            String title = DateUtilities.formatDate(now, "yyyy-MM-dd") + " | " + DateUtilities.formatDate(now, "HH:mm:ss") + " | New note";
            startEditActivityForNewNote(title);
            return true;
        } else if (selectedText.equals(getResources().getString(R.string.add_new_todo))) {
            Date now = DateUtilities.now();
            String title = DateUtilities.formatDate(now, "yyyy-MM-dd") + " | " + DateUtilities.formatDate(now, "HH:mm:ss") + " | New TODO";
            startEditActivityForNewNote(title);
            return true;
        } else if (selectedText.equals(getResources().getString(R.string.add_existing))) {
            reloadNotes();
            return true;
        } else if (selectedText.equals("▲")) {
            // Scroll up
            mLimitedViewItemManager.scrollUp();
            clearNotesFromView();
            setVisibleNotes();
            return true;
        } else if (selectedText.equals("▼")) {
            // Scroll down
            mLimitedViewItemManager.scrollDown();
            clearNotesFromView();
            setVisibleNotes();
            return true;
        } else if (getNoteWithTitle(selectedText) != null) {
            startEditActivityForNote(getNoteWithTitle(selectedText));
            return true;
        }

        return false;
    }

    void startEditActivityForNewNote(String title) {
        Note note = new Note(null, title, Note.DEFAULT_CONTENT, DateUtilities.timestamp(), DateUtilities.timestamp());
        mGlassNotesDataStore.createNote(note, new GlassNotesDataStore.Promise<Note>() {
            @Override
            public void resolved(Note data) {
                startEditActivityForNote(data);
            }

            @Override
            public void rejected(Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    void startEditActivityForNote(Note note) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(Note.EXTRA_TAG, note);
        startActivity(intent);
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    // do something on tap
                    mSelectableTextViewsManager.handleEnter();
                    playSound(Sounds.SUCCESS);
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    if (mSelectableTextViewsManager.handleUpRequest()) {
                        playSound(Sounds.TAP);
                    } else {
                        playSound(Sounds.ERROR);
                    }
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    // do something on left (backwards) swipe
                    if (mSelectableTextViewsManager.handleDownRequest()) {
                        playSound(Sounds.TAP);
                    } else {
                        playSound(Sounds.ERROR);
                    }
                    return true;
                }
                return false;
            }
        });
        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
                // do something on finger count changes
            }
        });
        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                // do something on scrolling
                return true;
            }
        });
        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

}