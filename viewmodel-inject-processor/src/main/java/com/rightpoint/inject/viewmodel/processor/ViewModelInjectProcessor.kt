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

import com.google.auto.service.AutoService
import com.rightpoint.inject.viewmodel.annotations.ViewModelModule
import com.rightpoint.inject.viewmodel.processor.internal.MirrorValue
import com.rightpoint.inject.viewmodel.processor.internal.applyEach
import com.rightpoint.inject.viewmodel.processor.internal.cast
import com.rightpoint.inject.viewmodel.processor.internal.castEach
import com.rightpoint.inject.viewmodel.processor.internal.createGeneratedAnnotation
import com.rightpoint.inject.viewmodel.processor.internal.findElementsAnnotatedWith
import com.rightpoint.inject.viewmodel.processor.internal.getAnnotation
import com.rightpoint.inject.viewmodel.processor.internal.getValue
import com.rightpoint.inject.viewmodel.processor.internal.hasAnnotation
import com.rightpoint.inject.viewmodel.processor.internal.toClassName
import com.rightpoint.inject.viewmodel.processor.internal.toTypeName
import com.squareup.javapoet.JavaFile
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

@AutoService(Processor::class)
class ViewModelInjectProcessor : AbstractProcessor() {
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var viewModelType: TypeMirror
    private var userModule: String? = null

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes() = setOf(Inject::class.java.canonicalName)

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        messager = env.messager
        filer = env.filer
        types = env.typeUtils
        elements = env.elementUtils
        viewModelType = elements.getTypeElement("androidx.lifecycle.ViewModel").asType()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val injectElements = roundEnv.findViewModelInjectCandidateElements()
            .mapNotNull { it.toViewModelInjectElementsOrNull() }

        val moduleElements = roundEnv.findViewModelModuleTypeElement()
            ?.toViewModelModuleElementsOrNull(injectElements)

        if (moduleElements != null) {
            handleModuleProcessing(moduleElements, userModule)
        }

        if (roundEnv.processingOver()) {
            startPostProcessing(userModule)
        }

        return false
    }

    private fun handleModuleProcessing(
        moduleElements: ViewModelModuleElements,
        userModuleFqcn: String?
    ) {
        val moduleType = moduleElements.moduleType

        if (userModuleFqcn != null) {
            val userModuleType = elements.getTypeElement(userModuleFqcn)
            error("Multiple @ViewModelModule-annotated modules found.", userModuleType)
            error("Multiple @ViewModelModule-annotated modules found.", moduleType)
            userModule = null
        } else {
            userModule = moduleType.qualifiedName.toString()
            val injectionModule = moduleElements.toViewModelInjectionModule()
            writeViewModelModule(moduleElements, injectionModule)
        }
    }

    private fun startPostProcessing(userModuleFqcn: String?) {
        if (userModuleFqcn != null) {
            // In the processing round in which we handle the @ViewModelModule the @Module
            // annotation's includes contain an <error> type because we haven't generated the
            // ViewModel module yet. As a result, we need to re-lookup the element so that its
            // referenced types are available.
            val userModule = elements.getTypeElement(userModuleFqcn)

            // Previous validation guarantees this annotation is present.
            val moduleAnnotation = userModule.getAnnotation("dagger.Module")!!
            // Dagger guarantees this property is present and is an array of types or errors.
            val includes = moduleAnnotation.getValue("includes", elements)!!
                .cast<MirrorValue.Array>()
                .filterIsInstance<MirrorValue.Type>()

            val generatedModuleName = userModule.toClassName().viewModelInjectModuleName()
            val referencesGeneratedModule = includes
                .map { it.toTypeName() }
                .any { it == generatedModuleName }
            if (!referencesGeneratedModule) {
                error(
                    message = "@ViewModelModule's @Module must include ${generatedModuleName.simpleName()}",
                    element = userModule
                )
            }
        }
    }

    private fun RoundEnvironment.findViewModelInjectCandidateElements(): List<TypeElement> {
        return findElementsAnnotatedWith<Inject>()
            .map { it.enclosingElement as TypeElement }
            .filter { types.isSubtype(it.asType(), viewModelType) }
    }

    private fun TypeElement.toViewModelInjectElementsOrNull(): ViewModelInjectElements? {
        var valid = true

        if (PRIVATE in modifiers) {
            error("@Inject-using ViewModels must not be private", this)
            valid = false
        }

        if (enclosingElement.kind == CLASS && STATIC !in modifiers) {
            error("Nested @Inject-using ViewModels must be static", this)
            valid = false
        }

        if (!types.isSubtype(asType(), viewModelType)) {
            error("@Inject-using types must be subtypes of ViewModel", this)
            valid = false
        }

        val constructors = enclosedElements
            .filter { it.kind == CONSTRUCTOR }
            .filter { it.hasAnnotation<Inject>() }
            .castEach<ExecutableElement>()

        if (constructors.size > 1) {
            error("Multiple @Inject-annotated ViewModel constructors found.", this)
            valid = false
        }

        if (!valid) return null

        val constructor = constructors.single()

        if (PRIVATE in constructor.modifiers) {
            error("ViewModel with @Inject constructor must not be private.", constructor)
            return null
        }

        return ViewModelInjectElements(this, constructor)
    }

    private fun RoundEnvironment.findViewModelModuleTypeElement(): TypeElement? {
        val modules = findElementsAnnotatedWith<ViewModelModule>().castEach<TypeElement>()
        if (modules.size > 1) {
            modules.forEach {
                error("Multiple @ViewModelModule-annotated modules found.", it)
            }
            return null
        }
        return modules.singleOrNull()
    }

    private fun TypeElement.toViewModelModuleElementsOrNull(
        injectElements: List<ViewModelInjectElements>
    ): ViewModelModuleElements? {
        if (!hasAnnotation("dagger.Module")) {
            error(
                message = "@ViewModelModule must also be annotated as a Dagger @Module",
                element = this
            )
            return null
        }

        val inflationTargetTypes = injectElements.map { it.targetType }
        return ViewModelModuleElements(this, inflationTargetTypes)
    }

    private fun ViewModelModuleElements.toViewModelInjectionModule(): ViewModelInjectionModule {
        val moduleName = moduleType.toClassName()
        val inflationNames = inflationTypes.map { it.toClassName() }
        val public = Modifier.PUBLIC in moduleType.modifiers
        val generatedAnnotation = createGeneratedAnnotation(elements)
        return ViewModelInjectionModule(moduleName, public, inflationNames, generatedAnnotation)
    }

    private fun writeViewModelModule(
        elements: ViewModelModuleElements,
        module: ViewModelInjectionModule
    ) {
        val generatedTypeSpec = module.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.moduleType)
            .applyEach(elements.inflationTypes) {
                addOriginatingElement(it)
            }
            .build()
        JavaFile.builder(module.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @ViewModelModule. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun error(message: String, element: Element? = null) {
        messager.printMessage(ERROR, message, element)
    }

    private data class ViewModelInjectElements(
        val targetType: TypeElement,
        val targetConstructor: ExecutableElement
    )

    private data class ViewModelModuleElements(
        val moduleType: TypeElement,
        val inflationTypes: List<TypeElement>
    )
}
