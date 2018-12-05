package com.rightpoint.inject.viewmodel.extensions

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.rightpoint.inject.viewmodel.extensions.R

class TestActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
    }
}