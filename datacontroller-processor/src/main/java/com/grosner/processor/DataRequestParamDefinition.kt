package com.grosner.processor

import com.grosner.datacontroller.annotations.DQuery
import com.grosner.kpoet.param
import com.grosner.kpoet.typeName
import com.grosner.processor.utils.annotation
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.VariableElement

class DataRequestParamDefinition(element: VariableElement, processorManager: DataControllerProcessorManager)
    : BaseDefinition(element, processorManager) {

    val variable = element

    var paramName = ""

    var isCallback = false

    init {
        paramName = elementName
        variable.annotation<DQuery>()?.let {
            paramName = it.value
        }
    }

    fun MethodSpec.Builder.addParamCode() = apply {
        val param = param(variable.asType().typeName, paramName)
        variable.annotationMirrors.forEach { param.addAnnotation(AnnotationSpec.get(it)) }
        addParameter(param.build())
    }

}