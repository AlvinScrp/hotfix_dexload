# hotfix_dexlaod

基于各种热修复原理的简易实现

## dexload：
原始Dex加载方案，4.4Dalvik模式下会有pre-verified问题

## dexload_Cydia:
通过 native hook 拦截dvmResolveClass方法，更改方法的入口参数，将 fromUnverifiedConstant 统一改为 true

## dexload_QZone:
所有构造方法插桩，破坏referrer class的pre-verified，使得CLASS_ISPREVERIFIED为假。破坏pre-verified会导致性能问题

## dexload_QFix:
native方式提前resolve补丁包中所有类

## meituan_robust:
预先埋入hook代码，后续加载补丁后，hook执行补丁内容。
