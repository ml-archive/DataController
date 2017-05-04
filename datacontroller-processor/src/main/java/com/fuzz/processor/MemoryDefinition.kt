package com.fuzz.processor

import com.fuzz.datacontroller.annotations.Memory
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.toClassName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException

class MemoryDefinition(config: DataControllerConfigDefinition?, element: Element, processorManager: DataControllerProcessorManager)
    : BaseSourceTypeDefinition<Memory>(config, Memory::class, element, processorManager) {

    override val requestSourceTarget = "memory"

    override val requestSourceType = DataSource.SourceType.MEMORY

    override fun Memory.processAnnotation() {
        try {
            refreshStrategy
        } catch (mte: MirroredTypeException) {
            refreshStrategyClassName = mte.typeMirror.toClassName()
        }
    }

    override fun MethodSpec.Builder.addToConstructor(dataType: TypeName?): Pair<String, Array<Any?>> {
        val list = mutableListOf<Any?>(MEMORY_SOURCE, dataType)
        val returnString = buildString {
            append("\n\$T.<\$T>builderInstance(")
            appendRefreshStrategy(this, list)
            append(").build()")
        }
        return returnString to list.toTypedArray()
    }

    override fun MethodSpec.Builder.addToType(params: List<DataRequestParamDefinition>,
                                              dataType: TypeName?, classDataType: ClassName,
                                              controllerName: String, reuse: Boolean,
                                              targets: Boolean,
                                              specialParams: List<DataRequestParamDefinition>, refInConstructor: Boolean) = Unit
}