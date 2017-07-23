package com.prisyazhnuy.radioplayer.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.List;

/**
 * Created by Dell on 23.07.2017.
 */

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {

    private List<Station> mStations;
    private Context mContext;

    public StationAdapter(Context context, List<Station> items) {
        this.mContext = context;
        this.mStations = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.station_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Station item = mStations.get(position);
        holder.cbIsFavourite.setChecked(item.isFavourite());
        holder.tvName.setText(item.getName());
        holder.tvUrl.setText(item.getUrl());
    }

    @Override
    public int getItemCount() {
        return mStations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvUrl;
        private CheckBox cbIsFavourite;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvUrl = (TextView) itemView.findViewById(R.id.tvUrl);
            cbIsFavourite = (CheckBox) itemView.findViewById(R.id.cbIsFavourite);
        }
    }
}
