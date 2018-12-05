package com.rightpoint.inject.viewmodel

import androidx.lifecycle.ViewModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Provider

@RunWith(JUnit4::class)
class ViewModelProviderFactoryTest {
    @Test fun validArgument() {
        val cls: Class<TestViewModel> = TestViewModel().javaClass
        val provider: Provider<ViewModel> = Provider { TestViewModel() }
        val creators = mutableMapOf<Class<out ViewModel>, Provider<ViewModel>>(cls to provider)
        val factory = ViewModelProviderFactory(creators)
        val actual = factory.create(cls)
        assertThat(actual).isInstanceOf(cls)
    }

    @Test fun childArgument() {
        val parentClass: Class<TestViewModel> = TestViewModel().javaClass
        val childClass: Class<ChildTestViewModel> = ChildTestViewModel().javaClass
        val provider: Provider<ViewModel> = Provider { ChildTestViewModel() }
        val creators = mutableMapOf<Class<out ViewModel>, Provider<ViewModel>>(childClass to provider)
        val factory = ViewModelProviderFactory(creators)
        val actual = factory.create(parentClass)
        assertThat(actual).isInstanceOf(childClass)
    }

    @Test(expected = IllegalArgumentException::class) fun illegalArgument() {
        val actual: Class<TestViewModel> = TestViewModel().javaClass
        val illegal: Class<TestViewModel2> = TestViewModel2().javaClass
        val provider: Provider<ViewModel> = Provider { TestViewModel() }
        val creators = mutableMapOf<Class<out ViewModel>, Provider<ViewModel>>(actual to provider)
        val factory = ViewModelProviderFactory(creators)
        factory.create(illegal)
    }

    @Test(expected = RuntimeException::class) fun providerException() {
        val cls: Class<TestViewModel> = TestViewModel().javaClass
        val provider: Provider<ViewModel> = Provider { throw RuntimeException() }
        val creators = mutableMapOf<Class<out ViewModel>, Provider<ViewModel>>(cls to provider)
        val factory = ViewModelProviderFactory(creators)
        factory.create(cls)
    }
}

private open class TestViewModel : ViewModel()

private class ChildTestViewModel : TestViewModel()

private class TestViewModel2 : ViewModel()