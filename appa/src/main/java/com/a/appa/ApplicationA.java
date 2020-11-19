package com.a.appa;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ApplicationA extends Application {
    private static final String TAG = ApplicationA.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        String hackPath = copyHackAssetIfNeed();
        installDex(this, hackPath);

        String patchPath = Environment.getExternalStorageDirectory() + "/patch.dex";
        installDex(this, patchPath);
    }

    private String copyHackAssetIfNeed() {
        String hackPath = Environment.getExternalStorageDirectory() + "/hack_dex.jar";
        File destFile = new File(hackPath);
        if (destFile.exists() && destFile.isDirectory()) {
            destFile.delete();
        }
        if (!destFile.exists()) {
            try {
                InputStream is = getAssets().open("hack_dex.jar");
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
        }

        return destFile.getAbsolutePath();


    }

    private void installDex(Context context, String filePath) {

        File file = new File(filePath);
        Log.i(TAG, "" + file.exists());
        if (file.exists()) {
            //把布丁插入到pathList中（pathList进程变量，一个存放dex编译文件的集合）
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2){
                installDexh4_3(context, filePath);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                installDexh4_4(context, filePath);
            } else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                installDexAbove_4_4(context, filePath);
            }

        }

    }

    private void installDexh4_3(Context context, String patch) {

        //优化目录必须是私有目录
        File cacheDir = context.getCacheDir();

        //PathClassLoader
        ClassLoader classLoader = context.getClassLoader();

        try {
            //先获取pathList属性
            Field pathList = getField(classLoader, "pathList");
            //通过属性反射获取属性的对象 DexPathList
            Object pathListObject = pathList.get(classLoader);
            //通过 pathListObject 对象获取 pathList类中的dexElements 属性
            //原本的dex element数组
            Field dexElementsField = getField(pathListObject, "dexElements");

            //通过dexElementsField 属性获取它存在的对象
            Object[] dexElementsObject = (Object[]) dexElementsField.get(pathListObject);

            List<File> files = new ArrayList<>();

            File file = new File(patch);//补丁包
            if (file.exists()) {
                files.add(file);
            }
            //插桩所用到的类
//            files.add(antiazyFile);
            Method method = getMethod(pathListObject, "makeDexElements", ArrayList.class, File.class);
//            final List<IOException> suppressedExceptionList = new ArrayList<IOException>();
            //补丁的element数组
            Object[] patchElement = (Object[]) method.invoke(null, files, cacheDir);
            //用于替换系统原本的element数组
            Object[] newElement = (Object[]) Array.newInstance(dexElementsObject.getClass().getComponentType(),
                    dexElementsObject.length + patchElement.length);

            //合并复制element
            System.arraycopy(patchElement, 0, newElement, 0, patchElement.length);
            System.arraycopy(dexElementsObject, 0, newElement, patchElement.length, dexElementsObject.length);

            //  替换
            dexElementsField.set(pathListObject, newElement);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "installPatch=" + e.toString());
        }
    }


    private void installDexh4_4(Context context, String patch) {

        //优化目录必须是私有目录
        File cacheDir = context.getCacheDir();

        //PathClassLoader
        ClassLoader classLoader = context.getClassLoader();

        try {
            //先获取pathList属性
            Field pathList = getField(classLoader, "pathList");
            //通过属性反射获取属性的对象 DexPathList
            Object pathListObject = pathList.get(classLoader);
            //通过 pathListObject 对象获取 pathList类中的dexElements 属性
            //原本的dex element数组
            Field dexElementsField = getField(pathListObject, "dexElements");

            //通过dexElementsField 属性获取它存在的对象
            Object[] dexElementsObject = (Object[]) dexElementsField.get(pathListObject);

            List<File> files = new ArrayList<>();

            File file = new File(patch);//补丁包
            if (file.exists()) {
                files.add(file);
            }
            //插桩所用到的类
//            files.add(antiazyFile);
            Method method = getMethod(pathListObject, "makeDexElements", ArrayList.class, File.class, ArrayList.class);
            final List<IOException> suppressedExceptionList = new ArrayList<IOException>();
            //补丁的element数组
            Object[] patchElement = (Object[]) method.invoke(null, files, cacheDir, suppressedExceptionList);
            //用于替换系统原本的element数组
            Object[] newElement = (Object[]) Array.newInstance(dexElementsObject.getClass().getComponentType(),
                    dexElementsObject.length + patchElement.length);

            //合并复制element
            System.arraycopy(patchElement, 0, newElement, 0, patchElement.length);
            System.arraycopy(dexElementsObject, 0, newElement, patchElement.length, dexElementsObject.length);

            //  替换
            dexElementsField.set(pathListObject, newElement);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "installPatch=" + e.toString());
        }
    }


    /**
     * 执行修复
     */
    public static void installDexAbove_4_4(Context context, String patch) {
        //优化目录必须是私有目录
        File cacheDir = context.getCacheDir();

        //PathClassLoader
        ClassLoader classLoader = context.getClassLoader();

        try {
            //先获取pathList属性
            Field pathList = getField(classLoader, "pathList");
            //通过属性反射获取属性的对象 DexPathList
            Object pathListObject = pathList.get(classLoader);
            //通过 pathListObject 对象获取 pathList类中的dexElements 属性
            //原本的dex element数组
            Field dexElementsField = getField(pathListObject, "dexElements");

            //通过dexElementsField 属性获取它存在的对象
            Object[] dexElementsObject = (Object[]) dexElementsField.get(pathListObject);

            List<File> files = new ArrayList<>();

            File file = new File(patch);//补丁包
            if (file.exists()) {
                files.add(file);
            }
            //插桩所用到的类
//            files.add(antiazyFile);
            Method method = getMethod(pathListObject, "makeDexElements", List.class, File.class, List.class, ClassLoader.class);
            final List<IOException> suppressedExceptionList = new ArrayList<IOException>();
            //补丁的element数组
            Object[] patchElement = (Object[]) method.invoke(null, files, cacheDir, suppressedExceptionList, classLoader);
            //用于替换系统原本的element数组
            Object[] newElement = (Object[]) Array.newInstance(dexElementsObject.getClass().getComponentType(),
                    dexElementsObject.length + patchElement.length);

            //合并复制element
            System.arraycopy(patchElement, 0, newElement, 0, patchElement.length);
            System.arraycopy(dexElementsObject, 0, newElement, patchElement.length, dexElementsObject.length);

            //  替换
            dexElementsField.set(pathListObject, newElement);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "installPatch=" + e.toString());
        }
    }


    /**
     * 反射获取某个属性
     *
     * @param instance
     * @param name
     * @return
     */
    public static Field getField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> cls = instance.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Field declaredField = cls.getDeclaredField(name);
                //如果反射获取的类 方法 属性不是public 需要设置权限
                declaredField.setAccessible(true);
                return declaredField;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        throw new NoSuchFieldException("Field: " + name + " not found in " + instance.getClass());
    }


    /**
     * 反射获取某个属性
     *
     * @param instance
     * @param name
     * @return
     */
    public static Method getMethod(Object instance, String name, Class<?>... parameterTypes) throws NoSuchFieldException {
        for (Class<?> cls = instance.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Method methodMethod = cls.getDeclaredMethod(name, parameterTypes);
                //如果反射获取的类 方法 属性不是public 需要设置权限
                methodMethod.setAccessible(true);
                return methodMethod;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        throw new NoSuchFieldException("Field: " + name + " not found in " + instance.getClass());
    }


}
