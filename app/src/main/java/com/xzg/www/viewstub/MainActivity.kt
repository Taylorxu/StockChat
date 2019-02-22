package com.xzg.www.viewstub

import android.graphics.drawable.TransitionDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.failed_view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler().postDelayed({
            stub_import.inflate()
            var transitionDrawable = ll_include.background as TransitionDrawable
            transitionDrawable.startTransition(3000)
            text_view.visibility = View.GONE
            progress_circular.visibility = View.GONE
        }, 6000)
    }
}
