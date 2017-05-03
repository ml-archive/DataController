package com.fuzz.processor

import com.fuzz.datacontroller.annotations.SharedPreferences
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.processor.utils.toClassName
import com.fuzz.processor.utils.toTypeElement
import com.grosner.kpoet.`private final field`
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException

class SharedPreferencesDefinition(element: Element, manager: DataControllerProcessorManager)

    : BaseSourceTypeDefinition<SharedPreferences>(SharedPreferences::class, element, manager) {

    var preferenceDelegateType: ClassName? = null

    var preferenceDelegateName = "preferenceDelegate_$elementName"

    override val requestSourceType = DataSource.SourceType.DISK

    override fun SharedPreferences.processAnnotation() {
        try {
            preferenceDelegate
        } catch (mte: MirroredTypeException) {
            preferenceDelegateType = mte.typeMirror.toTypeElement().toClassName()
        }
    }

    override fun TypeSpec.Builder.addToClass() {
        if (enabled) {
            `private final field`(preferenceDelegateType!!, preferenceDelegateName)
        }
    }

    override val requestSourceTarget: String
        get() = "disk"

    override fun MethodSpec.Builder.addToConstructor(dataType: TypeName?): Pair<String, Array<Any?>> {
        val args = mutableListOf<Any?>(SHARED_PREFERENCES_SOURCE, dataType)
        val returnString = buildString {
            append("\n\$T.<\$T>builderInstance(sharedPreferences, $preferenceDelegateName")
            appendRefreshStrategy(this, args)
            append(").build()")
        }
        return returnString to args.toTypedArray()
    }
}