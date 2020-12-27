package com.a.robust;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

//import com.a.library.DexInstaller;

public class ApplicationApp extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

//        DexInstaller.installDex(base, this.getExternalCacheDir().getAbsolutePath() + "/patch.dex");

//        ResolveTool.resolvePatchClasses(base);

//        Log.d("alvin", "bug class:" + com.a.fix.M.class);
    }

}
