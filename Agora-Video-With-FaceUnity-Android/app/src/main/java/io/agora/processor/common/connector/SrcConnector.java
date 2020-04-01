package io.agora.processor.common.connector;

import java.util.ArrayList;
import java.util.List;

public class SrcConnector<T> {
    //private SinkConnector<T> mSink;
    private List<SinkConnector<T>> mSinks;

    public SrcConnector() {
        mSinks = new ArrayList<>();
    }

    public void connect(SinkConnector<T> sink) {
        if (!mSinks.contains(sink)) {
            mSinks.add(sink);
        }
    }

    public void onDataAvailable(T data) {
        for (SinkConnector<T> sink : mSinks) {
            sink.onDataAvailable(data);
        }
    }

    public void disconnect(SinkConnector<T> sink) {
        if (mSinks.contains(sink)) {
            mSinks.remove(sink);
        }
    }

    public void clear() {
        mSinks.clear();
    }

}
