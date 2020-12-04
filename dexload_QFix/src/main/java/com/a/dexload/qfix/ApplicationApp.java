package com.a.dexload.qfix;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.a.fix.M;
import com.a.library.DexInstaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ApplicationApp extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

        DexInstaller.installDex(base, this.getExternalCacheDir().getAbsolutePath() + "/patch.dex");

        ResolveTool.resolvePatchClasses(base);

//        Log.d("alvin", "bug class:" + com.a.fix.M.class);
    }

}
