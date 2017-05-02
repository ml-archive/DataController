package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DataControllerRef
import com.fuzz.datacontroller.annotations.ParamData
import com.fuzz.datacontroller.annotations.Reuse
import com.fuzz.datacontroller.annotations.Targets
import com.fuzz.datacontroller.source.DataSource
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

    val memoryDefinition = MemoryDefinition(executableElement, manager)
    val dbDefinition = DatabaseDefinition(executableElement, manager)
    val networkDefinition = NetworkDefinition(executableElement, manager)
    val sharedPrefsDefinition = SharedPreferencesDefinition(executableElement, manager)

    var reuse = false
    var reuseMethodName = ""
    var targets = false

    var isRef = false

    var isSync = false

    val params: List<DataRequestParamDefinition>

    var dataType: TypeName? = null

    // simple representation of class
    var classDataType: ClassName? = null

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
                networkDefinition.enabled = true
                dbDefinition.enabled = true
                memoryDefinition.enabled = true
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
        var hasSourceParams = false
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
    }

    val hasSourceAnnotations: Boolean
        get() = dbDefinition.hasAnnotationDirect || memoryDefinition.hasAnnotationDirect
                || networkDefinition.hasAnnotationDirect || sharedPrefsDefinition.hasAnnotationDirect

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
                }
            }
        }

        if (sharedPrefsDefinition.enabled && dbDefinition.enabled) {
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
        if (!reuse) {
            `private final field`(ParameterizedTypeName.get(DATACONTROLLER, dataType), controllerName)
        }

        sharedPrefsDefinition.apply { addToClass() }

        public(executableElement.returnType.typeName, elementName) {
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
                        dataType, DATA_SOURCE_PARAMS)

                // memory is always first, followed by db
                if (dbDefinition.enabled && memoryDefinition.enabled) {
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

                networkDefinition.apply { addToType(params, dataType, classDataType!!, controllerName, reuse, targets, specialParams) }

                dbDefinition.apply { addToType(params, dataType, classDataType!!, controllerName, reuse, targets, specialParams) }

                if (targets) {
                    dbDefinition.apply { addIfTargets() }
                    sharedPrefsDefinition.apply { addIfTargets() }
                    memoryDefinition.apply { addIfTargets() }
                    networkDefinition.apply { addIfTargets() }
                }
                `return`("request.build()")
            }
        }
    }
}