package com.prisyazhnuy.radioplayer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.activities.MainActivity;

/**
 * Dell on 20.08.2017.
 */

public class MediaNotificationManager {
    private static final int NOTIFICATION_ID = 416;
    private static final String CHANNEL_ID = "RadioPlayer.pro";
    private static final int REQUEST_CODE = 100;
    private final BackgroundAudioService mService;

    private final NotificationManager mNotificationManager;

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mStopAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    private final PendingIntent stopIntent;

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
        Log.e(CHANNEL_ID, "state = " + state);
        if (state == null || state.getState() == PlaybackStateCompat.STATE_STOPPED) {
            mService.stopForeground(true);
            mService.stopSelf();
            return;
        }
        if (metadata == null) {
            return;
        }
        int playbackState = state.getState();
        boolean isPlaying = playbackState == PlaybackStateCompat.STATE_CONNECTING || playbackState == PlaybackStateCompat.STATE_PLAYING || playbackState == PlaybackStateCompat.STATE_BUFFERING;
        createNotificationChannel(CHANNEL_ID, "My Background Service");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
        MediaDescriptionCompat description = metadata.getDescription();

        CharSequence title = description.getTitle();
        CharSequence subtitle = description.getSubtitle();

        int backgroundColor = ContextCompat.getColor(mService, R.color.notification_bg);

        notificationBuilder
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .setColor(backgroundColor)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(title)
                .setContentText(subtitle)
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

        // If stop action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_STOP) != 0) {
            notificationBuilder.addAction(mStopAction);
        }

        Notification notification = notificationBuilder.build();

        if (isPlaying) {
            ContextCompat.startForegroundService(mService, new Intent(mService.getApplicationContext(), BackgroundAudioService.class));
            mService.startForeground(NOTIFICATION_ID, notification);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            mService.stopForeground(false);
        }
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mService, REQUEST_CODE, openUI, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void createNotificationChannel(String channelId, String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(channel);
        }
    }
}