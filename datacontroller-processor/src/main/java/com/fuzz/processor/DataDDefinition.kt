package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DataDefinition
import com.fuzz.processor.utils.ElementUtility
import com.grosner.kpoet.`private final field`
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
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

    val hasNetworkApi: Boolean

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

        reqDefinitions.forEach { it.evaluateReuse(reqDefinitions) }

        hasNetworkApi = reqDefinitions.find { it.network } != null
    }

    override val implementsClasses
        get() = arrayOf(elementTypeName!!)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        val constructor = MethodSpec.constructorBuilder().modifiers(public)
        val interfaceClassName = elementClassName!!.simpleName().toString()
        val interfaceClass = ClassName.get(packageName, outputClassName!!.simpleName().toString(), interfaceClassName)

        if (hasNetworkApi) {
            constructor.apply {
                addParameter(RETROFIT, "retrofit").build()
                statement("this.service = retrofit.create(\$T.class)", interfaceClass)
            }

            typeBuilder.apply {
                `private final field`(interfaceClass, "service")
            }

        }

        val retrofitInterface = TypeSpec.interfaceBuilder(interfaceClass)

        reqDefinitions.forEach {
            it.apply {
                retrofitInterface.addToRetrofitInterface()
                constructor.addToConstructor()
                typeBuilder.addToType()
            }
        }
        typeBuilder.addMethod(constructor.build())

        if (hasNetworkApi) {
            typeBuilder.addType(retrofitInterface.build())
        }
    }
}