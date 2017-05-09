package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DataControllerConfig
import com.fuzz.datacontroller.annotations.DataDefinition
import com.fuzz.datacontroller.annotations.ErrorMethod
import com.fuzz.datacontroller.annotations.SuccessMethod
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

/**
 * Description: The main base-level handler for performing some action when the
 * [DataControllerProcessor.process] is called.
 */
interface Handler {

    /**
     * Called when the process of the [DataControllerProcessor] is called

     * @param manager The manager that holds processing information
     * *
     * @param roundEnvironment The round environment
     */
    fun handle(manager: DataControllerProcessorManager, roundEnvironment: RoundEnvironment)
}


/**
 * Description: The base handler than provides common callbacks into processing annotated top-level elements
 */
abstract class BaseHandler<AnnotationClass : Annotation> : Handler {

    override fun handle(manager: DataControllerProcessorManager, roundEnvironment: RoundEnvironment) {
        val annotatedElements = LinkedHashSet(roundEnvironment.getElementsAnnotatedWith(annotationClass.java))
        processElements(manager, annotatedElements)
        if (annotatedElements.size > 0) {
            annotatedElements.forEach { onProcessElement(manager, it) }
        }
    }

    protected abstract val annotationClass: KClass<AnnotationClass>

    open fun processElements(dataControllerProcessorManager: DataControllerProcessorManager, annotatedElements: MutableSet<Element>) {

    }

    protected abstract fun onProcessElement(dataControllerProcessorManager: DataControllerProcessorManager, element: Element)
}

class DataDefinitionHandler : BaseHandler<DataDefinition>() {
    override val annotationClass = DataDefinition::class

    override fun onProcessElement(dataControllerProcessorManager: DataControllerProcessorManager, element: Element) {
        if (element is TypeElement) {
            dataControllerProcessorManager.dataDefinitions += DataDDefinition(element, dataControllerProcessorManager)
        }
    }
}

class DataControllerConfigHandler : BaseHandler<DataControllerConfig>() {
    override val annotationClass = DataControllerConfig::class

    override fun onProcessElement(dataControllerProcessorManager: DataControllerProcessorManager, element: Element) {
        if (element is TypeElement) {
            if (dataControllerProcessorManager.dataControllerConfigDefinition != null) {
                dataControllerProcessorManager.logError(DataControllerConfigDefinition::class,
                        "Cannot specify more than one ${DataControllerConfig::class}")
            } else {
                dataControllerProcessorManager.dataControllerConfigDefinition =
                        DataControllerConfigDefinition(element, dataControllerProcessorManager)
            }
        }
    }
}

class CallbackHandler : Handler {
    override fun handle(manager: DataControllerProcessorManager, roundEnvironment: RoundEnvironment) {
        // find classes that have callback error and success methods
        val annotatedElements = LinkedHashSet(roundEnvironment.getElementsAnnotatedWith(SuccessMethod::class.java)
                .map { it.enclosingElement })
        LinkedHashSet(roundEnvironment.getElementsAnnotatedWith(ErrorMethod::class.java))
                .mapTo(annotatedElements) { it.enclosingElement }
        if (annotatedElements.size > 0) {
            annotatedElements.asSequence()
                    .filter { it is TypeElement }
                    .map { it as TypeElement }
                    .forEach {
                        manager.callbackDefinitions += CallbackDefinition(it, manager)
                    }
        }
    }
}