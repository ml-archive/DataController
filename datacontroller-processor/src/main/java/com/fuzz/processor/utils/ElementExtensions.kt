package com.fuzz.processor.utils

import com.fuzz.processor.DataControllerProcessorManager
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

// element extensions

fun Element?.toTypeElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.Companion.manager) = this?.asType().toTypeElement(managerDataController)


fun Element?.toTypeErasedElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.Companion.manager) = this?.asType().erasure(managerDataController).toTypeElement(managerDataController)

val Element.simpleString
    get() = simpleName.toString()

// TypeMirror extensions

fun TypeMirror?.toTypeElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.Companion.manager): TypeElement? = managerDataController.elements.getTypeElement(toString())

fun TypeMirror?.erasure(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.Companion.manager): TypeMirror? = managerDataController.typeUtils.erasure(this)

fun TypeMirror?.toClassName(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.Companion.manager) = toTypeElement(managerDataController).toClassName()

// TypeName

fun TypeName?.toTypeElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.Companion.manager): TypeElement? = managerDataController.elements.getTypeElement(toString())

inline fun <reified T : Annotation> Element?.annotation() = this?.getAnnotation(T::class.java)

fun Element?.getPackage(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.Companion.manager) = managerDataController.elements.getPackageOf(this)

fun Element?.toClassName() = ClassName.get(this as TypeElement)

fun AnnotationMirror?.dataControllerAnnotation(): Boolean {
    return if (this == null) {
        false
    } else {
        annotationType.toTypeElement().getPackage().toString().startsWith("com.fuzz.datacontroller")
    }
}