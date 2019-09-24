package io.p13i.glassnotes.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Common activity for Glass Notes activities. Implemented common functionality like playing sounds
 */
public class GlassNotesActivity extends Activity {
    private static final String TAG = GlassNotesActivity.class.getName();

    /**
     * Plays a sound read {@link com.google.android.glass.media.Sounds}
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
}
