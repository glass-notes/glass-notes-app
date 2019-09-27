package io.p13i.glassnotes.exceptions;

import java.security.NoSuchAlgorithmException;

import io.p13i.glassnotes.datastores.GlassNotesDataStoreException;

public class GlassNotesRuntimeException extends RuntimeException {
    public GlassNotesRuntimeException(Throwable e) {
        super(e);
    }

    public GlassNotesRuntimeException(String msg) {
        super(msg);
    }
}
