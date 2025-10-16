package com.oriundo.lbretaappestudiantil.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.oriundo.lbretaappestudiantil.data.local.converters.Converters
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
import com.oriundo.lbretaappestudiantil.data.local.models.AnnotationEntity
import com.oriundo.lbretaappestudiantil.data.local.models.AttendanceEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ClassEntity
import com.oriundo.lbretaappestudiantil.data.local.models.MaterialRequestEntity
import com.oriundo.lbretaappestudiantil.data.local.models.MessageEntity
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.SchoolEventEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentParentRelation
import com.oriundo.lbretaappestudiantil.data.local.models.UserEntity



@Database(
    entities = [
        UserEntity::class,
        ProfileEntity::class,
        ClassEntity::class,
        StudentEntity::class,
        StudentParentRelation::class,
        AnnotationEntity::class,
        AttendanceEntity::class,
        MaterialRequestEntity::class,
        MessageEntity::class,
        SchoolEventEntity::class



    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LibretAppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun studentParentRelationDao(): StudentParentRelationDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun materialRequestDao(): MaterialRequestDao
    abstract fun messageDao(): MessageDao
    abstract fun schoolEventDao(): SchoolEventDao


}