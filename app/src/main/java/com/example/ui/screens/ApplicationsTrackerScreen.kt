package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ApplicationStatus
import com.example.data.model.JobApplication
import com.example.ui.ResumeViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ExecutiveNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechTeal
import java.util.UUID

@Composable
fun ApplicationsTrackerScreen(viewModel: ResumeViewModel) {
    val applications by viewModel.applications.collectAsState()
    val activeFilter by viewModel.appFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingApp by remember { mutableStateOf<JobApplication?>(null) }

    val filteredApps = remember(applications, activeFilter) {
        if (activeFilter == null) applications else applications.filter { it.status == activeFilter }
    }

    val totalCount = applications.size
    val interviewCount = applications.count { it.status == ApplicationStatus.INTERVIEWING }
    val offerCount = applications.count { it.status == ApplicationStatus.OFFERED }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Pipeline Metric Summary Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Application Pipeline Overview",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(label = "Total Tracked", count = totalCount, color = ExecutiveNavy)
                            StatBox(label = "Interviewing", count = interviewCount, color = TechCyan)
                            StatBox(label = "Offers", count = offerCount, color = SuccessGreen)
                        }
                    }
                }
            }

            // Pipeline Stage Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { viewModel.setApplicationFilter(null) },
                        label = { Text("All ($totalCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExecutiveNavy,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_all")
                    )
                    ApplicationStatus.values().forEach { status ->
                        val count = applications.count { it.status == status }
                        FilterChip(
                            selected = activeFilter == status,
                            onClick = { viewModel.setApplicationFilter(status) },
                            label = { Text("${status.label} ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExecutiveNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_${status.name.lowercase()}")
                        )
                    }
                }
            }

            // List of Applications
            if (filteredApps.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Work, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No job applications found in this stage.", style = MaterialTheme.typography.bodyMedium)
                            Text("Tap '+ New Job' to log an application.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(filteredApps, key = { it.id }) { app ->
                    JobApplicationCard(
                        application = app,
                        onStatusChange = { newStatus -> viewModel.updateApplicationStatus(app, newStatus) },
                        onEdit = {
                            editingApp = app
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteJobApplication(app.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                editingApp = null
                showAddDialog = true
            },
            containerColor = ExecutiveNavy,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_application_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Add Job")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Job")
            }
        }
    }

    if (showAddDialog) {
        AddEditApplicationDialog(
            initialApp = editingApp,
            onDismiss = {
                showAddDialog = false
                editingApp = null
            },
            onSave = { savedApp ->
                viewModel.saveJobApplication(savedApp)
                showAddDialog = false
                editingApp = null
            }
        )
    }
}

@Composable
fun StatBox(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun JobApplicationCard(
    application: JobApplication,
    onStatusChange: (ApplicationStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    val statusColor = when (application.status) {
        ApplicationStatus.SAVED -> Color(0xFF64748B)
        ApplicationStatus.APPLIED -> TechCyan
        ApplicationStatus.INTERVIEWING -> AccentAmber
        ApplicationStatus.OFFERED -> SuccessGreen
        ApplicationStatus.REJECTED -> Color(0xFF94A3B8)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().testTag("job_card_${application.company.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.company,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = application.position,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechTeal
                    )
                }

                // Status Badge with menu
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clickable { showStatusMenu = true }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = application.status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        ApplicationStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.label) },
                                onClick = {
                                    onStatusChange(st)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (application.location.isNotEmpty()) {
                    Text(
                        text = "📍 ${application.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (application.salary.isNotEmpty()) {
                    Text(
                        text = "💰 ${application.salary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (application.interviewDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interview: ${application.interviewDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentAmber
                    )
                }
            }

            if (application.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = application.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditApplicationDialog(
    initialApp: JobApplication?,
    onDismiss: () -> Unit,
    onSave: (JobApplication) -> Unit
) {
    var company by remember { mutableStateOf(initialApp?.company ?: "") }
    var position by remember { mutableStateOf(initialApp?.position ?: "") }
    var location by remember { mutableStateOf(initialApp?.location ?: "") }
    var salary by remember { mutableStateOf(initialApp?.salary ?: "") }
    var appliedDate by remember { mutableStateOf(initialApp?.appliedDate ?: "Oct 18, 2026") }
    var interviewDate by remember { mutableStateOf(initialApp?.interviewDate ?: "") }
    var notes by remember { mutableStateOf(initialApp?.notes ?: "") }
    var status by remember { mutableStateOf(initialApp?.status ?: ApplicationStatus.APPLIED) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialApp == null) "Log New Application" else "Edit Application",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth().testTag("app_input_company"),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Job Position / Title") },
                    modifier = Modifier.fillMaxWidth().testTag("app_input_position"),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = salary,
                        onValueChange = { salary = it },
                        label = { Text("Salary / Range") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = appliedDate,
                        onValueChange = { appliedDate = it },
                        label = { Text("Applied Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = interviewDate,
                        onValueChange = { interviewDate = it },
                        label = { Text("Interview Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Interview Notes / Follow-up") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val app = (initialApp ?: JobApplication(id = UUID.randomUUID().toString())).copy(
                            company = company,
                            position = position,
                            location = location,
                            salary = salary,
                            appliedDate = appliedDate,
                            interviewDate = interviewDate,
                            notes = notes,
                            status = status
                        )
                        onSave(app)
                    },
                    enabled = company.isNotBlank() && position.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_application_submit_btn")
                ) {
                    Text("Save Job Application")
                }
            }
        }
    }
}
