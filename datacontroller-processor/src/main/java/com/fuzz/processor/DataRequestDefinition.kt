package com.fuzz.processor

import com.fuzz.datacontroller.annotations.*
import com.fuzz.datacontroller.source.DataSource
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

    var sharedPrefs = false
    var hasSharedPrefsAnnotation = false
    var preferenceDelegateType: ClassName? = null

    var dbDefinition = DatabaseDefinition(executableElement, manager)
    var networkDefinition = NetworkDefinition(executableElement, manager)

    var reuse = false
    var reuseMethodName = ""
    var targets = false

    var isRef = false

    var hasMemoryAnnotation = false

    var isSync = false

    val params: List<DataRequestParamDefinition>

    var dataType: TypeName? = null

    // simple representation of class
    var classDataType: ClassName? = null

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
                networkDefinition.network = true
                dbDefinition.db = true
                memory = true
            }

            val returnType = executableElement.returnType.typeName
            validateReturnType(returnType)
            if (returnType is ParameterizedTypeName) {
                isSync = returnType.rawType != DATACONTROLLER_REQUEST

                dbDefinition.singleDb = !returnType.rawType.toTypeElement().implementsClass(List::class)

                val typeParameters = returnType.typeArguments
                dataType = typeParameters[0]

                dataType?.let { dataType ->
                    if (dataType is ParameterizedTypeName) {
                        val simpleType = dataType.typeArguments[0]
                        if (simpleType is ClassName) {
                            classDataType = simpleType
                        }
                    }
                }
                if (classDataType == null && dataType is ClassName) {
                    classDataType = dataType as ClassName
                }
                if (classDataType == null) {
                    manager.logError(DataRequestDefinition::class, "Invalid return type found $dataType")
                }
            } else {
                isSync = true
                dataType = returnType
                classDataType = dataType.toTypeElement().toClassName()

                if (!reuse) {
                    manager.logError(DataRequestDefinition::class, "Synchronous requests must reuse another $DATACONTROLLER")
                }
            }
        }

        var hasErrorFilter = false
        val paramDataMap = mutableMapOf<DataSource.SourceType, Boolean>()
        params.forEach {
            if (it.isCallback) {
                specialParams.add(it)
            }

            if (it.isErrorFilter) {
                if (hasErrorFilter) {
                    manager.logError(DataRequestDefinition::class, "Cannot specify more than one $ERROR_FILTER.")
                } else {
                    hasErrorFilter = true
                    specialParams.add(it)
                }
            }

            if (it.isParamData) {
                if (paramDataMap[it.targetedSourceForParam] != null) {
                    manager.logError(DataRequestDefinition::class,
                            "Cannot specify more than one ${ParamData::class} for ${it.targetedSourceForParam}")
                } else {
                    paramDataMap[it.targetedSourceForParam] = true
                    specialParams.add(it)
                }
            }
        }
    }

    val hasSourceAnnotations: Boolean
        get() = dbDefinition.hasDBAnnotation || hasMemoryAnnotation
                || networkDefinition.hasNetworkAnnotation || hasSharedPrefsAnnotation

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
                    networkDefinition.network = def.networkDefinition.network
                    dbDefinition.db = def.dbDefinition.db
                    dbDefinition.singleDb = def.dbDefinition.singleDb
                    dbDefinition.async = def.dbDefinition.async
                    memory = def.memory
                    sharedPrefs = def.sharedPrefs
                    if (preferenceDelegateType == null) {
                        preferenceDelegateType = def.preferenceDelegateType
                    }
                    preferenceDelegateName = def.preferenceDelegateName
                }
            }
        }

        if (sharedPrefs && dbDefinition.db) {
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
                if (dbDefinition.db) {
                    dbDefinition.apply { builders.add(addToConstructor(dataType)) }
                }

                if (sharedPrefs) {
                    builders.add("\n\$T.<\$T>builderInstance(sharedPreferences, $preferenceDelegateName).build()" to
                            arrayOf<Any?>(SHARED_PREFERENCES_SOURCE, dataType))
                }
                if (networkDefinition.network) {
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
        if (networkDefinition.hasRetrofit && (!reuse && networkDefinition.network || networkDefinition.hasNetworkAnnotation)) {
            public(ParameterizedTypeName.get(CALL, dataType), elementName) {
                applyAnnotations()
                modifiers(abstract)
                params.filter { it.isQuery }.forEach { it.apply { this@public.addRetrofitParamCode() } }
                this
            }
        }
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
                if (dbDefinition.db && !memory) {
                    sourceParams = "diskParams"
                }
                statement("\$T storage = $controllerName\n\t.getDataSource(\$T.$sourceParams())\n\t.getStoredData()",
                        dataType, DATA_SOURCE_PARAMS)

                // memory is always first, followed by db
                if (dbDefinition.db && memory) {
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

                networkDefinition.apply { addToType(params, dataType, controllerName, reuse, targets, specialParams) }

                dbDefinition.apply { addToType(params, dataType, classDataType!!, controllerName, reuse, targets, specialParams) }

                if (targets) {
                    dbDefinition.apply { addIfTargets() }

                    if (hasSharedPrefsAnnotation) {
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