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

package com.rightpoint.inject.viewmodel.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.annotation.UiThreadTest
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class ExtensionsTest {
    @JvmField @Rule val rule = ActivityTestRule(TestActivity::class.java)

    @Test fun provideViewModelForActivity() {
        val expected = TestViewModel()
        val factory = TestFactory(expected)
        val actual = rule.activity.provideViewModel<TestViewModel>(factory)
        assertThat(actual).isEqualTo(expected)
    }

    @UiThreadTest @Test fun provideViewModelForFragment() {
        val expected = TestViewModel()
        val factory = TestFactory(expected)
        val fragment = TestFragment()

        rule.activity.supportFragmentManager.beginTransaction()
            .add(R.id.contentFrame, fragment, "TEST")
            .commitNow()

        val actual = fragment.provideViewModel<TestViewModel>(factory)
        assertThat(actual).isEqualTo(expected)
    }

    @UiThreadTest @Test fun provideActivityViewModelForFragment() {
        val expected = TestViewModel()
        val factory = TestFactory(expected)
        val fragment = TestFragment()

        val activity = rule.activity
        activity.supportFragmentManager.beginTransaction()
            .add(R.id.contentFrame, fragment, "TEST")
            .commitNow()

        val fragmentResult = fragment.provideActivityViewModel<TestViewModel>(factory)
        assertThat(fragmentResult).isEqualTo(expected)

        val activityResult = activity.provideViewModel<TestViewModel>(factory)
        assertThat(activityResult).isEqualTo(fragmentResult)

    }

    @UiThreadTest @Test fun provideParentViewModelForFragment() {
        val expected = TestViewModel()
        val factory = TestFactory(expected)
        val parent = TestFragment()
        val child = TestFragment()

        rule.activity.supportFragmentManager.beginTransaction()
            .add(R.id.contentFrame, parent, "PARENT_TEST")
            .commitNow()

        parent.childFragmentManager.beginTransaction()
            .add(R.id.contentFrame, child, "CHILD_TEST")
            .commitNow()

        val childResult = child.provideParentViewModel<TestViewModel>(factory)
        assertThat(childResult).isEqualTo(expected)

        val parentResult = parent.provideViewModel<TestViewModel>(factory)
        assertThat(parentResult).isEqualTo(childResult)
    }

    class TestViewModel : ViewModel()

    class TestFactory(private val delegate: Any?) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return delegate as T
        }
    }
}