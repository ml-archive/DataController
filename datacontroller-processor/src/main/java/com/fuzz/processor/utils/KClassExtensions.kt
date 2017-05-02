package com.fuzz.processor.utils

import com.squareup.javapoet.ClassName
import kotlin.reflect.KClass

val <T : Any> KClass<T>.className
    get() = ClassName.get(this.java)!!