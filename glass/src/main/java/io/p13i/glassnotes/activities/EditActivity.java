package io.p13i.glassnotes.activities;


import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.glass.media.Sounds;

import java.util.Timer;
import java.util.TimerTask;

import io.p13i.glassnotes.R;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.github.GlassNotesGitHubAPIClient;
import io.p13i.glassnotes.ui.StatusTextView;
import io.p13i.glassnotes.user.Preferences;
import io.p13i.glassnotes.utilities.DateUtilities;


/**
 * Activity for editing a given Note
 */
public class EditActivity extends GlassNotesActivity {

    public final static String TAG = EditActivity.class.getName();

    StatusTextView mStatusTextView;

    EditText mNoteEditText;

    private Note mNote;
    private Timer mSaveTimer;
    private GlassNotesDataStore mGlassNotesDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen, no app bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_edit);
        mStatusTextView = (StatusTextView) findViewById(R.id.activity_edit_status);
        mNoteEditText = (EditText) findViewById(R.id.note_edit_text);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get the target Note from the activity transition
        mNote = (Note) getIntent().getSerializableExtra(Note.EXTRA_TAG);

        // Disable editing until the file is loaded from the datastore
        mNoteEditText.setEnabled(false);
        mNoteEditText.setText(R.string.activity_edit_loading);
        mNoteEditText.setTextColor(getResources().getColor(R.color.white));

        // Load the Note's mContent from the data store
        mGlassNotesDataStore = Preferences.getUserPreferredDataStore(this);
        mGlassNotesDataStore.getNote(mNote.getId(), new GlassNotesGitHubAPIClient.Promise<Note>() {
            @Override
            public void resolved(final Note data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNote = data;
                        mNoteEditText.setText(mNote.getContent());
                        // Scroll to the end of the file
                        mNoteEditText.setSelection(mNoteEditText.getText().length());
                        // Allow editing
                        mNoteEditText.setEnabled(true);
                        // Save on an interval
                        startSaveTimer();
                    }
                });
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, getString(R.string.error_failed_to_get_note), t);
            }
        });

        // Set some status bar elements
        mStatusTextView.setPageTitle(mNote.getTitle());
        mStatusTextView.setStatus("Welcome!");
    }

    /**
     * Saves the Note to the data store on an interval
     */
    void startSaveTimer() {
        mSaveTimer = new Timer();
        mSaveTimer.schedule(new SaveTimerTask(), /* start after: */ Preferences.SAVE_PERIOD_MS, /* run every: */ Preferences.SAVE_PERIOD_MS);
    }

    private void saveNote(GlassNotesDataStore.Promise<Note> notePromise) {
        // Only update the data store if the contents have changed
        String currentText = mNote.getContent();
        String updatedText = mNoteEditText.getText().toString();
        if (currentText.equals(updatedText)) {
            notePromise.resolved(mNote);
            return;
        }

        // Else, it was updated
        mNote.setContent(mNoteEditText.getText().toString());

        // Run the save task
        mGlassNotesDataStore.saveNote(mNote, notePromise);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.isCtrlPressed()) {
            if (keyCode == KeyEvent.KEYCODE_S) {
                saveNote(new GlassNotesDataStore.Promise<Note>() {
                    @Override
                    public void resolved(Note data) {
                        playSound(Sounds.SUCCESS);
                    }

                    @Override
                    public void rejected(Throwable t) {
                        playSound(Sounds.ERROR);
                    }
                });
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_X) {
                saveAndFinish();
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Saves the note and finishes the activity
     */
    private void saveAndFinish() {
        // Update the UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusTextView.setText(R.string.activity_edit_saving_existing);
                mStatusTextView.invalidate();   // forces update
            }
        });

        // Clear the timer
        mSaveTimer.cancel();
        mSaveTimer.purge();
        mSaveTimer = null;

        String contents = mNoteEditText.getText().toString();

        // Update the data model's mContent
        mNote.setContent(contents);

        saveNote(new GlassNotesGitHubAPIClient.Promise<Note>() {
            @Override
            public void resolved(Note data) {
                playSound(Sounds.DISMISSED);
                EditActivity.this.finish();
            }

            @Override
            public void rejected(Throwable t) {
                playSound(Sounds.ERROR);
                Log.e(TAG, getString(R.string.error_failed_to_save_note), t);
            }
        });
    }

    /**
     * Saves the note on a schedule
     */
    private class SaveTimerTask extends TimerTask {
        @Override
        public void run() {
            saveNote(new GlassNotesGitHubAPIClient.Promise<Note>() {
                @Override
                public void resolved(Note data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStatusTextView.setStatus("Saved: " + DateUtilities.nowAs("KK:mm:ss a"));
                        }
                    });
                }

                @Override
                public void rejected(Throwable t) {
                    Log.e(TAG, getString(R.string.error_failed_to_save_note), t);
                }
            });
        }
    }
}
