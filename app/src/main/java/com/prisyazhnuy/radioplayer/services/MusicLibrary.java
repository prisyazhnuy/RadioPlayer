package com.prisyazhnuy.radioplayer.services;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.prisyazhnuy.radioplayer.db.DBService;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.reactivex.Observable;
import io.realm.Realm;

/**
 * Dell on 20.08.2017.
 */

public class MusicLibrary {

    /*
    *     private static final String urlOnline = "http://cast.nrj.in.ua/nrj";
    private static final String urlPartyHits = "http://cast2.nrj.in.ua/nrj_party";
    private static final String urlAllHits = "http://cast2.nrj.in.ua/nrj_hits";
    private static final String urlTop40 = "http://cast2.nrj.in.ua/nrj_hot";
    private static final String urlKissFm = "http://online-kissfm.tavrmedia.ua/KissFM";
    private static final String urlKissFmDeep = "http://online-kissfm.tavrmedia.ua/KissFM_deep";
    private static final String urlKissFmDigital = "http://online-kissfm.tavrmedia.ua/KissFM_digital";
    private static final String urlKissFm32 = "http://online-kissfm.tavrmedia.ua/KissFM_32";
    * */

    private final Map<Long, MediaMetadataCompat> music = new LinkedHashMap<>();
    private final Map<Long, String> musicRes = new HashMap<>();

    private static MusicLibrary sInstance;
    private final DBService mDBService;

    private MusicLibrary(Context context) {
        Realm.init(context);
        mDBService = new DBService();
        updateLibrary().subscribe();
    }

    public Observable<Boolean> updateLibrary() {
        music.clear();
        musicRes.clear();
        return mDBService.getFavourite()
                .map(stations -> {
                    for (Station station : stations) {
                        createMediaMetadataCompat(station.getId(),
                                station.getName(),
                                station.getSubname(),
                                station.getUrl(),
                                (long) station.getPosition());
                    }
                    return true;
                });
    }

    public void updateTime(long stationId, long timeSec) {
        mDBService.updateTime(stationId, timeSec).subscribe(aLong -> {

        });
    }

    public static MusicLibrary getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MusicLibrary(context);
        }
        return sInstance;
    }

    public String getRoot() {
        return "root";
    }

    public String getSongUrl(Long mediaId) {
        return getMusicRes(mediaId);
    }

    private String getMusicRes(Long mediaId) {
        return musicRes.containsKey(mediaId) ? musicRes.get(mediaId) : "";
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : music.values()) {
            result.add(new MediaBrowserCompat.MediaItem(metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    public Long getPreviousSong(Long currentMediaId) {
        Log.d("MusicLibrary", "Current id: " + currentMediaId);
        Long prevId;
        LinkedList<Long> keyList = new LinkedList<>(music.keySet());
        int index = keyList.indexOf(currentMediaId);
        ListIterator<Long> iterator = keyList.listIterator(index);
        if (iterator.hasPrevious()) {
            prevId = iterator.previous();
        } else {
            prevId = keyList.get(keyList.size() - 1);
        }
        Log.d("MusicLibrary", "Prev id: " + prevId);
        return prevId;
    }

    public Long getNextSong(Long currentMediaId) {
        Log.d("MusicLibrary", "Current id: " + currentMediaId);
        Long nextId;
        LinkedList<Long> keyList = new LinkedList<>(music.keySet());
        int index = keyList.indexOf(currentMediaId);
        ListIterator<Long> iterator = keyList.listIterator(index);
        iterator.next();
        if (iterator.hasNext()) {
            nextId = iterator.next();
        } else {
            nextId = keyList.get(0);
        }
        Log.d("MusicLibrary", "Next id: " + nextId);
        return nextId;
    }

    public MediaMetadataCompat getMetadata(Long mediaId) {
        return music.get(mediaId);

        // Since MediaMetadataCompat is immutable, we need to create a copy to set the album art.
        // We don't set it initially on all items so that they don't take unnecessary memory.
//        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
//        for (String key :
//                new String[]{
//                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
//                        MediaMetadataCompat.METADATA_KEY_ALBUM,
//                        MediaMetadataCompat.METADATA_KEY_ARTIST,
//                        MediaMetadataCompat.METADATA_KEY_GENRE,
//                        MediaMetadataCompat.METADATA_KEY_TITLE,
//                        MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER
//                }) {
//            builder.putString(key, metadataWithoutBitmap.getString(key));
//        }
//        builder.putLong(
//                MediaMetadataCompat.METADATA_KEY_DURATION,
//                metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
//        return builder.build();
    }

    private void createMediaMetadataCompat(Long mediaId, String title, String artist,
                                           String url, Long position) {
        music.put(mediaId, new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(mediaId))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, position)
                .build());
        musicRes.put(mediaId, url);
    }
}
