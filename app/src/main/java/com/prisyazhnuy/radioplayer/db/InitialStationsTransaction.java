package com.prisyazhnuy.radioplayer.db;

import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.realm.Realm;

/**
 * Dell on 30.08.2017.
 */

public class InitialStationsTransaction implements Realm.Transaction {
    @Override
    public void execute(final Realm realm) {
        List<Station> stations = initialStations();
        Observable.fromIterable(stations)
                .map(new Function<Station, StationRealmModel>() {
                    @Override
                    public StationRealmModel apply(Station station) throws Exception {
                        StationRealmModel model = new StationRealmModel();
                        model.setId(station.getId());
                        model.setFavourite(station.isFavourite());
                        model.setPosition(station.getPosition());
                        model.setUrl(station.getUrl());
                        model.setSubname(station.getSubname());
                        model.setName(station.getName());
                        return model;
                    }
                })
                .subscribe(new Consumer<StationRealmModel>() {
                    @Override
                    public void accept(StationRealmModel stationRealmModel) throws Exception {
                        realm.copyToRealm(stationRealmModel);
                    }
                });
    }

    private List<Station> initialStations() {
        List<Station> stations = new ArrayList<>(5);
        Station energy = new Station(1, "Energy", "Online", "http://cast.nrj.in.ua/nrj", 1, true);
        Station energy2 = new Station(2, "Energy", "Top 40", "http://cast.nrj.in.ua/nrj_hot", 2, true);
        Station record = new Station(3, "Radio record", "Online", "http://air.radiorecord.ru:805/rr_320", 3, true);
        Station kissFm = new Station(4, "Kiss fm", "Online", "http://online-kissfm.tavrmedia.ua/KissFM", 4, true);
        Station kissFm2 = new Station(5, "Kiss fm", "Digital", "http://online-kissfm.tavrmedia.ua/KissFM_digital", 5, true);
        Station radioRecord = new Station(6, "Radio Record", "Online", "http://air2.radiorecord.ru:9003//rr_320", 6, true);
        stations.addAll(Arrays.asList(energy,energy2, record, kissFm, kissFm2, radioRecord));
        return stations;
    }
}
