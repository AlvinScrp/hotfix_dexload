package com.a.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class QFixPlugin implements Plugin<Project> {

    void apply(Project project1) {
        System.out.println("========================");
        System.out.println("Hello gradle QFixPlugin!");
        System.out.println("========================");

//               project1.afterEvaluate { project ->
//        project1.tasks.each {task->
//
//            System.out.println("============" + task)
//        }
//    }

        project1.afterEvaluate { project ->

            project.tasks.packageDebug {
                doLast {
                    println 'QFixPlugin inject Class after packageDebug'
//                    project.tasks.packageDebug.getInputs().getFiles().collect().each { element ->
//                        println "inputs: " + element
//                    }
                    // outputs: /Users/mawenqiang/Documents/demo_project/Hello/smalidex/build/intermediates/incremental/packageDebug/tmp
                    project.tasks.packageDebug.getOutputs().getFiles().collect().each { element ->
                        println "outputs: " + element
                    }
                }
            }

        }

        project1.afterEvaluate { project ->
            project.tasks.mergeDexDebug {
                doLast {
                    println 'QFixPlugin inject Class after mergeDexDebug'
                    project.tasks.mergeDexDebug.getOutputs().getFiles().each { dir ->
                        println "outputs: " + dir
                        if (dir != null && dir.exists()) {
                            def files = dir.listFiles()
                            files.each { file ->
                                String dexfilepath = file.getAbsolutePath()
                                println "Outputs Dex file's path: " + dexfilepath
//                                if (InjectClassHelper.dexHasPatchClass(dexfilepath, "Lcom/a/fix/M;")) {
                                   InjectClassHelper.injectHackClass(dexfilepath)
//                                }

                            }

                        }
                    }
                }
            }
        }
    }
}
