package com.a.plugin.robust.autopath;

import com.a.plugin.robust.RobustConstants;

import java.lang.reflect.Field;

import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.expr.Cast;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

class ReflectUtils {
    public static int invokeCount = 0;


    public static String setFieldString(CtField field, String patchClassName, String originClassName) {
        boolean isStatic = isStatic(field.getModifiers());
        StringBuilder stringBuilder = new StringBuilder("{");
        if (isStatic) {
            System.out.println("setFieldString static field " + field.getName() + "  declaringClass   " + field.getDeclaringClass().getName());
            if (AccessFlag.isPublic(field.getModifiers())) {
                stringBuilder.append("$_ = $proceed($$);");
            } else {
                if (field.getDeclaringClass().getName().equals(patchClassName)) {
                    stringBuilder.append(RobustConstants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + originClassName + ".class,$1);");
                } else {
                    stringBuilder.append(RobustConstants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.getName() + "\"," + field.getDeclaringClass().getName() + ".class,$1);");
                }
            }

            stringBuilder.append("  android.util.Log.d(\"robust\",\"set static  value is \" +\"" + (field.getName()) + " " + getCoutNumber() + "\");");

        } else {
            stringBuilder.append("java.lang.Object instance;");
            stringBuilder.append("java.lang.Class clazz;");
            stringBuilder.append(" if($0 instanceof " + patchClassName + "){");
            stringBuilder.append("instance=((" + patchClassName + ")$0)." + RobustConstants.ORIGINCLASS + ";");
            stringBuilder.append("}else{");
            stringBuilder.append("instance=$0;");
            stringBuilder.append("}");
            stringBuilder.append(RobustConstants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.getName() + "\",instance,$1," + field.getDeclaringClass().getName() + ".class);");
            stringBuilder.append("  android.util.Log.d(\"robust\",\"set value is \" + \"" + (field.getName()) + " " + getCoutNumber() + "\");");
        }
        stringBuilder.append("}");
//        println field.getName() + "  set  field repalce  by  " + stringBuilder.toString()
        return stringBuilder.toString();
    }


    public static String getFieldString(CtField field, String patchClassName, String originClassName) {

        boolean isStatic = isStatic(field.getModifiers());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        if (isStatic) {
            if (AccessFlag.isPublic(field.getModifiers())) {
                //deal with android R file
//                if (INLINE_R_FILE && isRFile(field.getDeclaringClass().getName())) {
//                    System.out.println("getFieldString static field " + field.getName() + "   is R file macthed   " + field.getDeclaringClass().getName());
//                    stringBuilder.append("$_ = " + field.getConstantValue() + ";");
//                } else {
                stringBuilder.append("$_ = $proceed($$);");
//                }
            } else {

                if (field.getDeclaringClass().getName().equals(patchClassName)) {
                    stringBuilder.append("$_=($r) " + RobustConstants.ROBUST_UTILS_FULL_NAME + ".getStaticFieldValue(\"" + field.getName() + "\"," + originClassName + ".class);");

                } else {
                    stringBuilder.append("$_=($r) " + RobustConstants.ROBUST_UTILS_FULL_NAME + ".getStaticFieldValue(\"" + field.getName() + "\"," + field.getDeclaringClass().getName() + ".class);");
                }
            }
            stringBuilder.append("  android.util.Log.d(\"robust\",\"get static  value is \" +\"" + (field.getName()) + "    " + getCoutNumber() + "\");");
        } else {
            stringBuilder.append("java.lang.Object instance;");
            stringBuilder.append(" if($0 instanceof " + patchClassName + "){");
            stringBuilder.append("instance=((" + patchClassName + ")$0)." + RobustConstants.ORIGINCLASS + ";");
            stringBuilder.append("}else{");
            stringBuilder.append("instance=$0;");
            stringBuilder.append("}");

            stringBuilder.append("$_=($r) " + RobustConstants.ROBUST_UTILS_FULL_NAME + ".getFieldValue(\"" + field.getName() + "\",instance," + field.getDeclaringClass().getName() + ".class);");

            stringBuilder.append("  android.util.Log.d(\"robust\",\"get value is \" +\"" + (field.getName()) + "   " + getCoutNumber() + "\");");

        }
        stringBuilder.append("}");
//        println field.getName() + "  get field repalce  by  " + stringBuilder.toString() + "\n"
        return stringBuilder.toString();
    }


