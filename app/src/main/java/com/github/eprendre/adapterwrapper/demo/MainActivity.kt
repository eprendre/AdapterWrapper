package com.github.eprendre.adapterwrapper.demo

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.github.eprendre.adapterwrapper.AdapterWrapper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
  val items by lazy { ArrayList<MyItem>() }
  var maxCount = 60
  val adapterWrapper by lazy { AdapterWrapper(MyAdapter(items)) }
  val latency = 1000L

  var position = 1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
    swipeRefreshLayout.setOnRefreshListener(this)

    adapterWrapper.loadMoreListener = { load(false) }
    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    recyclerView.adapter = adapterWrapper

    loadFullScreen()
  }

  private fun loadFullScreen() {
    adapterWrapper.itemType = AdapterWrapper.ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN
    onRefresh()
  }

  override fun onRefresh() {
    load(true)
  }

  private fun load(isRefresh: Boolean) {
    doAsync {
      Thread.sleep(latency)
      val newItems = (position..(position + 14)).map { MyItem(it) }

      var diffResult: DiffUtil.DiffResult? = null
      val isError = Random().nextInt(3) == 0
      if (!isError) {
        val oldItems = ArrayList(items)
        if (isRefresh) {
          items.clear()
        }
        items.addAll(newItems)
        diffResult = DiffUtil.calculateDiff(DiffCallback(oldItems, items))
      }

      uiThread {
        swipeRefreshLayout.isRefreshing = false

        if (isError) {//Error
          if (isRefresh) {
            if (adapterWrapper.itemType == AdapterWrapper.ITEM_TYPE_LOAD_MORE_LOADING_FULLSCREEN) {
              adapterWrapper.itemType = AdapterWrapper.ITEM_TYPE_LOAD_MORE_DISABLE
            }
            toast("refresh error, pull to retry")
          } else {
            adapterWrapper.itemType = AdapterWrapper.ITEM_TYPE_LOAD_MORE_ERROR
          }
          return@uiThread
        }
        position += 15
        adapterWrapper.notifyData({
          diffResult?.dispatchUpdatesTo(adapterWrapper)

          val type = when {
            items.size >= maxCount -> AdapterWrapper.ITEM_TYPE_LOAD_MORE_DONE
            items.size == 0 -> AdapterWrapper.ITEM_TYPE_LOAD_MORE_EMPTY
            else -> AdapterWrapper.ITEM_TYPE_LOAD_MORE_IDLE
          }
          return@notifyData type
        }, isRefresh)
      }
    }
  }
}
