package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Resume
import com.example.data.model.TemplateStyle
import com.example.service.PdfGeneratorService
import com.example.ui.AppTab
import com.example.ui.ResumeViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ExecutiveNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TemplatesDashboardScreen(viewModel: ResumeViewModel) {
    val context = LocalContext.current
    val resume by viewModel.activeResume.collectAsState()
    val pdfService = remember { PdfGeneratorService(context) }
    val scope = rememberCoroutineScope()

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Cached previews for all 5 templates
    val previewBitmaps = remember { mutableStateMapOf<TemplateStyle, Bitmap>() }
    var isGeneratingPreviews by remember { mutableStateOf(false) }

    LaunchedEffect(resume.id) {
        isGeneratingPreviews = true
        scope.launch {
            TemplateStyle.values().forEach { style ->
                val sample = resume.copy(templateStyle = style)
                val bmp = withContext(Dispatchers.Default) {
                    pdfService.generatePreviewBitmap(sample, scale = 0.72f)
                }
                previewBitmaps[style] = bmp
            }
            isGeneratingPreviews = false
        }
    }

    val allStyles = TemplateStyle.values().toList()
    val filteredStyles = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == null) allStyles else allStyles.filter { it.category == selectedCategoryFilter }
    }

    val categories = listOf("Executive", "Single-Column", "High-Density", "Design & Tech", "Federal & Enterprise")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Dashboard Hero Header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TechTeal.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DashboardCustomize,
                                    contentDescription = null,
                                    tint = TechTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ATS-Friendly Templates Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ExecutiveNavy
                            )
                            Text(
                                text = "Standard parser geometries for Workday, Taleo, Greenhouse & Lever",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key ATS Safeguards Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExecutiveNavy.copy(alpha = 0.05f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AtsFeaturePill(icon = Icons.Default.CheckCircle, label = "Zero Tables")
                        AtsFeaturePill(icon = Icons.Default.Verified, label = "Standard Headers")
                        AtsFeaturePill(icon = Icons.Default.Security, label = "95-100% Score")
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All Templates (${allStyles.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExecutiveNavy,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("template_filter_all")
                    )
                }
                items(categories) { cat ->
                    val count = allStyles.count { it.category == cat }
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text("$cat ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExecutiveNavy,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("template_filter_${cat.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // Gallery Template Cards
        items(filteredStyles, key = { it.name }) { style ->
            val isActive = resume.templateStyle == style
            val previewBitmap = previewBitmaps[style]

            TemplateGalleryCard(
                style = style,
                isActive = isActive,
                previewBitmap = previewBitmap,
                isLoading = isGeneratingPreviews && previewBitmap == null,
                onApply = {
                    viewModel.setTemplateStyle(style)
                    Toast.makeText(context, "Applied ${style.displayName}!", Toast.LENGTH_SHORT).show()
                },
                onViewInPreview = {
                    viewModel.setTemplateStyle(style)
                    viewModel.setTab(AppTab.PREVIEW_EXPORT)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AtsFeaturePill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TechTeal, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = ExecutiveNavy,
            fontSize = 11.sp
        )
    }
}

@Composable
fun TemplateGalleryCard(
    style: TemplateStyle,
    isActive: Boolean,
    previewBitmap: Bitmap?,
    isLoading: Boolean,
    onApply: () -> Unit,
    onViewInPreview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isActive) 6.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) Modifier.border(2.dp, TechTeal, RoundedCornerShape(16.dp)) else Modifier
            )
            .testTag("template_card_${style.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Template Name & ATS Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TechTeal.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = TechTeal, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Active", style = MaterialTheme.typography.labelSmall, color = TechTeal, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    Text(
                        text = style.idealFor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }

                // ATS Score Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (style.atsScore >= 98) SuccessGreen.copy(alpha = 0.15f) else TechTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${style.atsScore}% ATS Pass",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (style.atsScore >= 98) SuccessGreen else TechTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = style.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Live Vector Preview Miniature
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading || previewBitmap == null) {
                    CircularProgressIndicator(color = TechTeal, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                } else {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "${style.displayName} preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isActive) {
                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExecutiveNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_template_btn_${style.name.lowercase()}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Template", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onViewInPreview,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("customize_preview_btn_${style.name.lowercase()}")
                    ) {
                        Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Customize & Export", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
