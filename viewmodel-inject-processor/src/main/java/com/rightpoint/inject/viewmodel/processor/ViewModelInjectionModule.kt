/*
 *  Copyright 2019 RightPoint
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.rightpoint.inject.viewmodel.processor

import com.rightpoint.inject.viewmodel.ViewModelProviderFactory
import com.rightpoint.inject.viewmodel.annotations.ViewModelKey
import com.rightpoint.inject.viewmodel.processor.internal.applyEach
import com.rightpoint.inject.viewmodel.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

private val MODULE = ClassName.get("dagger", "Module")
private val BINDS = ClassName.get("dagger", "Binds")
private val INTO_MAP = ClassName.get("dagger.multibindings", "IntoMap")
private val VIEW_MODEL = ClassName.get("androidx.lifecycle", "ViewModel")
private val VIEW_MODEL_PROVIDER = ClassName.get("androidx.lifecycle", "ViewModelProvider")
private val VIEW_MODEL_KEY = ClassName.get(ViewModelKey::class.java)
private val FACTORY_IMPL = ClassName.get(ViewModelProviderFactory::class.java)

data class ViewModelInjectionModule(
    val moduleName: ClassName,
    val public: Boolean,
    val injectedNames: List<ClassName>,
    val generatedAnnotation: AnnotationSpec? = null
) {
    val generatedType = moduleName.viewModelInjectModuleName()

    fun brewJava(): TypeSpec {
        return TypeSpec.classBuilder(generatedType)
            .addAnnotation(MODULE)
            .apply {
                if (generatedAnnotation != null) {
                    addAnnotation(generatedAnnotation)
                }
            }
            .addModifiers(Modifier.ABSTRACT)
            .apply {
                if (public) {
                    addModifiers(Modifier.PUBLIC)
                }
            }
            .addMethod(
                MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build()
            )
            .applyEach(injectedNames) { injectedName ->
                addMethod(MethodSpec.methodBuilder(injectedName.bindMethodName())
                    .addAnnotation(BINDS)
                    .addAnnotation(INTO_MAP)
                    .addAnnotation(AnnotationSpec.builder(VIEW_MODEL_KEY)
                        .addMember("value", CodeBlock.builder()
                            .add("\$T.class", injectedName)
                            .build())
                        .build())
                    .addModifiers(Modifier.ABSTRACT)
                    .returns(VIEW_MODEL)
                    .addParameter(injectedName, "viewModel")
                    .build())
            }
            .addMethod(
                MethodSpec.methodBuilder("bindFactory")
                    .addAnnotation(BINDS)
                    .addModifiers(Modifier.ABSTRACT)
                    .returns(VIEW_MODEL_PROVIDER.nestedClass("Factory"))
                    .addParameter(FACTORY_IMPL, "factory")
                    .build()
            )
            .build()
    }

    private fun ClassName.bindMethodName(): String {
        return "bind_" + reflectionName().replace('.', '_')
    }
}

fun ClassName.viewModelInjectModuleName(): ClassName {
    return peerClassWithReflectionNesting("ViewModelInjectionModule")
}