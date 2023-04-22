package com.prisyazhnuy.radioplayer.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T : Any>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)
}