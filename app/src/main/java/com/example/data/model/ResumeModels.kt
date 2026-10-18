package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class TemplateStyle(
    val displayName: String,
    val description: String,
    val atsScore: Int,
    val category: String,
    val idealFor: String
) {
    MODERN_EXECUTIVE(
        displayName = "Modern Executive",
        description = "Polished navy accents, elegant headers, and executive presence",
        atsScore = 95,
        category = "Executive",
        idealFor = "Tech Leads, Directors, Staff Engineers"
    ),
    TECH_ATS(
        displayName = "Tech ATS Single-Column",
        description = "100% ATS-parseable single column layout, optimal keyword scanning",
        atsScore = 99,
        category = "Single-Column",
        idealFor = "Software Engineers, Cloud & DevOps"
    ),
    MINIMAL_CLEAN(
        displayName = "Minimalist Monochrome",
        description = "Understated Scandinavian typography with sharp, clean dividers",
        atsScore = 98,
        category = "Design & Tech",
        idealFor = "Product Designers, Full-Stack Developers"
    ),
    COMPACT_ONE_PAGER(
        displayName = "Compact One-Pager",
        description = "High-density formatting engineered to fit extensive experience on a single page",
        atsScore = 97,
        category = "High-Density",
        idealFor = "Competitive Tech Roles, Early to Mid Career"
    ),
    FEDERAL_ATS(
        displayName = "Modern Federal ATS",
        description = "Strict chronological government & enterprise standard layout with maximum compliance",
        atsScore = 100,
        category = "Federal & Enterprise",
        idealFor = "Defense, Federal Govt, Large Enterprises"
    )
}

@JsonClass(generateAdapter = true)
data class ExperienceItem(
    val id: String = UUID.randomUUID().toString(),
    val company: String = "",
    val role: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val bullets: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class EducationItem(
    val id: String = UUID.randomUUID().toString(),
    val institution: String = "",
    val degree: String = "",
    val fieldOfStudy: String = "",
    val graduationYear: String = "",
    val gpaOrHonors: String = ""
)

@JsonClass(generateAdapter = true)
data class SkillCategory(
    val id: String = UUID.randomUUID().toString(),
    val categoryName: String = "",
    val skills: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val techStack: String = "",
    val link: String = ""
)

@JsonClass(generateAdapter = true)
data class CertificationItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val issuer: String = "",
    val year: String = ""
)

@JsonClass(generateAdapter = true)
data class Resume(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "My Professional Resume",
    val targetRole: String = "Software Engineer",
    val templateStyle: TemplateStyle = TemplateStyle.MODERN_EXECUTIVE,
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val website: String = "",
    val linkedin: String = "",
    val github: String = "",
    val summary: String = "",
    val experiences: List<ExperienceItem> = emptyList(),
    val educations: List<EducationItem> = emptyList(),
    val skillCategories: List<SkillCategory> = emptyList(),
    val projects: List<ProjectItem> = emptyList(),
    val certifications: List<CertificationItem> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ApplicationStatus(val label: String) {
    SAVED("Saved"),
    APPLIED("Applied"),
    INTERVIEWING("Interviewing"),
    OFFERED("Offered"),
    REJECTED("Archived")
}

@JsonClass(generateAdapter = true)
data class JobApplication(
    val id: String = UUID.randomUUID().toString(),
    val company: String = "",
    val position: String = "",
    val location: String = "",
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val salary: String = "",
    val appliedDate: String = "",
    val interviewDate: String = "",
    val notes: String = "",
    val linkedResumeId: String? = null,
    val jobPostingUrl: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class JobTailorMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean = false,
    val text: String = "",
    val suggestedBullets: List<String> = emptyList(),
    val matchedKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val matchScore: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
