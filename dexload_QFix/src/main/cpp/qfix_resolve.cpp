//
// Created by mawenqiang on 2020/12/1.
//
#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>

#define  LOG_TAG    "alvin"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


void *(*dvmFindLoadedClass)(const char *);

void *(*dvmResolveClass)(const void *, unsigned int, bool);


extern "C" jboolean Java_com_a_dexload_qfix_ResolveTool_nativeResolveClass(JNIEnv *env, jclass thiz,
                                                                           jstring referrerDescriptor,
                                                                           jlong classIdx) {
    LOGE("enter nativeResolveClass");

    void *handle = 0;
    handle = dlopen("/system/lib/libdvm.so", RTLD_LAZY);
    if (!handle) {
        LOGE("dlopen libdvm.so fail");
        return false;
    }

    const char *loadClassSymbols[3] = {
            "_Z18dvmFindLoadedClassPKc",
            "_Z18kvmFindLoadedClassPKc",
            "dvmFindLoadedClass"
    };
    for (int i = 0; i < 3; i++) {
        dvmFindLoadedClass = reinterpret_cast<void *(*)(const char *)>(
                dlsym(handle, loadClassSymbols[i]));
        if (dvmFindLoadedClass) {
            LOGE("dlsym dvmFindLoadedClass success %s", loadClassSymbols[i]);
            break;
        }
    }

    const char *resolveClassSymbols[2] = {
            "dvmResolveClass",
            "vResolveClass"
    };
    for (int i = 0; i < 2; i++) {
        dvmResolveClass = reinterpret_cast<void *(*)(const void *, unsigned int, bool)>(
                dlsym(handle, resolveClassSymbols[i]));
        if (dvmResolveClass) {
            LOGE("dlsym dvmResolveClass success %s", resolveClassSymbols[i]);
            break;
        }
    }
//    dvmFindLoadedClass = reinterpret_cast<void *(*)(const char *)>(
//            dlsym(handle, "dvmFindLoadedClass"));
//
//    dvmResolveClass = reinterpret_cast<void *(*)(const void *, unsigned int, bool)>(
//            dlsym(handle, "dvmResolveClass"));

    if (!dvmFindLoadedClass) {
        LOGE("dlsym dvmFindLoadedClass fail");
    }
    if (!dvmResolveClass) {
        LOGE("dlsym dvmResolveClass fail");
    }
    if (!dvmFindLoadedClass || !dvmResolveClass) {
        return false;
    }

    const char *descriptorChars = (*env).GetStringUTFChars(referrerDescriptor, 0);
    void *referrerClassObj = dvmFindLoadedClass(descriptorChars);

    dvmResolveClass(referrerClassObj, classIdx, true);

    return true;
}


