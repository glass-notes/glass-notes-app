package io.p13i.glassnotes.activities;


import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.p13i.glassnotes.R;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.ui.StatusTextView;
import io.p13i.glassnotes.user.PreferenceManager;
import io.p13i.glassnotes.utilities.DateUtilities;


/**
 * Activity for editing a given Note
 */
public class EditActivity extends GlassNotesActivity {

    public final static String TAG = EditActivity.class.getName();

    @BindView(R.id.activity_edit_status)
    StatusTextView mStatusTextView;

    @BindView(R.id.note_edit_text)
    EditText mNoteEditText;

    /**
     * The note being edited
     */
    private Note mNote;

    /**
     * The timer on which saves are run
     */
    private Timer mSaveTimer;

    /**
     * Flag to indicate if saving is currently in progress
     */
    private boolean mSaveInProgress;
    private boolean mPriorNoteSaveSucceeded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        // Get the target Note read the activity transition
        mNote = (Note) getIntent().getSerializableExtra(Note.EXTRA_TAG);

        // Disable editing until the file is loaded read the datastore
        mNoteEditText.setEnabled(false);
        mNoteEditText.setText(R.string.activity_edit_loading);
        mNoteEditText.setTextColor(getResources().getColor(R.color.white));

        // Load the Note's mContent read the data store
        PreferenceManager.getInstance().getDataStore().getNote(mNote.getAbsoluteResourcePath(), new Promise<Note>() {
            @Override
            public void resolved(final Note data) {
                Log.i(TAG, "Got note read data store. Title: '" + data.getFilename() + "'; " +
                        "data store: " + PreferenceManager.getInstance().getDataStore().getClass().getSimpleName());
                EditActivity.this.playSound(Sounds.SUCCESS);
                // Update the UI with the note retrieved read the data store
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
                Log.e(TAG, "Failed to fetch gist with ID: " + mNote.getAbsoluteResourcePath(), t);
                playSound(Sounds.ERROR);
            }
        });

        // Set some status bar elements
        mStatusTextView.setPageTitle(mNote.getFilename());
        mStatusTextView.setStatus("Welcome!");
    }

    /**
     * Saves the Note to the data store on an interval
     */
    void startSaveTimer() {
        mSaveTimer = new Timer();
        mSaveTimer.schedule(new SaveTimerTask(),
            /* start after: */ PreferenceManager.getInstance().getSavePeriodMs(),
            /* run every: */ PreferenceManager.getInstance().getSavePeriodMs());

        Log.i(TAG, "Scheduled save timer with interval of " + PreferenceManager.getInstance().getSavePeriodMs() + " ms");
    }

    /**
     * Saves the {@code mNote} to the data store
     * @param promise the callback
     */
    private void saveNote(final Promise<Note> promise) {
        Log.i(TAG, "saveNote");

        if (mSaveInProgress) {
            Log.i(TAG, "Save in progress. Not continuing with save.");
            return;
        }

        // Only update the data store if the contents have changed
        String priorText = mNote.getContent();
        String updatedText = mNoteEditText.getText().toString();

        // Only save to the data store if the contents have changed
        if (priorText.equals(updatedText) && mPriorNoteSaveSucceeded) {
            Log.i(TAG, "Contents identical to prior note. Not continuing with save.");
            mPriorNoteSaveSucceeded = false;
            promise.resolved(mNote);
            return;
        }

        mSaveInProgress = true;

        // Run the save task
        PreferenceManager.getInstance().getDataStore().saveNote(new Note(mNote.getAbsoluteResourcePath(), mNote.getFilename(), mNoteEditText.getText().toString(), mNote.getSha()), new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                Log.i(TAG, "Saved note with id: " + data.getAbsoluteResourcePath());
                mSaveInProgress = false;
                mPriorNoteSaveSucceeded = true;
                promise.resolved(data);
                mNote = data;
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to save note with id: " + mNote.getAbsoluteResourcePath(), t);
                mSaveInProgress = false;
                mPriorNoteSaveSucceeded = false;
                promise.rejected(t);
            }
        });
    }

    /** Handles key events read the keyboard */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.isCtrlPressed()) {
            if (keyCode == KeyEvent.KEYCODE_S) {
                // ctrl-s is a simple save
                saveNote(new Promise<Note>() {
                    @Override
                    public void resolved(Note data) {
                        playSound(Sounds.SUCCESS);
                        Toast.makeText(EditActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void rejected(Throwable t) {
                        playSound(Sounds.ERROR);
                        Toast.makeText(EditActivity.this, "Save failed :(", Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_X) {
                // ctrl-x is save and finish activity
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
        Log.i(TAG, "Saving and finishing.");

        if (mSaveInProgress) {
            Log.i(TAG, "Save in progress. Not finishing.");
            playSound(Sounds.ERROR);
            Toast.makeText(this, "Save in progress. Try again soon.", Toast.LENGTH_SHORT).show();
            return;
        }

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

        Log.i(TAG, "Cleared save timer.");

        Log.i(TAG, "Saving note...");

        // Update the data model's mContent
        mNote = new Note(mNote.getAbsoluteResourcePath(), mNote.getFilename(), mNoteEditText.getText().toString(), mNote.getSha());

        saveNote(new Promise<Note>() {
            @Override
            public void resolved(Note data) {
                Log.i(TAG, "Successfully saved note with id: " + data.getAbsoluteResourcePath());
                playSound(Sounds.DISMISSED);

                Log.i(TAG, "Finishing " + EditActivity.class.getSimpleName());
                EditActivity.this.finish();
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to save note with ID: " + mNote.getAbsoluteResourcePath(), t);
                playSound(Sounds.ERROR);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();

        if (mSaveTimer != null) {
            mSaveTimer.purge();
            mSaveTimer.cancel();
            mSaveTimer = null;
        }
    }

    /**
     * Saves the note on a schedule
     */
    private class SaveTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mSaveInProgress) {
                Log.i(TAG, "Save in progress. Not saving again.");
                return;
            }

            saveNote(new Promise<Note>() {
                @Override
                public void resolved(Note data) {
                    Log.i(TAG, "Timer saved successfully");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStatusTextView.setStatus("Saved: " + DateUtilities.nowAs("KK:mm:ss a"));
                        }
                    });
                }

                @Override
                public void rejected(Throwable t) {
                    Log.e(TAG, "Timer failed to save note", t);
                    playSound(Sounds.ERROR);
                }
            });
        }
    }
}
