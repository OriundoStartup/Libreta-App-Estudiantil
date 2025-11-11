package com.oriundo.lbretaappestudiantil.di

import android.content.Context
import androidx.room.Room
import com.oriundo.lbretaappestudiantil.data.local.LibretAppDatabase
import com.oriundo.lbretaappestudiantil.data.local.daos.AbsenceJustificationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.AnnotationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.AttendanceDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ClassDao
import com.oriundo.lbretaappestudiantil.data.local.daos.MessageDao
import com.oriundo.lbretaappestudiantil.data.local.daos.ProfileDao
import com.oriundo.lbretaappestudiantil.data.local.daos.SchoolEventDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentDao
import com.oriundo.lbretaappestudiantil.data.local.daos.StudentParentRelationDao
import com.oriundo.lbretaappestudiantil.data.local.daos.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LibretAppDatabase {
        return Room.databaseBuilder(
            context,
            LibretAppDatabase::class.java,
            "libreta_app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: LibretAppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: LibretAppDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideClassDao(database: LibretAppDatabase): ClassDao {
        return database.classDao()
    }

    @Provides
    @Singleton
    fun provideStudentDao(database: LibretAppDatabase): StudentDao {
        return database.studentDao()
    }

    @Provides
    @Singleton
    fun provideStudentParentRelationDao(database: LibretAppDatabase): StudentParentRelationDao {
        return database.studentParentRelationDao()
    }

    @Provides
    @Singleton
    fun provideAnnotationDao(database: LibretAppDatabase): AnnotationDao {
        return database.annotationDao()
    }

    @Provides
    @Singleton
    fun provideAttendanceDao(database: LibretAppDatabase): AttendanceDao {
        return database.attendanceDao()
    }



    @Provides
    @Singleton
    fun provideMessageDao(database: LibretAppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideSchoolEventDao(database: LibretAppDatabase): SchoolEventDao {
        return database.schoolEventDao()
    }

    @Provides
    @Singleton
    fun provideAbsenceJustificationDao(database: LibretAppDatabase): AbsenceJustificationDao {
        return database.absenceJustificationDao()
    }




}