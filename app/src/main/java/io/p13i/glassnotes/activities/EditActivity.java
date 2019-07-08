package io.p13i.glassnotes.activities;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.R;
import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.github.GlassNotesGitHubAPIClient;
import io.p13i.glassnotes.ui.StatusTextView;
import io.p13i.glassnotes.utilities.DateUtilities;
import okhttp3.ResponseBody;

public class EditActivity extends Activity {

    public final static String TAG = EditActivity.class.getName();

    @BindView(R.id.activity_edit_status)
    StatusTextView mStatusTextView;

    @BindView(R.id.note_edit_text)
    EditText mNoteEditText;

    private Note mNote;
    private Timer mSaveTimer;
    private GlassNotesDataStore mGlassNotesDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        mNote = (Note) getIntent().getSerializableExtra(Note.EXTRA_TAG);

        mNoteEditText.setEnabled(false);
        mNoteEditText.setTextColor(getResources().getColor(R.color.white));

        mGlassNotesDataStore = new GlassNotesGitHubAPIClient();
        mGlassNotesDataStore.getNote(mNote.getId(), new GlassNotesGitHubAPIClient.Promise<Note>() {
            @Override
            public void resolved(Note data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNote = data;
                        mNoteEditText.setText(mNote.getContent());
                        mNoteEditText.setSelection(mNoteEditText.getText().length());
                        mNoteEditText.setEnabled(true);
                        startSaveTimer();
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Log.e(TAG, "Failed to fetch getGist.", t);
            }
        });

        mStatusTextView.setPageTitle(mNote.getTitle());
        mStatusTextView.setStatus("Welcome!");
    }

    void startSaveTimer() {
        mSaveTimer = new Timer();
        mSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String currentText = mNote.getContent();
                String updatedText = mNoteEditText.getText().toString();

                if (currentText.equals(updatedText)) {
                    return;
                }

                // Else, it was updated
                mNote.setContent(mNoteEditText.getText().toString());

                mGlassNotesDataStore.saveNote(mNote, new GlassNotesGitHubAPIClient.Promise<ResponseBody>() {
                    @Override
                    public void resolved(ResponseBody data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mStatusTextView.setStatus("Saved: " + DateUtilities.nowAs("KK:mm:ss a"));
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable t) {
                        Log.e(TAG, "Failed to fetch gist.", t);
                    }
                });
            }
        }, 5_000, 5_000);
    }

    boolean mSemicolonPressed = false;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEMICOLON) {
            if (!mSemicolonPressed) {
                mSemicolonPressed = true;
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_X) {
            if (mSemicolonPressed) {
                saveAndFinish();
                return true;
            }
        }

        mSemicolonPressed = false;

        return super.onKeyUp(keyCode, event);
    }

    private void saveAndFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusTextView.setText("Saving/exiting...");
                mStatusTextView.invalidate();
            }
        });

        String contents = mNoteEditText.getText().toString();
        contents = contents.replaceAll(":x", "");
        mNoteEditText.setText(contents);

        mNote.setContent(contents);

        mGlassNotesDataStore.saveNote(mNote, new GlassNotesGitHubAPIClient.Promise<ResponseBody>() {
            @Override
            public void resolved(ResponseBody data) {
                EditActivity.this.finish();
            }

            @Override
            public void failed(Throwable t) {
                Log.e(TAG, "Failed to fetch gist.", t);
            }
        });
    }
}
