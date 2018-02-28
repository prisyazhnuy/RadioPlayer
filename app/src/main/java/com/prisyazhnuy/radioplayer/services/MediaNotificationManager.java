package com.prisyazhnuy.radioplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.activities.MainActivity;

/**
 * Dell on 20.08.2017.
 */

public class MediaNotificationManager {
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    private final BackgroundAudioService mService;

    private final NotificationManager mNotificationManager;

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mStopAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    private boolean mStarted;
    private PendingIntent stopIntent;

    public MediaNotificationManager(BackgroundAudioService service) {
        mService = service;

        PendingIntent playIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(mService, PlaybackStateCompat.ACTION_PLAY);
        PendingIntent pauseIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(mService, PlaybackStateCompat.ACTION_PAUSE);
        stopIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(mService, PlaybackStateCompat.ACTION_STOP);
        PendingIntent nextIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(mService, PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        PendingIntent prevIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(mService, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        mPlayAction = new NotificationCompat.Action(
                R.drawable.ic_play_arrow_white_24dp,
                mService.getString(R.string.label_play),
                playIntent);
        mPauseAction = new NotificationCompat.Action(
                R.drawable.ic_pause_white_24dp,
                mService.getString(R.string.label_pause),
                pauseIntent);
        mStopAction = new NotificationCompat.Action(
                R.drawable.ic_pause_white_24dp,
                mService.getString(R.string.label_pause),
                stopIntent);
        mNextAction = new NotificationCompat.Action(
                R.drawable.ic_skip_next_white_24dp,
                mService.getString(R.string.label_next),
                nextIntent);
        mPrevAction = new NotificationCompat.Action(
                R.drawable.ic_skip_previous_white_24dp,
                mService.getString(R.string.label_previous),
                prevIntent);

        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    public void update(MediaMetadataCompat metadata, PlaybackStateCompat state, MediaSessionCompat.Token token) {

        if (state == null || state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
            mService.stopForeground(true);
            mService.stopSelf();
            return;
        }
        if (metadata == null) {
            return;
        }
        int playbackState = state.getState();
        boolean isPlaying = playbackState == PlaybackStateCompat.STATE_PLAYING || playbackState == PlaybackStateCompat.STATE_BUFFERING;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
        MediaDescriptionCompat description = metadata.getDescription();

        CharSequence title = description.getTitle();
        CharSequence subtitle = description.getSubtitle();

        int backgroundColor = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backgroundColor = ContextCompat.getColor(mService, R.color.notification_bg);
        } else {
            backgroundColor = ContextCompat.getColor(mService, R.color.notification_bg_old);
        }

        notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1, 2))
                .setColor(backgroundColor)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(title)
                .setContentText(subtitle)
//                .setLargeIcon(bitmap)//MusicLibrary.getAlbumBitmap(mService, description.getMediaId()))
                .setOngoing(isPlaying)
                .setWhen(isPlaying ? System.currentTimeMillis() - state.getPosition() : 0)
                .setShowWhen(isPlaying)
                .setUsesChronometer(isPlaying);

        // If skip to next action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(mPrevAction);
        }

        notificationBuilder.addAction(isPlaying ? mPauseAction : mPlayAction);
        notificationBuilder.setDeleteIntent(stopIntent);

        // If skip to prev action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(mNextAction);
        }

        Notification notification = notificationBuilder.build();

        if (isPlaying && !mStarted) {
            mService.startService(new Intent(mService.getApplicationContext(), BackgroundAudioService.class));
            mService.startForeground(NOTIFICATION_ID, notification);
            mStarted = true;
        } else {
            if (!isPlaying) {
                mService.stopForeground(false);
                mStarted = false;
            }
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}