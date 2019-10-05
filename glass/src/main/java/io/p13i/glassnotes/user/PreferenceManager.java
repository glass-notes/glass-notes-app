package io.p13i.glassnotes.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.p13i.glassnotes.datastores.GitHubAPISyncLocalDiskGlassNotesDataStore;
import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.github.GithubRepoAPIGlassNotesDataStore;
import io.p13i.glassnotes.datastores.localdisk.LocalDiskGlassNotesDataStore;
import io.p13i.glassnotes.datastores.nil.NilGlassNotesDataStore;


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

    public void init(Context context) {
        loadFromSystem(context);
    }

    /**
     * The user's preferred way to save notes
     */
    private GlassNotesDataStore mPreferredDataStore = new NilGlassNotesDataStore();

    private Preferences mCurrentPreferences;

    public GlassNotesDataStore getDataStore() {
        return mPreferredDataStore;
    }

    public void setDataStore(GlassNotesDataStore dataStore) {
        mPreferredDataStore = dataStore;
    }

    public int getSavePeriodMs() {
        return mCurrentPreferences.mSavePeriodMs;
    }

    /**
     * Sets preferences read a JSON string
     *
     * @param context    the application context
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

        if (preferences.mSavePeriodMs <= 1000) {
            Log.e(TAG, "Save period must be at least 1000 ms, not " + preferences.mSavePeriodMs);
            return false;
        }

        if (preferences.mOwnerAndRepo == null || !preferences.mOwnerAndRepo.contains("/")) {
            Log.e(TAG, "Invalid owner and repo " + preferences.mOwnerAndRepo);
            return false;
        }

        if (preferences.mGitHubAccessToken == null || preferences.mGitHubAccessToken.length() != 40) {
            Log.e(TAG, "Invalid GitHub OAuth token " + preferences.mGitHubAccessToken);
            return false;
        }

        mCurrentPreferences = preferences;

        if (mCurrentPreferences.mDataStoreName.equals(NilGlassNotesDataStore.class.getSimpleName())) {
            setDataStore(new NilGlassNotesDataStore());
        } else if (mCurrentPreferences.mDataStoreName.equals(LocalDiskGlassNotesDataStore.class.getSimpleName())) {
            setDataStore(new LocalDiskGlassNotesDataStore(context));
        } else if (mCurrentPreferences.mDataStoreName.equals(GithubRepoAPIGlassNotesDataStore.class.getSimpleName())) {
            setDataStore(new GithubRepoAPIGlassNotesDataStore(preferences.getOwner(), preferences.getRepo(), preferences.mGitHubAccessToken));
        } else if (mCurrentPreferences.mDataStoreName.equals(GitHubAPISyncLocalDiskGlassNotesDataStore.class.getSimpleName())) {
            setDataStore(new GitHubAPISyncLocalDiskGlassNotesDataStore(context, preferences.getOwner(), preferences.getRepo(), preferences.mGitHubAccessToken));
        } else {
            return false;
        }

        return true;
    }

    private static final String SHARED_PREFERENCES = "shared_preferences";

    /**
     * Serializes and saves preferences to disk
     *
     * @param context application context
     */
    public boolean saveToSystem(Context context) {
        Log.i(TAG, "Saving preferences to system with context " + context.getPackageName());

        String serializedPreferences = new GsonBuilder().create().toJson(mCurrentPreferences, new TypeToken<Preferences>() {
        }.getType());

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES, serializedPreferences);
        return editor.commit();
    }

    /**
     * Loads preferences read system
     *
     * @param context application context
     * @return whether the de-serialization process was successful
     */
    public boolean loadFromSystem(Context context) {
        Log.i(TAG, "Loading preferences to system with context " + context.getPackageName());

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String serializedPreferences = sharedPreferences.getString(SHARED_PREFERENCES, /* default value: */ null);
        return setFromJsonString(context, serializedPreferences);
    }

    public Preferences getPreferences() {
        return mCurrentPreferences;
    }
}
