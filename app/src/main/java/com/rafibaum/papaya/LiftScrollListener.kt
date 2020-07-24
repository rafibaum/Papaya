package com.rafibaum.papaya

import android.view.View
import androidx.recyclerview.widget.RecyclerView

private const val SCROLL_DIRECTION_UP = -1

class LiftScrollListener(
    private val liftView: View,
    private val upElevation: Float = 10f,
    private val downElevation: Float = 0f
) : RecyclerView.OnScrollListener() {
    private var curElevation = liftView.translationZ

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val targetElevation = if (recyclerView.canScrollVertically(SCROLL_DIRECTION_UP)) {
            upElevation
        } else {
            downElevation
        }

        if (targetElevation != curElevation) {
            curElevation = targetElevation
            liftView.animate().translationZ(targetElevation)
        }
    }
}