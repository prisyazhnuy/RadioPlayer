package com.prisyazhnuy.radioplayer.mvp.view;

import com.hannesdorfmann.mosby3.mvp.MvpView;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.List;

/**
 * Dell on 23.07.2017.
 */

public interface StationExplorerView extends MvpView {
    void showStations(List<Station> stations);

    void showEmptyList();

    void showDeleteResult(Station station);

    void showUpdateResult();

    void showEditStationDialog(Station station);
}