    public static String getCreateClassString(NewExpr e, String className, String patchClassName, String originClassName) {
        StringBuilder stringBuilder = new StringBuilder();
        if (e.getSignature() == null) {
            return "{$_=($r)$proceed($$);}";
        }
        String signatureBuilder = getParameterClassSignure(e.getSignature(), patchClassName, originClassName);
        stringBuilder.append("{");
        String paramsTypeText = signatureBuilder.length() > 1 ? ("new Class[]{" + signatureBuilder + "}") : "null";
        stringBuilder.append("$_=($r)" + RobustConstants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",$args," + paramsTypeText + ");");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    static String getParameterClassSignure(String signature, String pacthClassName, String originClassName) {
        if (signature == null || signature.length() < 1) {
            return "";
        }
        System.out.println("getParameterClassSignure: " + signature);
        StringBuilder signureBuilder = new StringBuilder();
        String name;
        boolean isArray = false;
        for (int index = 1; index < signature.indexOf(")"); index++) {
            if (RobustConstants.OBJECT_TYPE == signature.charAt(index) && signature.indexOf(RobustConstants.PACKNAME_END) != -1) {
                name = signature.substring(index + 1, signature.indexOf(RobustConstants.PACKNAME_END, index)).replaceAll("/", ".");
                if (name.equals(pacthClassName)) {
                    signureBuilder.append(originClassName);
                } else {
                    signureBuilder.append(name);
                }
                index = signature.indexOf(";", index);
                if (isArray) {
                    signureBuilder.append("[]");
                    isArray = false;
                }
                signureBuilder.append(".class,");
            }
            if (RobustConstants.PRIMITIVE_TYPE.contains(String.valueOf(signature.charAt(index)))) {
                switch (signature.charAt(index)) {
                    case 'Z':
                        signureBuilder.append("boolean");
                        break;
                    case 'C':
                        signureBuilder.append("char");
                        break;
                    case 'B':
                        signureBuilder.append("byte");
                        break;
                    case 'S':
                        signureBuilder.append("short");
                        break;
                    case 'I':
                        signureBuilder.append("int");
                        break;
                    case 'J':
                        signureBuilder.append("long");
                        break;
                    case 'F':
                        signureBuilder.append("float");
                        break;
                    case 'D':
                        signureBuilder.append("double");
                        break;
                    default:
                        break;
                }
                if (isArray) {
                    signureBuilder.append("[]");
                    isArray = false;
                }
                signureBuilder.append(".class,");
            }

            if (RobustConstants.ARRAY_TYPE.equals(String.valueOf(signature.charAt(index)))) {
                isArray = true;
            }
        }
        if (signureBuilder.length() > 0 && String.valueOf(signureBuilder.charAt(signureBuilder.length() - 1)).equals(","))
            signureBuilder.deleteCharAt(signureBuilder.length() - 1);
//        println("ggetParameterClassSignure   " + signureBuilder.toString())
        return signureBuilder.toString();
    }

    public static Object readField(Object object, String fieldName) {
        Field field = null;
        Class clazz = object.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    break;
                }
                return field;
            } catch (final NoSuchFieldException e) {
                // ignore
            }
            clazz = clazz.getSuperclass();
        }
        if (field != null) {
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static boolean isStatic(int modifiers) {
        return (modifiers & AccessFlag.STATIC) != 0;
    }

    private static String getCoutNumber() {
        return " No:  " + ++invokeCount;
    }

    public static String getCastString(Cast c, CtClass patchClass) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("  if($1 == this ){");
        stringBuilder.append("    $_=((" + patchClass.getName() + ")$1)." + RobustConstants.ORIGINCLASS + ";");
        stringBuilder.append("  }else{");
        stringBuilder.append("      $_=($r)$1;");
        stringBuilder.append("  }");
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    static String getMethodCallString(MethodCall methodCall, CtClass patchClass, boolean isInStaticMethod) throws NotFoundException {
        String signatureBuilder = getParameterClassString(methodCall.getMethod().getParameterTypes());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        // public method or methods in patched classes
        //这里面需要注意在static method中 使用static method和非static method 和在非static method中 使用static method和非static method的四种情况
//            stringBuilder.append("java.lang.Object instance;");
        stringBuilder.append(methodCall.getMethod().getDeclaringClass().getName() + " instance;");
        String declaredClassName = methodCall.getMethod().getDeclaringClass().getName();
        String signatureText = signatureBuilder.length() > 1 ? ("new Class[]{" + signatureBuilder + "}") : "null";
        if (isStatic(methodCall.getMethod().getModifiers())) {
            //call static method
            if (AccessFlag.isPublic(methodCall.getMethod().getModifiers())) {
                stringBuilder.append("$_ = $proceed($$);");
            } else {
                stringBuilder.append("$_=($r)" + RobustConstants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + methodCall.getMethodName() + "\"," + declaredClassName + ".class,$args," + signatureText + ");");
            }


        } else {
            if (!isInStaticMethod) {
                //在非static method中使用非static method
                stringBuilder.append(" if($0 == this ){");
                stringBuilder.append("instance=((" + patchClass.getName() + ")$0)." + RobustConstants.ORIGINCLASS + ";");
                stringBuilder.append("}else{");
                stringBuilder.append("instance=$0;");
                stringBuilder.append("}");
            } else {
                //在static method中使用非static method
                stringBuilder.append("instance=(" + methodCall.getMethod().getDeclaringClass().getName() + ")$0;");
            }
            stringBuilder.append("$_=($r) " + RobustConstants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + methodCall.getMethodName() + "\",instance,$args," + signatureText + "," + declaredClassName + ".class);");
        }
        stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke  method is  " + getCoutNumber() + " \" +\"" + methodCall.getMethodName() + "\");");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static String getParameterClassString(CtClass[] parameters) {
        if (parameters == null || parameters.length < 1) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < parameters.length; index++) {
            stringBuilder.append(parameters[index].getName() + ".class");
            if (index != parameters.length - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }
}
