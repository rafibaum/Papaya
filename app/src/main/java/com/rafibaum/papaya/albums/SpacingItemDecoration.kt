package com.rafibaum.papaya.albums

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(private val spans: Int, private val spacing: Int) :
    RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spans

        outRect.left = spacing - column * spacing / spans
        outRect.right = (column + 1) * spacing / spans
        outRect.bottom = spacing

        if (position < spans) {
            outRect.top = spacing
        }
    }
}