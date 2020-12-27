package com.a.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class RobustAutoPatchPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "this is my custom plugin RobustAutoPatchPlugin"

        project.android.registerTransform(new RobustAutoPatchTransform(project))
    }

}
