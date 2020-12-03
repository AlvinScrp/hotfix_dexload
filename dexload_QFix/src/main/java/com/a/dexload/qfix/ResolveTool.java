package com.a.dexload.qfix;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;

public class ResolveTool {
    static {
        System.loadLibrary("qfix");
    }

    public static void resolvePatchClasses(Context context) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(context.getExternalCacheDir().getAbsolutePath() + "/classIdx.txt"));
            String line = "";
            while (!TextUtils.isEmpty(line = br.readLine())) {
                String[] ss = line.split(":");
                //classes2.dex:com.a.Hack2:com.a.fix.M:2277
                if (ss != null && ss.length == 4) {
                    String hackClassName = ss[1];
                    long patchClassIdx = Long.parseLong(ss[3]);
                    Log.d("alvin", "readLine:" + line);
                    String hackClassDescriptor = "L" + hackClassName.replace('.', '/') + ";";
                    Log.d("alvin", "classNameToDescriptor: " + hackClassName + "  -->  " + hackClassDescriptor);
                    ResolveTool.loadClass(context, hackClassName);
                    ResolveTool.nativeResolveClass(hackClassDescriptor, patchClassIdx);
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * * "descriptor" should have the form "Ljava/lang/Class;" or
     * * "[Ljava/lang/Class;", i.e. a descriptor and not an internal-form
     * * class name.
     *
     * @param referrerDescriptor
     * @param classIdx
     * @return
     */
    public static native boolean nativeResolveClass(String referrerDescriptor, long classIdx);

    public static void loadClass(Context context, String className) {
        try {
            Log.d("alvin", context.getClassLoader().loadClass(className).getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("alvin", e.getMessage());
        }
    }


}
