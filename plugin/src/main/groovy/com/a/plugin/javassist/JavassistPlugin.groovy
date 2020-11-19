package com.a.plugin.javassist

import org.gradle.api.Plugin
import org.gradle.api.Project

class JavassistPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "this is my custom plugin JavassistPlugin"

        project.android.registerTransform(new JavassistTransform(project))
    }

}
