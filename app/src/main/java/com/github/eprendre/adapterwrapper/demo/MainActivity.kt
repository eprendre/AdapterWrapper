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
  var count = 1
  val adapterWrapper by lazy { AdapterWrapper(adapter, loadMoreListener) }

  val loadMoreListener: (adapterWrapper: AdapterWrapper) -> Unit = {
    doAsync {
      Thread.sleep(1000)
      val next = items.last().position + 1
      val newItems = (next..(next + 9)).map { MyItem(it) }

      val oldItems = ArrayList(items)
      items.addAll(newItems)
      count++

      uiThread {

        val type = if (count >= 3) {
          AdapterWrapper.ITEM_TYPE_LOAD_MORE_DONE
        } else {
          AdapterWrapper.ITEM_TYPE_LOAD_MORE_IDLE
        }

        adapterWrapper.notifyData({
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
      Thread.sleep(1000)
      val newItems = (1..20).map { MyItem(it) }
      val oldItems = ArrayList(items)
      items.clear()
      items.addAll(newItems)
      count = 1

      uiThread {
        swipeRefreshLayout.isRefreshing = false
        adapterWrapper.notifyData({
          DiffUtil.calculateDiff(DiffCallback(oldItems, items)).dispatchUpdatesTo(adapterWrapper)
        }, AdapterWrapper.ITEM_TYPE_LOAD_MORE_IDLE)
      }
    }
  }
}
