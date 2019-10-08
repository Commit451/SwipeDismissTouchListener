package com.commit451.swipedismisstouchlistener.sample

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

import com.commit451.swipedismisstouchlistener.SwipeDismissTouchListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<ViewGroup>(R.id.root)

        val view = findViewById<View>(R.id.card1)
        val swipeDismissTouchListener = SwipeDismissTouchListener(view)
        swipeDismissTouchListener.setOnDismissListener { dismissView -> root.removeView(dismissView) }
        view.setOnTouchListener(swipeDismissTouchListener)
    }
}
