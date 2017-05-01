package com.grosner.processor

import com.grosner.datacontroller.annotations.DB
import com.grosner.datacontroller.annotations.Memory
import com.grosner.kpoet.*
import com.grosner.processor.utils.annotation
import com.grosner.processor.utils.toClassName
import com.grosner.processor.utils.toTypeElement
import com.squareup.javapoet.*
import javax.lang.model.element.ExecutableElement

/**
 * Description: Represents a method containing annotations that construct our request methods.
 */
class DataRequestDefinition(executableElement: ExecutableElement, dataControllerProcessorManager: DataControllerProcessorManager)
    : BaseDefinition(executableElement, dataControllerProcessorManager), TypeAdder {

    var memory = false
    var db = false
    var singleDb = true
    var async = false
    var network = false

    val params: List<DataRequestParamDefinition>

    var dataType: ClassName? = null

    var callbackParamName = ""

    val controllerName: String

    init {

        db = executableElement.annotation<DB>() != null
        memory = executableElement.annotation<Memory>() != null

        executableElement.annotationMirrors.forEach {
            val typeName = it.annotationType.typeName
            if (retrofitMethodSet.contains(typeName)) {
                network = true
            }
        }

        params = executableElement.parameters.map { DataRequestParamDefinition(it, managerDataController) }

        val nameAllocator = NameAllocator()
        controllerName = nameAllocator.newName(elementName)

        // needs a proper annotation otherwise we throw it away.
        if (!db && !memory) {
            valid = false
        } else {

            val returnType = executableElement.returnType.typeName
            (returnType as ParameterizedTypeName).let {
                val typeParameters = it.typeArguments
                dataType = typeParameters[0].toTypeElement().toClassName()
            }
        }

        params.forEach {
            val typeName = it.variable.asType().typeName
            if (typeName is ParameterizedTypeName && typeName.rawType == DATACONTROLLER_CALLBACK) {
                callbackParamName = it.paramName
                it.isCallback = true
            }
        }
    }

    fun MethodSpec.Builder.addToConstructor() {
        code {
            add("$controllerName = \$T.controllerOf(", DATACONTROLLER)
            if (memory) {
                add("\n\$T.<\$T>builderInstance().build()", MEMORY_SOURCE, dataType)
            }
            if (db) {
                if (memory) {
                    add(",")
                }
                add("\n\$T.<\$T>builderInstance(\$T.class, ${async.L}).build()",
                        if (singleDb) DBFLOW_SINGLE_SOURCE else DBFLOW_LIST_SOURCE, dataType, dataType)
            }

            add(");\n")
        }
    }

    override fun TypeSpec.Builder.addToType() {
        `private final field`(ParameterizedTypeName.get(DATACONTROLLER, dataType), controllerName)

        public(DATACONTROLLER_REQUEST, elementName) {
            params.forEach { it.apply { this@public.addParamCode() } }
            (element as ExecutableElement).annotationMirrors.forEach {
                addAnnotation(AnnotationSpec.get(it))
            }
            annotation(Override::class)

            statement("\$T request = $controllerName.request()",
                    ParameterizedTypeName.get(DATACONTROLLER_REQUEST_BUILDER, dataType))
            if (callbackParamName.isNotEmpty()) {
                statement("request.register($callbackParamName)")
            }
            if (network) {

            }
            if (db) {
                code {
                    add("request.targetSource(\$T.diskParams(),", DATA_SOURCE_PARAMS)
                    indent()
                    add("\nnew \$T(\n\$T.select().from(\$T.class).where()",
                            DBFLOW_PARAMS, SQLITE, dataType)
                    indent()
                    params.forEach {
                        if (!it.isCallback) {
                            add("\n.and(\$T.${it.paramName}.eq(${it.paramName}))",
                                    ClassName.get(dataType!!.packageName(), "${dataType!!.simpleName()}_Table"))
                        }
                    }
                    unindent()

                    add("));\n")
                    unindent()
                }
            }
            `return`("request.build()")
        }
    }
}