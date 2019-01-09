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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

/**
 * Provides a ViewModel of the given type scoped to current FragmentActivity's lifecycle.
 */
inline fun <reified VM : ViewModel> FragmentActivity.provideViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(this, factory)[VM::class.java]
}

/**
 * Provides a ViewModel of the given type scoped to current Fragment's lifecycle.
 */
inline fun <reified VM : ViewModel> Fragment.provideViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(this, factory)[VM::class.java]
}

/**
 * Provides a ViewModel of the given type scoped to the lifecycle of the Activity to which the
 * current Fragment is attached.
 */
inline fun <reified VM : ViewModel> Fragment.provideActivityViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(requireActivity(), factory)[VM::class.java]
}

/**
 * Provides a ViewModel of the given type scoped to the lifecycle of the parent Fragment of the
 * current Fragment.
 */
inline fun <reified VM : ViewModel> Fragment.provideParentViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(requireNotNull(parentFragment), factory)[VM::class.java]
}