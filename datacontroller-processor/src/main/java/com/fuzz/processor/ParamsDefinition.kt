package com.fuzz.processor

import com.fuzz.datacontroller.annotations.Params
import com.fuzz.processor.utils.annotation
import com.fuzz.processor.utils.erasure
import com.fuzz.processor.utils.toTypeElement
import com.grosner.kpoet.`return`
import com.grosner.kpoet.annotation
import com.grosner.kpoet.public
import com.grosner.kpoet.typeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.ExecutableElement

class ParamsDefinition(executableElement: ExecutableElement, processorManager: DataControllerProcessorManager)
    : BaseDefinition(executableElement, processorManager), TypeAdder {

    var referencedMethodName = ""

    lateinit var referencedDataRequestDefinition: DataRequestDefinition

    var useNetworkParams = false
    var useDBParams = false

    val params: List<DataRequestParamDefinition>

    init {
        executableElement.annotation<Params>()?.let {
            referencedMethodName = it.value
        }

        params = executableElement.parameters.map { DataRequestParamDefinition(it, manager) }

        val returnTypeElement = executableElement.returnType.erasure().toTypeElement()
        if (!returnTypeElement.isSubclass(SOURCE_PARAMS)) {
            manager.logError(ParamsDefinition::class, "Return type ${executableElement.returnType} must " +
                    "be subclass of type $SOURCE_PARAMS")
        } else {
            val validateReturnTypes = arrayOf(RETROFIT_SOURCE_PARAMS, DBFLOW_PARAMS)
            val className = validateReturnTypes.find { returnTypeElement.isSubclass(it) }
            if (className == null) {
                manager.logError(ParamsDefinition::class, "Return type ${executableElement.returnType} is invalid. Must be one of $validateReturnTypes")
            } else {
                if (className == RETROFIT_SOURCE_PARAMS) {
                    useNetworkParams = true
                } else if (className == DBFLOW_PARAMS) {
                    useDBParams = true
                }
            }
        }
    }

    fun findDataRequestDef(definitions: List<DataRequestDefinition>) {
        referencedDataRequestDefinition = definitions.find { it.elementName == referencedMethodName }!!

        val nonSpecialParams = referencedDataRequestDefinition.nonSpecialParams
        if (nonSpecialParams.size != params.size) {
            manager.logError(ParamsDefinition::class, "The method that returns params $elementName must match " +
                    "same length of query parameters as the referenced method.")
        } else {
            nonSpecialParams.forEachIndexed { index, param ->
                if (param.paramName != params[index].paramName) {
                    manager.logError(ParamsDefinition::class, "Parameters must match name and " +
                            "be in the same order as referenced method. Expected ${param.paramName} " +
                            "but found ${params[index].paramName}")
                }
            }
        }
    }

    override fun TypeSpec.Builder.addToType() {

        var def: BaseSourceTypeDefinition<*> = referencedDataRequestDefinition.networkDefinition
        if (useDBParams) {
            def = referencedDataRequestDefinition.dbDefinition
        }

        public(executableElement.returnType.typeName, elementName) {
            annotation(Override::class)
            params.forEach { it.apply { addParamCode() } }
            referencedDataRequestDefinition.apply { addToParamsMethod("params", def) }
            `return`("params")
            this
        }
    }
}