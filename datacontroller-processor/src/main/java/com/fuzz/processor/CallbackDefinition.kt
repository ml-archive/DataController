package com.fuzz.processor

import com.fuzz.datacontroller.DataController.DataControllerCallback
import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.DataResponseError
import com.fuzz.datacontroller.annotations.ErrorMethod
import com.fuzz.datacontroller.annotations.SuccessMethod
import com.fuzz.processor.utils.*
import com.grosner.kpoet.*
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass


class CallbackMethodDefinition(element: ExecutableElement, manager: DataControllerProcessorManager)
    : BaseDefinition(element, manager) {

    val methodName = elementName

    val isSuccess: Boolean

    var callbackName = ""

    // if true, we unwrap the DataController response object for you to the raw type.
    var isSuccessRaw = false
    var nonNullWrapping = false

    var successCallbackType: TypeName? = null

    init {
        isSuccess = element.annotation<SuccessMethod>()?.let {
            callbackName = it.value
            nonNullWrapping = it.nonNullWrapping
        } != null
        element.annotation<ErrorMethod>()?.let { callbackName = it.value }

        val parameters = element.parameters
        if (parameters.size != 1) {
            manager.logError(CallbackDefinition::class, "Can only specify 1 parameter to " +
                    "a callback method for $elementName. Found ${parameters.size}")
        } else {
            val param = parameters[0].typeName
            if (param is ParameterizedTypeName && param.rawType.toTypeElement().implementsClass(DataControllerResponse::class)) {
                if (!isSuccess) {
                    foundInvalidTypeError(param, DataControllerResponse::class)
                } else {
                    successCallbackType = param.typeArguments[0]
                }
            } else {
                val paramType = param.toTypeElement()
                if (paramType.implementsClass(DataResponseError::class)) {
                    if (isSuccess) {
                        foundInvalidTypeError(param, DataResponseError::class)
                    }
                } else {
                    // assume callback type
                    if (isSuccess) {
                        successCallbackType = param
                        isSuccessRaw = true
                    }
                }
            }
        }

        if (executableElement.modifiers.find {
            it == Modifier.PRIVATE || it == Modifier.PROTECTED || it == Modifier.ABSTRACT
        } != null) {
            manager.logError(CallbackDefinition::class, "Invalid visibility modifiers. " +
                    "Method $elementName must be public or package private.")
        }
    }

    fun MethodSpec.Builder.addMethodCall() {
        val paramStatement = when {
            isSuccessRaw -> "$responseObjectName.getResponse()"
            isSuccess -> responseObjectName
            else -> errorObjectName
        }
        if (isSuccessRaw && nonNullWrapping) {
            `if`("$paramStatement != null") {
                statement("$callbackObjectName.$elementName($paramStatement)")
            }.end()
        } else {
            statement("$callbackObjectName.$elementName($paramStatement)")
        }
    }


    private fun foundInvalidTypeError(param: TypeName, suggested: KClass<*>) {
        manager.logError(CallbackMethodDefinition::class, "Found invalid parameter type $param for $elementName. Change to $suggested")
    }

}

/**
 * Description:
 */
class CallbackDefinition(element: TypeElement, manager: DataControllerProcessorManager)
    : BaseDefinition(element, manager) {

    val successCallbackMethods = mutableListOf<CallbackMethodDefinition>()
    val errorCallbackMethods = mutableListOf<CallbackMethodDefinition>()

    init {
        setOutputClassName("_CallbackRegistry")

        val members = ElementUtility.getAllElements(element, manager)
                .asSequence()
                .filter { it is ExecutableElement }.map { it as ExecutableElement }
        members.forEach {
            if (it is ExecutableElement) {
                val isSuccess = it.annotation<SuccessMethod>() != null
                val isError = it.annotation<ErrorMethod>() != null
                if (isSuccess) {
                    successCallbackMethods += CallbackMethodDefinition(it, manager)
                } else if (isError) {
                    errorCallbackMethods += CallbackMethodDefinition(it, manager)
                }
            }
        }
    }

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {

            `private final field`(elementTypeName!!, callbackObjectName)

            constructor(param(elementTypeName!!, callbackObjectName)) {
                modifiers(public)
                statement("this.$callbackObjectName = $callbackObjectName")
            }


            val successByMethods = successCallbackMethods.groupBy { it.callbackName }
            val errorByMethods = errorCallbackMethods.groupBy { it.callbackName }

            val uniqueCallbackNames = successCallbackMethods.map { it.callbackName }.toSortedSet()
            errorCallbackMethods.mapTo(uniqueCallbackNames) { it.callbackName }

            uniqueCallbackNames.forEach {
                val callbackType: TypeName? =
                        if (successByMethods.isNotEmpty()) {
                            successByMethods.getValue(it)[0].successCallbackType
                        } else {
                            null
                        }
                if (callbackType != null) {
                    `public field`(ParameterizedTypeName.get(DataControllerCallback::class.className, callbackType), it) {
                        addModifiers(final)
                        `=` {
                            add(`anonymous class`("") {
                                extends(ParameterizedTypeName.get(DataControllerCallback::class.className, callbackType))
                                `public`(TypeName.VOID, "onSuccess",
                                        param(ParameterizedTypeName.get(DataControllerResponse::class.className, callbackType),
                                                responseObjectName)) {
                                    annotation(Override::class)
                                    `if`("$callbackObjectName != null") {
                                        successByMethods.getValue(it).forEach {
                                            it.apply { this@`public`.addMethodCall() }
                                        }
                                        this
                                    }.end()
                                    this
                                }

                                public(TypeName.VOID, "onFailure", param(DataResponseError::class.typeName, errorObjectName)) {
                                    annotation(Override::class)
                                    `if`("$callbackObjectName != null") {
                                        errorByMethods.getValue(it).forEach {
                                            it.apply { this@`public`.addMethodCall() }
                                        }
                                        this
                                    }.end()
                                    this
                                }
                            }.toString())
                        }
                    }
                }

            }

        }
    }
}


