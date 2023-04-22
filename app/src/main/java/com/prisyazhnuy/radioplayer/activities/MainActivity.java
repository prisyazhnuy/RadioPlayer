package com.prisyazhnuy.radioplayer.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.Utils;
import com.prisyazhnuy.radioplayer.adapters.BrowseAdapter;
import com.prisyazhnuy.radioplayer.adapters.BrowseListener;
import com.prisyazhnuy.radioplayer.services.BackgroundAudioService;
import com.prisyazhnuy.radioplayer.services.MusicLibrary;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements BrowseListener {

    private BrowseAdapter mBrowserAdapter;
    private RecyclerView listView;
    private ImageButton mPlayPause;
    private TextView mTitle;
    private TextView mSubtitle;
    private ViewGroup mPlaybackControls;

    private MediaMetadataCompat mCurrentMetadata;
    private PlaybackStateCompat mCurrentState;

    private MediaBrowserCompat mMediaBrowser;
    private MusicLibrary mMusicLibrary;

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mSubscriptionCallback);
                    MediaControllerCompat mediaController = new MediaControllerCompat(MainActivity.this, mMediaBrowser.getSessionToken());
                    updatePlaybackState(mediaController.getPlaybackState());
                    updateMetadata(mediaController.getMetadata());
                    mediaController.registerCallback(mMediaControllerCallback);
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                }
            };

    // Receives callbacks from the MediaController and updates the UI state,
    // i.e.: Which is the current item, whether it's playing or paused, etc.
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    updateMetadata(metadata);
                    mBrowserAdapter.setCurrentMetadata(metadata);
                    int position = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
                    mBrowserAdapter.notifyItemChanged(position);
                    listView.getLayoutManager().scrollToPosition(position);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    updatePlaybackState(state);
                    mBrowserAdapter.setCurrentState(state);
                    mBrowserAdapter.notifyDataSetChanged();
                }

                @Override
                public void onSessionDestroyed() {
                    updatePlaybackState(null);
                    mBrowserAdapter.notifyDataSetChanged();
                }
            };

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d("TAG", "items: " + children);
            onMediaLoaded(children);
        }
    };

    private void onMediaLoaded(List<MediaBrowserCompat.MediaItem> media) {
        mBrowserAdapter.clear();
        mBrowserAdapter.addAll(media);
        mBrowserAdapter.notifyDataSetChanged();
    }

    private void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        if (item.isPlayable()) {
            MediaControllerCompat.getMediaController(this).getTransportControls().playFromMediaId(item.getMediaId(), null);
        }
    }

    @Override
    public void browseStation(@Nullable MediaBrowserCompat.MediaItem item) {
        if (item != null) {
            onMediaItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMusicLibrary = MusicLibrary.getInstance(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(getString(R.string.app_name));
        setSupportActionBar(findViewById(R.id.toolbar));

        mBrowserAdapter = new BrowseAdapter(this, mCurrentState, mCurrentMetadata, this);

        listView = findViewById(R.id.list_view);
        listView.setAdapter(mBrowserAdapter);

        // Playback controls configuration:
        mPlaybackControls = findViewById(R.id.playback_controls);
        mPlayPause = findViewById(R.id.play_pause);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mPlaybackButtonListener);

        ImageButton mBtnNext = findViewById(R.id.btnNext);
        mBtnNext.setOnClickListener(v -> MediaControllerCompat.getMediaController(MainActivity.this)
                .getTransportControls()
                .skipToNext());

        ImageButton mBtnPrev = findViewById(R.id.btnPrev);
        mBtnPrev.setOnClickListener(v -> MediaControllerCompat.getMediaController(MainActivity.this)
                .getTransportControls()
                .skipToPrevious());

        mTitle = findViewById(R.id.title);
        mSubtitle = findViewById(R.id.artist);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMusicLibrary.updateLibrary().subscribe(aBoolean -> {
            mMediaBrowser = new MediaBrowserCompat(MainActivity.this,
                    new ComponentName(MainActivity.this, BackgroundAudioService.class), mConnectionCallback, null);
            mMediaBrowser.connect();
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            if (mCurrentMetadata != null) {
                mMediaBrowser.unsubscribe(mCurrentMetadata.getDescription().getMediaId());
            }
            mMediaBrowser.disconnect();
        }
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        mCurrentState = state;
        if (state == null) {
            mPlayPause.setImageResource(R.drawable.ic_play_arrow_36);
        } else {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mPlayPause.setImageResource(R.drawable.ic_pause_black_36dp);
                    break;
                case PlaybackStateCompat.STATE_CONNECTING:
                case PlaybackStateCompat.STATE_BUFFERING:
                    mPlayPause.setImageDrawable(Utils.createProgressAnimation(this,
                            3,
                            ContextCompat.getColor(this, R.color.colorPrimary),
                            10));
                    break;
                case PlaybackStateCompat.STATE_ERROR:
                    Toast.makeText(this, "Station is not available", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    mPlayPause.setImageResource(R.drawable.ic_play_arrow_36);
            }
        }
        mPlaybackControls.setVisibility(state == null ? View.GONE : View.VISIBLE);
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        mCurrentMetadata = metadata;
        String title = "";
        String subtitle = "";
        if (metadata != null && metadata.getDescription() != null) {
            if (metadata.getDescription().getTitle() != null) {
                title = metadata.getDescription().getTitle().toString();
            }
            if (metadata.getDescription().getSubtitle() != null) {
                subtitle = metadata.getDescription().getSubtitle().toString();
            }
            mTitle.setText(title);
            mSubtitle.setText(subtitle);

            mBrowserAdapter.setCurrentState(mCurrentState);
            mBrowserAdapter.setCurrentMetadata(mCurrentMetadata);
            mBrowserAdapter.notifyItemChanged((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER));
        }
    }

    private final View.OnClickListener mPlaybackButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int state = mCurrentState == null ? PlaybackStateCompat.STATE_NONE : mCurrentState.getState();
            if (state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_STOPPED || state == PlaybackState.STATE_NONE) {
                if (mCurrentMetadata == null) {
                    mCurrentMetadata = mMusicLibrary.getMetadata(Long.valueOf(mMusicLibrary.getMediaItems().get(0).getMediaId()));
                    updateMetadata(mCurrentMetadata);
                }
                MediaControllerCompat.getMediaController(MainActivity.this)
                        .getTransportControls()
                        .playFromMediaId(mCurrentMetadata.getDescription().getMediaId(), null);
            } else {
                MediaControllerCompat.getMediaController(MainActivity.this)
                        .getTransportControls()
                        .pause();
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nav_station_manager) {
            Intent stationExplorer = new Intent(this, StationExplorerActivity.class);
            startActivity(stationExplorer);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
