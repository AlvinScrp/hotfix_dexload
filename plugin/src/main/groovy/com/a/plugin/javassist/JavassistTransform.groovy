package com.a.plugin.javassist

import com.android.SdkConstants
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import org.apache.commons.codec.digest.DigestUtils

class JavassistTransform extends Transform {

    def pool = ClassPool.default
    def project

    private static final def CLICK_LISTENER = "android.view.View\$OnClickListener"

    JavassistTransform(project) {
        this.project = project
    }

    @Override
    String getName() {
        return "JavassistTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws javax.xml.crypto.dsig.TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        println "javassist---transform----------"
        project.android.bootClasspath.each {
            pool.appendClassPath(it.absolutePath)
        }

        transformInvocation.inputs.each {

            it.jarInputs.each {
                println "111jarInput:${it.file.absolutePath}"
                pool.insertClassPath(it.file.absolutePath)

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = it.name
                def md5Name = DigestUtils.md5Hex(it.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName + md5Name, it.contentTypes, it.scopes, Format.JAR)

//                def dest = transformInvocation.outputProvider.getContentLocation(
//                        jarName, it.contentTypes, it.scopes, Format.JAR)


                println "jarInput-dest:${dest.absolutePath}"
                org.apache.commons.io.FileUtils.copyFile(it.file, dest)
            }


            it.directoryInputs.each {
                println "directoryInputs:${it.file.absolutePath}"
                def preFileName = it.file.absolutePath
                pool.insertClassPath(preFileName)

                findTarget(it.file, preFileName)

                // 获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.DIRECTORY)

                println "copy directory: " + it.file.absolutePath
                println "dest directory: " + dest.absolutePath
                // 将input的目录复制到output指定目录
                org.apache.commons.io.FileUtils.copyDirectory(it.file, dest)
            }
        }
    }

    private void findTarget(File dir, String fileName) {
        if (dir.isDirectory()) {
            dir.listFiles().each {
                findTarget(it, fileName)
            }
        } else {
            modify(dir, fileName)
        }
    }

    private void modify(File dir, String fileName) {
        def filePath = dir.absolutePath
        println "modify:${dir.absolutePath}"

        if (!filePath.endsWith(SdkConstants.DOT_CLASS)) {
            return
        }
        if (filePath.contains('R$') || filePath.contains('R.class')
                || filePath.contains("BuildConfig.class")) {
            return
        }

        def className = filePath.replace(fileName, "")
                .replace("\\", ".")
                .replace("/", ".")
        def name = className.replace(SdkConstants.DOT_CLASS, "")
                .substring(1)

        CtClass ctClass = pool.get(name)
        CtClass[] interfaces = ctClass.getInterfaces()
        if (interfaces.contains(pool.get(CLICK_LISTENER))) {
            if (name.contains("\$")) {
                println "class is inner class：" + ctClass.name
                println "CtClass: " + ctClass
                CtClass outer = pool.get(name.substring(0, name.indexOf("\$")))

                CtField field = ctClass.getFields().find {
                    return it.type == outer
                }
                if (field != null) {
                    println "fieldStr: " + field.name
                    def body = "android.widget.Toast.makeText(" + field.name + "," +
                            "\"javassist\", android.widget.Toast.LENGTH_SHORT).show();"
                    addCode(ctClass, body, fileName)
                }
            } else {
                println "class is outer class: " + ctClass.name
                //更改onClick函数
                def body = "android.widget.Toast.makeText(\$1.getContext(), \"javaskkkkkk\", android.widget.Toast.LENGTH_SHORT).show();"
                addCode(ctClass, body, fileName)
            }
        }
    }

    private void addCode(CtClass ctClass, String body, String fileName) {

        ctClass.defrost()
        CtMethod method = ctClass.getDeclaredMethod("onClick", pool.get("android.view.View"))
        method.insertAfter(body)

        ctClass.writeFile(fileName)
        ctClass.detach()
        println "write file: " + fileName + "\\" + ctClass.name
        println "modify method: " + method.name + " succeed"
    }

}
