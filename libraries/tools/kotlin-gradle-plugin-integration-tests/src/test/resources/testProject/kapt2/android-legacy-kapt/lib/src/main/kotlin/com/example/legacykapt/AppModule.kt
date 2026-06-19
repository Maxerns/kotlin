package com.example.legacykapt

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideGreeting(): String = "Hello from legacy-kapt"
}
