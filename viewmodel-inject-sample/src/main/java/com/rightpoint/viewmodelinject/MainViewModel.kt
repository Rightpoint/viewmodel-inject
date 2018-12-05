package com.rightpoint.viewmodelinject

import androidx.lifecycle.ViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {
    fun someRandomString() = "This is a Hello World"
}