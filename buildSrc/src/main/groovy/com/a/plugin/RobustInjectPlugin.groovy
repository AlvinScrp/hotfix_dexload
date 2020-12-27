package com.a.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class RobustInjectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "this is my custom plugin RobustInjectPlugin"

        project.android.registerTransform(new RobustInjectTransform(project))
    }

}
