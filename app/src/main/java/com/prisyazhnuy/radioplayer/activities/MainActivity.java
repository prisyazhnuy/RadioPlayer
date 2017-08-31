package com.prisyazhnuy.radioplayer.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.services.BackgroundAudioService;
import com.prisyazhnuy.radioplayer.services.MediaItemViewHolder;
import com.prisyazhnuy.radioplayer.services.MusicLibrary;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BrowseAdapter mBrowserAdapter;
    private ImageButton mPlayPause;
    private ImageButton mBtnPrev;
    private ImageButton mBtnNext;
    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mAlbumArt;
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
                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(
                                MainActivity.this, mMediaBrowser.getSessionToken());
                        updatePlaybackState(mediaController.getPlaybackState());
                        updateMetadata(mediaController.getMetadata());
                        mediaController.registerCallback(mMediaControllerCallback);
                        MediaControllerCompat.setMediaController(
                                MainActivity.this, mediaController);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    // Receives callbacks from the MediaController and updates the UI state,
    // i.e.: Which is the current item, whether it's playing or paused, etc.
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    updateMetadata(metadata);
                    mBrowserAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    updatePlaybackState(state);
                    mBrowserAdapter.notifyDataSetChanged();
                }

                @Override
                public void onSessionDestroyed() {
                    updatePlaybackState(null);
                    mBrowserAdapter.notifyDataSetChanged();
                }
            };

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMusicLibrary = MusicLibrary.getInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setTitle(getString(R.string.app_name));
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mBrowserAdapter = new BrowseAdapter(this);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mBrowserAdapter);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MediaBrowserCompat.MediaItem item = mBrowserAdapter.getItem(position);
                        onMediaItemSelected(item);
                    }
                });

        // Playback controls configuration:
        mPlaybackControls = (ViewGroup) findViewById(R.id.playback_controls);
        mPlayPause = (ImageButton) findViewById(R.id.play_pause);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mPlaybackButtonListener);

        mBtnNext = (ImageButton) findViewById(R.id.btnNext);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.getMediaController(MainActivity.this)
                        .getTransportControls()
                        .skipToNext();
            }
        });

        mBtnPrev = (ImageButton) findViewById(R.id.btnPrev);
        mBtnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.getMediaController(MainActivity.this)
                        .getTransportControls()
                        .skipToPrevious();
            }
        });

        mTitle = (TextView) findViewById(R.id.title);
        mSubtitle = (TextView) findViewById(R.id.artist);
        mAlbumArt = (ImageView) findViewById(R.id.album_art);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMusicLibrary.updateLibrary().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                mMediaBrowser = new MediaBrowserCompat(MainActivity.this,
                        new ComponentName(MainActivity.this, BackgroundAudioService.class), mConnectionCallback, null);
                mMediaBrowser.connect();
            }
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
        if (state == null
                || state.getState() == PlaybackState.STATE_PAUSED
                || state.getState() == PlaybackState.STATE_STOPPED) {
            mPlayPause.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_36dp));
        } else {
            mPlayPause.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_pause_black_36dp));
        }
        mPlaybackControls.setVisibility(state == null ? View.GONE : View.VISIBLE);
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        mCurrentMetadata = metadata;
        mTitle.setText(metadata == null ? "" : metadata.getDescription().getTitle());
        mSubtitle.setText(metadata == null ? "" : metadata.getDescription().getSubtitle());
        mAlbumArt.setImageBitmap(
                metadata == null
                        ? null
                        : mMusicLibrary.getAlbumBitmap(
                        this, Long.valueOf(metadata.getDescription().getMediaId())));
        mBrowserAdapter.notifyDataSetChanged();
    }

    // Displays list of browsed MediaItems.
    private class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {

        public BrowseAdapter(Activity context) {
            super(context, R.layout.media_list_item, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            MediaBrowserCompat.MediaItem item = getItem(position);
            int itemState = MediaItemViewHolder.STATE_NONE;
            if (item.isPlayable()) {
                String itemMediaId = item.getDescription().getMediaId();
                int playbackState = PlaybackStateCompat.STATE_NONE;
                itemState = MediaItemViewHolder.STATE_PLAYABLE;
                if (mCurrentState != null) {
                    playbackState = mCurrentState.getState();
                }
                if (mCurrentMetadata != null && itemMediaId.equals(mCurrentMetadata.getDescription().getMediaId())) {
                    if (playbackState == PlaybackState.STATE_PLAYING || playbackState == PlaybackState.STATE_BUFFERING) {
                        itemState = MediaItemViewHolder.STATE_PLAYING;
                    } else if (playbackState != PlaybackState.STATE_ERROR) {
                        itemState = MediaItemViewHolder.STATE_PAUSED;
                    }
                }
            }
            return MediaItemViewHolder.setupView(
                    (Activity) getContext(), convertView, parent, item.getDescription(), itemState);
        }
    }

    private final View.OnClickListener mPlaybackButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int state = mCurrentState == null ? PlaybackStateCompat.STATE_NONE : mCurrentState.getState();
                    if (state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_STOPPED || state == PlaybackState.STATE_NONE) {
                        if (mCurrentMetadata == null) {
                            mCurrentMetadata = mMusicLibrary.getMetadata(MainActivity.this,
                                            Long.valueOf(mMusicLibrary.getMediaItems().get(0).getMediaId()));
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_radio:
                break;
            case R.id.nav_station_manager:
                Intent stationExplorer = new Intent(this, StationExplorerActivity.class);
                startActivity(stationExplorer);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
