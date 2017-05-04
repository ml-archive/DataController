package com.fuzz.processor

import com.fuzz.datacontroller.DataController
import com.fuzz.datacontroller.annotations.*
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.*
import com.grosner.kpoet.*
import com.squareup.javapoet.*
import javax.lang.model.element.ExecutableElement

/**
 * Description: Represents a method containing annotations that construct our request methods.
 */
class DataRequestDefinition(config: DataControllerConfigDefinition?,
                            executableElement: ExecutableElement,
                            dataControllerProcessorManager: DataControllerProcessorManager)
    : BaseDefinition(executableElement, dataControllerProcessorManager), TypeAdder {

    val memoryDefinition = MemoryDefinition(config, executableElement, manager)
    val dbDefinition = DatabaseDefinition(config, executableElement, manager)
    val networkDefinition = NetworkDefinition(config, executableElement, manager)
    val sharedPrefsDefinition = SharedPreferencesDefinition(config, executableElement, manager)

    val sourcesArray = arrayOf(networkDefinition, dbDefinition, sharedPrefsDefinition, memoryDefinition)

    var reuse = false
    var reuseMethodName = ""
    var targets = false

    var isRef = false
    var refInConstructor = false
    var refOptional = false

    var isSync = false
    var isCall = false

    // if true, return type is of SourceParams or subclass.
    var isParams = false

    var cancelDataController = false

    val params: List<DataRequestParamDefinition>

    var dataType: TypeName? = null

    // simple representation of class
    var classDataType: ClassName? = null

    val specialParams: List<DataRequestParamDefinition>
    val nonSpecialParams: List<DataRequestParamDefinition>

    val controllerName: String
    var controllerType: TypeName? = null

    init {
        isRef = executableElement.annotation<DataControllerRef>()?.let {
            controllerName = elementName
            reuseMethodName = elementName
            refInConstructor = it.inConstructor
            refOptional = it.optional
        } != null

        cancelDataController = executableElement.annotation<CancelDataController>() != null

        targets = executableElement.annotation<Targets>() != null
        if (targets && isRef) {
            manager.logError(DataRequestDefinition::class,
                    "Cannot specify both ${Targets::class} and ${DataControllerRef::class}")
        }

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

            val returnType = executableElement.returnType.typeName
            validateReturnType(returnType)
            if (returnType is ParameterizedTypeName) {
                isCall = returnType.rawType == CALL
                isParams = returnType.rawType.toTypeElement().toTypeErasedElement().isSubclass(SOURCE_PARAMS)
                isSync = returnType.rawType != DATACONTROLLER_REQUEST && !isParams && !isCall

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
        var hasSourceParams = false
        specialParams = arrayListOf<DataRequestParamDefinition>()
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

            if (it.isSourceParams) {
                if (hasSourceParams) {
                    manager.logError(DataRequestDefinition::class, "Cannot specify more than one $SOURCE_PARAMS.")
                } else {
                    hasSourceParams = true
                    specialParams.add(it)
                }
            }
        }

        // calculate non special params that are used for queries.
        nonSpecialParams = params.filterNot { specialParams.contains(it) }

        controllerType = dataType

        // once constructed, calculate configuration options here.
        sourcesArray.forEach {
            it.postProcessAnnotation()
        }
    }

    val hasSourceAnnotations: Boolean
        get() = dbDefinition.hasAnnotationDirect || memoryDefinition.hasAnnotationDirect
                || networkDefinition.hasAnnotationDirect || sharedPrefsDefinition.hasAnnotationDirect

    private fun validateReturnType(returnType: TypeName) {
        if (returnType is ParameterizedTypeName &&
                (returnType.rawType != DATACONTROLLER_REQUEST && returnType.rawType != DATACONTROLLER
                        && !returnType.rawType.toTypeElement().isSubclass(SOURCE_PARAMS))
                && returnType.rawType != CALL) {
            manager.logError(DataRequestDefinition::class, "Invalid return type found $returnType")
        }

        if (returnType is ParameterizedTypeName && returnType.rawType == DATACONTROLLER) {
            if (params.isNotEmpty()) {
                manager.logError(DataRequestDefinition::class, "Cannot specify params for DataController reference methods.")
            }
        }
    }

    fun evaluateReuse(reqDefinitions: MutableList<DataRequestDefinition>) {
        if (reuse || isRef && !hasSourceAnnotations && !refInConstructor) {
            val def = reqDefinitions.find { it.controllerName == controllerName && it != this && !it.reuse }
            if (def == null) {
                manager.logError(DataRequestDefinition::class,
                        "Could not find data controller $reuseMethodName for method $elementName." +
                                " Ensure you specify the name properly. Or you define source type " +
                                "annotations for a DataControllerRef")
            } else {
                if (def.refInConstructor) {
                    refInConstructor = true
                    if (def.networkDefinition.enabled) {
                        networkDefinition.enabled = true
                    }
                    if (def.dbDefinition.enabled) {
                        dbDefinition.enabled = true
                        dbDefinition.singleDb = def.dbDefinition.singleDb
                        dbDefinition.async = def.dbDefinition.async
                    }
                    if (def.memoryDefinition.enabled) {
                        memoryDefinition.enabled = true
                    }
                    if (def.sharedPrefsDefinition.enabled) {
                        sharedPrefsDefinition.enabled = true
                    }
                } else {
                    if (def.dataType != dataType) {
                        manager.logError(DataRequestDefinition::class,
                                "The referenced $reuseMethodName must match $dataType. found ${def.dataType}.")
                    } else {
                        networkDefinition.enabled = def.networkDefinition.enabled
                        dbDefinition.enabled = def.dbDefinition.enabled
                        dbDefinition.singleDb = def.dbDefinition.singleDb
                        dbDefinition.async = def.dbDefinition.async
                        memoryDefinition.enabled = def.memoryDefinition.enabled
                        sharedPrefsDefinition.enabled = def.sharedPrefsDefinition.enabled
                        if (sharedPrefsDefinition.preferenceDelegateType == null) {
                            sharedPrefsDefinition.preferenceDelegateType = def.sharedPrefsDefinition.preferenceDelegateType
                        }
                        sharedPrefsDefinition.preferenceDelegateName = def.sharedPrefsDefinition.preferenceDelegateName
                        refInConstructor = def.refInConstructor
                    }
                }
                controllerType = def.controllerType
            }
        }

        if (sharedPrefsDefinition.enabled && dbDefinition.enabled) {
            manager.logError(DataRequestDefinition::class, "Cannot mix and match shared preferences and db references. Choose one for storage.")
        }

        // if none assume we want all if not in constructor
        if (!hasSourceAnnotations && !refInConstructor) {
            networkDefinition.enabled = true
            dbDefinition.enabled = true
            memoryDefinition.enabled = true
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

    fun MethodSpec.Builder.addToConstructor(optional: Boolean) {
        if (!reuse && !isParams && !isCall) {
            if (!refInConstructor) {
                code {
                    add("$controllerName = \$T.controllerOf(", DATACONTROLLER)
                    val builders = arrayListOf<Pair<String, Array<Any?>>>()
                    if (memoryDefinition.enabled) {
                        memoryDefinition.apply { builders.add(addToConstructor(dataType)) }
                    }
                    if (dbDefinition.enabled) {
                        dbDefinition.apply { builders.add(addToConstructor(dataType)) }
                    }
                    if (sharedPrefsDefinition.enabled) {
                        sharedPrefsDefinition.apply { builders.add(addToConstructor(dataType)) }
                    }
                    if (networkDefinition.enabled) {
                        networkDefinition.apply { builders.add(addToConstructor(dataType)) }
                    }

                    builders.forEachIndexed { index, (statement, args) ->
                        if (index > 0) add(",")
                        add(CodeBlock.of(statement, *args))
                    }

                    add(");\n")
                }
            } else {
                if (!optional) {
                    addParameter(param(ParameterizedTypeName.get(DataController::class.className, dataType), controllerName).build())
                    statement("this.$controllerName = $controllerName")
                } else {
                    statement("this.$controllerName = null")
                }
            }
        }
    }

    fun TypeSpec.Builder.addToRetrofitInterface() {
        if (networkDefinition.hasRetrofit && (!reuse && networkDefinition.enabled || networkDefinition.hasAnnotationDirect)) {
            public(ParameterizedTypeName.get(CALL, dataType), elementName) {
                applyAnnotations()
                modifiers(abstract)
                params.filter { it.isQuery }.forEach { it.apply { this@public.addRetrofitParamCode() } }
                this
            }
        }
    }

    override fun TypeSpec.Builder.addToType() {
        if (!reuse && !isParams && !isCall) {
            `private final field`(ParameterizedTypeName.get(DATACONTROLLER, dataType), controllerName)
        }

        sharedPrefsDefinition.apply { addToClass() }

        var methodReturnType = executableElement.returnType.typeName
        if (reuse && methodReturnType is ParameterizedTypeName && methodReturnType.typeArguments[0] != controllerType) {
            // remove generics on overridden method when type param of the source params do not match the datacontroller type.
            methodReturnType = methodReturnType.rawType
        }

        public(methodReturnType, elementName) {
            params.forEach { it.apply { addParamCode() } }
            applyAnnotations()
            annotation(Override::class)
            if (isRef) {
                `return`(reuseMethodName)
            } else if (isSync) {
                var sourceParams = "memoryParams"
                if (dbDefinition.enabled && !memoryDefinition.enabled) {
                    sourceParams = "diskParams"
                }
                statement("\$T storage = $controllerName\n\t.getDataSource(\$T.$sourceParams())\n\t.getStoredData()",
                        controllerType, DATA_SOURCE_PARAMS)

                // memory is always first, followed by db
                if (dbDefinition.enabled && memoryDefinition.enabled) {
                    `if`("storage == null") {
                        statement("storage = $controllerName\n\t.getDataSource(\$T.diskParams())\n\t.getStoredData()",
                                DATA_SOURCE_PARAMS)
                    }.end()
                }
                `return`("storage")
            } else if (isParams) {
                val dataRequestParamDefinition = ParamsDefinition(executableElement, manager)
                var def: BaseSourceTypeDefinition<*> = networkDefinition
                if (dataRequestParamDefinition.useDBParams) {
                    def = dbDefinition
                }
                this@DataRequestDefinition.apply { addToParamsMethod("params", def) }
                `return`("params")
            } else if (isCall) {
                networkDefinition.apply {
                    code {
                        add("return service.")
                        addServiceCall(params, controllerName, reuse)
                        add(";\n")
                    }
                }

                this
            } else {
                if (cancelDataController) {
                    statement("$controllerName.cancel()")
                }

                statement("\$T request = $controllerName.request()",
                        ParameterizedTypeName.get(DATACONTROLLER_REQUEST_BUILDER, controllerType))
                specialParams.forEach { it.apply { addSpecialCode() } }
                sourcesArray.forEach { addToParamsToMethod(it) }

                // apply default param data here if we do not specify source annotations.
                if (!hasSourceAnnotations) {

                    val param = specialParams.filter { it.isParamData }.getOrNull(0)
                    if (param != null) {
                        if (!param.isSourceParamsData) {
                            statement("\$T __params = new \$T()", SOURCE_PARAMS, SOURCE_PARAMS)
                            statement("__params.data = ${param.paramName}")
                            statement("request.sourceParams(__params)")
                        } else {
                            statement("request.sourceParams(${param.paramName})")
                        }
                    }
                }

                if (targets) {
                    sourcesArray.forEach { it.apply { addIfTargets() } }
                }
                `return`("request.build()")
            }
        }
    }

    fun MethodSpec.Builder.addToParamsToMethod(baseSourceTypeDefinition: BaseSourceTypeDefinition<*>) {
        baseSourceTypeDefinition.apply {
            addToType(params, dataType, classDataType!!, controllerName,
                    reuse, targets, specialParams, refInConstructor)
        }
    }

    fun MethodSpec.Builder.addToParamsMethod(paramsName: String,
                                             baseSourceTypeDefinition: BaseSourceTypeDefinition<*>) {
        baseSourceTypeDefinition.apply {
            addParams(paramsName, params, dataType, classDataType!!, controllerName,
                    reuse)
        }
    }
}