package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DB
import com.fuzz.datacontroller.source.DataSource.SourceType.DISK
import com.fuzz.processor.utils.annotation
import com.grosner.kpoet.L
import com.grosner.kpoet.code
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element

/**
 * Description:
 */
class DatabaseDefinition(element: Element, processorManager: DataControllerProcessorManager)
    : BaseDefinition(element, processorManager) {

    var db = false
    var singleDb = true
    var async = false

    var hasDBAnnotation = false

    init {
        db = executableElement.annotation<DB>()?.let { hasDBAnnotation = true } != null

    }

    fun MethodSpec.Builder.addToConstructor(dataType: TypeName?): Pair<String, Array<Any?>> {
        return ("\n\$T.<\$T>builderInstance(\$T.class, ${async.L}).build()" to
                arrayOf<Any?>(if (singleDb) DBFLOW_SINGLE_SOURCE else DBFLOW_LIST_SOURCE, dataType, dataType))
    }

    fun MethodSpec.Builder.addToType(params: List<DataRequestParamDefinition>,
                                     dataType: TypeName?,
                                     classDataType: ClassName,
                                     controllerName: String, reuse: Boolean,
                                     targets: Boolean, specialParams: List<DataRequestParamDefinition>) {
        if (db && (hasDBAnnotation || !targets)) {
            code {
                val param = specialParams.filter { it.isParamData && it.targetedSourceForParam == DISK }.getOrNull(0)
                val paramsName = "params$DISK"

                add("\$T $paramsName = new \$T(\n\$T.select().from(\$T.class).where()",
                        ParameterizedTypeName.get(DBFLOW_PARAMS, dataType), DBFLOW_PARAMS, SQLITE, dataType)
                indent()
                params.forEach {
                    if (it.isQuery) {
                        add("\n.and(\$T.${it.paramName}.eq(${it.paramName}))",
                                ClassName.get(classDataType.packageName(), "${classDataType.simpleName()}_Table"))
                    }
                }
                add(");\n")
                unindent()
                if (param != null) {
                    statement("$paramsName.data = ${param.elementName}")
                }
                statement("request.targetSource(\$T.diskParams(), $paramsName)", DATA_SOURCE_PARAMS)
            }
        }
    }

    fun MethodSpec.Builder.addIfTargets() {
        if (hasDBAnnotation) {
            statement("request.addRequestSourceTarget(\$T.diskParams())", DATA_SOURCE_PARAMS);
        }
    }
}