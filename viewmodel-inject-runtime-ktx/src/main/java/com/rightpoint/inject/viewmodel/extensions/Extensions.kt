package com.rightpoint.inject.viewmodel.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

inline fun <reified VM : ViewModel> FragmentActivity.provideViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(this, factory)[VM::class.java]
}

inline fun <reified VM : ViewModel> Fragment.provideViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(this, factory)[VM::class.java]
}

inline fun <reified VM : ViewModel> Fragment.provideActivityViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(requireActivity(), factory)[VM::class.java]
}

inline fun <reified VM : ViewModel> Fragment.provideParentViewModel(
    factory: ViewModelProvider.Factory
): VM {
    return ViewModelProviders.of(requireNotNull(parentFragment), factory)[VM::class.java]
}