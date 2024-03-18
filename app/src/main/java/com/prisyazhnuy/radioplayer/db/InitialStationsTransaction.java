package com.prisyazhnuy.radioplayer.db;

import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;

/**
 * Dell on 30.08.2017.
 */

public class InitialStationsTransaction implements Realm.Transaction {
    @Override
    public void execute(final Realm realm) {
        List<Station> stations = initialStations();
        Observable.fromIterable(stations)
                .map(station -> {
                    StationRealmModel model = new StationRealmModel();
                    model.setId(station.getId());
                    model.setFavourite(station.isFavourite());
                    model.setPosition(station.getPosition());
                    model.setUrl(station.getUrl());
                    model.setSubname(station.getSubname());
                    model.setName(station.getName());
                    return model;
                })
                .subscribe(stationRealmModel -> realm.copyToRealm(stationRealmModel));
    }

    private List<Station> initialStations() {
        List<Station> stations = new ArrayList<>(5);
        Station energy = new Station(1, "Energy", "Online", "https://cast.mediaonline.net.ua/nrj320", 1, true);
        Station energy2 = new Station(2, "Lux FM", "Online", "https://streamvideo.luxnet.ua/lux/smil:lux.stream.smil/chunklist.m3u8", 2, true);
        Station record = new Station(3, "Radio rocks", "Online", "https://online.radioroks.ua/RadioROKS_HD", 3, true);
        Station kissFm = new Station(4, "Kiss fm", "Online", "https://online.kissfm.ua/KissFM_HD", 4, true);
        Station kissFm2 = new Station(5, "Kiss fm", "Digital", "https://online.kissfm.ua/KissFM_Digital_HD", 5, true);
        Station kissFmDeep = new Station(12, "Kiss fm", "Deep", "https://online.kissfm.ua/KissFM_Deep", 12, true);
        Station radioRecord = new Station(6, "Radio rocks", "Classic", "https://online.radioroks.ua/RadioROKS_ClassicRock_HD", 6, true);
        Station radioRocksHard = new Station(13, "Radio rocks", "Hard", "https://online.radioroks.ua/RadioROKS_HardnHeavy_HD", 13, true);
        Station hitFmHd = new Station(7, "Hit FM", "320", "https://online.hitfm.ua/HitFM_HD", 7, true);
        Station hitFm = new Station(8, "Hit FM", "128", "https://online.hitfm.ua/HitFM", 8, true);
        Station hitFmUkr = new Station(9, "Hit FM", "Українські хіти", "https://online.hitfm.ua/HitFM_Ukr", 9, true);
        Station hitFmBest = new Station(10, "Hit FM", "Best", "https://online.hitfm.ua/HitFM_Best_HD", 10, true);
        Station hitFmTop = new Station(11, "Hit FM", "Top", "https://online.hitfm.ua/HitFM_Top_HD", 11, true);
        Station nashe = new Station(14, "Наше радіо", "Online", "https://online.nasheradio.ua/NasheRadio_HD", 14, true);
        Station nasheTop = new Station(15, "Наше радіо", "Top", "https://online.nasheradio.ua/NasheRadio_Ukr_HD", 15, true);
        stations.addAll(Arrays.asList(energy, energy2, record, kissFm, kissFm2, radioRecord, kissFmDeep, radioRocksHard, hitFmHd, hitFm, hitFmUkr, hitFmBest, hitFmTop, nashe, nasheTop));
        return stations;
    }
}
