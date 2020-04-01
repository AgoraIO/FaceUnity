package io.agora.processor.common.connector;

public interface SinkConnector<T> {
    void onDataAvailable(T data);
}
