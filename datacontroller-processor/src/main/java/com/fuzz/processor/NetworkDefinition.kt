package com.fuzz.processor

import com.fuzz.datacontroller.annotations.Network
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.toClassName
import com.fuzz.processor.utils.toTypeElement
import com.grosner.kpoet.abstract
import com.grosner.kpoet.code
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.squareup.javapoet.*
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class NetworkDefinition(config: DataControllerConfigDefinition?,
                        element: Element, processorManager: DataControllerProcessorManager)
    : BaseSourceTypeDefinition<Network>(config, Network::class, element, processorManager) {

    var responseHandler = ClassName.OBJECT!!
    var errorConverter = ClassName.OBJECT!!
    var callReturnType = ClassName.OBJECT!!

    var hasRetrofit = false

    init {
        element.annotationMirrors.forEach {
            val typeName = it.annotationType.toClassName()
            if (typeName.packageName().startsWith("retrofit2")) {
                hasRetrofit = true
                enabled = true
                hasAnnotationDirect = true
            }
        }
    }

    override val requestSourceType = DataSource.SourceType.NETWORK

    override val requestSourceTarget = "network"

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

        try {
            callReturnType
        } catch (mte: MirroredTypeException) {
            this@NetworkDefinition.callReturnType = mte.typeMirror.toClassName()
        }
    }

    override fun postProcessAnnotation() {
        // config overrides values here
        config?.let {
            if (errorConverter == ClassName.OBJECT) {
                errorConverter = it.errorConverter
            }
            if (responseHandler == ClassName.OBJECT) {
                responseHandler = it.responseHandler
            }
        }

    }

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
                                              targets: Boolean, specialParams: List<DataRequestParamDefinition>, refInConstructor: Boolean) {
        if (hasRetrofit && enabled && (hasAnnotationDirect || !targets || refInConstructor)) {
            addRequestCode(params, dataType, classDataType, controllerName, reuse, specialParams)
        }
    }

    override fun MethodSpec.Builder.addParams(paramsName: String,
                                              params: List<DataRequestParamDefinition>,
                                              dataType: TypeName?, classDataType: ClassName,
                                              controllerName: String, reuse: Boolean) {
        code {
            add("\$T $paramsName = new \$T<>(service.", ParameterizedTypeName.get(RETROFIT_SOURCE_PARAMS, dataType), RETROFIT_SOURCE_PARAMS)
            addServiceCall(params, controllerName, reuse).add(");\n")
        }
    }

    fun TypeSpec.Builder.addToRetrofitInterface(definition: DataRequestDefinition) {
        var returnType = definition.dataType
        if (callReturnType != ClassName.OBJECT) {
            returnType = callReturnType
        }
        if (hasRetrofit && (!definition.reuse && enabled || hasAnnotationDirect)) {
            public(ParameterizedTypeName.get(CALL, returnType), elementName) {
                definition.apply { applyAnnotations() }
                modifiers(abstract)
                definition.params.filter { it.isQuery }.forEach { it.apply { this@public.addRetrofitParamCode() } }
                this
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