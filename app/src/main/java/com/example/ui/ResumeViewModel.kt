package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApplicationStatus
import com.example.data.model.CertificationItem
import com.example.data.model.DefaultResumeData
import com.example.data.model.EducationItem
import com.example.data.model.ExperienceItem
import com.example.data.model.JobApplication
import com.example.data.model.JobTailorMessage
import com.example.data.model.ProjectItem
import com.example.data.model.Resume
import com.example.data.model.SkillCategory
import com.example.data.model.TemplateStyle
import com.example.data.remote.BulletOptimizationResult
import com.example.data.remote.JobTailorAnalysisResult
import com.example.data.repository.ResumeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab(val title: String) {
    RESUME_EDITOR("Resume"),
    TEMPLATES("ATS Templates"),
    PREVIEW_EXPORT("Preview & Export"),
    JOB_TAILOR("Job Tailor AI"),
    APPLICATIONS("Job Tracker")
}

data class AtsScoreBreakdown(
    val totalScore: Int,
    val contactScore: Int,
    val summaryScore: Int,
    val experienceMetricsScore: Int,
    val skillsScore: Int,
    val educationScore: Int,
    val suggestions: List<String>
)

class ResumeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ResumeRepository(application)

    private val _currentTab = MutableStateFlow(AppTab.RESUME_EDITOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _resumes = MutableStateFlow<List<Resume>>(emptyList())
    val resumes: StateFlow<List<Resume>> = _resumes.asStateFlow()

    private val _activeResume = MutableStateFlow(DefaultResumeData.createSampleResume())
    val activeResume: StateFlow<Resume> = _activeResume.asStateFlow()

    private val _isAutoSaving = MutableStateFlow(false)
    val isAutoSaving: StateFlow<Boolean> = _isAutoSaving.asStateFlow()

    private val _lastSavedTime = MutableStateFlow(System.currentTimeMillis())
    val lastSavedTime: StateFlow<Long> = _lastSavedTime.asStateFlow()

    // Bullet Optimization
    private val _isOptimizingBullet = MutableStateFlow(false)
    val isOptimizingBullet: StateFlow<Boolean> = _isOptimizingBullet.asStateFlow()

    private val _activeOptimization = MutableStateFlow<BulletOptimizationResult?>(null)
    val activeOptimization: StateFlow<BulletOptimizationResult?> = _activeOptimization.asStateFlow()
    private var optimizingExpId: String? = null
    private var optimizingBulletIndex: Int = -1

    // LinkedIn Import
    private val _isImportingLinkedIn = MutableStateFlow(false)
    val isImportingLinkedIn: StateFlow<Boolean> = _isImportingLinkedIn.asStateFlow()

    private val _linkedInStatusMessage = MutableStateFlow<String?>(null)
    val linkedInStatusMessage: StateFlow<String?> = _linkedInStatusMessage.asStateFlow()

    // Job Tailor AI Workspace
    private val _jobDescription = MutableStateFlow("")
    val jobDescription: StateFlow<String> = _jobDescription.asStateFlow()

    private val _isAnalyzingJob = MutableStateFlow(false)
    val isAnalyzingJob: StateFlow<Boolean> = _isAnalyzingJob.asStateFlow()

    private val _jobAnalysis = MutableStateFlow<JobTailorAnalysisResult?>(null)
    val jobAnalysis: StateFlow<JobTailorAnalysisResult?> = _jobAnalysis.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<JobTailorMessage>>(listOf(
        JobTailorMessage(
            isUser = false,
            text = "Welcome to Job Tailor AI! Paste any job description above, and I will analyze missing ATS keywords, calculate your match score, and generate tailored bullet points you can add directly to your resume."
        )
    ))
    val chatMessages: StateFlow<List<JobTailorMessage>> = _chatMessages.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    // Job Applications Tracker
    private val _applications = MutableStateFlow<List<JobApplication>>(emptyList())
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    private val _appFilter = MutableStateFlow<ApplicationStatus?>(null)
    val appFilter: StateFlow<ApplicationStatus?> = _appFilter.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.allResumes.collectLatest { list ->
                _resumes.value = list
                if (list.isNotEmpty()) {
                    val currentId = _activeResume.value.id
                    val matched = list.find { it.id == currentId } ?: list.first()
                    _activeResume.value = matched
                } else {
                    // Seed initial resume
                    val defaultResume = DefaultResumeData.createSampleResume()
                    repository.saveResume(defaultResume)
                    _activeResume.value = defaultResume
                }
            }
        }

        viewModelScope.launch {
            repository.allApplications.collectLatest { list ->
                _applications.value = list
            }
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectResume(resume: Resume) {
        _activeResume.value = resume
    }

    fun createNewResume(title: String = "New Resume", targetRole: String = "Software Engineer") {
        val newResume = Resume(
            id = UUID.randomUUID().toString(),
            title = title,
            targetRole = targetRole,
            fullName = _activeResume.value.fullName,
            email = _activeResume.value.email,
            phone = _activeResume.value.phone,
            location = _activeResume.value.location,
            linkedin = _activeResume.value.linkedin
        )
        _activeResume.value = newResume
        saveActiveResumeImmediately(newResume)
    }

    fun updateActiveResume(transform: (Resume) -> Resume) {
        val updated = transform(_activeResume.value)
        _activeResume.value = updated
        triggerDebouncedAutoSave(updated)
    }

    private fun triggerDebouncedAutoSave(resume: Resume) {
        _isAutoSaving.value = true
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            repository.saveResume(resume)
            _isAutoSaving.value = false
            _lastSavedTime.value = System.currentTimeMillis()
        }
    }

    private fun saveActiveResumeImmediately(resume: Resume) {
        viewModelScope.launch {
            _isAutoSaving.value = true
            repository.saveResume(resume)
            _isAutoSaving.value = false
            _lastSavedTime.value = System.currentTimeMillis()
        }
    }

    fun deleteResume(resumeId: String) {
        viewModelScope.launch {
            repository.deleteResume(resumeId)
        }
    }

    fun setTemplateStyle(style: TemplateStyle) {
        updateActiveResume { it.copy(templateStyle = style) }
    }

    // --- Section Editing Helpers ---
    fun updatePersonalInfo(
        name: String,
        jobTitle: String,
        email: String,
        phone: String,
        location: String,
        website: String,
        linkedin: String,
        github: String,
        summary: String
    ) {
        updateActiveResume {
            it.copy(
                fullName = name,
                jobTitle = jobTitle,
                email = email,
                phone = phone,
                location = location,
                website = website,
                linkedin = linkedin,
                github = github,
                summary = summary
            )
        }
    }

    fun addExperience() {
        val newExp = ExperienceItem(
            id = UUID.randomUUID().toString(),
            company = "Company Name",
            role = "Title / Role",
            startDate = "Jan 2024",
            endDate = "Present",
            isCurrent = true,
            bullets = listOf("Architected key systems driving business scalability.")
        )
        updateActiveResume { it.copy(experiences = it.experiences + newExp) }
    }

    fun updateExperience(expId: String, updated: ExperienceItem) {
        updateActiveResume { resume ->
            resume.copy(experiences = resume.experiences.map { if (it.id == expId) updated else it })
        }
    }

    fun removeExperience(expId: String) {
        updateActiveResume { resume ->
            resume.copy(experiences = resume.experiences.filter { it.id != expId })
        }
    }

    fun addExperienceBullet(expId: String, text: String = "Engineered automated workflow improving throughput by 30%.") {
        updateActiveResume { resume ->
            resume.copy(experiences = resume.experiences.map { exp ->
                if (exp.id == expId) exp.copy(bullets = exp.bullets + text) else exp
            })
        }
    }

    fun updateExperienceBullet(expId: String, index: Int, newText: String) {
        updateActiveResume { resume ->
            resume.copy(experiences = resume.experiences.map { exp ->
                if (exp.id == expId) {
                    val bullets = exp.bullets.toMutableList()
                    if (index in bullets.indices) {
                        bullets[index] = newText
                    }
                    exp.copy(bullets = bullets)
                } else exp
            })
        }
    }

    fun removeExperienceBullet(expId: String, index: Int) {
        updateActiveResume { resume ->
            resume.copy(experiences = resume.experiences.map { exp ->
                if (exp.id == expId) {
                    val bullets = exp.bullets.toMutableList()
                    if (index in bullets.indices) {
                        bullets.removeAt(index)
                    }
                    exp.copy(bullets = bullets)
                } else exp
            })
        }
    }

    fun addEducation() {
        val newEdu = EducationItem(
            id = UUID.randomUUID().toString(),
            institution = "University Name",
            degree = "Bachelor of Science",
            fieldOfStudy = "Computer Science",
            graduationYear = "2022"
        )
        updateActiveResume { it.copy(educations = it.educations + newEdu) }
    }

    fun updateEducation(eduId: String, updated: EducationItem) {
        updateActiveResume { resume ->
            resume.copy(educations = resume.educations.map { if (it.id == eduId) updated else it })
        }
    }

    fun removeEducation(eduId: String) {
        updateActiveResume { resume ->
            resume.copy(educations = resume.educations.filter { it.id != eduId })
        }
    }

    fun addSkillCategory(name: String, skills: List<String>) {
        val newCategory = SkillCategory(
            id = UUID.randomUUID().toString(),
            categoryName = name,
            skills = skills
        )
        updateActiveResume { it.copy(skillCategories = it.skillCategories + newCategory) }
    }

    fun updateSkillCategory(categoryId: String, updated: SkillCategory) {
        updateActiveResume { resume ->
            resume.copy(skillCategories = resume.skillCategories.map { if (it.id == categoryId) updated else it })
        }
    }

    fun removeSkillCategory(categoryId: String) {
        updateActiveResume { resume ->
            resume.copy(skillCategories = resume.skillCategories.filter { it.id != categoryId })
        }
    }

    // --- AI Bullet Point Optimizer ---
    fun requestBulletOptimization(expId: String, bulletIndex: Int, bulletText: String) {
        optimizingExpId = expId
        optimizingBulletIndex = bulletIndex
        _isOptimizingBullet.value = true
        _activeOptimization.value = null

        viewModelScope.launch {
            val role = _activeResume.value.targetRole.ifEmpty { _activeResume.value.jobTitle }
            val result = repository.optimizeBullet(bulletText, role)
            _activeOptimization.value = result
            _isOptimizingBullet.value = false
        }
    }

    fun applyOptimizedBullet(chosenText: String) {
        val expId = optimizingExpId
        val index = optimizingBulletIndex
        if (expId != null && index >= 0) {
            updateExperienceBullet(expId, index, chosenText)
        }
        dismissBulletOptimization()
    }

    fun dismissBulletOptimization() {
        _activeOptimization.value = null
        _isOptimizingBullet.value = false
        optimizingExpId = null
        optimizingBulletIndex = -1
    }

    // --- LinkedIn Profile Auto-Fill ---
    fun importLinkedInProfile(rawProfileText: String) {
        if (rawProfileText.isBlank()) return
        _isImportingLinkedIn.value = true
        _linkedInStatusMessage.value = "Analyzing profile and extracting structured sections..."

        viewModelScope.launch {
            try {
                val parsed = repository.parseLinkedIn(rawProfileText)
                updateActiveResume { current ->
                    current.copy(
                        fullName = parsed.fullName.ifEmpty { current.fullName },
                        jobTitle = parsed.jobTitle.ifEmpty { current.jobTitle },
                        email = parsed.email.ifEmpty { current.email },
                        phone = parsed.phone.ifEmpty { current.phone },
                        location = parsed.location.ifEmpty { current.location },
                        summary = parsed.summary.ifEmpty { current.summary },
                        experiences = if (parsed.experiences.isNotEmpty()) parsed.experiences else current.experiences,
                        educations = if (parsed.educations.isNotEmpty()) parsed.educations else current.educations,
                        skillCategories = if (parsed.skills.isNotEmpty()) {
                            listOf(
                                SkillCategory(
                                    categoryName = "Imported Skills",
                                    skills = parsed.skills
                                )
                            ) + current.skillCategories
                        } else current.skillCategories
                    )
                }
                _linkedInStatusMessage.value = "Success! Resume sections populated from profile."
            } catch (e: Exception) {
                _linkedInStatusMessage.value = "Could not parse profile. Please verify text formatting."
            } finally {
                _isImportingLinkedIn.value = false
            }
        }
    }

    fun clearLinkedInMessage() {
        _linkedInStatusMessage.value = null
    }

    // --- Job Tailor AI ---
    fun setJobDescription(jd: String) {
        _jobDescription.value = jd
    }

    fun analyzeJobDescription() {
        val jd = _jobDescription.value
        if (jd.isBlank()) return
        _isAnalyzingJob.value = true

        viewModelScope.launch {
            val analysis = repository.tailorResumeToJob(_activeResume.value, jd)
            _jobAnalysis.value = analysis
            _isAnalyzingJob.value = false

            val chatMsg = JobTailorMessage(
                isUser = false,
                text = "I've analyzed your target job posting! Your ATS Match Score is ${analysis.matchScore}%. Found ${analysis.matchedKeywords.size} matching keywords and ${analysis.missingKeywords.size} key gaps. Below are tailored bullet suggestions crafted for this role.",
                suggestedBullets = analysis.tailoredBullets,
                matchedKeywords = analysis.matchedKeywords,
                missingKeywords = analysis.missingKeywords,
                matchScore = analysis.matchScore
            )
            _chatMessages.value = _chatMessages.value + chatMsg
        }
    }

    fun sendJobTailorChatMessage(query: String) {
        if (query.isBlank()) return
        val userMsg = JobTailorMessage(isUser = true, text = query)
        _chatMessages.value = _chatMessages.value + userMsg
        _isSendingChat.value = true

        viewModelScope.launch {
            val responseText = repository.chatWithJobTailor(_activeResume.value, _jobDescription.value, query)
            val aiMsg = JobTailorMessage(isUser = false, text = responseText)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isSendingChat.value = false
        }
    }

    fun addTailoredBulletToResume(bullet: String) {
        val experiences = _activeResume.value.experiences
        if (experiences.isNotEmpty()) {
            addExperienceBullet(experiences.first().id, bullet)
        } else {
            addExperience()
            viewModelScope.launch {
                delay(100)
                _activeResume.value.experiences.firstOrNull()?.let {
                    addExperienceBullet(it.id, bullet)
                }
            }
        }
    }

    // --- Job Applications Tracker ---
    fun setApplicationFilter(status: ApplicationStatus?) {
        _appFilter.value = status
    }

    fun saveJobApplication(app: JobApplication) {
        viewModelScope.launch {
            repository.saveApplication(app)
        }
    }

    fun deleteJobApplication(appId: String) {
        viewModelScope.launch {
            repository.deleteApplication(appId)
        }
    }

    fun updateApplicationStatus(app: JobApplication, newStatus: ApplicationStatus) {
        saveJobApplication(app.copy(status = newStatus))
    }

    // --- ATS Readiness Score Calculation ---
    fun calculateAtsBreakdown(): AtsScoreBreakdown {
        val r = _activeResume.value
        var contact = 0
        if (r.fullName.isNotBlank()) contact += 5
        if (r.email.isNotBlank()) contact += 5
        if (r.phone.isNotBlank()) contact += 5
        if (r.location.isNotBlank()) contact += 5

        val summaryScore = when {
            r.summary.length >= 100 -> 20
            r.summary.isNotBlank() -> 10
            else -> 0
        }

        val allBullets = r.experiences.flatMap { it.bullets }
        val metricCount = allBullets.count { b ->
            b.contains("%") || b.any { it.isDigit() } || b.contains("reduced", ignoreCase = true) || b.contains("increased", ignoreCase = true)
        }
        val expScore = when {
            r.experiences.size >= 2 && metricCount >= 3 -> 25
            r.experiences.isNotEmpty() -> 15
            else -> 0
        }

        val totalSkills = r.skillCategories.flatMap { it.skills }.size
        val skillsScore = when {
            totalSkills >= 8 -> 20
            totalSkills >= 3 -> 12
            else -> 0
        }

        val eduScore = if (r.educations.isNotEmpty()) 15 else 0

        val total = contact + summaryScore + expScore + skillsScore + eduScore

        val suggestions = mutableListOf<String>()
        if (r.summary.isBlank()) suggestions.add("Add a concise 3-4 sentence professional summary.")
        if (metricCount < 3) suggestions.add("Include quantifiable metrics (%, latency, revenue) in your bullet points.")
        if (totalSkills < 8) suggestions.add("Add at least 8 specific technical skills and tools.")
        if (r.educations.isEmpty()) suggestions.add("Add your degree and university credentials.")

        return AtsScoreBreakdown(
            totalScore = total.coerceIn(0, 100),
            contactScore = contact,
            summaryScore = summaryScore,
            experienceMetricsScore = expScore,
            skillsScore = skillsScore,
            educationScore = eduScore,
            suggestions = suggestions
        )
    }
}
