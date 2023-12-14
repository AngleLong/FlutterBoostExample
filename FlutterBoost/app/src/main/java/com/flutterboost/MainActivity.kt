package com.flutterboost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.idlefish.flutterboost.FlutterBoost
import com.idlefish.flutterboost.FlutterBoostRouteOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun jumpBtn(view: View) {
        val options = FlutterBoostRouteOptions.Builder()
            .pageName("mainPage")
            .arguments(hashMapOf())
            .build()
        FlutterBoost.instance().open(options)
    }
}