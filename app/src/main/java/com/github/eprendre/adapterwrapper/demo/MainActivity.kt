package com.github.eprendre.adapterwrapper.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.github.eprendre.adapterwrapper.AdapterWrapper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
  val items by lazy { ArrayList<MyItem>() }
  val adapter by lazy { MyAdapter(items) }
  var maxCount = 50
  val adapterWrapper by lazy { AdapterWrapper(adapter, loadMoreListener) }
  val latency = 1000L

  val loadMoreListener: (adapterWrapper: AdapterWrapper) -> Unit = {
    doAsync {
      Thread.sleep(latency)
      val next = items.last().position + 1
      val newItems = (next..(next + 9)).map { MyItem(it) }

      uiThread {
        val type = if (items.size >= maxCount) {
          AdapterWrapper.ITEM_TYPE_LOAD_MORE_DONE
        } else {
          AdapterWrapper.ITEM_TYPE_LOAD_MORE_IDLE
        }

        adapterWrapper.notifyData({
          val oldItems = ArrayList(items)
          items.addAll(newItems)
          DiffUtil.calculateDiff(DiffCallback(oldItems, items)).dispatchUpdatesTo(adapterWrapper)
        }, type)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
    swipeRefreshLayout.setOnRefreshListener(this)

    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    recyclerView.adapter = adapterWrapper

    loadFullScreen()
  }

  fun loadFullScreen() {
    adapterWrapper.changeItemType(AdapterWrapper.ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN)
    onRefresh()
  }

  override fun onRefresh() {
    doAsync {
      Thread.sleep(latency)
      val newItems = (1..20).map { MyItem(it) }

      uiThread {
        swipeRefreshLayout.isRefreshing = false
        val type = if (items.size >= maxCount) {
          AdapterWrapper.ITEM_TYPE_LOAD_MORE_DONE
        } else {
          AdapterWrapper.ITEM_TYPE_LOAD_MORE_IDLE
        }
        adapterWrapper.notifyData({
          val oldItems = ArrayList(items)
          items.clear()
          items.addAll(newItems)
          DiffUtil.calculateDiff(DiffCallback(oldItems, items)).dispatchUpdatesTo(adapterWrapper)
        }, type)
      }
    }
  }
}
