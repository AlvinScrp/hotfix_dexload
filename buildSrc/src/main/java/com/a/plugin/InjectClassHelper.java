package com.a.plugin;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

public class InjectClassHelper {


    public static boolean dexHasPatchClass(String dexPath, String patchClassType) throws Exception {
        DexBackedDexFile dexFile = DexFileFactory.loadDexFile(dexPath, Opcodes.getDefault());
        for (DexBackedClassDef aClass : dexFile.getClasses()) {
            if (aClass.getType().equals(patchClassType)) {
                return true;
            }
        }

        return false;
    }

    public static void injectHackClass(String dexPath) {
        try {


            File file = new File(dexPath);
            String fileName = file.getName();
            String indexStr = fileName.split("\\.")[0].replace("classes", "");
            System.out.println(" =============indexStr:"+indexStr);
            String className = "com.a.Hack"+ indexStr;
            String classType = "Lcom/a/Hack" + indexStr + ";";


            System.out.println("===================");
            DexBackedDexFile dexFile = DexFileFactory.loadDexFile(dexPath, Opcodes.getDefault());
//        for (DexBackedClassDef aClass : dexFile.getClasses()) {
//            System.out.println("before inject:" + aClass.getType());
//        }


            ImmutableDexFile immutableDexFile = ImmutableDexFile.of(dexFile);

            Set<ClassDef> classDefs = new HashSet<>();
            for (ImmutableClassDef classDef : immutableDexFile.getClasses()) {
                classDefs.add(classDef);
            }
            ImmutableClassDef immutableClassDef = new ImmutableClassDef(
                    classType,
                    AccessFlags.PUBLIC.getValue(),
                    "Ljava/lang/Object;",
                    null, null, null, null, null);
            classDefs.add(immutableClassDef);


            String resultPath = dexPath;
            File resultFile = new File(resultPath);
            if (resultFile != null && resultFile.exists()) resultFile.delete();
            DexFileFactory.writeDexFile(resultPath, new DexFile() {
                @Nonnull
                @Override
                public Set<ClassDef> getClasses() {
                    return new HashSet<>(classDefs);
                }

                @Nonnull
                @Override
                public Opcodes getOpcodes() {
                    return dexFile.getOpcodes();
                }
            });

//        DexBackedDexFile dexFile2 = DexFileFactory.loadDexFile(dexPath, Opcodes.getDefault());
//        for (DexBackedClassDef aClass : dexFile2.getClasses()) {
//            System.out.println("after inject:" + aClass.getType());
//        }

            System.out.println("Outputs injectHackClass: " + file.getName() + ":" + className);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
