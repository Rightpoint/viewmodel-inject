package com.rightpoint.viewmodelinject

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class App : DaggerApplication() {
    val component by lazy {
        DaggerAppComponent.builder()
            .app(this)
            .build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return component
    }
}