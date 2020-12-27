package com.a.plugin.robust.autopath;


import com.a.plugin.robust.RobustConstants;

import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;
import javassist.expr.Cast;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

public class RobustAutoPathFactory {
    Project project;
    ClassPool classPool;

    public RobustAutoPathFactory(Project project, ClassPool classPool) {
        this.project = project;
        this.classPool = classPool;
    }

    public CtClass createPatchClass(CtClass originClass, Set<String> methodLongNames) throws Exception {

        String patchClassName = originClass.getName() + "Patch";
        System.out.println("createPatchClass Name: " + patchClassName);
        //make class
        CtClass patchClass = classPool.makeClass(patchClassName);
        patchClass.getClassFile().setMajorVersion(ClassFile.JAVA_7);
//        targetClass.setSuperclass(originClass.getSuperclass());
        patchClass.setSuperclass(classPool.get("java.lang.Object"));
        patchClass.setModifiers(AccessFlag.clear(patchClass.getModifiers(), AccessFlag.ABSTRACT));

        //add originClass field
//        CtField originField = new CtField(originClass, "originClass", patchClass);
        patchClass.addField(CtField.make(originClass.getName() + " originClass;", patchClass));

        //add Constructor
        StringBuilder patchClassConstruct = new StringBuilder();
        patchClassConstruct.append(" public " + patchClass.getSimpleName() + "(Object o) {");
        patchClassConstruct.append("    originClass=(" + originClass.getName() + ")o;");
        patchClassConstruct.append("}");
        CtConstructor constructor = CtNewConstructor.make(patchClassConstruct.toString(), patchClass);
        patchClass.addConstructor(constructor);

        //copy field and method from sourceClass
        for (CtField field : originClass.getDeclaredFields()) {
            patchClass.addField(new CtField(field, patchClass));
        }
        ClassMap classMap = new ClassMap();
        classMap.put(patchClassName, originClass.getName());
        classMap.fix(originClass);
        for (CtMethod method : originClass.getDeclaredMethods()) {
            if (methodLongNames.contains(method.getLongName())) {
                CtMethod newCtMethod = new CtMethod(method, patchClass, classMap);
                patchClass.addMethod(newCtMethod);
            }
        }

        //method Code change to reflect style
        for (CtMethod method : patchClass.getDeclaredMethods()) {
            //  shit !!too many situations need take into  consideration
            method.instrument(
                    new ExprEditor() {
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            try {
                                if (f.isReader()) {
                                    f.replace(ReflectUtils.getFieldString(f.getField(), patchClass.getName(), originClass.getName()));
                                } else if (f.isWriter()) {
                                    f.replace(ReflectUtils.setFieldString(f.getField(), patchClass.getName(), originClass.getName()));
                                }
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e.getMessage());
                            }
                        }


                        @Override
                        public void edit(NewExpr e) throws CannotCompileException {
                            e.replace(ReflectUtils.getCreateClassString(e, e.getClassName(), patchClass.getName(), originClass.getName()));
                        }

                        @Override
                        public void edit(Cast c) throws CannotCompileException {
                            try {
//                            MethodInfo thisMethod = (MethodInfo) ReflectUtils.readField(c, "thisMethod");
//                            CtClass thisClass = (CtClass) ReflectUtils.readField(c, "thisClass");
//                            boolean isStatic = ReflectUtils.isStatic(thisMethod.getAccessFlags());
                                boolean isStatic = ReflectUtils.isStatic(c.where().getModifiers());
                                if (!isStatic && !c.getType().isArray()) {
                                    // static函数是没有this指令的，直接会报错。
                                    c.replace(ReflectUtils.getCastString(c, patchClass));
                                }
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e.getMessage());
                            }
                        }

                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            try {
                                boolean isStatic = ReflectUtils.isStatic(method.getModifiers());
                                m.replace(ReflectUtils.getMethodCallString(m, patchClass, isStatic));
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

        List<CtField> ctFieldList = new ArrayList<>();
        for (CtField ctField : patchClass.getDeclaredFields()) {
            if(ctField.getName().equals("originClass")) continue;
            ctFieldList.add(ctField);
        }
        for (CtField ctField : ctFieldList) {
            patchClass.removeField(ctField);
        }


        return patchClass;
    }

    public CtClass createPatchControlClass(CtClass patchClass, CtClass originClass, Set<String> methodLongNames, Map<String, Integer> injectMethodMap) throws Exception {

        String controlClassName = originClass.getName() + "PatchControl";
        System.out.println("createControlClass : " + controlClassName);
        //make class
        CtClass controlClass = classPool.makeClass(controlClassName);
        controlClass.getClassFile().setMajorVersion(ClassFile.JAVA_7);
        controlClass.setInterfaces(new CtClass[]{classPool.get(RobustConstants.ChangeQuickRedirectName)});
        controlClass.setSuperclass(classPool.get("java.lang.Object"));
        controlClass.setModifiers(AccessFlag.PUBLIC);

        //createMethod
        String methodNumStr = getPatchMethodNumString(methodLongNames, injectMethodMap);
        controlClass.addMethod(CtMethod.make(createIsSupportMethodText(controlClass, methodNumStr), controlClass));

        Map<String, Integer> methodMap = getPatchMethodMap(patchClass, originClass, methodLongNames, injectMethodMap);
        controlClass.addMethod(CtMethod.make(createAccessDispatchMethodText(patchClass, controlClass, methodMap), controlClass));

        return controlClass;
    }

    @NotNull
    private String getPatchMethodNumString(Set<String> methodLongNames, Map<String, Integer> injectMethodMap) {
        StringBuilder methodNumStrBuilder = new StringBuilder();
        for (String methodLongName : methodLongNames) {
            Integer methodNum = injectMethodMap.get(methodLongName);
            if (methodNum != null && methodNum > 0) {
                methodNumStrBuilder.append(":" + methodNum);
            }
        }
        if (methodNumStrBuilder.length() > 0) {
            methodNumStrBuilder.append(":");
        }
        return methodNumStrBuilder.toString();
    }

    @NotNull
    private Map<String, Integer> getPatchMethodMap(CtClass patchClass, CtClass originClass, Set<String> methodLongNames, Map<String, Integer> injectMethodMap) {
        Map<String, Integer> map = new HashMap<>();
        System.out.println("injectMethodMap:" + injectMethodMap);
        System.out.println("methodLongNames:" + methodLongNames);
        for (String methodLongName : methodLongNames) {
            String patchMethodLongMame = methodLongName.replaceAll(originClass.getName(), patchClass.getName());
            Integer methodNum = injectMethodMap.get(methodLongName);
            if (methodNum != null && methodNum > 0) {
                map.put(patchMethodLongMame, methodNum);
            }
        }

        return map;
    }


//    boolean isSupport(Object[] methodParams, Object originObj, boolean isStatic, int methodNumber);
//     return ":1:".contains(new StringBuffer().append(":").append(methodName.split(":")[3]).append(":").toString());

    private String createIsSupportMethodText(CtClass controlClass, String methodNumStr) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("public boolean isSupport(Object[] methodParams, Object originObj, boolean isStatic, int methodNumber) {");
        sb.append(" return \"" + methodNumStr + "\".contains(new StringBuffer().append(\":\").append(methodNumber).append(\":\").toString());");
        sb.append("}");
        return sb.toString();
    }


