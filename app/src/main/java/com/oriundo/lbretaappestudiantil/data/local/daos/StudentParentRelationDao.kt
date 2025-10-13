package com.oriundo.lbretaappestudiantil.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oriundo.lbretaappestudiantil.data.local.models.ProfileEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentEntity
import com.oriundo.lbretaappestudiantil.data.local.models.StudentParentRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentParentRelationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRelation(relation: StudentParentRelation): Long

    @Query("""
        SELECT s.* FROM students s
        INNER JOIN student_parent_relation spr ON s.id = spr.student_id
        WHERE spr.parent_id = :parentId AND s.is_active = 1
    """)
    fun getStudentsByParent(parentId: Int): Flow<List<StudentEntity>>

    @Query("""
        SELECT p.* FROM profiles p
        INNER JOIN student_parent_relation spr ON p.id = spr.parent_id
        WHERE spr.student_id = :studentId
    """)
    fun getParentsByStudent(studentId: Int): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM student_parent_relation WHERE student_id = :studentId AND parent_id = :parentId")
    suspend fun getRelation(studentId: Int, parentId: Int): StudentParentRelation?
}