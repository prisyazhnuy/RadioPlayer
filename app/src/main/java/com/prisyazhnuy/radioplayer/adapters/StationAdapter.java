package com.prisyazhnuy.radioplayer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.presenter.StationPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Dell on 23.07.2017.
 */

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private List<Station> mStations;
    private Context mContext;
    private final StationPresenter mPresenter;

    public StationAdapter(Context context, StationPresenter presenter, List<Station> items) {
        this.mContext = context;
        this.mStations = items;
        this.mPresenter = presenter;
    }

    public void clear() {
        mStations.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Station> stations) {
        mStations = new ArrayList<>(stations);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.station_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Station item = mStations.get(position);
        holder.cbIsFavourite.setOnCheckedChangeListener(null);
        holder.cbIsFavourite.setChecked(item.isFavourite());
        holder.cbIsFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Station station = mStations.get(holder.getAdapterPosition());
                station.setFavourite(isChecked);
                mPresenter.updatePosition(Collections.singletonList(station));
            }
        });
        holder.tvName.setText(item.getName());
        holder.tvSubName.setText(item.getSubname());
        holder.tvTime.setText(timeFormat(item.getTime()));
    }

    private String timeFormat(Long seconds) {
        if (seconds != null) {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long sec = seconds % 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, sec);
        } else {
            return "00:00:00";
        }
    }

    @Override
    public int getItemCount() {
        return mStations.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Station stationFrom = mStations.get(fromPosition);
        Station stationTo = mStations.get(toPosition);
        int posTmp = stationFrom.getPosition();
        stationFrom.setPosition(stationTo.getPosition());
        stationTo.setPosition(posTmp);
        List<Station> items = new ArrayList<>(2);
        items.add(stationFrom);
        items.add(stationTo);
        mPresenter.updatePosition(items);

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mStations, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mStations, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        if (mPresenter != null) {
            mPresenter.removeStation(mStations.get(position).getId());
        }
        mStations.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvSubName;
        private CheckBox cbIsFavourite;
        private TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvSubName = (TextView) itemView.findViewById(R.id.tvUrl);
            cbIsFavourite = (CheckBox) itemView.findViewById(R.id.cbIsFavourite);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPresenter != null) {
                        mPresenter.stationClicked(mStations.get(getAdapterPosition()));
                    }
                }
            });
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
        }
    }
}
