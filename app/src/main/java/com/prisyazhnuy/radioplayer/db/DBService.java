package com.prisyazhnuy.radioplayer.db;

import android.util.Log;

import com.prisyazhnuy.radioplayer.db.migration.Migration;
import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
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
                .map(id1 -> {
                    Realm realm = Realm.getInstance(mConfig);
                    realm.beginTransaction();
                    StationRealmModel model = realm.where(StationRealmModel.class).equalTo("id", id1).findFirst();
                    Station station = new Station(model);
                    model.deleteFromRealm();
                    realm.commitTransaction();
                    realm.close();
                    return station;
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Station> updateList(List<Station> items) {
        return Observable.fromIterable(items)
                .flatMap((Function<Station, ObservableSource<Station>>) station -> Observable.just(station)
                        .map(station1 -> {
                            Log.d("DBService", "thread: " + Thread.currentThread());
                            final Realm realm = Realm.getInstance(mConfig);
                            realm.beginTransaction();
                            StationRealmModel model = realm.where(StationRealmModel.class)
                                    .equalTo("id", station1.getId())
                                    .findFirst();
                            if (model != null) {
                                model.setFavourite(station1.isFavourite());
                                model.setPosition(station1.getPosition());
                                model.setUrl(station1.getUrl());
                                model.setName(station1.getName());
                                model.setSubname(station1.getSubname());
                            }
                            realm.commitTransaction();
                            realm.close();
                            return station1;
                        })
                        .subscribeOn(Schedulers.computation()))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Long> updateTime(long stationId, final long time) {
        return Observable.just(stationId)
                .map(id -> {
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
                });
    }

    public Observable<Long> saveStation(Station station) {
        return Observable.just(station)
                .flatMap((Function<Station, ObservableSource<StationRealmModel>>) station1 -> {
                    StationRealmModel model = new StationRealmModel();
                    model.setFavourite(station1.isFavourite());
                    model.setName(station1.getName());
                    model.setUrl(station1.getUrl());
                    model.setPosition(station1.getPosition());
                    model.setSubname(station1.getSubname());
                    model.setTime(station1.getTime());
                    return Observable.just(model);
                })
                .map(stationRealmModel -> {
                    Realm realm = Realm.getInstance(mConfig);
                    realm.beginTransaction();
                    long id;
                    int position;
                    try {
                        id = realm.where(StationRealmModel.class).max("id").longValue() + 1;
                        position = realm.where(StationRealmModel.class).max("position").intValue() + 1;
                    } catch (Exception e) {
                        id = 0L;
                        position = -1;
                    }
                    stationRealmModel.setId(id);
                    stationRealmModel.setPosition(position);
                    realm.copyToRealm(stationRealmModel);
                    realm.commitTransaction();
                    realm.close();
                    return id;
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Station>> getAll() {
        return Observable.create((ObservableOnSubscribe<List<Station>>) e -> {
            Log.d("DBService", "thread: " + Thread.currentThread());
            Realm realm = Realm.getInstance(mConfig);
            realm.beginTransaction();
            RealmResults<StationRealmModel> stationsModels = realm.where(StationRealmModel.class)
                    .sort("position", Sort.ASCENDING)
                    .findAll();
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
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Station>> getFavourite() {
        return Observable.create((ObservableOnSubscribe<List<Station>>) e -> {
            Log.d("DBService", "thread: " + Thread.currentThread());
            Realm realm = Realm.getInstance(mConfig);
            realm.beginTransaction();
            RealmResults<StationRealmModel> stationsModels = realm.where(StationRealmModel.class)
                    .equalTo("isFavourite", true)
                    .sort("position", Sort.ASCENDING)
                    .findAll();
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
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
