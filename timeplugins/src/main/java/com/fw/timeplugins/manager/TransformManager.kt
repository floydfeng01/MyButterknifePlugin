package com.fw.timeplugins.manager

import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.DefaultContentType.CLASSES
import com.android.build.api.transform.QualifiedContent.DefaultContentType.RESOURCES
import com.android.build.api.transform.QualifiedContent.Scope.*
import com.android.build.api.transform.QualifiedContent.ScopeType

object TransformManager {
    val CONTENT_CLASS = mutableSetOf<ContentType>(CLASSES)
    val CONTENT_JARS = mutableSetOf<ContentType>(CLASSES, RESOURCES)
    val CONTENT_RESOURCES = mutableSetOf<ContentType>(RESOURCES)

    val SCOPE_EMPTY = mutableSetOf<ScopeType>()
    val PROJECT_ONLY = mutableSetOf<ScopeType>(PROJECT)
    val SCOPE_FULL_PROJECT = mutableSetOf<ScopeType>(PROJECT, SUB_PROJECTS, EXTERNAL_LIBRARIES)
    val SCOPE_PROVIDED_ONLY = mutableSetOf<ScopeType>(PROVIDED_ONLY)
    val SCOPE_FULL_PROJECT_LOCAL_DEPS = mutableSetOf<ScopeType>(PROJECT_LOCAL_DEPS, SUB_PROJECTS_LOCAL_DEPS)

}