package io.p13i.glassnotes.exceptions;

public class GlassNotesRuntimeException extends RuntimeException {
    public GlassNotesRuntimeException(Throwable e) {
        super(e);
    }

    public GlassNotesRuntimeException(String msg) {
        super(msg);
    }
}
