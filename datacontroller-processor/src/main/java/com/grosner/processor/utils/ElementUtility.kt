package com.grosner.processor.utils

import com.grosner.processor.DataControllerProcessorManager
import com.squareup.javapoet.ClassName
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
object ElementUtility {

    /**
     * @return real full-set of elements, including ones from super-class.
     */
    fun getAllElements(element: TypeElement, managerDataController: DataControllerProcessorManager): List<Element> {
        val elements = ArrayList(managerDataController.elements.getAllMembers(element))
        var superMirror: TypeMirror? = null
        var typeElement: TypeElement? = element
        while (typeElement?.superclass.let { superMirror = it; it != null }) {
            typeElement = managerDataController.typeUtils.asElement(superMirror) as TypeElement?
            typeElement?.let {
                val superElements = managerDataController.elements.getAllMembers(typeElement)
                superElements.forEach { if (!elements.contains(it)) elements += it }
            }
        }
        return elements
    }

    fun isInSamePackage(managerDataController: DataControllerProcessorManager, elementToCheck: Element, original: Element): Boolean {
        return managerDataController.elements.getPackageOf(elementToCheck).toString() == managerDataController.elements.getPackageOf(original).toString()
    }

    fun isPackagePrivate(element: Element): Boolean {
        return !element.modifiers.contains(Modifier.PUBLIC) && !element.modifiers.contains(Modifier.PRIVATE)
                && !element.modifiers.contains(Modifier.STATIC)
    }

    fun getClassName(elementClassname: String, managerDataController: DataControllerProcessorManager): ClassName? {
        val typeElement: TypeElement? = managerDataController.elements.getTypeElement(elementClassname)
        return if (typeElement != null) {
            ClassName.get(typeElement)
        } else {
            val names = elementClassname.split(".")
            if (names.isNotEmpty()) {
                // attempt to take last part as class name
                val className = names[names.size - 1]
                ClassName.get(elementClassname.replace("." + className, ""), className)
            } else {
                null
            }
        }
    }
}
