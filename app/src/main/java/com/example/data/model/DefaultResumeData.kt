package com.example.data.model

import java.util.UUID

object DefaultResumeData {
    fun createSampleResume(): Resume {
        return Resume(
            id = "default-sample-resume-id",
            title = "Executive Software Architect",
            targetRole = "Staff Software Engineer / Solutions Architect",
            templateStyle = TemplateStyle.MODERN_EXECUTIVE,
            fullName = "Alexander Mitchell",
            jobTitle = "Staff Cloud Architect & Systems Engineer",
            email = "alex.mitchell@careertech.dev",
            phone = "+1 (415) 890-2341",
            location = "San Francisco, CA",
            website = "https://alexmitchell.tech",
            linkedin = "linkedin.com/in/alexandermitchell-eng",
            github = "github.com/amitchell-systems",
            summary = "Pragmatic Staff Systems Engineer with 8+ years leading cloud architecture, distributed systems, and real-time data pipelines. Proven track record scaling platforms to 10M+ daily active users while reducing cloud infrastructure expenditure by 32%. Passionate about ATS-friendly engineering leadership, resilient microservices, and high-velocity engineering cultures.",
            experiences = listOf(
                ExperienceItem(
                    id = UUID.randomUUID().toString(),
                    company = "Nexus Cloud Technologies",
                    role = "Lead Distributed Systems Architect",
                    location = "San Francisco, CA",
                    startDate = "Jan 2022",
                    endDate = "Present",
                    isCurrent = true,
                    bullets = listOf(
                        "Architected event-driven microservices processing 45,000 requests/sec with p99 latency under 22ms using Kotlin, gRPC, and Kafka.",
                        "Spearheaded multi-region cloud migration across AWS and GCP, eliminating single-point failures and driving 99.995% service uptime.",
                        "Mentored 14 senior engineers across distributed teams, standardizing telemetry dashboards and reducing Mean-Time-To-Detect (MTTD) incidents by 45%."
                    )
                ),
                ExperienceItem(
                    id = UUID.randomUUID().toString(),
                    company = "Aura Data Systems",
                    role = "Senior Backend Engineer",
                    location = "Seattle, WA",
                    startDate = "Jun 2018",
                    endDate = "Dec 2021",
                    isCurrent = false,
                    bullets = listOf(
                        "Designed and deployed automated database sharding for PostgreSQL clusters, cutting query response times by 55% across 8TB of analytics data.",
                        "Constructed continuous deployment pipelines with Terraform and GitHub Actions, accelerating release cycles from bi-weekly to 4 deployments daily.",
                        "Authored core OAuth2 and RBAC security frameworks adhering to SOC2 Type II compliance standards."
                    )
                )
            ),
            educations = listOf(
                EducationItem(
                    id = UUID.randomUUID().toString(),
                    institution = "University of Washington",
                    degree = "Bachelor of Science",
                    fieldOfStudy = "Computer Science & Software Engineering",
                    graduationYear = "2018",
                    gpaOrHonors = "Summa Cum Laude (GPA 3.92/4.0)"
                )
            ),
            skillCategories = listOf(
                SkillCategory(
                    id = UUID.randomUUID().toString(),
                    categoryName = "Languages & Runtimes",
                    skills = listOf("Kotlin", "Java", "Go", "Python", "TypeScript", "SQL")
                ),
                SkillCategory(
                    id = UUID.randomUUID().toString(),
                    categoryName = "Cloud & Distributed Systems",
                    skills = listOf("AWS (ECS, EKS, RDS)", "GCP", "Kubernetes", "Kafka", "Docker", "gRPC", "Redis")
                ),
                SkillCategory(
                    id = UUID.randomUUID().toString(),
                    categoryName = "Methodologies & Tools",
                    skills = listOf("CI/CD Automation", "Terraform", "System Architecture", "ATS Optimization", "Distributed Tracing")
                )
            ),
            projects = listOf(
                ProjectItem(
                    id = UUID.randomUUID().toString(),
                    name = "Chronos: Distributed Task Orchestrator",
                    description = "High-availability task scheduling engine with zero-downtime leader election and raft consensus.",
                    techStack = "Go, Raft, gRPC, Prometheus",
                    link = "github.com/amitchell-systems/chronos"
                )
            ),
            certifications = listOf(
                CertificationItem(
                    id = UUID.randomUUID().toString(),
                    name = "AWS Certified Solutions Architect – Professional",
                    issuer = "Amazon Web Services",
                    year = "2023"
                ),
                CertificationItem(
                    id = UUID.randomUUID().toString(),
                    name = "Certified Kubernetes Administrator (CKA)",
                    issuer = "Cloud Native Computing Foundation",
                    year = "2022"
                )
            )
        )
    }

    fun createSampleApplications(): List<JobApplication> {
        return listOf(
            JobApplication(
                id = UUID.randomUUID().toString(),
                company = "Stripe",
                position = "Staff Infrastructure Engineer",
                location = "San Francisco, CA (Hybrid)",
                status = ApplicationStatus.INTERVIEWING,
                salary = "$240,000 - $280,000",
                appliedDate = "Oct 10, 2026",
                interviewDate = "Oct 24, 2026",
                notes = "Technical system design round scheduled with engineering director. Focus on payments idempotency and high-concurrency ledger design.",
                linkedResumeId = "default-sample-resume-id",
                jobPostingUrl = "https://stripe.com/jobs/staff-infrastructure"
            ),
            JobApplication(
                id = UUID.randomUUID().toString(),
                company = "Datadog",
                position = "Principal Cloud Platform Architect",
                location = "Remote",
                status = ApplicationStatus.APPLIED,
                salary = "$250,000 - $290,000",
                appliedDate = "Oct 15, 2026",
                interviewDate = "",
                notes = "Submitted resume tailored for distributed metrics ingestion and Kubernetes operator management.",
                linkedResumeId = "default-sample-resume-id",
                jobPostingUrl = "https://datadoghq.com/careers"
            ),
            JobApplication(
                id = UUID.randomUUID().toString(),
                company = "Airbnb",
                position = "Staff Software Engineer, Core Services",
                location = "San Francisco, CA",
                status = ApplicationStatus.OFFERED,
                salary = "$265,000 + Equity",
                appliedDate = "Sep 28, 2026",
                interviewDate = "Completed",
                notes = "Offer received! Reviewing equity grant schedule and remote flexibility terms.",
                linkedResumeId = "default-sample-resume-id",
                jobPostingUrl = "https://careers.airbnb.com"
            )
        )
    }
}
