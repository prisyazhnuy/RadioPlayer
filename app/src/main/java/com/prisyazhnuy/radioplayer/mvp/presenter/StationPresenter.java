package com.prisyazhnuy.radioplayer.mvp.presenter;

import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.prisyazhnuy.radioplayer.mvp.view.StationExplorerView;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public interface StationPresenter extends MvpPresenter<StationExplorerView> {
    void loadStations();
}
