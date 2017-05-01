package com.grosner.processor

import com.grosner.datacontroller.annotations.DataDefinition
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.grosner.processor.utils.ElementUtility
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description: Represents [DataDefinition]
 */
class DataDDefinition(typeElement: TypeElement, dataControllerProcessorManager: DataControllerProcessorManager)
    : BaseDefinition(typeElement, dataControllerProcessorManager) {

    val reqDefinitions = mutableListOf<DataRequestDefinition>()

    init {
        setOutputClassName("_Def")
        val members = ElementUtility.getAllElements(typeElement, dataControllerProcessorManager)
        members.forEach {
            if (it is ExecutableElement) {
                val definition = DataRequestDefinition(it, dataControllerProcessorManager)
                if (definition.valid) {
                    reqDefinitions += definition
                }
            }
        }
    }

    override val implementsClasses
        get() = arrayOf(elementTypeName!!)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        val constructor = MethodSpec.constructorBuilder().modifiers(public)
        reqDefinitions.forEach {
            it.apply {
                constructor.addToConstructor()
                typeBuilder.addToType()
            }
        }
        typeBuilder.addMethod(constructor.build())
    }
}