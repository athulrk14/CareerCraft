package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EducationItem
import com.example.data.model.ExperienceItem
import com.example.data.model.ProjectItem
import com.example.data.model.SkillCategory
import com.example.ui.ResumeViewModel
import com.example.ui.components.BulletOptimizationDialog
import com.example.ui.components.LinkedInImportDialog
import com.example.ui.theme.ExecutiveNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TechTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeEditorScreen(viewModel: ResumeViewModel) {
    val resume by viewModel.activeResume.collectAsState()
    val allResumes by viewModel.resumes.collectAsState()
    val isAutoSaving by viewModel.isAutoSaving.collectAsState()
    val atsBreakdown = remember(resume) { viewModel.calculateAtsBreakdown() }

    var showLinkedInDialog by remember { mutableStateOf(false) }
    val isImportingLinkedIn by viewModel.isImportingLinkedIn.collectAsState()
    val linkedInStatusMessage by viewModel.linkedInStatusMessage.collectAsState()

    val isOptimizingBullet by viewModel.isOptimizingBullet.collectAsState()
    val activeOptimization by viewModel.activeOptimization.collectAsState()

    var showResumeMenu by remember { mutableStateOf(false) }

    // Section collapse states
    var personalExpanded by remember { mutableStateOf(true) }
    var summaryExpanded by remember { mutableStateOf(true) }
    var expExpanded by remember { mutableStateOf(true) }
    var skillsExpanded by remember { mutableStateOf(true) }
    var eduExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Top Bar: Resume Switcher & Auto-save Status
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showResumeMenu = true }
                                    .testTag("resume_selector_dropdown")
                            ) {
                                Column {
                                    Text(
                                        text = resume.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = resume.targetRole.ifEmpty { "Target Role" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TechTeal
                                    )
                                }
                                Icon(Icons.Default.ExpandMore, contentDescription = "Select Resume")
                            }

                            DropdownMenu(
                                expanded = showResumeMenu,
                                onDismissRequest = { showResumeMenu = false }
                            ) {
                                allResumes.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r.title) },
                                        onClick = {
                                            viewModel.selectResume(r)
                                            showResumeMenu = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ Create New Resume") },
                                    onClick = {
                                        viewModel.createNewResume("Resume ${allResumes.size + 1}")
                                        showResumeMenu = false
                                    }
                                )
                            }
                        }

                        // Auto-Save Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (isAutoSaving) {
                                CircularProgressIndicator(
                                    color = TechTeal,
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Saving...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Saved",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ATS Score Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ExecutiveNavy.copy(alpha = 0.08f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (atsBreakdown.totalScore >= 75) SuccessGreen else TechTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${atsBreakdown.totalScore}%",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ATS Readiness Score",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (atsBreakdown.totalScore >= 80) "Optimized for keyword parsers" else "Add metrics & keywords to reach 80%+",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // LinkedIn Import Quick Button
                        OutlinedButton(
                            onClick = { showLinkedInDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_linkedin_import_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TechTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LinkedIn Import", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Section 1: Personal & Contact Information
        item {
            CollapsibleSectionCard(
                title = "Contact & Personal Information",
                subtitle = "Name, title, email, phone, and links",
                isExpanded = personalExpanded,
                onToggle = { personalExpanded = !personalExpanded }
            ) {
                OutlinedTextField(
                    value = resume.fullName,
                    onValueChange = { viewModel.updatePersonalInfo(it, resume.jobTitle, resume.email, resume.phone, resume.location, resume.website, resume.linkedin, resume.github, resume.summary) },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_full_name"),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = resume.jobTitle,
                        onValueChange = { viewModel.updatePersonalInfo(resume.fullName, it, resume.email, resume.phone, resume.location, resume.website, resume.linkedin, resume.github, resume.summary) },
                        label = { Text("Current / Target Title") },
                        modifier = Modifier.weight(1f).testTag("input_job_title"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = resume.targetRole,
                        onValueChange = { viewModel.updateActiveResume { r -> r.copy(targetRole = it) } },
                        label = { Text("Target Industry Role") },
                        modifier = Modifier.weight(1f).testTag("input_target_role"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = resume.email,
                        onValueChange = { viewModel.updatePersonalInfo(resume.fullName, resume.jobTitle, it, resume.phone, resume.location, resume.website, resume.linkedin, resume.github, resume.summary) },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f).testTag("input_email"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = resume.phone,
                        onValueChange = { viewModel.updatePersonalInfo(resume.fullName, resume.jobTitle, resume.email, it, resume.location, resume.website, resume.linkedin, resume.github, resume.summary) },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f).testTag("input_phone"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = resume.location,
                        onValueChange = { viewModel.updatePersonalInfo(resume.fullName, resume.jobTitle, resume.email, resume.phone, it, resume.website, resume.linkedin, resume.github, resume.summary) },
                        label = { Text("Location (e.g. San Francisco, CA)") },
                        modifier = Modifier.weight(1f).testTag("input_location"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = resume.linkedin,
                        onValueChange = { viewModel.updatePersonalInfo(resume.fullName, resume.jobTitle, resume.email, resume.phone, resume.location, resume.website, it, resume.github, resume.summary) },
                        label = { Text("LinkedIn URL / Handle") },
                        modifier = Modifier.weight(1f).testTag("input_linkedin"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Section 2: Executive Summary
        item {
            CollapsibleSectionCard(
                title = "Executive Summary",
                subtitle = "Hook recruiters with your top achievements & scope",
                isExpanded = summaryExpanded,
                onToggle = { summaryExpanded = !summaryExpanded }
            ) {
                OutlinedTextField(
                    value = resume.summary,
                    onValueChange = { viewModel.updatePersonalInfo(resume.fullName, resume.jobTitle, resume.email, resume.phone, resume.location, resume.website, resume.linkedin, resume.github, it) },
                    label = { Text("Professional Summary") },
                    placeholder = { Text("3-4 sentences summarizing leadership, engineering scale, and career achievements...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("input_summary"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Section 3: Work Experience with Inline AI Optimization
        item {
            CollapsibleSectionCard(
                title = "Work Experience",
                subtitle = "${resume.experiences.size} roles with real-time AI bullet optimization",
                isExpanded = expExpanded,
                onToggle = { expExpanded = !expExpanded }
            ) {
                resume.experiences.forEach { exp ->
                    ExperienceCard(
                        experience = exp,
                        onUpdate = { viewModel.updateExperience(exp.id, it) },
                        onDelete = { viewModel.removeExperience(exp.id) },
                        onAddBullet = { viewModel.addExperienceBullet(exp.id) },
                        onUpdateBullet = { index, text -> viewModel.updateExperienceBullet(exp.id, index, text) },
                        onRemoveBullet = { index -> viewModel.removeExperienceBullet(exp.id, index) },
                        onOptimizeBullet = { index, text -> viewModel.requestBulletOptimization(exp.id, index, text) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = { viewModel.addExperience() },
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_experience_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Work Experience")
                }
            }
        }

        // Section 4: Technical Skills
        item {
            CollapsibleSectionCard(
                title = "Technical Skills & Competencies",
                subtitle = "${resume.skillCategories.size} skill categories configured",
                isExpanded = skillsExpanded,
                onToggle = { skillsExpanded = !skillsExpanded }
            ) {
                resume.skillCategories.forEach { cat ->
                    SkillCategoryCard(
                        category = cat,
                        onUpdate = { viewModel.updateSkillCategory(cat.id, it) },
                        onDelete = { viewModel.removeSkillCategory(cat.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { viewModel.addSkillCategory("Tools & Frameworks", listOf("Docker", "CI/CD", "Git")) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_skill_category_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Skill Category")
                }
            }
        }

        // Section 5: Education
        item {
            CollapsibleSectionCard(
                title = "Education & Credentials",
                subtitle = "${resume.educations.size} degrees and universities",
                isExpanded = eduExpanded,
                onToggle = { eduExpanded = !eduExpanded }
            ) {
                resume.educations.forEach { edu ->
                    EducationCard(
                        education = edu,
                        onUpdate = { viewModel.updateEducation(edu.id, it) },
                        onDelete = { viewModel.removeEducation(edu.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { viewModel.addEducation() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_education_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Education")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // LinkedIn Import Dialog
    if (showLinkedInDialog) {
        LinkedInImportDialog(
            isLoading = isImportingLinkedIn,
            statusMessage = linkedInStatusMessage,
            onDismiss = {
                showLinkedInDialog = false
                viewModel.clearLinkedInMessage()
            },
            onImport = { rawText ->
                viewModel.importLinkedInProfile(rawText)
            }
        )
    }

    // AI Bullet Optimization Dialog
    if (isOptimizingBullet || activeOptimization != null) {
        BulletOptimizationDialog(
            isLoading = isOptimizingBullet,
            result = activeOptimization,
            onDismiss = { viewModel.dismissBulletOptimization() },
            onApply = { chosen -> viewModel.applyOptimizedBullet(chosen) }
        )
    }
}

@Composable
fun CollapsibleSectionCard(
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Section"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ExperienceCard(
    experience: ExperienceItem,
    onUpdate: (ExperienceItem) -> Unit,
    onDelete: () -> Unit,
    onAddBullet: () -> Unit,
    onUpdateBullet: (Int, String) -> Unit,
    onRemoveBullet: (Int) -> Unit,
    onOptimizeBullet: (Int, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = experience.company.ifEmpty { "New Company" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = experience.role,
                    onValueChange = { onUpdate(experience.copy(role = it)) },
                    label = { Text("Role / Job Title") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = experience.company,
                    onValueChange = { onUpdate(experience.copy(company = it)) },
                    label = { Text("Company Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = experience.startDate,
                    onValueChange = { onUpdate(experience.copy(startDate = it)) },
                    label = { Text("Start Date") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = experience.endDate,
                    onValueChange = { onUpdate(experience.copy(endDate = it)) },
                    label = { Text("End Date") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Key Achievements & Bullets:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            experience.bullets.forEachIndexed { idx, bullet ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = bullet,
                            onValueChange = { onUpdateBullet(idx, it) },
                            placeholder = { Text("Accomplished [X] as measured by [Y] by doing [Z]...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = { onRemoveBullet(idx) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Bullet", modifier = Modifier.size(16.dp))
                        }
                    }
                    // Inline AI Optimize Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TechTeal.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOptimizeBullet(idx, bullet) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("optimize_bullet_inline_btn_$idx")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = TechTeal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Optimize with AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TechTeal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onAddBullet,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Bullet", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SkillCategoryCard(
    category: SkillCategory,
    onUpdate: (SkillCategory) -> Unit,
    onDelete: () -> Unit
) {
    var newSkillText by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = category.categoryName,
                    onValueChange = { onUpdate(category.copy(categoryName = it)) },
                    label = { Text("Category Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Skills chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                category.skills.take(6).forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ExecutiveNavy.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = skill,
                            style = MaterialTheme.typography.labelSmall,
                            color = ExecutiveNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSkillText,
                    onValueChange = { newSkillText = it },
                    placeholder = { Text("e.g. Kotlin, Docker, AWS") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        if (newSkillText.isNotBlank()) {
                            onUpdate(category.copy(skills = category.skills + newSkillText.trim()))
                            newSkillText = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun EducationCard(
    education: EducationItem,
    onUpdate: (EducationItem) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = education.degree.ifEmpty { "New Degree" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = education.institution,
                    onValueChange = { onUpdate(education.copy(institution = it)) },
                    label = { Text("University / College") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = education.degree,
                    onValueChange = { onUpdate(education.copy(degree = it)) },
                    label = { Text("Degree (e.g. BS, MS)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = education.fieldOfStudy,
                    onValueChange = { onUpdate(education.copy(fieldOfStudy = it)) },
                    label = { Text("Field of Study") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = education.graduationYear,
                    onValueChange = { onUpdate(education.copy(graduationYear = it)) },
                    label = { Text("Graduation Year") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}
