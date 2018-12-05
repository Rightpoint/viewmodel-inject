package com.rightpoint.viewmodelinject

import com.rightpoint.inject.viewmodel.annotations.ViewModelModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@ViewModelModule
@Module(includes = [ViewModelInjectionModule::class])
abstract class AppModule {
    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity
}