//#include <jni.h>
#include "include/substrate.h"
#include <android/log.h>
//#include <unistd.h>
//#include <stdio.h>
//#include <fcntl.h>
//#include <sys/types.h>
//#include <string.h>
//#include <sys/stat.h>
//#include <stdlib.h>
//#include "include/dalvik.h"

#define TAG "alvin"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

void *(*oldDvmResolveClass)(void *referrer, unsigned int classIdx, bool fromUnverifiedConstant);

void *newDvmResolveClass(void *referrer, unsigned int classIdx, bool fromUnverifiedConstant) {

    return oldDvmResolveClass(referrer, classIdx, true);

//    void *res = oldDvmResolveClass(referrer, classIdx, true);
//    try {
//        ClassObject *referrerClass = reinterpret_cast<ClassObject *>(referrer);
//
//        ClassObject *resClass = reinterpret_cast<ClassObject *>(res);

//        ClassObject *referrerClass = referrer;
//
//        ClassObject *resClass =res;
//        if (resClass == NULL) {
//            LOGE("newDvmResolveClass  %s, %s", referrerClass->descriptor,
//                 "resClass is NULL");
//        } else {
//            LOGE("newDvmResolveClass  %s, %s", referrerClass->descriptor,
//                 resClass->descriptor);
//        }
//    } catch (std::exception e) {
//        LOGE("newDvmResolveClass fromUnverifiedConstant exception  %s", e.what());
//    }

//    return res;


}

//指明要hook的lib ：
MSConfig(MSFilterLibrary, "/system/lib/libdvm.so")
MSConfig(MSFilterExecutable, "com.a.dexload.cydia")

MSInitialize {
    LOGE("Cydia Init");
    MSImageRef image = MSGetImageByName("/system/lib/libdvm.so");
    if (image == NULL) {
        LOGE("image is not null");
        return;
    }
    void *resloveMethd = MSFindSymbol(image, "dvmResolveClass");
    if (resloveMethd == NULL) {
        LOGE("error find dvmResolveClass");
        return;
    }
    LOGE("resloveMethd is not null addr is %p", resloveMethd);
    MSHookFunction(resloveMethd, (void *) newDvmResolveClass, (void **) &oldDvmResolveClass);

}