package io.agora.kit.media.connector;

public interface SinkConnector<T> {
    int onDataAvailable(T data);
}
