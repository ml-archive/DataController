package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DQuery
import com.fuzz.datacontroller.annotations.ParamData
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.annotation
import com.fuzz.processor.utils.dataControllerAnnotation
import com.fuzz.processor.utils.toTypeElement
import com.fuzz.processor.utils.toTypeErasedElement
import com.grosner.kpoet.param
import com.grosner.kpoet.statement
import com.grosner.kpoet.typeName
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import javax.lang.model.element.VariableElement

class DataRequestParamDefinition(element: VariableElement, processorManager: DataControllerProcessorManager)
    : BaseDefinition(element, processorManager) {

    val variable = element

    var paramName = ""

    var isParamData = false
    var isSourceParamsData = false
    var targetedSourceForParam = DataSource.SourceType.NETWORK

    init {
        paramName = elementName
        variable.annotation<DQuery>()?.let {
            paramName = it.value
        }
        isParamData = variable.annotation<ParamData>()?.let {
            targetedSourceForParam = it.targetedSource
        } != null

        if (isParamData) {
            isSourceParamsData = element.toTypeErasedElement().isSubclass(SOURCE_PARAMS)
        }
    }
    /**
     * If true, it is used in query and network requests, otherwise used as special kind of param.
     */
    val isQuery: Boolean
        get() = (!isCallback && !isErrorFilter && !isParamData && !isSourceParams)

    val isCallback: Boolean
        get() {
            val typeName = variable.asType().typeName
            return (typeName is ParameterizedTypeName && typeName.rawType == DATACONTROLLER_CALLBACK)
        }

    val isErrorFilter: Boolean
        get() = variable.asType().typeName == ERROR_FILTER

    val isSourceParams: Boolean
        get() = !isParamData && variable.asType().toTypeElement().isSubclass(SOURCE_PARAMS)

    fun MethodSpec.Builder.addSpecialCode() {
        if (isCallback) {
            statement("request.register($paramName)")
        } else if (isErrorFilter) {
            statement("request.errorFilter($paramName)")
        } else if (isSourceParams) {
            statement("request.sourceParams($paramName)")
        }
    }

    fun MethodSpec.Builder.addParamCode() = apply {
        val param = param(variable.asType().typeName, paramName)
        variable.annotationMirrors.forEach { param.addAnnotation(AnnotationSpec.get(it)) }
        addParameter(param.build())
    }

    fun MethodSpec.Builder.addRetrofitParamCode() = apply {
        val param = param(variable.asType().typeName, paramName)
        variable.annotationMirrors.filterNot {
            it.dataControllerAnnotation()
        }.forEach { param.addAnnotation(AnnotationSpec.get(it)) }
        addParameter(param.build())
    }

}