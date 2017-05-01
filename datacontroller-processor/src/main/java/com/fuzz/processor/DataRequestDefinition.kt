package com.fuzz.processor

import com.fuzz.datacontroller.annotations.*
import com.fuzz.processor.utils.annotation
import com.fuzz.processor.utils.dataControllerAnnotation
import com.fuzz.processor.utils.toClassName
import com.fuzz.processor.utils.toTypeElement
import com.grosner.kpoet.*
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
    var reuse = false
    var reuseMethodName = ""
    var targets = false

    var isRef = false

    var hasRetrofit = false

    var hasNetworkAnnotation = false
    var hasMemoryAnnotation = false
    var hasDBAnnotation = false

    var isSync = false

    val params: List<DataRequestParamDefinition>

    var dataType: ClassName? = null

    var specialParams = arrayListOf<DataRequestParamDefinition>()

    val controllerName: String

    init {
        isRef = executableElement.annotation<DataControllerRef>()?.let {
            controllerName = elementName
            reuseMethodName = elementName
        } != null

        targets = executableElement.annotation<Targets>() != null
        if (targets && isRef) {
            manager.logError(DataRequestDefinition::class,
                    "Cannot specify both ${Targets::class} and ${DataControllerRef::class}")
        }

        db = executableElement.annotation<DB>() != null
        if (db) hasDBAnnotation = true
        memory = executableElement.annotation<Memory>() != null
        if (memory) hasMemoryAnnotation = true
        executableElement.annotation<Reuse>()?.let {
            reuse = true
            reuseMethodName = it.value
        }

        executableElement.annotationMirrors.forEach {
            val typeName = it.annotationType.typeName
            if (retrofitMethodSet.contains(typeName)) {
                hasRetrofit = true
                network = true
                hasNetworkAnnotation = true
            }
        }

        if (!network) {
            network = executableElement.annotation<Network>()?.let { hasNetworkAnnotation = true } != null
        }

        params = executableElement.parameters.map { DataRequestParamDefinition(it, manager) }

        val nameAllocator = NameAllocator()

        if (!reuse) {
            controllerName = nameAllocator.newName(elementName)
        } else {
            controllerName = reuseMethodName
        }

        // needs a proper annotation otherwise we throw it away.
        if (!hasSourceAnnotations && !isRef && !reuse) {
            manager.logError(DataRequestDefinition::class, "The method $elementName must " +
                    "specify or target at least one source. Add an annotation to specify which to target.")
        } else {

            // if none assume we want all
            if (!hasSourceAnnotations) {
                network = true
                db = true
                memory = true
            }

            val returnType = executableElement.returnType.typeName
            validateReturnType(returnType)
            if (returnType is ParameterizedTypeName) {
                val typeParameters = returnType.typeArguments
                dataType = typeParameters[0].toTypeElement().toClassName()
            } else {
                isSync = true
                dataType = returnType.toTypeElement().toClassName()

                if (!reuse) {
                    manager.logError(DataRequestDefinition::class, "Synchronous requests must reuse another $DATACONTROLLER")
                }
            }
        }

        // find special param types and keep track here.
        params.filter { it.isCallback }.getOrNull(0)?.let { specialParams.add(it) }
        params.filter { it.isErrorFilter }.getOrNull(0)?.let { specialParams.add(it) }

    }

    val hasSourceAnnotations: Boolean
        get() = hasDBAnnotation || hasMemoryAnnotation || hasNetworkAnnotation

    private fun validateReturnType(returnType: TypeName) {
        if (returnType is ParameterizedTypeName &&
                (returnType.rawType != DATACONTROLLER_REQUEST && returnType.rawType != DATACONTROLLER)) {
            manager.logError(DataRequestDefinition::class, "Invalid return type found $returnType")
        }

        if (returnType is ParameterizedTypeName && returnType.rawType == DATACONTROLLER) {
            if (params.isNotEmpty()) {
                manager.logError(DataRequestDefinition::class, "Cannot specify params for DataController reference methods.")
            }
        }
    }

    fun evaluateReuse(reqDefinitions: MutableList<DataRequestDefinition>) {
        if (reuse || isRef && !hasSourceAnnotations) {
            val def = reqDefinitions.find { it.controllerName == controllerName && it != this && !it.reuse }
            if (def == null) {
                manager.logError(DataRequestDefinition::class,
                        "Could not find data controller $reuseMethodName for method $elementName." +
                                " Ensure you specify the name properly. Or you define source type " +
                                "annotations for a DataControllerRef")
            } else {
                if (def.dataType != dataType) {
                    manager.logError(DataRequestDefinition::class,
                            "The referenced $reuseMethodName must match $dataType. found ${def.dataType}.")
                } else {
                    network = def.network
                    db = def.db
                    singleDb = def.singleDb
                    async = def.async
                    memory = def.memory
                }
            }
        }
    }

    fun MethodSpec.Builder.applyAnnotations() {
        // don't redeclare any library annotations. forward anything else through.
        executableElement.annotationMirrors.filterNot {
            it.dataControllerAnnotation()
        }.forEach {
            addAnnotation(AnnotationSpec.get(it))
        }
    }

    fun MethodSpec.Builder.addToConstructor() {
        if (!reuse) {
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
                if (network) {
                    if (db || memory) {
                        add(",")
                    }
                    add("\n\$T.<\$T>builderInstance().build()", RETROFIT_SOURCE, dataType)
                }

                add(");\n")
            }
        }
    }

    fun TypeSpec.Builder.addToRetrofitInterface() {
        if (hasRetrofit && (!reuse && network || hasNetworkAnnotation)) {
            public(ParameterizedTypeName.get(CALL, dataType), elementName) {
                applyAnnotations()
                modifiers(abstract)
                params.filter { it.isQuery }.forEach { it.apply { this@public.addRetrofitParamCode() } }
                this
            }
        }
    }

    fun addServiceCall(codeBlock: CodeBlock.Builder) {
        codeBlock.add("${if (reuse && !hasNetworkAnnotation) controllerName else elementName}(")
        codeBlock.add(params.filter { it.isQuery }.joinToString { it.paramName })
        codeBlock.add(")")
    }

    override fun TypeSpec.Builder.addToType() {
        if (!reuse) {
            `private final field`(ParameterizedTypeName.get(DATACONTROLLER, dataType), controllerName)
        }

        public(executableElement.returnType.typeName, elementName) {
            params.forEach { it.apply { addParamCode() } }
            applyAnnotations()
            annotation(Override::class)
            if (isRef) {
                `return`(reuseMethodName)
            } else if (isSync) {
                var sourceParams = "memoryParams"
                if (db && !memory) {
                    sourceParams = "diskParams"
                }
                statement("\$T storage = $controllerName\n\t.getDataSource(\$T.$sourceParams())\n\t.getStoredData()",
                        dataType, DATA_SOURCE_PARAMS)

                // memory is always first, followed by db
                if (db && memory) {
                    `if`("storage == null") {
                        statement("storage = $controllerName\n\t.getDataSource(\$T.diskParams())\n\t.getStoredData()",
                                DATA_SOURCE_PARAMS)
                    }.end()
                }
                `return`("storage")
            } else {

                statement("\$T request = $controllerName.request()",
                        ParameterizedTypeName.get(DATACONTROLLER_REQUEST_BUILDER, dataType))
                specialParams.forEach { it.apply { addSpecialCode() } }
                if (hasRetrofit && network && (hasNetworkAnnotation || !targets)) {
                    code {
                        add("request.targetSource(\$T.networkParams(),", DATA_SOURCE_PARAMS)
                        indent()
                        add("\n new \$T<>(service.", RETROFIT_SOURCE_PARAMS)
                        addServiceCall(this)
                        add("));\n")
                        unindent()
                    }
                }
                if (db && (hasDBAnnotation || !targets)) {
                    code {
                        add("request.targetSource(\$T.diskParams(),", DATA_SOURCE_PARAMS)
                        indent()
                        add("\nnew \$T(\n\$T.select().from(\$T.class).where()",
                                DBFLOW_PARAMS, SQLITE, dataType)
                        indent()
                        params.forEach {
                            if (it.isQuery) {
                                add("\n.and(\$T.${it.paramName}.eq(${it.paramName}))",
                                        ClassName.get(dataType!!.packageName(), "${dataType!!.simpleName()}_Table"))
                            }
                        }
                        unindent()

                        add("));\n")
                        unindent()
                    }
                }

                if (targets) {
                    if (hasDBAnnotation) {
                        statement("request.addRequestSourceTarget(\$T.diskParams())", DATA_SOURCE_PARAMS);
                    }
                    if (hasMemoryAnnotation) {
                        statement("request.addRequestSourceTarget(\$T.memoryParams())", DATA_SOURCE_PARAMS);
                    }
                    if (hasNetworkAnnotation) {
                        statement("request.addRequestSourceTarget(\$T.networkParams())", DATA_SOURCE_PARAMS);
                    }
                }
                `return`("request.build()")
            }
        }
    }
}