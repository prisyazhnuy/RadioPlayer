package com.prisyazhnuy.radioplayer.services;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import androidx.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import androidx.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import java.util.List;

/**
 * Dell on 09.08.2017.
 */

public class BackgroundAudioService extends MediaBrowserServiceCompat {

    private static final String TAG = "AudioService";
    private MediaSessionCompat mSession;
    private PlaybackManager mPlayback;
    private MusicLibrary mMusicLibrary;

    final MediaSessionCompat.Callback mCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            mSession.setActive(true);
            MediaMetadataCompat metadata = mMusicLibrary.getMetadata(Long.valueOf(mediaId));
            mSession.setMetadata(metadata);
            try {
                mPlayback.play(metadata);
            } catch (RuntimeException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                mPlayback.onError(null, PlaybackStateCompat.ERROR_CODE_NOT_SUPPORTED, 0);
                onSkipToNext();
            }
        }

        @Override
        public void onPlay() {
            if (mPlayback.getCurrentMediaId() != null) {
                onPlayFromMediaId(String.valueOf(mPlayback.getCurrentMediaId()), null);
            }
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
        }

        @Override
        public void onSkipToNext() {
            onPlayFromMediaId(String.valueOf(mMusicLibrary.getNextSong(mPlayback.getCurrentMediaId())), null);
        }

        @Override
        public void onSkipToPrevious() {
            onPlayFromMediaId(String.valueOf(mMusicLibrary.getPreviousSong(mPlayback.getCurrentMediaId())), null);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mMusicLibrary = MusicLibrary.getInstance(this);

        // 1) Start a new MediaSession.
        mSession = new MediaSessionCompat(this, "MusicService");
        // 2) Set the media session callback
        mSession.setCallback(mCallback);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // 3) Set the media session token
        setSessionToken(mSession.getSessionToken());

        final MediaNotificationManager mediaNotificationManager = new MediaNotificationManager(this);

        mPlayback = new PlaybackManager(this, state -> {
            mSession.setPlaybackState(state);
            mediaNotificationManager.update(mPlayback.getCurrentMedia(), state, getSessionToken());
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        KeyEvent keyEvent = MediaButtonReceiver.handleIntent(mSession, intent);
        Log.d(TAG, "onStartCommand, keyEvent: " + keyEvent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mPlayback.stop();
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(mMusicLibrary.getRoot(), null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = mMusicLibrary.getMediaItems();
        Log.d("TAG", "mediaItems: " + mediaItems);
        result.sendResult(mediaItems);
    }
}
