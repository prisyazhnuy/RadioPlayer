package com.prisyazhnuy.radioplayer.db;

import android.util.Log;

import com.prisyazhnuy.radioplayer.db.migration.Migration;
import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Dell on 23.07.2017.
 */

public class DBService {
    private RealmConfiguration mConfig = new RealmConfiguration.Builder()
            .schemaVersion(4)
            .migration(new Migration())
            .initialData(new InitialStationsTransaction())
            .build();

    public Observable<Station> deleteStation(long id) {
        return Observable.just(id)
                .map(new Function<Long, Station>() {
                    @Override
                    public Station apply(Long id) throws Exception {
                        Realm realm = Realm.getInstance(mConfig);
                        realm.beginTransaction();
                        StationRealmModel model = realm.where(StationRealmModel.class).equalTo("id", id).findFirst();
                        Station station = new Station(model);
                        model.deleteFromRealm();
                        realm.commitTransaction();
                        realm.close();
                        return station;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Station> updateList(List<Station> items) {
        return Observable.fromIterable(items)
                .flatMap(new Function<Station, ObservableSource<Station>>() {
                    @Override
                    public ObservableSource<Station> apply(Station station) throws Exception {
                        return Observable.just(station)
                                .map(new Function<Station, Station>() {
                                    @Override
                                    public Station apply(Station station) throws Exception {
                                        Log.d("DBService", "thread: " + Thread.currentThread());
                                        final Realm realm = Realm.getInstance(mConfig);
                                        realm.beginTransaction();
                                        StationRealmModel model = realm.where(StationRealmModel.class)
                                                .equalTo("id", station.getId())
                                                .findFirst();
                                        if (model != null) {
                                            model.setFavourite(station.isFavourite());
                                            model.setPosition(station.getPosition());
                                            model.setUrl(station.getUrl());
                                            model.setName(station.getName());
                                            model.setSubname(station.getSubname());
                                        }
                                        realm.commitTransaction();
                                        realm.close();
                                        return station;
                                    }
                                })
                                .subscribeOn(Schedulers.computation());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Long> updateTime(long stationId, final long time) {
        return Observable.just(stationId)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long id) throws Exception {
                        final Realm realm = Realm.getInstance(mConfig);
                        realm.beginTransaction();
                        StationRealmModel model = realm.where(StationRealmModel.class)
                                .equalTo("id", id)
                                .findFirst();
                        if (model != null) {
                            long totalTime  = model.getTime() + time;
                            model.setTime(totalTime);
                        }
                        realm.commitTransaction();
                        realm.close();
                        return id;
                    }
                });
    }

    public Observable<Long> saveStation(Station station) {
        return Observable.just(station)
                .flatMap(new Function<Station, ObservableSource<StationRealmModel>>() {
                    @Override
                    public ObservableSource<StationRealmModel> apply(Station station) throws Exception {
                        StationRealmModel model = new StationRealmModel();
                        model.setFavourite(station.isFavourite());
                        model.setName(station.getName());
                        model.setUrl(station.getUrl());
                        model.setPosition(station.getPosition());
                        model.setSubname(station.getSubname());
                        return Observable.just(model);
                    }
                })
                .map(new Function<StationRealmModel, Long>() {
                    @Override
                    public Long apply(StationRealmModel stationRealmModel) throws Exception {
                        Realm realm = Realm.getInstance(mConfig);
                        realm.beginTransaction();
                        long id;
                        int position;
                        try {
                            id = realm.where(StationRealmModel.class).max("id").longValue() + 1;
                            position = realm.where(StationRealmModel.class).max("position").intValue() + 1;
                        } catch (Exception e) {
                            id = 0L;
                            position = 0;
                        }
                        stationRealmModel.setId(id);
                        stationRealmModel.setPosition(position);
                        realm.copyToRealm(stationRealmModel);
                        realm.commitTransaction();
                        realm.close();
                        return id;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Station>> getAll() {
        return Observable.create(new ObservableOnSubscribe<List<Station>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Station>> e) throws Exception {
                Log.d("DBService", "thread: " + Thread.currentThread());
                Realm realm = Realm.getInstance(mConfig);
                realm.beginTransaction();
                RealmResults<StationRealmModel> stationsModels = realm.where(StationRealmModel.class)
                        .findAllSorted("position", Sort.DESCENDING);
                List<Station> stations = new ArrayList<>(stationsModels.size());
                for (StationRealmModel model : stationsModels) {
                    Station station = new Station(model.getId(), model.getName(), model.getSubname(),
                            model.getUrl(), model.getPosition(), model.isFavourite());
                    station.setTime(model.getTime());
                    stations.add(station);
                }
                realm.commitTransaction();
                realm.close();
                if (!e.isDisposed()) {
                    e.onNext(stations);
                    e.onComplete();
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Station>> getFavourite() {
        return Observable.create(new ObservableOnSubscribe<List<Station>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Station>> e) throws Exception {
                Log.d("DBService", "thread: " + Thread.currentThread());
                Realm realm = Realm.getInstance(mConfig);
                realm.beginTransaction();
                RealmResults<StationRealmModel> stationsModels = realm.where(StationRealmModel.class)
                        .equalTo("isFavourite", true)
                        .findAllSorted("position", Sort.DESCENDING);
                List<Station> stations = new ArrayList<>(stationsModels.size());
                for (StationRealmModel model : stationsModels) {
                    Station station = new Station(model.getId(), model.getName(), model.getSubname(),
                            model.getUrl(), model.getPosition(), model.isFavourite());
                    station.setTime(model.getTime());
                    stations.add(station);
                }
                realm.commitTransaction();
                realm.close();
                if (!e.isDisposed()) {
                    e.onNext(stations);
                    e.onComplete();
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
