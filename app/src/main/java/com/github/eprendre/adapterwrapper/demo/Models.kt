package com.github.eprendre.adapterwrapper.demo

/**
 * Created by eprendre on 07/05/2017.
 */
data class MyItem(
    val position: Int
) {

  override fun hashCode(): Int{
    return position
  }

  override fun equals(other: Any?): Boolean{
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as MyItem

    if (position != other.position) return false

    return true
  }
}
