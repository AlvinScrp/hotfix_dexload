package com.a.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class HackPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "this is my custom plugin HackPlugin"

        project.android.registerTransform(new HackTransform(project))
    }

}
