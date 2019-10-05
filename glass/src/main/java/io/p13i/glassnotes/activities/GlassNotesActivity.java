package io.p13i.glassnotes.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * Common activity for Glass Notes activities. Implemented common functionality like playing sounds,
 * full screen, and no screen timeout.
 */
public class GlassNotesActivity extends Activity {
    private static final String TAG = GlassNotesActivity.class.getName();

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
}
