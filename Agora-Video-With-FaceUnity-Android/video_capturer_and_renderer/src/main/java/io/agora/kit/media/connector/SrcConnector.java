package io.agora.kit.media.connector;

public class SrcConnector<T> {
    private SinkConnector<T> mSink;

    public SrcConnector() {
    }

    public void connect(SinkConnector<T> sink) {
        mSink = sink;
    }

    public int onDataAvailable(T data) {
        if (mSink != null)
            return mSink.onDataAvailable(data);
        return -1;
    }

    public void disconnect() {
        mSink = null;
    }
}
