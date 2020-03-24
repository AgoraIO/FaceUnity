// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.agora.kit.media.capture;

public class VideoCaptureFormat {
    protected int mWidth;
    protected int mHeight;
    protected int mFrameRate;
    protected int mPixelFormat;

    public VideoCaptureFormat(int width, int height, int framerate, int pixelformat) {
        mWidth = width;
        mHeight = height;
        mFrameRate = framerate;
        mPixelFormat = pixelformat;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getFramerate() {
        return mFrameRate;
    }

    public int getPixelFormat() {
        return mPixelFormat;
    }

    public String toString() {
        return "VideoCaptureFormat{" +
                "mPixelFormat=" + mPixelFormat +
                "mFrameRate=" + mFrameRate +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                '}';
    }
}
