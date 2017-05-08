package com.github.eprendre.adapterwrapper

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class LoadMoreViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false))

class LoadingFullScreenViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading_fullscreen, parent, false))

class LoadMoreDoneViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading_done, parent, false))

class LoadMoreErrorViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading_error, parent, false)) {
  fun setLoadMoreListener(f: () -> Unit) {
    itemView.setOnClickListener { f() }
  }
}