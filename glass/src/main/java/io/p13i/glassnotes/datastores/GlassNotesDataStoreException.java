package io.p13i.glassnotes.datastores;

import android.util.Log;

import java.io.IOException;

import io.p13i.glassnotes.exceptions.GlassNotesRuntimeException;
import retrofit2.Response;

public class GlassNotesDataStoreException extends GlassNotesRuntimeException {
    private static final String TAG = GlassNotesDataStoreException.class.getName();

    public GlassNotesDataStoreException(String s) {
        super(s);
    }

    public GlassNotesDataStoreException(Response response) {
        super("Error response with code " + response.code() + "\n" + response.message() + "\n" + tryGetResponseErrorBody(response));
    }

    private static String tryGetResponseErrorBody(Response response) {
        try {
            return response.errorBody().string();
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to read error response body (reached null)" + response.toString());
            return "";
        } catch (IOException e) {
            Log.e(TAG, "Failed to read error response body " + response.toString());
            return "";
        }
    }
}
