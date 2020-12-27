package com.a.plugin

import com.a.plugin.robust.autopath.RobustAutoPathFactory
import com.android.SdkConstants
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project

class RobustAutoPatchTransform extends Transform {

    ClassPool classPool = ClassPool.default
    Project project
    com.a.plugin.robust.inject.RobustInjectWithJavaAssist robustJavaAssist

    Class modifyAnnotationClass;
    Map<String, Integer> injectMethodMap = new HashMap<>();
    Map<String, Set<String>> modifiedMap = new HashMap<>();
    Map<String, String> outClassMap = new HashMap<>();
    RobustAutoPathFactory robustAutoPathFactory;


    RobustAutoPatchTransform(project) {
        this.project = project

        robustAutoPathFactory = new RobustAutoPathFactory(this.project, classPool)
    }

    @Override
    String getName() {
        return "RobustAutoPatchTransform"
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
    void transform(TransformInvocation transformInvocation) throws Exception {
        super.transform(transformInvocation)

        println "----------Robust auto patch transform start----------"
        this.robustJavaAssist = new com.a.plugin.robust.inject.RobustInjectWithJavaAssist()

        project.android.bootClasspath.each { classPool.appendClassPath(it.absolutePath) }

        readInjectMethodFromFile()
        generateModifiedInfoMap(transformInvocation);
        generatePatchClasses()
        zipAllPatchClassesToDex()
        generateOutClassMapTxt();
        println "----------Robust auto patch transform end----------"

        throw new NullPointerException(("hahahahahahaha"))
    }

    /**
     * key:包含Modify方法注解的class.name ,比如 "com.a.robust.data.M"
     * value:集合，key类中Modify注解的Method.longName ，比如 "com.a.robust.data.M.a(int)"
     * @param transformInvocation
     * @return
     */
    private Map<String, Set<String>> generateModifiedInfoMap(TransformInvocation transformInvocation) {
        Map<String, Set<String>> modifiedMap = new HashMap<>();

        transformInvocation.inputs.each { input ->
            input.jarInputs.each { jarInput ->
                classPool.insertClassPath(jarInput.file.absolutePath)
            }
            input.directoryInputs.each { directoryInput ->
                println(directoryInput.file.absolutePath)
                classPool.insertClassPath(directoryInput.file.absolutePath)
                org.apache.commons.io.FileUtils.listFiles(directoryInput.file, null, true)
                        .each { file -> handleFile(file, directoryInput.file.absolutePath, modifiedMap) }
            }
        }
        this.modifiedMap = modifiedMap;
        println("modifiedMap:" + modifiedMap)
        return modifiedMap;

    }

    private void handleFile(File file, String inputDir, Map<String, Set<String>> modifiedMap) {
        if (!file.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
            return
        }
        def className = file.absolutePath.replace(inputDir, "")
                .replace(File.separator, ".")
                .replace(SdkConstants.DOT_CLASS, "")
                .substring(1)
        if (!className.startsWith("com.a.robust")) {
            return
        }
        println("handleFile: ${className}")
        CtClass ctClass = classPool.get(className)

        if (modifyAnnotationClass == null) {
            modifyAnnotationClass = classPool.get("com.a.robust.patch.annotation.Modify").toClass()
        }
//        println("modifyAnnotationClass: ${modifyAnnotationClass}")

        Set<String> modifiedMethodNames = new HashSet<>();
        ctClass.getDeclaredMethods().each { ctMethod ->
//           println("${ctMethod} annotations:${ctMethod.annotations}")
            if (ctMethod.hasAnnotation(modifyAnnotationClass)) {
                modifiedMethodNames.add(ctMethod.longName)
            }
        }
        if (modifiedMethodNames.size() > 0) {
            modifiedMap.put(ctClass.name, modifiedMethodNames)
        }

    }

    /**
     * 插入了changeQuickRedirect的Methods
     * key:Method.LongName
     * value: this increment Method index ,which we defined.
     * @return
     */
    private Map<String, Integer> readInjectMethodFromFile() {
        Map<String, String> methodMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(project.getProjectDir().getAbsolutePath() + "/robust/methodMap.txt"))
            String line = "";
            while ((line = br.readLine()) != null && line.length() > 0) {
                String[] ss = line.split(":");
//                com.a.robust.data.M.a(int):1
                methodMap.put(ss[0], Integer.parseInt(ss[1]))
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.injectMethodMap = methodMap;
        println("injectMethodMap:" + injectMethodMap)
        return methodMap;

    }


    private void generatePatchClasses() {
        File pathDir = new File(project.getProjectDir().getAbsolutePath() + "/robust/patch");
        if (pathDir.exists()) {
            pathDir.delete()
        }
        pathDir.mkdirs();

        modifiedMap.entrySet().each {
            CtClass originClass = this.classPool.get(it.key)
            Set<String> methodLongNames = it.value;
            CtClass patchClass = robustAutoPathFactory.createPatchClass(originClass, methodLongNames)
            patchClass.writeFile(pathDir.absolutePath)
            patchClass.defrost();

            CtClass patchControlClass = robustAutoPathFactory.createPatchControlClass(patchClass, originClass, methodLongNames, injectMethodMap)
            patchControlClass.writeFile(pathDir.absolutePath)
            patchControlClass.defrost();

            outClassMap.put(originClass.getName(), patchControlClass.getName());
        }


    }

//    dx --dex --no-optimize --keep-classes --output meituan_robust/robust/out/robust_patch4.dex   meituan_robust/robust/patch/

    private void zipAllPatchClassesToDex() {
        File patchDir = new File(project.getProjectDir().getAbsolutePath() + "/robust/patch");
        File outDir = new File(project.getProjectDir().getAbsolutePath() + "/robust/out");
        if (outDir.exists()) {
            outDir.delete();
        }
        outDir.mkdirs();
        String command = "dx --dex --output ${outDir.absolutePath}/robust_patch.dex ${patchDir.absolutePath}";
        command.execute();
    }

    private void generateOutClassMapTxt() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : outClassMap.entrySet()) {
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            sb.append("\n");
        }

        File file = new File(project.getProjectDir().getAbsolutePath() + "/robust/out/classMap.txt");
        FileUtils.writeToFile(file, sb.toString());
    }


}
