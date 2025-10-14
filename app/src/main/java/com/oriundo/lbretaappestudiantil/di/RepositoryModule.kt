package com.oriundo.lbretaappestudiantil.di

import com.oriundo.lbretaappestudiantil.data.local.repositories.AnnotationRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.AttendanceRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.AuthRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.ClassRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.MaterialRequestRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.MessageRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.ProfileRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.SchoolEventRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.StudentRepositoryImpl
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.AttendanceRepository // ✅
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.MaterialRequestRepository // ✅
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt responsable de proporcionar todas las dependencias de la capa de Dominio (Interfaces Repository).
 *
 * @Module: Indica que esta es una clase de módulo de Dagger.
 * @InstallIn(SingletonComponent::class): Asegura que las dependencias duren mientras la aplicación esté viva.
 * (Singleton).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // --- Enlaces de Repositorio de Autenticación y Perfil ---

    /**
     * Provee una instancia Singleton de AuthRepository, enlazándola a su implementación concreta.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    /**
     * Provee una instancia Singleton de ProfileRepository, enlazándola a su implementación.
     */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository

    // --- Enlaces de Repositorios de Información Estudiantil ---

    /**
     * Provee una instancia Singleton de ClassRepository.
     */
    @Binds
    @Singleton
    abstract fun bindClassRepository(classRepositoryImpl: ClassRepositoryImpl): ClassRepository

    /**
     * Provee una instancia Singleton de StudentRepository.
     */
    @Binds
    @Singleton
    abstract fun bindStudentRepository(studentRepositoryImpl: StudentRepositoryImpl): StudentRepository

    /**
     * Provee una instancia Singleton de AnnotationRepository.
     */
    @Binds
    @Singleton
    abstract fun bindAnnotationRepository(annotationRepositoryImpl: AnnotationRepositoryImpl): AnnotationRepository

    /**
     * Provee una instancia Singleton de MessageRepository.
     */
    @Binds
    @Singleton
    abstract fun bindMessageRepository(messageRepositoryImpl: MessageRepositoryImpl): MessageRepository

    // --- Enlaces de Repositorios de Funcionalidades Específicas ---

    /**
     * Provee una instancia Singleton de AttendanceRepository.
     */
    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(attendanceRepositoryImpl: AttendanceRepositoryImpl): AttendanceRepository

    /**
     * Provee una instancia Singleton de MaterialRequestRepository.
     */
    @Binds
    @Singleton
    abstract fun bindMaterialRequestRepository(materialRequestRepositoryImpl: MaterialRequestRepositoryImpl): MaterialRequestRepository

    /**
     * Provee una instancia Singleton de SchoolEventRepository.
     * Esta es la dependencia que faltaba y causó el error de MissingBinding.
     */
    @Binds
    @Singleton
    abstract fun bindSchoolEventRepository(
        schoolEventRepositoryImpl: SchoolEventRepositoryImpl
    ): SchoolEventRepository
}