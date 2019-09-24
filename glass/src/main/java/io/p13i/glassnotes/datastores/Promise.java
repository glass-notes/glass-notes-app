package io.p13i.glassnotes.datastores;

public interface Promise<T> {
    void resolved(T data);
    void rejected(Throwable t);
}
