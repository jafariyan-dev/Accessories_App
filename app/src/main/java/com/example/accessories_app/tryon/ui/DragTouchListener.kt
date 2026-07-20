package com.example.accessories_app.tryon.ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class DragTouchListener : View.OnTouchListener {

    private var initialViewX = 0f
    private var initialViewY = 0f

    private var initialTouchX = 0f
    private var initialTouchY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(
        view: View,
        event: MotionEvent
    ): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialViewX = view.x
                initialViewY = view.y

                initialTouchX = event.rawX
                initialTouchY = event.rawY

                true
            }

            MotionEvent.ACTION_MOVE -> {
                view.x =
                    initialViewX + event.rawX - initialTouchX

                view.y =
                    initialViewY + event.rawY - initialTouchY

                true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                view.performClick()
                true
            }

            else -> false
        }
    }
}