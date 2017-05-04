package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DB
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.toClassName
import com.grosner.kpoet.L
import com.grosner.kpoet.code
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class DatabaseDefinition(config: DataControllerConfigDefinition?,
                         element: Element, processorManager: DataControllerProcessorManager)
    : BaseSourceTypeDefinition<DB>(config, DB::class, element, processorManager) {

    var singleDb = true
    var async = false

    override fun DB.processAnnotation() {
        try {
            refreshStrategy
        } catch (mte: MirroredTypeException) {
            refreshStrategyClassName = mte.typeMirror.toClassName()
        }
    }

    override val requestSourceTarget = "disk"

    override val requestSourceType = DataSource.SourceType.DISK

    override fun MethodSpec.Builder.addToConstructor(dataType: TypeName?): Pair<String, Array<Any?>> {
        val list = mutableListOf<Any?>(if (singleDb) DBFLOW_SINGLE_SOURCE else DBFLOW_LIST_SOURCE, dataType, dataType)
        val returnString = buildString {
            append("\n\$T.<\$T>builderInstance(\$T.class, ${async.L}")
            appendRefreshStrategy(this, list)
            append(").build()")
        }
        return (returnString to list.toTypedArray())
    }

    override fun MethodSpec.Builder.addParams(paramsName: String,
                                              params: List<DataRequestParamDefinition>,
                                              dataType: TypeName?, classDataType: ClassName,
                                              controllerName: String, reuse: Boolean) {
        code {
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
        }
    }

    override fun MethodSpec.Builder.addToType(params: List<DataRequestParamDefinition>,
                                              dataType: TypeName?,
                                              classDataType: ClassName,
                                              controllerName: String, reuse: Boolean,
                                              targets: Boolean, specialParams: List<DataRequestParamDefinition>, refInConstructor: Boolean) {
        if (enabled && (hasAnnotationDirect || !targets || refInConstructor)) {
            addRequestCode(params, dataType, classDataType, controllerName, reuse, specialParams)
        }
    }

}