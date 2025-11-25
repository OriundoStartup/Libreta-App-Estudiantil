package com.oriundo.lbretaappestudiantil.di

import com.oriundo.lbretaappestudiantil.data.local.repositories.AnnotationRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.AttendanceRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.ClassRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.JustificationRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.MessageRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.PreferencesRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.ProfileRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.SchoolEventRepositoryImpl
import com.oriundo.lbretaappestudiantil.data.local.repositories.StudentRepositoryImpl
import com.oriundo.lbretaappestudiantil.domain.model.repository.AnnotationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.AttendanceRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.JustificationRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.PreferencesRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar todos los repositorios
 *
 * IMPORTANTE: Cambió de `object` a `abstract class` para poder usar @Binds
 * @Binds es más eficiente que @Provides porque genera menos código
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bindea ProfileRepository
     * Hilt automáticamente inyectará ProfileDao en ProfileRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    /**
     * Bindea ClassRepository
     * Hilt automáticamente inyectará: classDao, localDatabaseRepository, firebaseAuth
     */
    @Binds
    @Singleton
    abstract fun bindClassRepository(
        impl: ClassRepositoryImpl
    ): ClassRepository

    /**
     * Bindea StudentRepository
     * Hilt automáticamente inyectará: studentDao, classDao, studentParentRelationDao
     */
    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        impl: StudentRepositoryImpl
    ): StudentRepository

    /**
     * Bindea AnnotationRepository
     * Hilt automáticamente inyectará: annotationDao, profileDao, studentDao
     */
    @Binds
    @Singleton
    abstract fun bindAnnotationRepository(
        impl: AnnotationRepositoryImpl
    ): AnnotationRepository

    /**
     * Bindea AttendanceRepository
     * Hilt automáticamente inyectará: attendanceDao, studentDao, profileDao, firestore
     */
    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        impl: AttendanceRepositoryImpl
    ): AttendanceRepository

    /**
     * Bindea MessageRepository
     * Hilt automáticamente inyectará: messageDao
     */
    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository

    /**
     * Bindea SchoolEventRepository
     * Hilt automáticamente inyectará: schoolEventDao
     */
    @Binds
    @Singleton
    abstract fun bindSchoolEventRepository(
        impl: SchoolEventRepositoryImpl
    ): SchoolEventRepository

    /**
     * Bindea JustificationRepository
     * Hilt automáticamente inyectará: dao, studentDao, classDao, userDao, firestore, auth, localDatabaseRepository
     */
    @Binds
    @Singleton
    abstract fun bindJustificationRepository(
        impl: JustificationRepositoryImpl
    ): JustificationRepository

    /**
     * Bindea PreferencesRepository
     * Hilt automáticamente inyectará: context (anotado con @ApplicationContext)
     */
    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository
}