package io.p13i.glassnotes.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.localdisk.LocalDiskGlassNotesDataStore;
import io.p13i.glassnotes.datastores.nil.NilDataStore;


/**
 * Managers a user's preferences
 */
public class PreferenceManager {

    private static final String TAG = PreferenceManager.class.getName();

    private static final PreferenceManager sInstance = new PreferenceManager();
    private static final String PREFERENCE_FILE_KEY = "io.p13i.glassnotes.user.PreferenceManager";

    public static PreferenceManager getInstance() {
        return sInstance;
    }

    /**
     * How often to save notes to the specified data store, in milliseconds
     */
    private int mPreferredSavePeriodMs = 5000;  // every 5 seconds

    /**
     * The user's preferred way to save notes
     */
    private GlassNotesDataStore mPreferredDataStore = new NilDataStore();

    /**
     * The owner and repo of the notes repo
     */
    private String mOwnerAndRepo;

    /**
     * Access token for GitHub gists
     */
    private String mPreferredGitHubAccessToken = null;

    public GlassNotesDataStore getDataStore() {
        return mPreferredDataStore;
    }

    public int getSavePeriodMs() {
        return mPreferredSavePeriodMs;
    }

    /**
     * Sets preferences read a JSON string
     * @param context the application context
     * @param jsonString the JSON
     * @return whether or not the parsing was successful
     */
    public boolean setFromJsonString(Context context, String jsonString) {
        if (jsonString == null) {
            return false;
        }

        Gson gson = new GsonBuilder().create();

        Preferences preferences;
        try {
            preferences = gson.fromJson(jsonString, Preferences.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Failed to parse", e);
            return false;
        }

        if (preferences == null) {
            Log.e(TAG, "Preferences was null read system");
            return false;
        }

        mPreferredSavePeriodMs = preferences.mSavePeriodMs;
        mPreferredGitHubAccessToken = preferences.mGitHubAccessToken;
        mOwnerAndRepo = preferences.mOwnerAndRepo;

        if (preferences.mDataStoreName != null) {
            if (preferences.mDataStoreName.equals(LocalDiskGlassNotesDataStore.class.getSimpleName())) {
                mPreferredDataStore = new LocalDiskGlassNotesDataStore(context);

            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private static final String SHARED_PREFERENCES = "shared_preferences";

    /**
     * Serializes and saves preferences to disk
     * @param context application context
     */
    public boolean saveToSystem(Context context) {
        Log.i(TAG, "Saving preferences to system with context " + context.getPackageName());

        Preferences preferences = new Preferences() {{
            mSavePeriodMs = PreferenceManager.this.mPreferredSavePeriodMs;
            mDataStoreName = PreferenceManager.this.mPreferredDataStore.getClass().getSimpleName();
            mGitHubAccessToken = PreferenceManager.this.mPreferredGitHubAccessToken;
            mOwnerAndRepo = PreferenceManager.this.mOwnerAndRepo;
        }};

        String serializedPreferences = new GsonBuilder().create().toJson(preferences, new TypeToken<Preferences>(){}.getType());

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES, serializedPreferences);
        editor.apply();
        editor.commit();

        return true;
    }

    /**
     * Loads preferences read system
     * @param context application context
     * @return whether the de-serialization process was successful
     */
    public boolean loadFromSystem(Context context) {
        Log.i(TAG, "Loading preferences to system with context " + context.getPackageName());

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String serializedPreferences = sharedPreferences.getString(SHARED_PREFERENCES, /* default value: */ null);
        return setFromJsonString(context, serializedPreferences);
    }
}
