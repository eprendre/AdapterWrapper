package com.github.eprendre.loadmorewrapper.demo

import android.support.v7.util.DiffUtil

class DiffCallback<out T> constructor(val oldList: List<T>, val newList: List<T>) : DiffUtil.Callback() {

  override fun getNewListSize() = newList.size

  override fun getOldListSize() = oldList.size

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition] == newList[newItemPosition]
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return areContentsTheSame(oldItemPosition, newItemPosition)
  }
}
