package io.p13i.glassnotes;


import android.app.Activity;
import android.os.Bundle;
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

public class EditActivity extends Activity {

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
        mNoteEditText.setText(mNote.getContents());

        mSaveTimer = new Timer();
        mSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mNote.setContents(mNoteEditText.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        String currentDateandTime = sdf.format(new Date());
                        mLastSaveTextView.setText("Last save: (" + currentDateandTime + ")");
                    }
                });
            }
        }, 5000, 5000);

    }
}