    //    Object accessDispatch(Object[] methodParams, Object originObj, boolean isStatic, int methodNumber);
    private String createAccessDispatchMethodText(CtClass patchClass, CtClass controlClass, Map<String, Integer> methodMap) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("public Object accessDispatch(Object[] methodParams, Object originObj, boolean isStatic, int methodNumber) {\n");
        sb.append(" try {\n");
        sb.append("     " + patchClass.getName() + " patch=null;\n");
        sb.append("     if (!isStatic) {\n");
        sb.append("         patch = new " + patchClass.getName() + "(originObj);\n");
        sb.append("     } else {\n");
        sb.append("         patch = new " + patchClass.getName() + "(null);\n");
        sb.append("     }\n");

        for (CtMethod ctMethod : patchClass.getDeclaredMethods()) {
            String methodLongName = ctMethod.getLongName();
            int methodNum = methodMap.get(methodLongName);
            sb.append("     if(" + methodNum + " == methodNumber){\n");
            sb.append(getExecuteBody(patchClass, ctMethod));
            sb.append("     }\n");
        }
        sb.append(" } catch (Throwable th) {\n");
        sb.append("     th.printStackTrace();\n");
        sb.append(" }\n");
        sb.append(" return null;\n");
        sb.append("}\n");

        System.out.println("createAccessDispatchMethodText :\n " + sb.toString());
        return sb.toString();
    }

    private String getExecuteBody(CtClass patchClass, CtMethod ctMethod) throws Exception {
        CtClass returnType = ctMethod.getReturnType();
        CtClass[] parameterTypes = ctMethod.getParameterTypes();
        StringBuilder sb = new StringBuilder();
        sb.append("         " + getReturnPrefixText(returnType.getName(), ctMethod.getName()));
//        Object[] methodParams

//        ((Integer) paramArrayOfObject[0]).intValue(),((Integer) paramArrayOfObject[0]).intValue()
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass parameterType = parameterTypes[i];
            String typeName = parameterType.getName();
            sb.append("((" + getWrapperClass(typeName) + ")methodParams[" + i + "])" + wrapperToPrime(typeName));
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            }
        }

        sb.append("));\n");


        return sb.toString();
    }

    public static String getReturnPrefixText(String typeName, String methodName) {
        StringBuilder sbReturnPrefix = new StringBuilder();
        if (typeName.equals("void")) {
            sbReturnPrefix.append("(patch." + methodName + "(");
        } else {
            switch (typeName) {
                case "boolean":
                    sbReturnPrefix.append("return Boolean.valueOf(patch." + methodName + "(");
                    break;
                case "byte":
                    sbReturnPrefix.append("return Byte.valueOf(patch." + methodName + "(");
                    break;
                case "char":
                    sbReturnPrefix.append("return Character.valueOf(patch." + methodName + "(");
                    break;
                case "double":
                    sbReturnPrefix.append("return Double.valueOf(patch." + methodName + "(");
                    break;
                case "float":
                    sbReturnPrefix.append("return Float.valueOf(patch." + methodName + "(");
                    break;
                case "int":
                    sbReturnPrefix.append("return Integer.valueOf(patch." + methodName + "(");
                    break;
                case "long":
                    sbReturnPrefix.append("return Long.valueOf(patch." + methodName + "(");
                    break;
                case "short":
                    sbReturnPrefix.append("return Short.valueOf(patch." + methodName + "(");
                    break;
                default:
                    sbReturnPrefix.append("return (patch." + methodName + "(");
                    break;
            }
        }
        return sbReturnPrefix.toString();
    }

    public static String getWrapperClass(String typeName) {
        String warpperType = typeName;
        switch (typeName) {
            case "boolean":
                warpperType = "java.lang.Boolean";
                break;
            case "byte":
                warpperType = "java.lang.Byte";
                break;
            case "char":
                warpperType = "java.lang.Character";
                break;
            case "double":
                warpperType = "java.lang.Double";
                break;
            case "float":
                warpperType = "java.lang.Float";
                break;
            case "int":
                warpperType = "java.lang.Integer";
                break;
            case "long":
                warpperType = "java.lang.Long";
                break;
            case "short":
                warpperType = "java.lang.Short";
                break;
            default:
                break;
        }
        return warpperType;
    }

    public static String wrapperToPrime(String typeName) {
        String warpperType = "";
        switch (typeName) {
            case "boolean":
                warpperType = ".booleanValue()";
                break;
            case "byte":
                warpperType = ".byteValue()";
                break;
            case "char":
                warpperType = ".charValue()";
                break;
            case "double":
                warpperType = ".doubleValue()";
                break;
            case "float":
                warpperType = ".floatValue()";
                break;
            case "int":
                warpperType = ".intValue()";
                break;
            case "long":
                warpperType = ".longValue()";
                break;
            case "short":
                warpperType = ".shortValue()";
                break;
            default:
                break;
        }
        return warpperType;
    }
}
