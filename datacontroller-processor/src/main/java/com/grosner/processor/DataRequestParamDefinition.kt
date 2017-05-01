package com.grosner.processor

import com.grosner.datacontroller.annotations.DQuery
import com.grosner.kpoet.param
import com.grosner.kpoet.typeName
import com.grosner.processor.utils.annotation
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
        addParameter(param(variable.asType().typeName, paramName).build())
    }

}