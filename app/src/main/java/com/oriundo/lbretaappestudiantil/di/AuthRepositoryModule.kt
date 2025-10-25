package com.oriundo.lbretaappestudiantil.di

import com.oriundo.lbretaappestudiantil.data.local.firebase.FirebaseAuthRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Archivo: com/oriundo/lbretaappestudiantil/di/AuthRepositoryModule.kt

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: FirebaseAuthRepository // ⬅️ Usa la implementación de Firebase
    ): AuthRepository
}