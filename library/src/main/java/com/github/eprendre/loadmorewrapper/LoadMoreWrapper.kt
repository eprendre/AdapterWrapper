package com.github.eprendre.loadmorewrapper

import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.ViewGroup

@Suppress("UNCHECKED_CAST")
class LoadMoreWrapper(inner: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
                      val loadMore: (loadMoreWrapper: LoadMoreWrapper) -> Unit) : BaseWrapper(inner as RecyclerView.Adapter<RecyclerView.ViewHolder>) {
  companion object {
    val ITEM_TYPE_LOAD_MORE_IDLE = -999
    val ITEM_TYPE_LOAD_MORE_LOADING = -1000
    val ITEM_TYPE_LOAD_MORE_DONE = -1001
    val ITEM_TYPE_LOAD_MORE_ERROR = -1002
    val ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN = -1003
    val ITEM_TYPE_LOAD_MORE_DISABLE = -1004
  }

  private var itemType = ITEM_TYPE_LOAD_MORE_DISABLE

  fun changeItemType(type: Int) {
    Handler().post {
      val isInsert = (itemType == ITEM_TYPE_LOAD_MORE_DISABLE && type != itemType)
      if (type >= ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN && type <= ITEM_TYPE_LOAD_MORE_IDLE) {
        itemType = type
        if (isInsert) {
          notifyItemInserted(itemCount - 1)
        } else {
          notifyItemChanged(itemCount - 1)
        }
      } else {
        if (itemType != ITEM_TYPE_LOAD_MORE_DISABLE) {
          itemType = ITEM_TYPE_LOAD_MORE_DISABLE
          notifyItemRemoved(itemCount)
        }
      }
    }
  }

  fun notifyData(notifyAdapter: ()-> Unit, finalItemType: Int) {
    if (itemType == ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN) {
      changeItemType(ITEM_TYPE_LOAD_MORE_DISABLE)
    }
    Handler().post { notifyAdapter() }
    changeItemType(finalItemType)
  }

  override fun getItemViewType(position: Int): Int {
    if (isPositionLoadMore(position)) {
      return itemType
    }
    return innerAdapter.getItemViewType(position)
  }

  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      ITEM_TYPE_LOAD_MORE_IDLE -> LoadMoreViewHolder(parent!!)
      ITEM_TYPE_LOAD_MORE_LOADING -> LoadMoreViewHolder(parent!!)
      ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN -> LoadMoreFullScreenViewHolder(parent!!)
      ITEM_TYPE_LOAD_MORE_DONE -> LoadMoreDoneViewHolder(parent!!)
      ITEM_TYPE_LOAD_MORE_ERROR -> LoadMoreErrorViewHolder(parent!!)
      else -> innerAdapter.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
    if (bindViewHolder(position, holder)) {
      return
    }
    innerAdapter.onBindViewHolder(holder, position)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int, payloads: MutableList<Any>?) {
    if (bindViewHolder(position, holder)) {
      return
    }
    innerAdapter.onBindViewHolder(holder, position, payloads)
  }

  private fun bindViewHolder(position: Int, holder: RecyclerView.ViewHolder?): Boolean {
    if (isPositionLoadMore(position)) {
      when (itemType) {
        ITEM_TYPE_LOAD_MORE_IDLE -> {
          if (holder is LoadMoreViewHolder) {
            changeItemType(ITEM_TYPE_LOAD_MORE_LOADING)
            loadMore(this)
          }
        }
        ITEM_TYPE_LOAD_MORE_ERROR -> {
          if (holder is LoadMoreErrorViewHolder) {
            holder.setLoadMoreListener {
              changeItemType(ITEM_TYPE_LOAD_MORE_IDLE)
            }
          }
        }
      }
      return true
    }
    return false
  }


  override fun getItemCount(): Int {
    return innerAdapter.itemCount + if (itemType == ITEM_TYPE_LOAD_MORE_DISABLE) 0 else 1
  }

  override fun getItemId(position: Int): Long {
    if (isPositionLoadMore(position)) {
      return itemType.toLong()
    }
    return innerAdapter.getItemId(position)
  }

  fun isPositionLoadMore(position: Int): Boolean {
    return position >= innerAdapter.itemCount
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
    val manager = recyclerView!!.layoutManager
    if (manager is GridLayoutManager) {
      manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
          val type = getItemViewType(position)
          return if (type == ITEM_TYPE_LOAD_MORE_IDLE ||
              type == ITEM_TYPE_LOAD_MORE_LOADING ||
              type == ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN ||
              type == ITEM_TYPE_LOAD_MORE_DONE ||
              type == ITEM_TYPE_LOAD_MORE_ERROR)
            manager.spanCount else 1
        }
      }
    }
    innerAdapter.onAttachedToRecyclerView(recyclerView)
  }

  override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder?) {
    if (isLoadMoreHolder(holder)) {
      val lp = holder!!.itemView.layoutParams
      if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams) {
        lp.isFullSpan = true
      }
    } else {
      innerAdapter.onViewAttachedToWindow(holder)
    }
  }

  fun isLoadMoreHolder(holder: RecyclerView.ViewHolder?): Boolean {
    if (isPositionLoadMore(holder!!.layoutPosition) ||
        (holder is LoadMoreViewHolder || holder is LoadMoreDoneViewHolder || holder is LoadMoreErrorViewHolder)) {
      return true
    }
    return false
  }

  override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder?) {
    if (isLoadMoreHolder(holder)) {
      return
    }
    super.onViewDetachedFromWindow(holder)
  }

  override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
    if (isLoadMoreHolder(holder)) {
      return
    }
    super.onViewRecycled(holder)
  }
}