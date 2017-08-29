package com.prisyazhnuy.radioplayer.mvp.presenter;

import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.view.FillDataView;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public interface FillStationPresenter extends MvpPresenter<FillDataView> {
    void addStation(String name, String subname, String url, boolean isFavorite);

    void fillStation(Station station);

    void updateStation(Station station);
}
