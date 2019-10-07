package io.p13i.glassnotes.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.p13i.glassnotes.models.Note;
import io.p13i.ra.databases.in_memory.InMemoryDocumentDatabase;
import io.p13i.ra.engine.RemembranceAgentEngine;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.KeyboardLoggerBreakingBuffer;

/**
 * Common activity for Glass Notes activities. Implemented common functionality like playing sounds,
 * full screen, and no screen timeout.
 */
public abstract class GlassNotesActivity extends Activity {
    private static final String TAG = GlassNotesActivity.class.getName();

    /**
     * The remembrance agent engine
     */
    protected RemembranceAgentEngine remembranceAgentEngine;

    /**
     * The timer the RA is running on
     */
    protected Timer remembranceAgentTimer;

    /**
     * The keyboard buffer that characters are stored in
     */
    protected KeyboardLoggerBreakingBuffer keyboardBuffer = new KeyboardLoggerBreakingBuffer(50);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // full screen, no app bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Key the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Plays a sound read {@link com.google.android.glass.media.Sounds}
     *
     * @param sound the Sounds.* value
     */
    protected void playSound(int sound) {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            Log.i(TAG, "Playing sound with value: " + sound);
            audioManager.playSoundEffect(sound);
        } else {
            Log.e(TAG, AudioManager.class.getSimpleName() + " is null");
        }
    }

    protected void startRemembranceAgent(InMemoryDocumentDatabase inMemoryDocumentDatabase) {

        // Initialize RA
        remembranceAgentEngine = new RemembranceAgentEngine(inMemoryDocumentDatabase);
        remembranceAgentEngine.loadDocuments();
        remembranceAgentEngine.indexDocuments();

        // Start the update timer
        if (remembranceAgentTimer != null) {
            remembranceAgentTimer.purge();
            remembranceAgentTimer.cancel();
        }
        remembranceAgentTimer = new Timer();
        remembranceAgentTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String query = keyboardBuffer.toString();

                Log.i(TAG, "Running RA with query " + query);

                final List<ScoredDocument> scoredDocuments = remembranceAgentEngine.determineSuggestions(new Query(query, io.p13i.ra.models.Context.NULL, 3) {{ index(); }});

                // Stop if no suggestions were received
                if (scoredDocuments.isEmpty()) {
                    Log.i(TAG, "Received 0 suggestions");
                    return;
                }

                onRemembranceAgentSuggestions(scoredDocuments);
            }
        }, 5000, 5000);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        char character = event.getDisplayLabel();
        Log.i(TAG, "Received character " + character);
        keyboardBuffer.addCharacter(character);

        return super.onKeyUp(keyCode, event);
    }

    protected String getScoredDocumentShortString(ScoredDocument scoredDocument) {
        return scoredDocument.toShortString(25, 3)
                .replace(".local", "")
                .replace(Note.MARKDOWN_EXTENSION, "");
    }

    protected void onRemembranceAgentSuggestions(List<ScoredDocument> suggestionTexts) {
        for (ScoredDocument scoredDocument : suggestionTexts) {
            Log.i(TAG, "Suggestion " + getScoredDocumentShortString(scoredDocument));
        }
    }
}
