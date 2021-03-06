# DEPRECATED

**Attention:** This library has been deprecated and is not in active development. For ViewModel injection support please use the [Hilt Jetpack integration for ViewModels](https://developer.android.com/training/dependency-injection/hilt-jetpack). For the Kotlin extensions please use the extensions provided as part of [Android KTX](https://developer.android.com/kotlin/ktx).

# viewmodel-inject
Utilizes Dagger to generate a ViewModelProvider.Factory

# Usage
Add the `@ViewModelModule` annotation and include the `ViewModelInjectionModule` module (generated at compile time) on an Application-scope module:
```kotlin
@ViewModelModule
@Module(includes = [ViewModelInjectionModule::class])
abstract class AppModule {
  ...
}
```

Now just annotate your `ViewModel` constructors with Dagger's `@Inject` annotation:
```kotlin
class YourViewModel @Inject constructor() : ViewModel() {
  ...
}
```

Dagger should now provide a `ViewModelProvider.Factory` implementation with an instance of your ViewModel! You can now inject the factory into your `FragmentActivity` or `Fragment`:
```kotlin
class YourActivity : FragmentActivity() {
  @Inject lateinit var factory: ViewModelProvider.Factory
  private lateinit var viewModel: YourViewModel
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Handle your injection logic here
    viewModel = ViewModelProviders.of(this, factory)[YourViewModel::class]
  }
}
```

Additionally, Kotlin projects can take advantage of the Kotlin extensions:
```kotlin
class YourActivity : FragmentActivity() {
  @Inject lateinit var factory: ViewModelProvider.Factory
  private lateinit var viewModel: YourViewModel
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Handle your injection logic here
    viewModel = provideViewModel(factory)
  }
}
```

# Installation
Add to the root project's `build.gradle` file:
```groovy
allprojects {
  repositories {
    jcenter()
  }
}
```

In the project `build.gradle` file:
```groovy
dependencies {
  implementation "com.rightpoint:viewmodel-inject-runtime:$latest_version"
  annotationProcessor "com.rightpoint:viewmodel-inject-processor:$latest_version"
}
```

For Kotlin projects:
```groovy
dependencies {
  implementation "com.rightpoint:viewmodel-inject-runtime-ktx:$latest_version"
  kapt "com.rightpoint:viewmodel-inject-processor:$latest_version"
}
```

# License
```
Copyright 2019 Rightpoint

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
