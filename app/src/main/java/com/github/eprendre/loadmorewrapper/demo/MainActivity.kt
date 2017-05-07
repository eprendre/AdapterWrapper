package com.github.eprendre.loadmorewrapper.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.github.eprendre.loadmorewrapper.LoadMoreWrapper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
  val items by lazy { ArrayList<MyItem>() }
  val adapter by lazy { MyAdapter(items) }
  var count = 1
  val loadMoreWrapper by lazy { LoadMoreWrapper(adapter, loadMoreListener) }

  val loadMoreListener: (loadMoreWrapper: LoadMoreWrapper) -> Unit = {
    doAsync {
      Thread.sleep(1000)
      val next = items.last().position + 1
      val newItems = (next..(next + 9)).map { MyItem(it) }
      items.addAll(newItems)
      count++

      uiThread {
        loadMoreWrapper.notifyDataSetChanged()
        if (count >= 3) {
          loadMoreWrapper.changeItemType(LoadMoreWrapper.ITEM_TYPE_LOAD_MORE_DONE)
        } else {
          loadMoreWrapper.changeItemType(LoadMoreWrapper.ITEM_TYPE_LOAD_MORE_IDLE)
        }
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
    recyclerView.adapter = loadMoreWrapper

    onRefresh()
  }

  override fun onRefresh() {
    swipeRefreshLayout.isRefreshing = true
    doAsync {
      Thread.sleep(1000)
      items.clear()
      items.addAll((1..20).map { MyItem(it) })
      count = 1

      uiThread {
        swipeRefreshLayout.isRefreshing = false

        loadMoreWrapper.notifyDataSetChanged()
        loadMoreWrapper.changeItemType(LoadMoreWrapper.ITEM_TYPE_LOAD_MORE_IDLE)
      }
    }
  }
}
