package io.p13i.glassnotes;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
import io.p13i.glassnotes.API.GlassNotesClient;
import okhttp3.ResponseBody;

public class EditActivity extends Activity {

    public final static String TAG = EditActivity.class.getName();

    @BindView(R.id.note_edit_text)
    EditText mNoteEditText;

    @BindView(R.id.last_save)
    TextView mLastSaveTextView;

    private Note mNote;

    private Timer mSaveTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        mNote = (Note) getIntent().getSerializableExtra(Note.EXTRA_TAG);

        MainActivity.setTextViewCommonStyles(this, mLastSaveTextView);
        mLastSaveTextView.setTextColor(getResources().getColor(R.color.black));
        mNoteEditText.setEnabled(false);

        GlassNotesClient.getNote(mNote.getId(), new GlassNotesClient.Promise<Note>() {
            @Override
            public void resolved(Note data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNote = data;
                        mNoteEditText.setText(mNote.getContent());
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



    }

    void startSaveTimer() {
        mSaveTimer = new Timer();
        mSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String currentText = mNote.mContents;
                String updatedText = mNoteEditText.getText().toString();

                if (currentText.equals(updatedText)) {
                    return;
                }

                // Else, it was updated
                mNote.setContent(mNoteEditText.getText().toString());

                GlassNotesClient.saveNote(mNote, new GlassNotesClient.Promise<ResponseBody>() {
                    @Override
                    public void resolved(ResponseBody data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                String currentDateandTime = sdf.format(new Date());
                                mLastSaveTextView.setText("Last save: (" + currentDateandTime + ")");
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable t) {
                        Log.e(TAG, "Failed to fetch getGist.", t);
                    }
                });
            }
        }, 5_000, 5_000);
    }
}
