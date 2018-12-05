package com.rightpoint.inject.viewmodel.processor

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Ignore
import org.junit.Test

class ViewModelInjectionProcessorTest {
    @Ignore
    @Test fun simple() {
        val inputViewModel = JavaFileObjects.forSourceString("test.TestViewModel", """
            package test;

            import androidx.lifecycle.ViewModel;
            import javax.inject.Inject;

            class TestViewModel extends ViewModel {

                @Inject
                TestViewModel() {}
            }
        """)

        val inputModule = JavaFileObjects.forSourceString("test.TestModule", """
            package test;

            import com.rightpoint.inject.viewmodel.annotations.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes = ViewModelInjectionModule.class)
            abstract class AppModule {}
        """)

        val expectedModule = JavaFileObjects.forSourceString("test.ViewModelInjectionModule", """
            package test;

            import androidx.lifecycle.ViewModel;
            import androidx.lifecycle.ViewModelProvider;
            import com.rightpoint.inject.viewmodel.ViewModelProviderFactory;
            import com.rightpoint.inject.viewmodel.annotations.ViewModelKey;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.IntoMap;

            @Module
            public abstract class ViewModelInjectionModule {
              private ViewModelInjectionModule() {
              }

              @Binds
              @IntoMap
              @ViewModelKey(TestViewModel.class)
              abstract ViewModel bind_test_TestViewModel(TestViewModel viewModel);

              @Binds
              abstract ViewModelProvider.Factory bindFactory(ViewModelProviderFactory factory);
            }
        """)

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedModule)
    }
}