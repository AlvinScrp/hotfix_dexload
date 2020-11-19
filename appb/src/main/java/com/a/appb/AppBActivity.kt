package com.a.appb

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_app_b.*

class AppBActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_b)


        var i=1
        var loader = classLoader
        if (loader != null) {
            Log.i("AppBActivity","ProcessId:${Process.myPid()} [onCreate] classLoader $i : $loader" )
            while (loader!!.parent != null) {
                loader = loader!!.parent
                i++
                Log.i("AppBActivity", "ProcessId:${Process.myPid()} [onCreate] classLoader $i : $loader")
            }
        }
        requestPermissions()
        tvText.text="sdsdsd334444"
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_PHONE_STATE
                    ),
                    1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (i in permissions.indices) {
                Log.i(
                    "MainActivity",
                    "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]
                )
            }
        }
    }



}
