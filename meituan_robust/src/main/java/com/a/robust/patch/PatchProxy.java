package com.a.robust.patch;

import android.text.TextUtils;

import java.util.concurrent.CopyOnWriteArrayList;


public class PatchProxy {

    public static boolean isSupport(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber) {
        //Robust补丁优先执行，其他功能靠后
        if (changeQuickRedirect == null) {
            return false;
        }
        try {
            return changeQuickRedirect.isSupport( paramsArray, current, isStatic,  methodNumber);
        } catch (Throwable t) {
            return false;
        }
    }

    public static Object accessDispatch(Object[] paramsArray, Object current, ChangeQuickRedirect changeQuickRedirect, boolean isStatic, int methodNumber) {

        if (changeQuickRedirect == null) {
            return null;
        }

        return changeQuickRedirect.accessDispatch( paramsArray, current,isStatic,methodNumber);
    }

}
