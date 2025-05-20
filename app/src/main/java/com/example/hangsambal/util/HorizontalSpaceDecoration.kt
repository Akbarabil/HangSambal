package com.example.hangsambal.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceDecoration(val leftSpace: Int = 0, val rightSpace: Int = 0, val topSpace: Int = 0, val bottomSpace: Int = 0, val multiplier: Int = 1) : RecyclerView.ItemDecoration() {

    constructor(space: Int = 0, multiplier: Int = 1) : this(space, space, space, space, multiplier)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        when {
            parent.getChildLayoutPosition(view) == 0 -> {
                outRect.left = leftSpace * multiplier
                outRect.right = rightSpace
            }

            parent.getChildLayoutPosition(view) == parent.adapter?.itemCount?.minus(1) -> {
                outRect.left = leftSpace
                outRect.right = rightSpace * multiplier
            }

            else -> {
                outRect.left = leftSpace
                outRect.right = rightSpace
            }
        }

        outRect.top = topSpace
        outRect.bottom = bottomSpace
    }
}