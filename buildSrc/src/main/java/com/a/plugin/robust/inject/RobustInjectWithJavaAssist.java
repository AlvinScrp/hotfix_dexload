package com.a.plugin.robust.inject;


import com.a.plugin.robust.RobustConstants;
import com.android.utils.FileUtils;

import org.gradle.api.Project;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;

public class RobustInjectWithJavaAssist {

    public HashMap<String, Integer> methodMap = new LinkedHashMap<>();
    protected AtomicInteger insertMethodCount = new AtomicInteger(0);

    public RobustInjectWithJavaAssist() {
        insertMethodCount.set(0);
    }

    public void addCode(Project project,CtClass ctClass, String inputDir) {
        System.out.println("modify class:" + ctClass.getName());
        try {
            ClassPool classPool = ctClass.getClassPool();
            CtClass type = classPool.getOrNull(RobustConstants.ChangeQuickRedirectName);
            CtField ctField = new CtField(type, "changeQuickRedirect", ctClass);

            ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
            ctClass.addField(ctField);


//            if (PatchProxy.isSupport(new Object[]{new Integer(i)}, this, changeQuickRedirect, false, 1)) {
//                return (String) PatchProxy.accessDispatch(new Object[]{new Integer(i)}, this, changeQuickRedirect, false, 1);
//            }

            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : ctMethods) {
                methodMap.put(ctMethod.getLongName(), insertMethodCount.incrementAndGet());
                boolean isStatic = (ctMethod.getModifiers() & AccessFlag.STATIC) != 0;
                CtClass returnType = ctMethod.getReturnType();
                String returnTypeString = returnType.getName();

                String body = "Object argThis = null;";
                if (!isStatic) {
                    body += "argThis = $0;";
                }
                System.out.println(ctMethod.getLongName());
                int methodNumber = methodMap.get(ctMethod.getLongName());
                body += "   if (changeQuickRedirect!=null && changeQuickRedirect.isSupport($args, argThis, " + isStatic + ", " + methodNumber + ")) {";
                body += getReturnStatement(returnTypeString, isStatic, methodNumber);
                body += "   }";
                //finish the insert-code body ,let`s insert it
                ctMethod.insertBefore(body);

            }

            ctClass.writeFile(inputDir);
            ctClass.detach();
            System.out.println("write file: " + inputDir + "\\" + ctClass.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateMethodMapTxt(Project project) throws  Exception{
        StringBuilder sb=new StringBuilder();
        for (Map.Entry<String, Integer> entry : methodMap.entrySet()) {
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            sb.append("\n");
        }
//        System.out.println(project.getBuildDir());
        File file=new File(project.getBuildDir().getAbsolutePath()+"/robust/methodMap.txt");
        FileUtils.writeToFile(file,sb.toString());

    }


    private String getReturnStatement(String type, boolean isStatic, int methodNumber) {
        switch (type) {
            case "int":
                return "   return ((java.lang.Integer)changeQuickRedirect.accessDispatch( $args, argThis, " + isStatic + "," + methodNumber + ")).intValue();";
            default:
                return "   return (" + type + ")changeQuickRedirect.accessDispatch( $args, argThis, " + isStatic + "," + methodNumber + ");";
        }
    }
}
