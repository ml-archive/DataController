package com.grosner.processor

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeSpec

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
interface TypeAdder {

    fun TypeSpec.Builder.addToType()
}

interface CodeAdder {

    fun CodeBlock.Builder.addCode(): CodeBlock.Builder
}