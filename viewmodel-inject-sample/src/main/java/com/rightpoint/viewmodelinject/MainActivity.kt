package com.rightpoint.viewmodelinject

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.rightpoint.inject.viewmodel.extensions.provideViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {
    @Inject lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = provideViewModel(factory)

        findViewById<TextView>(R.id.textView).text = viewModel.someRandomString()
    }
}
