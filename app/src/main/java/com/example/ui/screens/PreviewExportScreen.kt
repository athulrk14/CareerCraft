package com.example.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TemplateStyle
import com.example.service.PdfGeneratorService
import com.example.ui.ResumeViewModel
import com.example.ui.components.LaTeXStudioDialog
import com.example.ui.theme.ExecutiveNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TechTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PreviewExportScreen(viewModel: ResumeViewModel) {
    val resume by viewModel.activeResume.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val pdfService = remember { PdfGeneratorService(context) }
    val atsBreakdown = remember(resume) { viewModel.calculateAtsBreakdown() }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRenderingPreview by remember { mutableStateOf(true) }
    var showLatexDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Regenerate preview when resume or template changes
    LaunchedEffect(resume) {
        isRenderingPreview = true
        scope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                pdfService.generatePreviewBitmap(resume, scale = 1.3f)
            }
            previewBitmap = bitmap
            isRenderingPreview = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Template Selector Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Style, contentDescription = null, tint = TechTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Professional Templates",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TemplateStyle.values().toList()) { style ->
                            val isSelected = resume.templateStyle == style
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setTemplateStyle(style) },
                                label = {
                                    Text(
                                        text = "${style.displayName} (${style.atsScore}%)",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ExecutiveNavy,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("template_chip_${style.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = resume.templateStyle.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Export Actions Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (activity != null) {
                                pdfService.printResume(activity, resume)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExecutiveNavy,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_save_pdf_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print / Save PDF", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { pdfService.sharePdf(resume) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("share_pdf_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showLatexDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("open_latex_studio_btn")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LaTeX", fontSize = 12.sp)
                    }
                }
            }
        }

        // Live PDF Preview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Vector Document Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Page 1 of 1 • A4 Standard",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRenderingPreview || previewBitmap == null) {
                            Column(
                                modifier = Modifier.padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = TechTeal)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Rendering high-res vector preview...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "Resume PDF Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }

        // ATS Parseability Audit Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATS Compatibility Audit",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = "${atsBreakdown.totalScore}/100",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (atsBreakdown.totalScore >= 75) SuccessGreen else TechTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { atsBreakdown.totalScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (atsBreakdown.totalScore >= 75) SuccessGreen else TechTeal,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScoreCategoryItem(title = "Contact Info", score = atsBreakdown.contactScore, max = 20)
                        ScoreCategoryItem(title = "Summary", score = atsBreakdown.summaryScore, max = 20)
                        ScoreCategoryItem(title = "Metrics", score = atsBreakdown.experienceMetricsScore, max = 25)
                        ScoreCategoryItem(title = "Skills", score = atsBreakdown.skillsScore, max = 20)
                        ScoreCategoryItem(title = "Education", score = atsBreakdown.educationScore, max = 15)
                    }

                    if (atsBreakdown.suggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Suggestions to improve ATS match rate:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        atsBreakdown.suggestions.forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = TechTeal)
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLatexDialog) {
        LaTeXStudioDialog(
            resume = resume,
            onDismiss = { showLatexDialog = false }
        )
    }
}

@Composable
fun ScoreCategoryItem(title: String, score: Int, max: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$score/$max", style = MaterialTheme.typography.labelMedium, color = ExecutiveNavy)
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
    }
}
