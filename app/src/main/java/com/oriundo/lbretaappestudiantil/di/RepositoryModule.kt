package com.oriundo.lbretaappestudiantil.di

import com.oriundo.lbretaappestudiantil.data.local.daos.AnnotationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.AttendanceDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.MaterialRequestDao
import com.oriundo.lbretaappestudiantil.data.local.daos.MessageDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.SchoolEventDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentParentRelationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.UserDao
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
import com.oriundo.lbretaappestudiantil.domain.model.repository.AttendanceRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.AuthRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ClassRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.MaterialRequestRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.MessageRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.ProfileRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.SchoolEventRepository
import com.oriundo.lbretaappestudiantil.domain.model.repository.StudentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        profileDao: ProfileDao,
        classDao: ClassDao,
        studentDao: StudentDao,
        studentParentRelationDao: StudentParentRelationDao
    ): AuthRepository {
        return AuthRepositoryImpl(
            userDao = userDao,
            profileDao = profileDao,
            classDao = classDao,
            studentDao = studentDao,
            studentParentRelationDao = studentParentRelationDao
        )
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        profileDao: ProfileDao
    ): ProfileRepository {
        return ProfileRepositoryImpl(profileDao)
    }

    @Provides
    @Singleton
    fun provideClassRepository(
        classDao: ClassDao
    ): ClassRepository {
        return ClassRepositoryImpl(classDao)
    }

    @Provides
    @Singleton
    fun provideStudentRepository(
        studentDao: StudentDao,
        classDao: ClassDao,
        studentParentRelationDao: StudentParentRelationDao
    ): StudentRepository {
        return StudentRepositoryImpl(
            studentDao = studentDao,
            classDao = classDao,
            studentParentRelationDao = studentParentRelationDao
        )
    }

    @Provides
    @Singleton
    fun provideAnnotationRepository(
        annotationDao: AnnotationDao
    ): AnnotationRepository {
        return AnnotationRepositoryImpl(annotationDao)
    }

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        attendanceDao: AttendanceDao
    ): AttendanceRepository {
        return AttendanceRepositoryImpl(attendanceDao)
    }

    @Provides
    @Singleton
    fun provideMaterialRequestRepository(
        materialRequestDao: MaterialRequestDao
    ): MaterialRequestRepository {
        return MaterialRequestRepositoryImpl(materialRequestDao)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        messageDao: MessageDao
    ): MessageRepository {
        return MessageRepositoryImpl(messageDao)
    }

    @Provides
    @Singleton
    fun provideSchoolEventRepository(
        schoolEventDao: SchoolEventDao
    ): SchoolEventRepository {
        return SchoolEventRepositoryImpl(schoolEventDao)
    }
}