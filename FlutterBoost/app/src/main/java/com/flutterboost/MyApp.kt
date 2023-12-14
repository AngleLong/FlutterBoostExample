package com.flutterboost

import android.app.Application
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.idlefish.flutterboost.FlutterBoost
import com.idlefish.flutterboost.FlutterBoostDelegate
import com.idlefish.flutterboost.FlutterBoostRouteOptions
import com.idlefish.flutterboost.containers.FlutterBoostActivity
import io.flutter.embedding.android.FlutterActivityLaunchConfigs


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FlutterBoost.instance().setup(this, object : FlutterBoostDelegate {
            override fun pushNativeRoute(options: FlutterBoostRouteOptions?) {
                Log.e("TAG======>", "pushNativeRoute: ${options.toString()}")
                if (TextUtils.equals(options?.pageName(), "second")) {
                    //这里根据options.pageName来判断你想跳转哪个页面，这里简单给一个
                    val intent = Intent(
                        FlutterBoost.instance().currentActivity(),
                        SecondActivity::class.java
                    )
                    FlutterBoost.instance().currentActivity()
                        .startActivityForResult(intent, options!!.requestCode())
                }

            }

            override fun pushFlutterRoute(options: FlutterBoostRouteOptions?) {
                Log.e("TAG======>", "pushFlutterRoute: ${options.toString()}")
                val intent =
                    FlutterBoostActivity.CachedEngineIntentBuilder(FlutterBoostActivity::class.java)
                        .backgroundMode(FlutterActivityLaunchConfigs.BackgroundMode.transparent)
                        .destroyEngineWithActivity(false)
                        .uniqueId(options!!.uniqueId())
                        .url(options.pageName())
                        .urlParams(options.arguments())
                        .build(FlutterBoost.instance().currentActivity())
                FlutterBoost.instance().currentActivity().startActivity(intent)
            }
        }) {
            Log.e("TAG======>", "onCreate: ")
        }
    }
}