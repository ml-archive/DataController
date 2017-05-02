package com.fuzz.processor

import com.fuzz.datacontroller.annotations.Network
import com.fuzz.datacontroller.source.DataSource.SourceType.NETWORK
import com.fuzz.processor.utils.annotation
import com.fuzz.processor.utils.toClassName
import com.fuzz.processor.utils.toTypeElement
import com.grosner.kpoet.code
import com.grosner.kpoet.statement
import com.grosner.kpoet.typeName
import com.squareup.javapoet.*
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
                                     dataType: TypeName?,
                                     controllerName: String, reuse: Boolean,
                                     targets: Boolean, specialParams: List<DataRequestParamDefinition>) {
        if (hasRetrofit && network && (hasNetworkAnnotation || !targets)) {
            this.code {
                val param = specialParams.filter { it.isParamData && it.targetedSourceForParam == NETWORK }.getOrNull(0)
                val paramsName = "params$NETWORK"

                add("\$T $paramsName = new \$T<>(service.", ParameterizedTypeName.get(RETROFIT_SOURCE_PARAMS, dataType), RETROFIT_SOURCE_PARAMS)
                addServiceCall(params, controllerName, reuse).add(");\n")
                if (param != null) {
                    statement("$paramsName.data = ${param.elementName}")
                }
                statement("request.targetSource(\$T.networkParams(), $paramsName)", DATA_SOURCE_PARAMS)
            }
        }
    }

    fun CodeBlock.Builder.addServiceCall(params: List<DataRequestParamDefinition>,
                                         controllerName: String, reuse: Boolean) = apply {
        add("${if (reuse && !hasNetworkAnnotation) controllerName else elementName}(")
        add(params.filter { it.isQuery }.joinToString { it.paramName })
        add(")")
    }

    fun MethodSpec.Builder.addIfTargets() {
        if (hasNetworkAnnotation) {
            statement("request.addRequestSourceTarget(\$T.networkParams())", DATA_SOURCE_PARAMS);
        }
    }
}