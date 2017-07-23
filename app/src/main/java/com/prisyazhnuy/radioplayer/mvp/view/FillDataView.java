package com.prisyazhnuy.radioplayer.mvp.view;

import com.hannesdorfmann.mosby3.mvp.MvpView;

/**
 * Created by Dell on 23.07.2017.
 */

public interface FillDataView extends MvpView {
    void successData();

    void failedData();
}
