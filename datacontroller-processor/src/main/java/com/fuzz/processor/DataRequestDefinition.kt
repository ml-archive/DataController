package com.fuzz.processor

import com.fuzz.datacontroller.annotations.*
import com.fuzz.processor.utils.annotation
import com.fuzz.processor.utils.dataControllerAnnotation
import com.fuzz.processor.utils.toClassName
import com.fuzz.processor.utils.toTypeElement
import com.grosner.kpoet.*
import com.squareup.javapoet.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description: Represents a method containing annotations that construct our request methods.
 */
class DataRequestDefinition(executableElement: ExecutableElement, dataControllerProcessorManager: DataControllerProcessorManager)
    : BaseDefinition(executableElement, dataControllerProcessorManager), TypeAdder {

    var memory = false

    var db = false
    var singleDb = true
    var async = false

    var sharedPrefs = false
    var hasSharedPrefsAnnotation = false
    var preferenceDelegateType: ClassName? = null

    var network = false
    var networkDefinition = NetworkDefinition(executableElement, manager)

    var reuse = false
    var reuseMethodName = ""
    var targets = false

    var isRef = false

    var hasMemoryAnnotation = false
    var hasDBAnnotation = false

    var isSync = false

    val params: List<DataRequestParamDefinition>

    var dataType: ClassName? = null

    var specialParams = arrayListOf<DataRequestParamDefinition>()

    val controllerName: String

    var preferenceDelegateName = "preferenceDelegate_$elementName"

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

        db = executableElement.annotation<DB>()?.let { hasDBAnnotation = true } != null
        memory = executableElement.annotation<Memory>()?.let { hasMemoryAnnotation = true } != null

        sharedPrefs = executableElement.annotation<SharedPreferences>()?.let {
            hasSharedPrefsAnnotation = true
            try {
                it.preferenceDelegate
            } catch (mte: MirroredTypeException) {
                preferenceDelegateType = mte.typeMirror.toTypeElement().toClassName()
            }
        } != null

        executableElement.annotation<Reuse>()?.let {
            reuse = true
            reuseMethodName = it.value
        }

        if (!network) {
            network = networkDefinition.network
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
        get() = hasDBAnnotation || hasMemoryAnnotation || networkDefinition.hasNetworkAnnotation || hasSharedPrefsAnnotation

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
                    sharedPrefs = def.sharedPrefs
                    if (preferenceDelegateType == null) {
                        preferenceDelegateType = def.preferenceDelegateType
                    }
                    preferenceDelegateName = def.preferenceDelegateName
                }
            }
        }

        if (sharedPrefs && db) {
            manager.logError(DataRequestDefinition::class, "Cannot mix and match shared preferences and db references. Choose one for storage.")
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
                val builders = arrayListOf<Pair<String, Array<Any?>>>()
                if (memory) {
                    builders.add("\n\$T.<\$T>builderInstance().build()" to arrayOf<Any?>(MEMORY_SOURCE, dataType))
                }
                if (db) {
                    builders.add("\n\$T.<\$T>builderInstance(\$T.class, ${async.L}).build()" to
                            arrayOf<Any?>(if (singleDb) DBFLOW_SINGLE_SOURCE else DBFLOW_LIST_SOURCE, dataType, dataType))
                }
                if (sharedPrefs) {
                    builders.add("\n\$T.<\$T>builderInstance(sharedPreferences, $preferenceDelegateName).build()" to
                            arrayOf<Any?>(SHARED_PREFERENCES_SOURCE, dataType))
                }
                if (network) {
                    networkDefinition.apply { builders.add(addToConstructor(dataType)) }
                }

                builders.forEachIndexed { index, (statement, args) ->
                    if (index > 0) add(",")
                    add(CodeBlock.of(statement, *args))
                }

                add(");\n")
            }
        }
    }

    fun TypeSpec.Builder.addToRetrofitInterface() {
        if (networkDefinition.hasRetrofit && (!reuse && network || networkDefinition.hasNetworkAnnotation)) {
            public(ParameterizedTypeName.get(CALL, dataType), elementName) {
                applyAnnotations()
                modifiers(abstract)
                params.filter { it.isQuery }.forEach { it.apply { this@public.addRetrofitParamCode() } }
                this
            }
        }
    }

    fun addServiceCall(codeBlock: CodeBlock.Builder) {
        codeBlock.add("${if (reuse && !networkDefinition.hasNetworkAnnotation) controllerName else elementName}(")
        codeBlock.add(params.filter { it.isQuery }.joinToString { it.paramName })
        codeBlock.add(")")
    }

    override fun TypeSpec.Builder.addToType() {
        if (!reuse) {
            `private final field`(ParameterizedTypeName.get(DATACONTROLLER, dataType), controllerName)
        }

        if (sharedPrefs) {
            `private final field`(preferenceDelegateType!!, preferenceDelegateName)
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

                networkDefinition.apply { addToType(params, controllerName, reuse, targets) }

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
                    if (hasDBAnnotation || hasSharedPrefsAnnotation) {
                        statement("request.addRequestSourceTarget(\$T.diskParams())", DATA_SOURCE_PARAMS);
                    }
                    if (hasMemoryAnnotation) {
                        statement("request.addRequestSourceTarget(\$T.memoryParams())", DATA_SOURCE_PARAMS);
                    }
                    networkDefinition.apply { addIfTargets() }
                }
                `return`("request.build()")
            }
        }
    }
}