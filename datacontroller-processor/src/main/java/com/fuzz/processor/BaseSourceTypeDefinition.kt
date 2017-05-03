package com.fuzz.processor

import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.className
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import kotlin.reflect.KClass

/**
 * Description: The base class for source type definitions that define code that gets generated based on sources used.
 */
abstract class BaseSourceTypeDefinition<in T : Annotation>(
        annotationClass: KClass<T>,
        element: Element, processorManager: DataControllerProcessorManager)
    : BaseDefinition(element, processorManager) {

    var refreshStrategyClassName = DataSource.DefaultRefreshStrategy::class.className

    var hasAnnotationDirect = false
    var enabled = false

    init {
        enabled = element.getAnnotation(annotationClass.java)?.let {
            hasAnnotationDirect = true
            apply { it.processAnnotation() }
        } != null
    }

    abstract fun T.processAnnotation()

    abstract val requestSourceTarget: String

    abstract val requestSourceType: DataSource.SourceType

    abstract fun MethodSpec.Builder.addToConstructor(dataType: TypeName?): Pair<String, Array<Any?>>

    open fun TypeSpec.Builder.addToClass() = Unit

    open fun MethodSpec.Builder.addParams(paramsName: String,
                                          params: List<DataRequestParamDefinition>,
                                          dataType: TypeName?,
                                          classDataType: ClassName,
                                          controllerName: String,
                                          reuse: Boolean) = Unit

    open fun MethodSpec.Builder.addToType(params: List<DataRequestParamDefinition>,
                                          dataType: TypeName?,
                                          classDataType: ClassName,
                                          controllerName: String, reuse: Boolean,
                                          targets: Boolean, specialParams: List<DataRequestParamDefinition>,
                                          refInConstructor: Boolean) = Unit

    fun MethodSpec.Builder.addRequestCode(params: List<DataRequestParamDefinition>,
                                          dataType: TypeName?,
                                          classDataType: ClassName,
                                          controllerName: String,
                                          reuse: Boolean,
                                          specialParams: List<DataRequestParamDefinition>) {
        val param = specialParams.filter { it.isParamData && it.targetedSourceForParam == requestSourceType }.getOrNull(0)
        var paramsName = "params$requestSourceType"

        if (param == null || !param.isSourceParamsData) {
            addParams(paramsName, params, dataType, classDataType, controllerName, reuse)
            if (param != null) {
                statement("$paramsName.data = ${param.paramName}")
            }
        } else {
            // source params passed in as ParamsData override default implementation.
            paramsName = param.paramName
        }
        statement("request.targetSource(\$T.${requestSourceTarget}Params(), $paramsName)", DATA_SOURCE_PARAMS)
    }

    fun appendRefreshStrategy(stringBuilder: StringBuilder,
                              args: MutableList<Any?>) {
        if (refreshStrategyClassName != DataSource.DefaultRefreshStrategy::class.className) {
            stringBuilder.append(").refreshStrategy(new \$T()")
            args += refreshStrategyClassName
        }
    }

    fun MethodSpec.Builder.addIfTargets() {
        if (hasAnnotationDirect) {
            statement("request.addRequestSourceTarget(\$T.${requestSourceTarget}Params())", DATA_SOURCE_PARAMS)
        }
    }
}
