package com.prisyazhnuy.radioplayer.services;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.prisyazhnuy.radioplayer.R;

/**
 * Dell on 20.08.2017.
 */

public class MediaItemViewHolder {

    ImageView mImageView;
    TextView mTitleView;
    TextView mDescriptionView;

    public static View setupView(Activity activity, View convertView, ViewGroup parent, MediaDescriptionCompat description, int state) {
        MediaItemViewHolder holder;

        Integer cachedState = PlaybackStateCompat.STATE_NONE;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.media_list_item, parent, false);
            holder = new MediaItemViewHolder();
            holder.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
            holder.mTitleView = (TextView) convertView.findViewById(R.id.title);
            holder.mDescriptionView = (TextView) convertView.findViewById(R.id.description);
            convertView.setTag(holder);
        } else {
            holder = (MediaItemViewHolder) convertView.getTag();
            cachedState = (Integer) convertView.getTag(R.id.tag_mediaitem_state_cache);
        }

        holder.mTitleView.setText(description.getTitle());
        holder.mDescriptionView.setText(description.getSubtitle());

        // If the state of convertView is different, we need to adapt the view to the
        // new state.
        if (cachedState == null || cachedState != state) {
            switch (state) {
                case PlaybackStateCompat.STATE_NONE:
                    holder.mImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_arrow_black_36dp));
                    holder.mImageView.setVisibility(View.VISIBLE);
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    AnimationDrawable animation = (AnimationDrawable) activity.getResources().getDrawable(R.drawable.ic_equalizer_white_36dp);
                    holder.mImageView.setImageDrawable(animation);
                    holder.mImageView.setVisibility(View.VISIBLE);
                    animation.start();
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    try {
                        AnimationDrawable animation1 = (AnimationDrawable) holder.mImageView.getDrawable();
                        if (animation1 != null) {
                            animation1.stop();
                        }
                    } catch (ClassCastException e) {
                        holder.mImageView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_equalizer1_white_36dp));
                    }
                    holder.mImageView.setVisibility(View.VISIBLE);
                    break;
                case PlaybackStateCompat.STATE_BUFFERING:
                    holder.mImageView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.favorite_checkbox_icon));
                    holder.mImageView.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.mImageView.setVisibility(View.GONE);
            }
            convertView.setTag(R.id.tag_mediaitem_state_cache, state);
        }

        return convertView;
    }
}
