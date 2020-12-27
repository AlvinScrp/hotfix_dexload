package com.a.plugin

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.codec.digest.DigestUtils

class RobustInjectTransform extends Transform {

    def pool = ClassPool.default
    def project
    com.a.plugin.robust.inject.RobustInjectWithJavaAssist robustJavaAssist

    RobustInjectTransform(project) {
        this.project = project

    }

    @Override
    String getName() {
        return "RobustInjectTransform"
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

        println "Robust---transform----------"
        this.robustJavaAssist = new com.a.plugin.robust.inject.RobustInjectWithJavaAssist()

        project.android.bootClasspath.each {
            pool.appendClassPath(it.absolutePath)
        }
        transformInvocation.inputs.each {

            it.jarInputs.each {
                pool.insertClassPath(it.file.absolutePath)

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = it.name
                def md5Name = DigestUtils.md5Hex(it.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName + md5Name, it.contentTypes, it.scopes, Format.JAR)
                org.apache.commons.io.FileUtils.copyFile(it.file, dest)
            }

            it.directoryInputs.each { directoryInput ->
                def inputDir = directoryInput.file.absolutePath
                pool.insertClassPath(inputDir)
                org.apache.commons.io.FileUtils.listFiles(directoryInput.file, null, true)
                        .each { file -> modify(file, directoryInput.file.absolutePath) }
               robustJavaAssist.generateMethodMapTxt(project);
//                findTarget(it.file, inputDir)
                def dest = transformInvocation.outputProvider.getContentLocation(
                        directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                org.apache.commons.io.FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }

//    private void findTarget(File fileOrDir, String inputDir) {
//        if (fileOrDir.isDirectory()) {
//            fileOrDir.listFiles().each {
//                findTarget(it, inputDir)
//            }
//        } else {
//            modify(fileOrDir, inputDir)
//
//        }
//    }

    private void modify(File file, String inputDir) {
        def filePath = file.absolutePath

        if (!filePath.endsWith(SdkConstants.DOT_CLASS)) {
            return
        }
        println "----------------"
        println "modify filePath:${filePath}"
//        println "modify inputDir:${inputDir}"
        def className = filePath.replace(inputDir, "")
                .replace("\\", ".")
                .replace("/", ".")
                .replace(SdkConstants.DOT_CLASS, "")
                .substring(1)
//        println "modify className:${className}"

        if (!className.startsWith("com.a.robust.data")) {
            return
        }
        CtClass ctClass = pool.get(className)
        this.robustJavaAssist.addCode(project,ctClass, inputDir)
    }


}
