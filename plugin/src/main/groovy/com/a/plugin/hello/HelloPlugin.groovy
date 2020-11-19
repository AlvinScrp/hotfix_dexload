package com.a.plugin.hello

import org.gradle.api.Plugin
import org.gradle.api.Project

class HelloPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "this is my custom plugin HelloPlugin"

        project.android.registerTransform(new HelloTransform(project))
    }

}
