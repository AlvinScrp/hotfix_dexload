package com.a.robust.patchexecute;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.a.robust.patch.EnhancedRobustUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class PatchExecutor {
    /**
     * 真正执行补丁加载，代码也是超级简单
     *
     * @param context
     */
    public static void doPatch(Context context) {
        try {

            File classMapFile = copyAssetsToExternalCache(context, "classMap.txt");
            File patchFile = copyAssetsToExternalCache(context, "robust_patch.dex");
            Map<String, String> classMap = generatePatchClassMap(context, classMapFile);
            ClassLoader classLoader = generateClassLoader(context, patchFile);

            /**
             * classMap--> [com.a.robust.data.M:com.a.robust.data.MPatchControl]
             */
            for (Map.Entry<String, String> entry : classMap.entrySet()) {
                Class originClass = classLoader.loadClass(entry.getKey());
                Class patchControlClass = classLoader.loadClass(entry.getValue());
                /**
                 * 反射调用，MPatchControl对象设置到M.changeQuickRedirect静态变量
                 */
                EnhancedRobustUtils.setStaticFieldValue("changeQuickRedirect", originClass, patchControlClass.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * patch dex的classLoader，parent为context.getClassLoader()
     *
     * @param context
     * @param patchFile
     * @return
     */
    private static ClassLoader generateClassLoader(Context context, File patchFile) {
        return new DexClassLoader(
                patchFile.getAbsolutePath(),
                patchFile.getParentFile().getAbsolutePath(),
                null, context.getClassLoader());
    }

    /**
     * 从Assets 复制到外部缓存，模拟补丁下载
     *
     * @param context
     * @param assetFileName
     * @return
     */
    private static File copyAssetsToExternalCache(Context context, String assetFileName) {
        String hackPath = context.getExternalCacheDir().getAbsolutePath() + "/" + assetFileName;
        File destFile = new File(hackPath);
        if (destFile.exists()) {
            destFile.delete();
        }
        try {
            InputStream is = context.getAssets().open(assetFileName);
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return destFile;


    }

    /**
     * 把classMap.txt 还原成Map--> [com.a.robust.data.M:com.a.robust.data.MPatchControl]
     * 与robust不同，robust是生成类方式，存放map
     *
     * @param context
     * @param classMapFile
     * @return
     */
    public static Map<String, String> generatePatchClassMap(Context context, File classMapFile) {
        Map<String, String> classMap = new HashMap<>();
        try {

            BufferedReader br = new BufferedReader(new FileReader(classMapFile));
            String line = "";
            while (!TextUtils.isEmpty(line = br.readLine())) {
                String[] ss = line.split(":");
                classMap.put(ss[0], ss[1]);
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return classMap;
    }
}
