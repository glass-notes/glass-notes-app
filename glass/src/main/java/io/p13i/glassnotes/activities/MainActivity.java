package io.p13i.glassnotes.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.p13i.glassnotes.R;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.ui.StatusTextView;
import io.p13i.glassnotes.user.PreferenceManager;
import io.p13i.glassnotes.utilities.DateUtilities;
import io.p13i.glassnotes.utilities.LimitedViewItemManager;
import io.p13i.glassnotes.utilities.SelectableTextViewsManager;


public class MainActivity extends GlassNotesActivity implements
        SelectableTextViewsManager.OnTextViewSelectedListener {

    private final static String TAG = MainActivity.class.getName();

    /**
     * The maximum visible notes on the screen
     */
    private static final int MAX_VISIBLE_NOTES = 3;

    /**
     * Used to get results read the QR-code reading activity
     */
    private static final int SETTINGS_QR_CODE_READER_RESULT_CODE = 0xc0de;

    @BindView(R.id.activity_edit_status)
    StatusTextView mStatusTextView;
    @BindView(R.id.activity_main_layout)
    LinearLayout mLinearLayout;

    /**
     * Used to handle swipe gestures
     */
    GestureDetector mGestureDetector;

    /**
     * Manages the select-ability of the text views in the terminal-like GUI
     */
    SelectableTextViewsManager mSelectableTextViewsManager;

    /**
     * The refresh button
     */
    private TextView mRefreshTextView;

    /**
     * Manages the limited view window of notes visible in the GUI
     */
    LimitedViewItemManager<Note> mLimitedViewItemManager;

    /**
     * All the notes in this data-store
     */
    List<Note> mNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind to views
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mGestureDetector = createGestureDetector(this);

        // So that keyboard entry will be registered
        mLinearLayout.setFocusable(true);
        mLinearLayout.requestFocus();

        // Add views
        populateLayout();

        // Try to load the user's preferences read the disk
        if (PreferenceManager.getInstance().loadFromSystem(this)) {
            Toast.makeText(this, "Loaded preferences read disk", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to load preferences read disk", Toast.LENGTH_SHORT).show();
        }

        PreferenceManager.getInstance().init(this);

        // Fetch read the data store and load into the UI
        reloadNotes();
    }

    /**
     * Adds elements to the GUI
     */
    private void populateLayout() {
        // Add the controls
        mSelectableTextViewsManager = new SelectableTextViewsManager(mLinearLayout, this) {

            {
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

            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.load_settings);
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            mRefreshTextView = addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.refresh);
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

    /**
     * Reloads all notes read the data store
     */
    private void reloadNotes() {
        clearNotesFromView();

        String wifiName = getWifiName(this);
        if (wifiName == null) {
            Toast.makeText(this, "Not connected to Wi-Fi", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Connected to " + wifiName, Toast.LENGTH_SHORT).show();
        }

        String newStatus = PreferenceManager.getInstance().getDataStore().getName();
        mStatusTextView.setStatus(newStatus);

        // Get the notes read the data store
        PreferenceManager.getInstance().getDataStore().getNotes(new Promise<List<Note>>() {
            @Override
            public void resolved(List<Note> data) {
                // Set status elements
                mStatusTextView.setPageTitle("Welcome to GlassNotes!");

                mNotes = data;
                // Display the notes in the GUI
                mLimitedViewItemManager = new LimitedViewItemManager<Note>(mNotes,
                        /* maximumCount: */MAX_VISIBLE_NOTES);

                mSelectableTextViewsManager.init();
                mSelectableTextViewsManager.setSelectedTextView(mRefreshTextView);

                setVisibleNotes();
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to get notes.", t);
                mStatusTextView.setPageTitle("Failed to get notes.");
            }
        });
    }

    /**
     * Removes all the notes read the view after the arrow selectors
     */
    private void clearNotesFromView() {
        final int INDEX_AFTER_ARROWS = 7;
        // Remove all children after the arrows
        while (mLinearLayout.getChildCount() > INDEX_AFTER_ARROWS) {
            mSelectableTextViewsManager.removeViewChildAtIndex(INDEX_AFTER_ARROWS);
        }
    }

    /**
     * Adds the visible notes back into the GUI
     */
    private void setVisibleNotes() {
        // Populate again
        List<Note> visibleNotes = mLimitedViewItemManager.getVisibleItems();
        for (final Note note : visibleNotes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSelectableTextViewsManager.addManagedTextViewChild(new TextView(MainActivity.this) {{
                        setId(View.generateViewId());
                        setText(note.getFilename().replace(Note.MARKDOWN_EXTENSION, ""));
                        setTextViewCommonStyles(MainActivity.this, this);
                    }});
                }
            });
        }
    }

    /**
     * Sets the terminal-like style for a text view
     * @param context the activity context
     * @param textView the text view to update styles for
     */
    private static void setTextViewCommonStyles(Context context, TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (event.isCtrlPressed()) {
            if (keyCode == KeyEvent.KEYCODE_Q) {
                startQRCodeActivityToGetPreferences();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                // ctrl-backspace is to delete this note
                deleteSelectedNote();
                return true;
            }
        }

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
                if (mSelectableTextViewsManager.handleTap(Gesture.TAP)) {
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

    private void deleteSelectedNote() {
        String noteTitle = mSelectableTextViewsManager.getSelectedTextView().getText().toString();
        Note note = getNoteWithTitle(noteTitle);
        if (note == null) {
            Log.w(TAG, "Didn't find note with title " + noteTitle);
            return;
        }
        PreferenceManager.getInstance().getDataStore().deleteNote(note, new Promise() {
            @Override
            public void resolved(Object data) {
                reloadNotes();
                playSound(Sounds.SUCCESS);
            }

            @Override
            public void rejected(Throwable t) {
                playSound(Sounds.ERROR);
            }
        });
    }

    private void startQRCodeActivityToGetPreferences() {
        Intent intent = new Intent(this, QRCodeReaderActivity.class);
        startActivityForResult(intent, SETTINGS_QR_CODE_READER_RESULT_CODE);
    }

    /**
     * @param title the title
     * @return a note with the given title
     */
    private Note getNoteWithTitle(String title) {
        for (Note note : mNotes) {
            if (note.getFilename().equals(title + Note.MARKDOWN_EXTENSION)) {
                return note;
            }
        }
        return null;
    }

    @Override
    public boolean onTextViewSelected(TextView textView, Gesture gesture) {
        String selectedText = textView.getText().toString();
        if (selectedText.equals(getResources().getString(R.string.create_new_note))) {
            String title = generateNewNoteFilename("note");
            startEditActivityForNewNoteWithPath(title + Note.MARKDOWN_EXTENSION);
            return true;

        } else if (selectedText.equals(getResources().getString(R.string.add_new_todo))) {
            String title = generateNewNoteFilename("TODO");
            startEditActivityForNewNoteWithPath(title + Note.MARKDOWN_EXTENSION);
            return true;

        } else if (selectedText.equals(getResources().getString(R.string.load_settings))) {
            startQRCodeActivityToGetPreferences();
            return true;

        }  else if (selectedText.equals(getResources().getString(R.string.refresh))) {
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
            if (gesture == Gesture.TAP) {
                startEditActivityForNote(getNoteWithTitle(selectedText));
            } else if (gesture == Gesture.LONG_PRESS) {
                startPresentationActivityForNote(getNoteWithTitle(selectedText));
            }
            return true;
        }

        return false;
    }

    private static String generateNewNoteFilename(String noteType) {
        Date now = DateUtilities.now();
        return DateUtilities.formatDate(now, "yyyy-MM-dd") + " " + DateUtilities.formatDate(now, "HH:mm:ss") + " New " + noteType;
    }

    private void startEditActivityForNewNoteWithPath(String path) {
        PreferenceManager.getInstance().getDataStore().createNote(path, new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                startEditActivityForNote(data);
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to create note", t);
            }
        });
    }

    private void startEditActivityForNote(Note note) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(Note.EXTRA_TAG, note);
        startActivity(intent);
    }

    private void startPresentationActivityForNote(Note note) {
        Intent intent = new Intent(this, PresentationActivity.class);
        intent.putExtra(Note.EXTRA_TAG, note);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_QR_CODE_READER_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                String qrCodeData = data.getStringExtra(QRCodeReaderActivity.INTENT_RESULT_KEY);
                if (PreferenceManager.getInstance().setFromJsonString(getApplicationContext(), qrCodeData)) {

                    Log.i(TAG, "Saved read preferences");

                    if (PreferenceManager.getInstance().saveToSystem(getApplicationContext())) {
                        Toast.makeText(this, "Saved preferences to system", Toast.LENGTH_SHORT).show();
                        playSound(Sounds.SUCCESS);
                        reloadNotes();
                    }

                } else {
                    Log.e(TAG, "Failed to save read preferences");
                    playSound(Sounds.ERROR);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private GestureDetector createGestureDetector(Context context) {
        return new GestureDetector(context) {{
            setBaseListener(new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    if (gesture == Gesture.TAP) {
                        // do something on tap
                        mSelectableTextViewsManager.handleTap(gesture);
                        playSound(Sounds.SUCCESS);
                        return true;
                    } else if (gesture == Gesture.SWIPE_LEFT) {
                        if (mSelectableTextViewsManager.handleUpRequest()) {
                            playSound(Sounds.TAP);
                            return true;
                        } else {
                            playSound(Sounds.ERROR);
                            return false;
                        }
                    } else if (gesture == Gesture.SWIPE_RIGHT) {
                        // do something on left (backwards) swipe
                        if (mSelectableTextViewsManager.handleDownRequest()) {
                            playSound(Sounds.TAP);
                            return true;
                        } else {
                            playSound(Sounds.ERROR);
                            return false;
                        }
                    } else if (gesture == Gesture.LONG_PRESS) {
                        mSelectableTextViewsManager.handleTap(gesture);
                        return true;
                    }
                    return false;
                }
            });
        }};
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

    /**
     * Source https://stackoverflow.com/a/24326948/5071723
     * @param context
     * @return
     */
    public String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return null;
    }


}