package com.fuzz.processor

import com.fuzz.processor.DataControllerProcessorManager.Companion.manager
import com.fuzz.processor.utils.ElementUtility
import com.fuzz.processor.utils.erasure
import com.fuzz.processor.utils.toTypeElement
import com.squareup.javapoet.ClassName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.KClass

/**
 * Whether the specified element implements the [KClass]
 */
fun TypeElement?.implementsClass(className: KClass<*>, processingEnvironment: ProcessingEnvironment = manager.processingEnvironment)
        = implementsClass(ClassName.get(className.java), processingEnvironment)

/**
 * Whether the specified element implements the [ClassName]
 */
fun TypeElement?.implementsClass(className: ClassName, processingEnvironment: ProcessingEnvironment = manager.processingEnvironment)
        = implementsClass(className.toString(), processingEnvironment)

/**
 * Whether the specified element is assignable to the fqTn parameter

 * @param processingEnvironment The environment this runs in
 * *
 * @param fqTn                  THe fully qualified type name of the element we want to check
 * *
 * @param element               The element to check that implements
 * *
 * @return true if element implements the fqTn
 */
fun TypeElement?.implementsClass(fqTn: String, processingEnvironment: ProcessingEnvironment = manager.processingEnvironment): Boolean {
    val typeElement = processingEnvironment.elementUtils.getTypeElement(fqTn)
    if (typeElement == null) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR,
                "Type Element was null for: $fqTn ensure that the visibility of the class is not private.")
        return false
    } else {
        val classMirror: TypeMirror? = typeElement.asType().erasure()
        if (classMirror == null || this?.asType() == null) {
            return false
        }
        val elementType = this.asType()
        return elementType != null && (processingEnvironment.typeUtils.isAssignable(elementType, classMirror) || elementType == classMirror)
    }
}

/**
 * Whether the specified element is assignable to the [className] parameter
 */
fun TypeElement?.isSubclass(className: ClassName, processingEnvironment: ProcessingEnvironment
= manager.processingEnvironment)
        = isSubclass(className.toString(), processingEnvironment)

/**
 * Whether the specified element is assignable to the [fqTn] parameter
 */
fun TypeElement?.isSubclass(fqTn: String, processingEnvironment: ProcessingEnvironment): Boolean {
    val typeElement = processingEnvironment.elementUtils.getTypeElement(fqTn)
    if (typeElement == null) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "Type Element was null for: $fqTn ensure that the visibility of the class is not private.")
        return false
    } else {
        val classMirror = typeElement.asType()
        return classMirror != null && this != null && this.asType() != null && processingEnvironment.typeUtils.isSubtype(this.asType(), classMirror)
    }
}

fun fromTypeMirror(typeMirror: TypeMirror, processorManager: DataControllerProcessorManager): ClassName? {
    val element = getTypeElement(typeMirror)
    return if (element != null) {
        ClassName.get(element)
    } else {
        ElementUtility.getClassName(typeMirror.toString(), processorManager)
    }
}

fun getTypeElement(element: Element): TypeElement? {
    val typeElement: TypeElement?
    if (element is TypeElement) {
        typeElement = element
    } else {
        typeElement = getTypeElement(element.asType())
    }
    return typeElement
}

fun getTypeElement(typeMirror: TypeMirror): TypeElement? {
    val manager = manager
    var typeElement: TypeElement? = typeMirror.toTypeElement(manager)
    if (typeElement == null) {
        val el = manager.typeUtils.asElement(typeMirror)
        typeElement = if (el != null) (el as TypeElement) else null
    }
    return typeElement
}

