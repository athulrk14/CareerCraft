package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.JobApplicationEntity
import com.example.data.local.ResumeEntity
import com.example.data.model.DefaultResumeData
import com.example.data.model.JobApplication
import com.example.data.model.Resume
import com.example.data.remote.BulletOptimizationResult
import com.example.data.remote.GeminiResumeAssistant
import com.example.data.remote.JobTailorAnalysisResult
import com.example.data.remote.LinkedInParseResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ResumeRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val resumeDao = database.resumeDao()
    private val jobApplicationDao = database.jobApplicationDao()
    private val geminiAssistant = GeminiResumeAssistant()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val allResumes: Flow<List<Resume>> = resumeDao.getAllResumes().map { list ->
        list.map { it.toDomain() }
    }

    val allApplications: Flow<List<JobApplication>> = jobApplicationDao.getAllApplications().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getResumeById(id: String): Resume? {
        return resumeDao.getResumeById(id)?.toDomain()
    }

    suspend fun saveResume(resume: Resume) {
        val updated = resume.copy(updatedAt = System.currentTimeMillis())
        resumeDao.insertOrUpdate(ResumeEntity.fromDomain(updated))
    }

    suspend fun deleteResume(id: String) {
        resumeDao.deleteResumeById(id)
    }

    suspend fun saveApplication(application: JobApplication) {
        val updated = application.copy(updatedAt = System.currentTimeMillis())
        jobApplicationDao.insertOrUpdate(JobApplicationEntity.fromDomain(updated))
    }

    suspend fun deleteApplication(id: String) {
        jobApplicationDao.deleteApplicationById(id)
    }

    // AI Features
    suspend fun optimizeBullet(bullet: String, role: String): BulletOptimizationResult {
        return geminiAssistant.optimizeBulletPoint(bullet, role)
    }

    suspend fun parseLinkedIn(rawText: String): LinkedInParseResult {
        return geminiAssistant.parseLinkedInProfile(rawText)
    }

    suspend fun tailorResumeToJob(resume: Resume, jobDescription: String): JobTailorAnalysisResult {
        return geminiAssistant.tailorResumeToJob(resume, jobDescription)
    }

    suspend fun chatWithJobTailor(resume: Resume, jobDescription: String, userQuery: String): String {
        return geminiAssistant.chatWithJobTailor(resume, jobDescription, userQuery)
    }
}
