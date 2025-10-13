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
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import com.oriundo.lbretaappestudiantil.repositories.AttendanceRepository
import com.oriundo.lbretaappestudiantil.repositories.MaterialRequestRepository
import com.oriundo.lbretaappestudiantil.repositories.SchoolEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindClassRepository(
        classRepositoryImpl: ClassRepositoryImpl
    ): ClassRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        studentRepositoryImpl: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    @Singleton
    abstract fun bindAnnotationRepository(
        annotationRepositoryImpl: AnnotationRepositoryImpl
    ): AnnotationRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        attendanceRepositoryImpl: AttendanceRepositoryImpl
    ): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindMaterialRequestRepository(
        materialRequestRepositoryImpl: MaterialRequestRepositoryImpl
    ): MaterialRequestRepository

    @Binds
    @Singleton
    abstract fun bindSchoolEventRepository(
        schoolEventRepositoryImpl: SchoolEventRepositoryImpl
    ): SchoolEventRepository
}