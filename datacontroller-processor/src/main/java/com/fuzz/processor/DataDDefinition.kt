package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DataDefinition
import com.grosner.kpoet.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description: Represents [DataDefinition]
 */
class DataDDefinition(typeElement: TypeElement, manager: DataControllerProcessorManager)
    : BaseDefinition(typeElement, manager) {

    val reqDefinitions = mutableListOf<DataRequestDefinition>()

    val hasNetworkApi: Boolean

    init {
        setOutputClassName("_Def")
        val members = typeElement.enclosedElements
        members.forEach {
            if (it is ExecutableElement) {
                val definition = DataRequestDefinition(it, manager)
                if (definition.valid) {
                    reqDefinitions += definition
                }
            }
        }

        reqDefinitions.forEach { it.evaluateReuse(reqDefinitions) }

        hasNetworkApi = reqDefinitions.find { it.network } != null
    }

    override val implementsClasses
        get() = arrayOf(elementTypeName!!, DATA_DEFINITION)

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

        reqDefinitions.distinctBy { it.controllerName }.forEach {
            constructor.addParameter(param(DATASOURCE_PROVIDER, "${it.controllerName}Provider").build())
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