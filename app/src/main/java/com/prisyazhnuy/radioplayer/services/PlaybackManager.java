package com.prisyazhnuy.radioplayer.services;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;

/**
 * Dell on 20.08.2017.
 */

class PlaybackManager implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    private final Context mContext;
    private int mState;
    private boolean mPlayOnFocusGain;
    private volatile MediaMetadataCompat mCurrentMedia;
    private MusicLibrary mMusicLibrary;

    private MediaPlayer mMediaPlayer;

    private final Callback mCallback;
    private final AudioManager mAudioManager;
    private boolean isError = false;
    private long mStartPlayingTime;

    PlaybackManager(Context context, Callback callback) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mCallback = callback;
        this.mMusicLibrary = MusicLibrary.getInstance(context);
    }

    private boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    Long getCurrentMediaId() {
        return Long.valueOf(mCurrentMedia == null ? null : mCurrentMedia.getDescription().getMediaId());
    }

    private int getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    void play(MediaMetadataCompat metadata) throws RuntimeException {
        String mediaId = metadata.getDescription().getMediaId();
        boolean mediaChanged = (mCurrentMedia == null || !getCurrentMediaId().equals(Long.parseLong(mediaId)));

        if (mMediaPlayer == null) {
            isError = false;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setWakeMode(mContext.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mediaChanged = true;
        } else {
            if (mediaChanged) {
                mMediaPlayer.reset();
            }
        }

        if (mediaChanged) {
            storeTime();
            mCurrentMedia = metadata;
            try {
                mMediaPlayer.setDataSource(mMusicLibrary.getSongUrl(Long.valueOf(mediaId)));
                mMediaPlayer.setOnPreparedListener(mp -> {
                    if (tryToGetAudioFocus()) {
                        mStartPlayingTime = System.currentTimeMillis();
                        mPlayOnFocusGain = false;
                        mMediaPlayer.start();
                        mState = PlaybackStateCompat.STATE_PLAYING;
                        updatePlaybackState();
                    } else {
                        mPlayOnFocusGain = true;
                    }
                });
                mMediaPlayer.prepareAsync();
                mState = PlaybackStateCompat.STATE_BUFFERING;
                updatePlaybackState();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            mMediaPlayer.start();
            mState = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
        }
    }

    private void storeTime() {
        long finishPlayingTime = System.currentTimeMillis();
        if (mStartPlayingTime != 0) {
            long playingTime = (finishPlayingTime - mStartPlayingTime) / 1000;
            mMusicLibrary.updateTime(Long.parseLong(mCurrentMedia.getDescription().getMediaId()), playingTime);
        }
        mStartPlayingTime = 0;
    }

    void pause() {
        if (isPlaying()) {
            storeTime();
            mMediaPlayer.pause();
            mAudioManager.abandonAudioFocus(this);
        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    void stop() {
        storeTime();
        mState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
        // Give up Audio focus
        mAudioManager.abandonAudioFocus(this);
        // Relax all resources
        releaseMediaPlayer();
    }

    /** Try to get the system audio focus. */
    private boolean tryToGetAudioFocus() {
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * Called by AudioManager on audio focus changes. Implementation of {@link
     * AudioManager.OnAudioFocusChangeListener}.
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        boolean gotFullFocus = false;
        boolean canDuck = false;
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            gotFullFocus = true;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
        }

        if (gotFullFocus || canDuck) {
            if (mMediaPlayer != null) {
                if (mPlayOnFocusGain) {
                    mPlayOnFocusGain = false;
                    mMediaPlayer.start();
                    mState = PlaybackStateCompat.STATE_PLAYING;
                    updatePlaybackState();
                }
                float volume = canDuck ? 0.2f : 1.0f;
                mMediaPlayer.setVolume(volume, volume);
            }
        } else if (mState == PlaybackStateCompat.STATE_PLAYING) {
            mMediaPlayer.pause();
            mState = PlaybackStateCompat.STATE_PAUSED;
            updatePlaybackState();
        }
    }

    /**
     * Called when media player is done playing current song.
     *
     * @see MediaPlayer.OnCompletionListener
     */
    @Override
    public void onCompletion(MediaPlayer player) {
        if (!isError) {
            mMediaPlayer = null;
            play(mCurrentMedia);
        } else {
            stop();
        }
    }

    /** Releases resources used by the service for playback. */
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_STOP;
        if (isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    private void updatePlaybackState() {
        if (mCallback == null) {
            return;
        }
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(getAvailableActions());

        stateBuilder.setState(mState, getCurrentStreamPosition(), 1.0f, SystemClock.elapsedRealtime());
        mCallback.onPlaybackStatusChanged(stateBuilder.build());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        isError = true;
        mState = PlaybackStateCompat.STATE_ERROR;
        updatePlaybackState();
        return false;
    }

    interface Callback {
        void onPlaybackStatusChanged(PlaybackStateCompat state);
    }
}
