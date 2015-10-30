/*
 * Copyright (c) 2013, David Brodsky. All rights reserved.
 *
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.perchlive.broadcast.sdk.av;

import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;

import timber.log.Timber;

/**
 * A FileObserver that listens for actions
 * specific to the creation of an HLS stream
 * e.g: A .ts segment is written
 * or a .m3u8 manifest is modified
 *
 * @author davidbrodsky
 * @hide
 */
public class HlsFileObserver extends FileObserver {
    private static final String TAG = "HlsFileObserver";
    private static final boolean VERBOSE = false;

    private static final String M3U8_EXT = "m3u8";
    private static final String TS_EXT = "ts";
    private static final String JPG_EXT = "jpg";

    private final String observedPath;
    private final HlsFileObserverCallback callback;

    /**
     * Begin observing the given path for changes
     * to .ts, .m3u8 and .jpg files
     *
     * @param path     the absolute path to observe.
     * @param callback a callback that will receive notifications
     */
    public HlsFileObserver(@NonNull String path,
                           @NonNull HlsFileObserverCallback callback) {
        super(path, CLOSE_WRITE | MOVED_TO);
        this.callback = callback;
        observedPath = path;
    }

    @Override
    public void onEvent(int event, String path) {
        if (path == null) return; // If the directory was deleted.
        String ext = path.substring(path.lastIndexOf('.') + 1);
        String absolutePath = observedPath + File.separator + path;
        Log.d(TAG, String.format("Event %d at %s ext %s", event, path, ext));

        if (event == MOVED_TO && ext.equals(M3U8_EXT)) {
            if (VERBOSE) Timber.d("posting manifest written " + absolutePath);
            callback.onManifestWritten(absolutePath);

        } else if (event == CLOSE_WRITE && ext.equals(TS_EXT)) {
            if (VERBOSE) Timber.d("posting hls segment written " + absolutePath);
            callback.onVideoSegmentWritten(absolutePath);

        } else if (event == CLOSE_WRITE && ext.equals(JPG_EXT)) {
            if (VERBOSE) Timber.d("posting thumbnail written " + absolutePath);
            callback.onThumbnailWritten(absolutePath);
        }
    }

    public interface HlsFileObserverCallback {

        void onManifestWritten(@NonNull String manifestPath);

        void onVideoSegmentWritten(@NonNull String videoSegmentPath);

        void onThumbnailWritten(@NonNull String thumbnailPath);

    }

}
