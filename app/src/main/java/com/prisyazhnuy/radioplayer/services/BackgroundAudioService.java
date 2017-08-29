package com.prisyazhnuy.radioplayer.services;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

/**
 * Dell on 09.08.2017.
 */

public class BackgroundAudioService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mSession;
    private PlaybackManager mPlayback;
    private MusicLibrary mMusicLibrary;

    final MediaSessionCompat.Callback mCallback =
            new MediaSessionCompat.Callback() {
                @Override
                public void onPlayFromMediaId(String mediaId, Bundle extras) {
                    mSession.setActive(true);
                    MediaMetadataCompat metadata = mMusicLibrary.getMetadata(BackgroundAudioService.this, Long.valueOf(mediaId));
                    mSession.setMetadata(metadata);
                    try {
                        mPlayback.play(metadata);
                    } catch (RuntimeException e) {
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
                    stopSelf();
                }

                @Override
                public void onSkipToNext() {
                    onPlayFromMediaId(
                            String.valueOf(mMusicLibrary.getNextSong(mPlayback.getCurrentMediaId())), null);
                }

                @Override
                public void onSkipToPrevious() {
                    onPlayFromMediaId(
                            String.valueOf(mMusicLibrary.getPreviousSong(mPlayback.getCurrentMediaId())), null);
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicLibrary = MusicLibrary.getInstance(this);

        // Start a new MediaSession.
        mSession = new MediaSessionCompat(this, "MusicService");
        mSession.setCallback(mCallback);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mSession.getSessionToken());

        final MediaNotificationManager mediaNotificationManager = new MediaNotificationManager(this);

        mPlayback = new PlaybackManager(this, new PlaybackManager.Callback() {
            @Override
            public void onPlaybackStatusChanged(PlaybackStateCompat state) {
                mSession.setPlaybackState(state);
                mediaNotificationManager.update(mPlayback.getCurrentMedia(), state, getSessionToken());
            }
        });
    }

    @Override
    public void onDestroy() {
        mPlayback.stop();
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(mMusicLibrary.getRoot(), null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(mMusicLibrary.getMediaItems());
    }
}
