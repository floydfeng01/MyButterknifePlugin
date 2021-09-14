package com.fw.timeplugins

import com.android.build.api.dsl.extension.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TimeTestPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project.hasProperty("android")) {
            var appExtension : AppExtension = (project.properties.get(key = "android")) as AppExtension
            appExtension.registerTransform(TimeTestTransform())
        } else {
            println("TimeTestPlugin>>>apply>>>project has not key 'android'")
        }
    }
}