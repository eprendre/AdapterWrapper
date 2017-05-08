package com.github.eprendre.adapterwrapper.demo

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_mylist.view.*

/**
 * Created by eprendre on 07/05/2017.
 */
class MyAdapter(val data: List<MyItem>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
    val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_mylist, parent, false)
    return MyViewHolder(view)
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(holder: MyViewHolder?, position: Int) {
    holder?.bindData(data[position])
  }


  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bindData(item: MyItem) {
      itemView.textView.text = item.position.toString()
    }
  }
}
