package com.fuzz.processor

import com.grosner.kpoet.`interface`
import com.grosner.kpoet.`public final class`
import com.grosner.kpoet.constructor
import com.grosner.kpoet.javaFile
import com.squareup.javapoet.TypeVariableName
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.reflect.KClass

/**
 * Description: The main object graph during processing. This class collects all of the
 * processor classes and writes them to the corresponding database holders.
 */
class DataControllerProcessorManager internal constructor(val processingEnvironment: ProcessingEnvironment) : Handler {

    companion object {
        lateinit var manager: DataControllerProcessorManager
    }


    private val handlers = mutableSetOf(DataDefinitionHandler(), DataControllerConfigHandler(), CallbackHandler())

    val dataDefinitions = mutableListOf<DataDDefinition>()

    val callbackDefinitions = mutableListOf<CallbackDefinition>()

    var dataControllerConfigDefinition: DataControllerConfigDefinition? = null

    init {
        manager = this
    }

    val messager: Messager = processingEnvironment.messager

    val typeUtils: Types = processingEnvironment.typeUtils

    val elements: Elements = processingEnvironment.elementUtils

    fun logError(callingClass: KClass<*>?, error: String?, vararg args: Any?) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format("*==========*${callingClass ?: ""} :" + error?.trim() + "*==========*", *args))
        var stackTraceElements = Thread.currentThread().stackTrace
        if (stackTraceElements.size > 8) {
            stackTraceElements = stackTraceElements.copyOf(8)
        }
        stackTraceElements.forEach { messager.printMessage(Diagnostic.Kind.ERROR, it.toString()) }
    }

    fun logError(error: String?, vararg args: Any?) = logError(callingClass = null, error = error, args = args)

    fun logWarning(error: String, vararg args: Any) {
        messager.printMessage(Diagnostic.Kind.WARNING, String.format("*==========*\n$error\n*==========*", *args))
    }

    fun logWarning(callingClass: Class<*>, error: String, vararg args: Any) {
        logWarning("$callingClass : $error", *args)
    }

    override fun handle(manager: DataControllerProcessorManager, roundEnvironment: RoundEnvironment) {
        handlers.forEach { it.handle(manager, roundEnvironment) }

        dataDefinitions.forEach { it.prepareToWrite(dataControllerConfigDefinition) }
        dataDefinitions.forEach { it.write() }

        callbackDefinitions.forEach { it.write() }

    }

}
