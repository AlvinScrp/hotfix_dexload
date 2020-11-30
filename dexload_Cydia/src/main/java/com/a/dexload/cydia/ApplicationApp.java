package com.a.dexload.cydia;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.a.library.DexInstaller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ApplicationApp extends Application {
    static {
        System.loadLibrary("cydiahook");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        String[] dexFileNames = { "patch.dex"};
        for (String dexFileName : dexFileNames) {
            DexInstaller.installDexFromAssets(base,dexFileName);
        }
    }
}
