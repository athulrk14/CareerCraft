package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ApplicationsTrackerScreen
import com.example.ui.screens.JobTailorScreen
import com.example.ui.screens.PreviewExportScreen
import com.example.ui.screens.ResumeEditorScreen
import com.example.ui.screens.TemplatesDashboardScreen
import com.example.ui.theme.ExecutiveNavy
import com.example.ui.theme.TechTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerCraftApp(viewModel: ResumeViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    // Handle back button on secondary tabs to navigate back to Resume Editor
    BackHandler(enabled = currentTab != AppTab.RESUME_EDITOR) {
        viewModel.setTab(AppTab.RESUME_EDITOR)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TechTeal,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CareerCraft",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExecutiveNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("app_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.RESUME_EDITOR,
                    onClick = { viewModel.setTab(AppTab.RESUME_EDITOR) },
                    icon = {
                        Icon(
                            if (currentTab == AppTab.RESUME_EDITOR) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                            contentDescription = "Editor"
                        )
                    },
                    label = { Text("Resume", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ExecutiveNavy,
                        selectedTextColor = ExecutiveNavy,
                        indicatorColor = TechTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_resume")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TEMPLATES,
                    onClick = { viewModel.setTab(AppTab.TEMPLATES) },
                    icon = {
                        Icon(
                            if (currentTab == AppTab.TEMPLATES) Icons.Filled.DashboardCustomize else Icons.Outlined.DashboardCustomize,
                            contentDescription = "Templates"
                        )
                    },
                    label = { Text("Templates", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ExecutiveNavy,
                        selectedTextColor = ExecutiveNavy,
                        indicatorColor = TechTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_templates")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.PREVIEW_EXPORT,
                    onClick = { viewModel.setTab(AppTab.PREVIEW_EXPORT) },
                    icon = {
                        Icon(
                            if (currentTab == AppTab.PREVIEW_EXPORT) Icons.Filled.Preview else Icons.Outlined.Preview,
                            contentDescription = "Preview"
                        )
                    },
                    label = { Text("PDF & LaTeX", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ExecutiveNavy,
                        selectedTextColor = ExecutiveNavy,
                        indicatorColor = TechTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_preview")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.JOB_TAILOR,
                    onClick = { viewModel.setTab(AppTab.JOB_TAILOR) },
                    icon = {
                        Icon(
                            if (currentTab == AppTab.JOB_TAILOR) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Job Tailor"
                        )
                    },
                    label = { Text("Job Tailor AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ExecutiveNavy,
                        selectedTextColor = ExecutiveNavy,
                        indicatorColor = TechTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_job_tailor")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.APPLICATIONS,
                    onClick = { viewModel.setTab(AppTab.APPLICATIONS) },
                    icon = {
                        Icon(
                            if (currentTab == AppTab.APPLICATIONS) Icons.Default.Assignment else Icons.Outlined.Assignment,
                            contentDescription = "Tracker"
                        )
                    },
                    label = { Text("Tracker", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ExecutiveNavy,
                        selectedTextColor = ExecutiveNavy,
                        indicatorColor = TechTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_tracker")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.RESUME_EDITOR -> ResumeEditorScreen(viewModel = viewModel)
                AppTab.TEMPLATES -> TemplatesDashboardScreen(viewModel = viewModel)
                AppTab.PREVIEW_EXPORT -> PreviewExportScreen(viewModel = viewModel)
                AppTab.JOB_TAILOR -> JobTailorScreen(viewModel = viewModel)
                AppTab.APPLICATIONS -> ApplicationsTrackerScreen(viewModel = viewModel)
            }
        }
    }
}
