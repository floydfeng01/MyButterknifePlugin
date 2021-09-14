package com.fw.timeplugins

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.fw.timeplugins.manager.TransformManager
import java.io.File

class TimeTestTransform : Transform {

    constructor() : super()

    override fun getName(): String {
        return "TimeTestTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        //处理class文件
        //当前是否是增量编译
        var isIncremental = transformInvocation?.isIncremental ?: false

        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        var inputs : Collection<TransformInput>? =  transformInvocation?.inputs ?: null

        //引用型输入，无需输出。
        var referencedInputs : Collection<TransformInput>? = transformInvocation?.referencedInputs ?: null

        //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        var outputProvider : TransformOutputProvider? = transformInvocation?.outputProvider ?: null

        //遍历获取信息
        inputs?.forEach {
            var jarInputs : Collection<JarInput> = it.jarInputs

            //对每一个jar遍历处理
            jarInputs.forEach { jarInput ->
                var jarName = jarInput.name
                var jarPath = jarInput.file.absolutePath
                var jarContentType : Set<QualifiedContent.ContentType> = jarInput.contentTypes
                var jarScope : MutableSet<in QualifiedContent.Scope>? = jarInput.scopes
                var destFile : File? = outputProvider?.getContentLocation(
                        jarPath,
                        jarContentType,
                        jarScope,
                        Format.JAR
                )
                TODO("修改字节码操作")

                //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                FileUtils.copyFile(jarInput.file, destFile);

            }

        }
    }
}