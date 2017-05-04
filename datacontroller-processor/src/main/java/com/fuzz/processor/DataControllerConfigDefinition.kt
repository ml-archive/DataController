package com.fuzz.processor

import com.fuzz.datacontroller.annotations.DataControllerConfig
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.annotation
import com.fuzz.processor.utils.className
import com.fuzz.processor.utils.toClassName
import com.fuzz.processor.utils.toTypeElement
import com.squareup.javapoet.ClassName
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description: Defines the top-level class that configures all other annotations by default.
 */
class DataControllerConfigDefinition(typeElement: TypeElement, processorManager: DataControllerProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    var responseHandler = ClassName.OBJECT!!
    var errorConverter = ClassName.OBJECT!!
    var refreshStrategyClassName = DataSource.DefaultRefreshStrategy::class.className

    init {
        val annotation = typeElement.annotation<DataControllerConfig>()!!
        try {
            annotation.responseHandler
        } catch (mte: MirroredTypeException) {
            this.responseHandler = mte.typeMirror.toTypeElement().toClassName()
        }
        try {
            annotation.errorConverter
        } catch (mte: MirroredTypeException) {
            this.errorConverter = mte.typeMirror.toTypeElement().toClassName()
        }
        try {
            annotation.refreshStrategy
        } catch (mte: MirroredTypeException) {
            refreshStrategyClassName = mte.typeMirror.toClassName()
        }
    }
}