package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ApplicationStatus
import com.example.data.model.CertificationItem
import com.example.data.model.EducationItem
import com.example.data.model.ExperienceItem
import com.example.data.model.JobApplication
import com.example.data.model.ProjectItem
import com.example.data.model.Resume
import com.example.data.model.SkillCategory
import com.example.data.model.TemplateStyle

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetRole: String,
    val templateStyle: String,
    val fullName: String,
    val jobTitle: String,
    val email: String,
    val phone: String,
    val location: String,
    val website: String,
    val linkedin: String,
    val github: String,
    val summary: String,
    val experiences: List<ExperienceItem>,
    val educations: List<EducationItem>,
    val skillCategories: List<SkillCategory>,
    val projects: List<ProjectItem>,
    val certifications: List<CertificationItem>,
    val updatedAt: Long
) {
    fun toDomain(): Resume {
        return Resume(
            id = id,
            title = title,
            targetRole = targetRole,
            templateStyle = try {
                TemplateStyle.valueOf(templateStyle)
            } catch (e: Exception) {
                TemplateStyle.MODERN_EXECUTIVE
            },
            fullName = fullName,
            jobTitle = jobTitle,
            email = email,
            phone = phone,
            location = location,
            website = website,
            linkedin = linkedin,
            github = github,
            summary = summary,
            experiences = experiences,
            educations = educations,
            skillCategories = skillCategories,
            projects = projects,
            certifications = certifications,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(resume: Resume): ResumeEntity {
            return ResumeEntity(
                id = resume.id,
                title = resume.title,
                targetRole = resume.targetRole,
                templateStyle = resume.templateStyle.name,
                fullName = resume.fullName,
                jobTitle = resume.jobTitle,
                email = resume.email,
                phone = resume.phone,
                location = resume.location,
                website = resume.website,
                linkedin = resume.linkedin,
                github = resume.github,
                summary = resume.summary,
                experiences = resume.experiences,
                educations = resume.educations,
                skillCategories = resume.skillCategories,
                projects = resume.projects,
                certifications = resume.certifications,
                updatedAt = resume.updatedAt
            )
        }
    }
}

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey val id: String,
    val company: String,
    val position: String,
    val location: String,
    val status: String,
    val salary: String,
    val appliedDate: String,
    val interviewDate: String,
    val notes: String,
    val linkedResumeId: String?,
    val jobPostingUrl: String,
    val updatedAt: Long
) {
    fun toDomain(): JobApplication {
        return JobApplication(
            id = id,
            company = company,
            position = position,
            location = location,
            status = try {
                ApplicationStatus.valueOf(status)
            } catch (e: Exception) {
                ApplicationStatus.APPLIED
            },
            salary = salary,
            appliedDate = appliedDate,
            interviewDate = interviewDate,
            notes = notes,
            linkedResumeId = linkedResumeId,
            jobPostingUrl = jobPostingUrl,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(app: JobApplication): JobApplicationEntity {
            return JobApplicationEntity(
                id = app.id,
                company = app.company,
                position = app.position,
                location = app.location,
                status = app.status.name,
                salary = app.salary,
                appliedDate = app.appliedDate,
                interviewDate = app.interviewDate,
                notes = app.notes,
                linkedResumeId = app.linkedResumeId,
                jobPostingUrl = app.jobPostingUrl,
                updatedAt = app.updatedAt
            )
        }
    }
}
