package io.p13i.glassnotes.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

public class GlassNotesActivity extends Activity {
    protected void playSound(int sound) {
        AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(sound);
    }
}