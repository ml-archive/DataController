package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DataDefinition
import com.fuzz.datacontroller.annotations.Params
import com.fuzz.processor.utils.annotation
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
    val paramsDefinitions = mutableListOf<ParamsDefinition>()

    val hasNetworkApi: Boolean
    val hasSharedPreferences: Boolean
    val hasOptionalConstructor: Boolean

    val networkDefinition = NetworkDefinition(typeElement, manager)

    init {
        setOutputClassName("_Def")
        val members = typeElement.enclosedElements
        members.forEach {
            if (it is ExecutableElement) {
                if (it.annotation<Params>() != null) {
                    val def = ParamsDefinition(it, manager)
                    paramsDefinitions += def
                } else {
                    val definition = DataRequestDefinition(it, manager)
                    if (definition.valid) {
                        reqDefinitions += definition
                    }
                }
            }
        }

        // one of the methods is has same name. enforce unique method name restriction
        if (reqDefinitions.distinctBy { it.elementName }.size != reqDefinitions.size) {
            manager.logError(DataDDefinition::class, "Interface methods in a DataDefinition must have unique names")
        }

        reqDefinitions.forEach {
            if (it.networkDefinition.enabled) {
                // override non specified values
                if (networkDefinition.responseHandler != ClassName.OBJECT && it.networkDefinition.responseHandler == ClassName.OBJECT) {
                    it.networkDefinition.responseHandler = networkDefinition.responseHandler
                }
                if (networkDefinition.errorConverter != ClassName.OBJECT && it.networkDefinition.errorConverter == ClassName.OBJECT) {
                    it.networkDefinition.errorConverter = networkDefinition.errorConverter
                }
            }

            it.evaluateReuse(reqDefinitions)
        }

        paramsDefinitions.forEach { it.findDataRequestDef(reqDefinitions) }

        hasNetworkApi = reqDefinitions.find { it.networkDefinition.enabled } != null
        hasSharedPreferences = reqDefinitions.find { it.sharedPrefsDefinition.enabled } != null

        hasOptionalConstructor = reqDefinitions.find { it.refOptional } != null
    }

    override val implementsClasses
        get() = arrayOf(elementTypeName!!, DATA_DEFINITION)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        val fullConstructor = MethodSpec.constructorBuilder().modifiers(public)
        val optionalConstructor = MethodSpec.constructorBuilder().modifiers(public)

        val interfaceClassName = elementClassName!!.simpleName().toString()
        val interfaceClass = ClassName.get(packageName, outputClassName!!.simpleName().toString(), interfaceClassName)

        if (hasNetworkApi) {
            arrayOf(fullConstructor, optionalConstructor).forEach {
                it.apply {
                    addParameter(RETROFIT, "retrofit").build()
                    statement("this.service = retrofit.create(\$T.class)", interfaceClass)
                }
            }

            typeBuilder.apply {
                `private final field`(interfaceClass, "service")
            }

        }

        if (hasSharedPreferences) {
            arrayOf(fullConstructor, optionalConstructor).forEach {
                it.apply {
                    addParameter(param(SHARED_PREFERENCES, "sharedPreferences").build())
                }
            }
        }

        val retrofitInterface = TypeSpec.interfaceBuilder(interfaceClass)

        reqDefinitions.filter { it.sharedPrefsDefinition.hasAnnotationDirect }.forEach {
            fullConstructor.statement("this.${it.sharedPrefsDefinition.preferenceDelegateName} = new \$T()", it.sharedPrefsDefinition.preferenceDelegateType)
            optionalConstructor.statement("this.${it.sharedPrefsDefinition.preferenceDelegateName} = new \$T()", it.sharedPrefsDefinition.preferenceDelegateType)
        }
        reqDefinitions.forEach {
            it.apply {
                retrofitInterface.addToRetrofitInterface()
                fullConstructor.addToConstructor(false)
                optionalConstructor.addToConstructor(true)
                typeBuilder.addToType()
            }
        }

        paramsDefinitions.forEach { it.apply { typeBuilder.addToType() } }

        typeBuilder.addMethod(fullConstructor.build())
        if (hasOptionalConstructor) {
            typeBuilder.addMethod(optionalConstructor.build())
        }

        if (hasNetworkApi) {
            typeBuilder.addType(retrofitInterface.build())
        }
    }
}