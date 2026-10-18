package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {
    @Query("SELECT * FROM resumes ORDER BY updatedAt DESC")
    fun getAllResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE id = :id LIMIT 1")
    suspend fun getResumeById(id: String): ResumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(resume: ResumeEntity)

    @Query("DELETE FROM resumes WHERE id = :id")
    suspend fun deleteResumeById(id: String)
}

@Dao
interface JobApplicationDao {
    @Query("SELECT * FROM job_applications ORDER BY updatedAt DESC")
    fun getAllApplications(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE id = :id LIMIT 1")
    suspend fun getApplicationById(id: String): JobApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(application: JobApplicationEntity)

    @Query("DELETE FROM job_applications WHERE id = :id")
    suspend fun deleteApplicationById(id: String)
}
