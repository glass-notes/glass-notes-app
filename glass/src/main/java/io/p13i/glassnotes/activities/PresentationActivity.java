package io.p13i.glassnotes.activities;

import android.content.Context;
import android.os.Bundle;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.p13i.glassnotes.R;
import io.p13i.glassnotes.datastores.Promise;
import io.p13i.glassnotes.models.Note;
import io.p13i.glassnotes.ui.StatusTextView;
import io.p13i.glassnotes.user.PreferenceManager;
import io.p13i.glassnotes.utilities.LimitedViewItemManager;
import io.p13i.glassnotes.utilities.ListUtils;
import io.p13i.glassnotes.utilities.StringUtilities;

public class PresentationActivity extends GlassNotesActivity {

    private static final String TAG = PresentationActivity.class.getName();

    @BindView(R.id.activity_presentation_status_text_view)
    StatusTextView mStatusTextView;

    @BindView(R.id.activity_presentation_text_view)
    TextView mTextView;

    private LimitedViewItemManager<Character> mVisibleCharactersManager;


    private GestureDetector mGestureDetector;
    private Note mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // full screen, no app bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Key the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Bind to views
        setContentView(R.layout.activity_presentation);
        ButterKnife.bind(this);

        // Get the target Note read the activity transition
        mNote = (Note) getIntent().getSerializableExtra(Note.EXTRA_TAG);

        PreferenceManager.getInstance().getDataStore().getNote(mNote.getAbsoluteResourcePath(), new Promise<Note>() {
            @Override
            public void resolved(final Note data) {
                Log.i(TAG, "Got note read data store. Title: '" + data.getFilename() + "'; " +
                        "data store: " + PreferenceManager.getInstance().getDataStore().getName());
                PresentationActivity.this.playSound(Sounds.SUCCESS);
                // Update the UI with the note retrieved read the data store
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNote = data;

                        mVisibleCharactersManager = new LimitedViewItemManager<Character>(StringUtilities.asList(mNote.getContent()), 250);
                        mTextView.setText(StringUtilities.asString(mVisibleCharactersManager.getVisibleItems()));

                        mStatusTextView.setPageTitle(mNote.getFilename());
                    }
                });
            }

            @Override
            public void rejected(Throwable t) {
                Log.e(TAG, "Failed to fetch gist with ID: " + mNote.getAbsoluteResourcePath(), t);
                playSound(Sounds.ERROR);
            }
        });

        mGestureDetector = createGestureDetector(this);

        mStatusTextView.setStatus("Presentation");

    }

    private GestureDetector createGestureDetector(Context context) {
        return new GestureDetector(context) {{
            setBaseListener(new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    if (gesture == Gesture.SWIPE_RIGHT) {
                        mVisibleCharactersManager.nextPage();
                        mTextView.setText(StringUtilities.asString(mVisibleCharactersManager.getVisibleItems()));
                        playSound(Sounds.TAP);
                        return true;
                    } else if (gesture == Gesture.SWIPE_LEFT) {
                        mVisibleCharactersManager.priorPage();
                        mTextView.setText(StringUtilities.asString(mVisibleCharactersManager.getVisibleItems()));
                        playSound(Sounds.TAP);
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

}
