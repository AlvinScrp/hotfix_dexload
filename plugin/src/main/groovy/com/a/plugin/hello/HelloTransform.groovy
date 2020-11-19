package com.a.plugin.hello

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import org.apache.commons.codec.digest.DigestUtils

class HelloTransform extends Transform {

    def pool = ClassPool.default
    def project

    HelloTransform(project) {
        this.project = project
    }

    @Override
    String getName() {
        return "HelloTransform"
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

        println "hello---transform----------"
        project.android.bootClasspath.each {
            pool.appendClassPath(it.absolutePath)
        }
        pool.makeClass("com.a.hack.HackCode")

        transformInvocation.inputs.each {

            it.jarInputs.each {
                println "hello---jarInput:${it.file.absolutePath}"
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
                println "hello---jarInput-dest:${dest.absolutePath}"
                org.apache.commons.io.FileUtils.copyFile(it.file, dest)
            }


            it.directoryInputs.each {
                println "directoryInputs:${it.file.absolutePath}"
                def inputDir = it.file.absolutePath
                pool.insertClassPath(inputDir)

                findTarget(it.file, inputDir)

                // 获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.DIRECTORY)
                println "hello---copy directory: " + it.file.absolutePath
                println "hello---dest directory: " + dest.absolutePath
                // 将input的目录复制到output指定目录
                org.apache.commons.io.FileUtils.copyDirectory(it.file, dest)
            }
        }
    }

    private void findTarget(File fileOrDir, String inputDir) {
        if (fileOrDir.isDirectory()) {
            fileOrDir.listFiles().each {
                findTarget(it, inputDir)
            }
        } else {
            println "hello---:${fileOrDir.absolutePath}"
            modify(fileOrDir, inputDir)

        }
    }

    private void modify(File file, String inputDir) {
        def filePath = file.absolutePath
        println "modify:${file.absolutePath}"

        if (!filePath.endsWith(SdkConstants.DOT_CLASS)) {
            return
        }
        if (filePath.contains('R$') || filePath.contains('R.class')
                || filePath.contains("BuildConfig.class")) {
            return
        }

        def className = filePath.replace(inputDir, "")
                .replace("\\", ".")
                .replace("/", ".")
        def name = className.replace(SdkConstants.DOT_CLASS, "")
                .substring(1)
        CtClass ctClass = pool.get(name)
        if (ctClass.getSuperclass() != null
                && ctClass.getSuperclass().name == "android.app.Application") {
            println "modify skip:${file.absolutePath}"
            return
        }
        addCode(ctClass, inputDir)

    }

    private void addCode(CtClass ctClass, String fileName) {

        ctClass.defrost()
        CtConstructor[] constructors = ctClass.getDeclaredConstructors()
        if (constructors != null && constructors.length > 0) {
            CtConstructor constructor = constructors[0]
            def body = "android.util.Log.e(\"alvin\",\"${constructor.name} constructor\" + com.a.hack.HackCode.class);"
            constructor.insertBefore(body)
            println "modify constructor: ${constructor.name}  succeed"
        }
        ctClass.writeFile(fileName)
        ctClass.detach()
        println "write file: " + fileName + "\\" + ctClass.name

    }

}
