package com.a.robust.patch;

public interface ChangeQuickRedirect {

    /**
     * 如果此方法支持补丁，则分发执行补丁内容，次接口实现中，调用补丁类的补丁方法
     * @param methodParams  被注入方法的参数
     * @param originObj   被注入方法所在类
     * @param isStatic    是否是静态方法
     * @param methodNumber  方法对应序列号，是我们注入过程中，递增生成的。 为后续生成自动补丁和运行补丁代码做准备
     * @return
     */
    Object accessDispatch(Object[] methodParams, Object originObj, boolean isStatic, int methodNumber);

    /**
     * 次方法是否支持补丁
     * @param methodParams
     * @param originObj
     * @param isStatic
     * @param methodNumber
     * @return
     */
    boolean isSupport(Object[] methodParams, Object originObj, boolean isStatic, int methodNumber);
}
