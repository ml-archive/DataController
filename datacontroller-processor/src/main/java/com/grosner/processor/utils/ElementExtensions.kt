package com.grosner.processor.utils

import com.grosner.processor.DataControllerProcessorManager
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

// element extensions

fun Element?.toTypeElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.managerDataController) = this?.asType().toTypeElement(managerDataController)


fun Element?.toTypeErasedElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.managerDataController) = this?.asType().erasure(managerDataController).toTypeElement(managerDataController)

val Element.simpleString
    get() = simpleName.toString()

// TypeMirror extensions

fun TypeMirror?.toTypeElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.managerDataController): TypeElement? = managerDataController.elements.getTypeElement(toString())

fun TypeMirror?.erasure(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.managerDataController): TypeMirror? = managerDataController.typeUtils.erasure(this)


// TypeName

fun TypeName?.toTypeElement(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.managerDataController): TypeElement? = managerDataController.elements.getTypeElement(toString())

inline fun <reified T : Annotation> Element?.annotation() = this?.getAnnotation(T::class.java)

fun Element?.getPackage(managerDataController: DataControllerProcessorManager = DataControllerProcessorManager.managerDataController) = managerDataController.elements.getPackageOf(this)

fun Element?.toClassName() = ClassName.get(this as TypeElement)