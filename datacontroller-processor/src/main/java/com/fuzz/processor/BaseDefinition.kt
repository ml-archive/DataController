package com.fuzz.processor

import com.fuzz.processor.utils.ElementUtility
import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.processor.definition.TypeDefinition
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import java.lang.IllegalStateException
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Description: Holds onto a common-set of fields and provides a common-set of methods to output class files.
 */
abstract class BaseDefinition : TypeDefinition {

    val managerDataController: DataControllerProcessorManager

    var elementClassName: ClassName? = null
    var elementTypeName: TypeName? = null
    var outputClassName: ClassName? = null
    var erasedTypeName: TypeName? = null

    var element: Element
    var typeElement: TypeElement? = null
    var elementName: String

    var packageName: String

    var valid = true

    constructor(element: ExecutableElement, dataControllerProcessorManager: DataControllerProcessorManager) {
        this.managerDataController = dataControllerProcessorManager
        this.element = element
        packageName = managerDataController.elements.getPackageOf(element)?.qualifiedName?.toString() ?: ""
        elementName = element.simpleName.toString()

        try {
            val typeMirror = element.asType()
            elementTypeName = typeMirror.typeName
            elementTypeName?.let {
                if (!it.isPrimitive) {
                    elementClassName = getElementClassName(element)
                }
            }
            val erasedType = dataControllerProcessorManager.typeUtils.erasure(typeMirror)
            erasedTypeName = erasedType.typeName
        } catch (e: Exception) {

        }
    }

    constructor(element: Element, dataControllerProcessorManager: DataControllerProcessorManager) {
        this.managerDataController = dataControllerProcessorManager
        this.element = element
        packageName = managerDataController.elements.getPackageOf(element)?.qualifiedName?.toString() ?: ""
        try {
            val typeMirror: TypeMirror
            if (element is ExecutableElement) {
                typeMirror = element.returnType
                elementTypeName = typeMirror.typeName
            } else {
                typeMirror = element.asType()
                elementTypeName = typeMirror.typeName
            }
            val erasedType = dataControllerProcessorManager.typeUtils.erasure(typeMirror)
            erasedTypeName = TypeName.get(erasedType)
        } catch (i: IllegalArgumentException) {
            managerDataController.logError("Found illegal type: ${element.asType()} for ${element.simpleName}")
            managerDataController.logError("Exception here: $i")
        }

        elementName = element.simpleName.toString()
        elementTypeName?.let {
            if (!it.isPrimitive) elementClassName = getElementClassName(element)
        }

        if (element is TypeElement) {
            typeElement = element
        }
    }

    constructor(element: TypeElement, dataControllerProcessorManager: DataControllerProcessorManager) {
        this.managerDataController = dataControllerProcessorManager
        this.typeElement = element
        this.element = element
        elementClassName = ClassName.get(typeElement)
        elementTypeName = element.asType().typeName
        elementName = element.simpleName.toString()
        packageName = managerDataController.elements.getPackageOf(element)?.qualifiedName?.toString() ?: ""
    }

    protected open fun getElementClassName(element: Element?): ClassName? {
        try {
            return ElementUtility.getClassName(element?.asType().toString(), managerDataController)
        } catch (e: Exception) {
            return null
        }

    }

    protected fun setOutputClassName(postfix: String) {
        val outputName: String
        if (elementClassName == null) {
            if (elementTypeName is ClassName) {
                outputName = (elementTypeName as ClassName).simpleName()
            } else if (elementTypeName is ParameterizedTypeName) {
                outputName = (elementTypeName as ParameterizedTypeName).rawType.simpleName()
                elementClassName = (elementTypeName as ParameterizedTypeName).rawType
            } else {
                outputName = elementTypeName.toString()
            }
        } else {
            outputName = elementClassName!!.simpleName()
        }
        outputClassName = ClassName.get(packageName, outputName + postfix)
    }

    protected fun setOutputClassNameFull(fullName: String) {
        outputClassName = ClassName.get(packageName, fullName)
    }

    override val typeSpec: TypeSpec
        get() {
            return `public final class`(outputClassName?.simpleName() ?: "") {
                extendsClass?.let { extends(it) }
                implementsClasses.forEach { implements(it) }
                javadoc("This is generated code. Please do not modify")
                onWriteDefinition(this)
                this
            }
        }

    protected open val extendsClass: TypeName?
        get() = null

    protected open val implementsClasses: Array<TypeName>
        get() = arrayOf()

    open fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {

    }

    fun write(): Boolean {
        var success = false
        try {
            javaFile(packageName) { typeSpec }
                    .writeTo(managerDataController.processingEnvironment.filer)
            success = true
        } catch (e: IOException) {
            // ignored
        } catch (i: IllegalStateException) {
            managerDataController.logError(BaseDefinition::class, "Found error for class:$elementName")
            managerDataController.logError(BaseDefinition::class, i.message)
        }

        return success
    }

}
