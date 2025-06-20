package com.example.hangsambal.util

interface ItemClickListener<T> {
    fun onClickItem(item : T)
    fun onClickMap(item : T)
}