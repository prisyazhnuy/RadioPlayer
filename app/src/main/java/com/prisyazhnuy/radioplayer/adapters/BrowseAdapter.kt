package com.prisyazhnuy.radioplayer.adapters

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.prisyazhnuy.radioplayer.R
import com.prisyazhnuy.radioplayer.Utils
import java.lang.ref.WeakReference

interface BrowseListener {
    fun browseStation(item: MediaBrowserCompat.MediaItem?)
}

class BrowseAdapter(
    context: Context,
    var currentState: PlaybackStateCompat?,
    var currentMetadata: MediaMetadataCompat?,
    listener: BrowseListener?
) : BaseAdapter<MediaBrowserCompat.MediaItem, BrowseAdapter.BrowseViewHolder>(context) {

    private val weakListener = WeakReference(listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseViewHolder =
        BrowseViewHolder(inflater.inflate(R.layout.media_list_item, parent, false))

    override fun onBindViewHolder(holder: BrowseViewHolder, position: Int) {
        position.takeUnless { it == RecyclerView.NO_POSITION }
            ?.let { holder.bind(getItem(it), currentState, currentMetadata) }
    }

    inner class BrowseViewHolder(view: View) : BaseViewHolder<MediaBrowserCompat.MediaItem>(view) {

        var container: ViewGroup = view.findViewById(R.id.container)
        var ivPlay: ImageView? = view.findViewById(R.id.play_eq)
        var titleView: TextView? = view.findViewById(R.id.title)
        var descriptionView: TextView? = view.findViewById(R.id.description)

        fun bind(
            item: MediaBrowserCompat.MediaItem,
            currentState: PlaybackStateCompat?,
            currentMetadata: MediaMetadataCompat?
        ) {
            var state = PlaybackStateCompat.STATE_NONE
            if (item.isPlayable) {
                val itemMediaId = item.description.mediaId
                var playbackState = PlaybackStateCompat.STATE_NONE
                //                itemState = MediaItemViewHolder.STATE_PLAYABLE;
                if (currentState != null) {
                    playbackState = currentState.state
                }
                if (currentMetadata != null && TextUtils.equals(
                        itemMediaId,
                        currentMetadata.description.mediaId
                    )
                ) {
                    state = playbackState

//                    if (playbackState == PlaybackState.STATE_PLAYING || playbackState == PlaybackState.STATE_BUFFERING) {
//                        itemState = PlaybackStateCompat.STATE_PLAYING;
//                    } else if (playbackState != PlaybackState.STATE_ERROR) {
//                        itemState = PlaybackStateCompat.STATE_PAUSED;
//                    }
                }
            }

            titleView?.text = item.description.title
            descriptionView?.text = item.description.subtitle

            // If the state of convertView is different, we need to adapt the view to the
            // new state.
            when (state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    val animation = ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_equalizer_white_36dp
                    ) as AnimationDrawable
                    ivPlay?.setImageDrawable(animation)
                    ivPlay?.visibility = View.VISIBLE
                    animation.start()
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    ivPlay?.setImageResource(R.drawable.ic_equalizer1_white_36dp)
                    ivPlay?.visibility = View.VISIBLE
                }
                PlaybackStateCompat.STATE_CONNECTING,
                PlaybackStateCompat.STATE_BUFFERING -> {
                    ivPlay?.setImageDrawable(
                        Utils.createProgressAnimation(
                            context,
                            2,
                            ContextCompat.getColor(context, R.color.media_item_icon_not_playing),
                            8
                        )
                    )
                    ivPlay?.visibility = View.VISIBLE
                }
                else -> ivPlay?.visibility = View.INVISIBLE
            }
            container.setOnClickListener {
                weakListener.get()?.browseStation(item)
            }
        }

        override fun bind(item: MediaBrowserCompat.MediaItem) = Unit

    }

}