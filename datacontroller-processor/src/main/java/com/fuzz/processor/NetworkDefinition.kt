package com.fuzz.processor

import com.fuzz.datacontroller.annotations.Network
import com.fuzz.datacontroller.source.DataSource.SourceType.NETWORK
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
    : BaseSourceTypeDefinition<Network>(Network::class, element, processorManager) {

    var responseHandler = ClassName.OBJECT
    var errorConverter = ClassName.OBJECT

    var hasRetrofit = false

    init {
        element.annotationMirrors.forEach {
            val typeName = it.annotationType.typeName
            if (retrofitMethodSet.contains(typeName)) {
                hasRetrofit = true
                enabled = true
                hasAnnotationDirect = true
            }
        }
    }

    override fun Network.processAnnotation() {
        try {
            responseHandler
        } catch (mte: MirroredTypeException) {
            this@NetworkDefinition.responseHandler = mte.typeMirror.toTypeElement().toClassName()
        }
        try {
            errorConverter
        } catch (mte: MirroredTypeException) {
            this@NetworkDefinition.errorConverter = mte.typeMirror.toTypeElement().toClassName()
        }
        try {
            refreshStrategy
        } catch (mte: MirroredTypeException) {
            refreshStrategyClassName = mte.typeMirror.toClassName()
        }
    }

    override val requestSourceTarget = "network"

    override fun MethodSpec.Builder.addToConstructor(dataType: TypeName?): Pair<String, Array<Any?>> {
        val args = mutableListOf<Any?>(RETROFIT_SOURCE, dataType)
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
            appendRefreshStrategy(this, args)
            append(").build()")
        }

        return returnString to args.toTypedArray()
    }

    override fun MethodSpec.Builder.addToType(params: List<DataRequestParamDefinition>,
                                              dataType: TypeName?,
                                              classDataType: ClassName,
                                              controllerName: String, reuse: Boolean,
                                              targets: Boolean, specialParams: List<DataRequestParamDefinition>) {
        if (hasRetrofit && enabled && (hasAnnotationDirect || !targets)) {
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
        add("${if (reuse && !hasAnnotationDirect) controllerName else elementName}(")
        add(params.filter { it.isQuery }.joinToString { it.paramName })
        add(")")
    }

}