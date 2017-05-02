package com.fuzz.processor

import com.fuzz.datacontroller.annotations.Network
import com.fuzz.processor.utils.annotation
import com.fuzz.processor.utils.toClassName
import com.fuzz.processor.utils.toTypeElement
import com.grosner.kpoet.code
import com.grosner.kpoet.statement
import com.grosner.kpoet.typeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class NetworkDefinition(element: Element, processorManager: DataControllerProcessorManager)
    : BaseDefinition(element, processorManager) {

    var responseHandler = ClassName.OBJECT
    var errorConverter = ClassName.OBJECT

    var network = false
    var hasNetworkAnnotation = false
    var hasRetrofit = false

    init {
        network = element.annotation<Network>()?.let {
            hasNetworkAnnotation = true
            try {
                it.responseHandler
            } catch (mte: MirroredTypeException) {
                responseHandler = mte.typeMirror.toTypeElement().toClassName()
            }
            try {
                it.errorConverter
            } catch (mte: MirroredTypeException) {
                errorConverter = mte.typeMirror.toTypeElement().toClassName()
            }
        } != null

        element.annotationMirrors.forEach {
            val typeName = it.annotationType.typeName
            if (retrofitMethodSet.contains(typeName)) {
                hasRetrofit = true
                network = true
                hasNetworkAnnotation = true
            }
        }
    }

    fun MethodSpec.Builder.addToConstructor(dataType: TypeName?): Pair<String, Array<Any?>> {
        var args = arrayOf<Any?>(RETROFIT_SOURCE, dataType)
        val returnString = buildString {
            append("\n\$T.<\$T>builderInstance(")
            if (errorConverter != ClassName.OBJECT && responseHandler == ClassName.OBJECT) {
                append("new \$T()")
                args += errorConverter
            } else if (errorConverter != ClassName.OBJECT && responseHandler != ClassName.OBJECT) {
                append("new \$T(), new \$T()")
                args += responseHandler
                args += errorConverter
            } else if (errorConverter == ClassName.OBJECT && responseHandler != ClassName.OBJECT) {
                append("new \$T()")
                args += responseHandler
            }
            append(").build()")
        }

        return returnString to args
    }

    fun MethodSpec.Builder.addToType(params: List<DataRequestParamDefinition>,
                                     controllerName: String, reuse: Boolean,
                                     targets: Boolean) {
        if (hasRetrofit && network && (hasNetworkAnnotation || !targets)) {
            this.code {
                add("request.targetSource(\$T.networkParams(),", DATA_SOURCE_PARAMS)
                indent()
                add("\n new \$T<>(service.", RETROFIT_SOURCE_PARAMS)
                addServiceCall(params, controllerName, reuse, this)
                add("));\n")
                unindent()
            }
        }
    }

    fun addServiceCall(params: List<DataRequestParamDefinition>,
                       controllerName: String, reuse: Boolean, codeBlock: CodeBlock.Builder) {
        codeBlock.add("${if (reuse && !hasNetworkAnnotation) controllerName else elementName}(")
        codeBlock.add(params.filter { it.isQuery }.joinToString { it.paramName })
        codeBlock.add(")")
    }

    fun MethodSpec.Builder.addIfTargets() {
        statement("request.addRequestSourceTarget(\$T.networkParams())", DATA_SOURCE_PARAMS);
    }
}