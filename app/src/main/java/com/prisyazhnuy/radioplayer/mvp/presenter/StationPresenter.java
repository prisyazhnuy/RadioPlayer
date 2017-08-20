package com.prisyazhnuy.radioplayer.mvp.presenter;

import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.view.StationExplorerView;

import java.util.List;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public interface StationPresenter extends MvpPresenter<StationExplorerView> {
    void loadAllStations();

    void loadFavouriteStations();

    void removeStation(long id);
    void updatePosition(List<Station> items);
    void dispose();
    void stationClicked(Station station);
}
