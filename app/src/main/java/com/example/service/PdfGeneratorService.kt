package com.example.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.Resume
import com.example.data.model.TemplateStyle
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

class PdfGeneratorService(private val context: Context) {

    companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
    }

    /**
     * Generates a multi-page vector PDF document representing the resume.
     */
    fun generatePdfFile(resume: Resume): File {
        val pdfDocument = PdfDocument()
        renderPages(pdfDocument, resume)

        val outputFile = File(context.cacheDir, "resume_${resume.id.take(8)}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return outputFile
    }

    /**
     * Generates a crisp Bitmap representation of Page 1 for real-time Compose preview.
     */
    fun generatePreviewBitmap(resume: Resume, scale: Float = 1.5f): Bitmap {
        val width = (PAGE_WIDTH * scale).toInt()
        val height = (PAGE_HEIGHT * scale).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(scale, scale)

        // Draw White Background
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Render page 1
        val drawer = ResumeTemplateDrawer(canvas, PAGE_WIDTH, PAGE_HEIGHT, resume)
        drawer.drawTemplate()

        return bitmap
    }

    /**
     * Launches Android's native PrintManager to print or save the resume as PDF.
     */
    fun printResume(activity: Activity, resume: Resume) {
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "${resume.fullName.ifEmpty { "Resume" }}_CareerCraft"

        printManager.print(
            jobName,
            object : PrintDocumentAdapter() {
                private var pdfFile: File? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }

                    pdfFile = generatePdfFile(resume)

                    val info = PrintDocumentInfo.Builder("$jobName.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()

                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    val file = pdfFile ?: generatePdfFile(resume)
                    try {
                        FileInputStream(file).use { input ->
                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            },
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )
    }

    /**
     * Shares the PDF file via Android's ACTION_SEND Intent.
     */
    fun sharePdf(resume: Resume) {
        val file = generatePdfFile(resume)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${resume.fullName} - Resume")
            putExtra(Intent.EXTRA_TEXT, "Attached is the professional resume for ${resume.fullName}.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Resume PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun renderPages(pdfDocument: PdfDocument, resume: Resume) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Draw resume
        val drawer = ResumeTemplateDrawer(canvas, PAGE_WIDTH, PAGE_HEIGHT, resume)
        drawer.drawTemplate()

        pdfDocument.finishPage(page)
    }
}

/**
 * Handles multi-template vector typography, layout geometry, and visual styling.
 */
internal class ResumeTemplateDrawer(
    private val canvas: Canvas,
    private val width: Int,
    private val height: Int,
    private val resume: Resume
) {
    // Colors
    private val colorNavy = Color.parseColor("#1E3A8A")
    private val colorSlateDark = Color.parseColor("#0F172A")
    private val colorSlateBody = Color.parseColor("#334155")
    private val colorSlateMuted = Color.parseColor("#64748B")
    private val colorTeal = Color.parseColor("#0D9488")
    private val colorDivider = Color.parseColor("#CBD5E1")

    fun drawTemplate() {
        when (resume.templateStyle) {
            TemplateStyle.MODERN_EXECUTIVE -> drawModernExecutive()
            TemplateStyle.TECH_ATS -> drawTechAts()
            TemplateStyle.MINIMAL_CLEAN -> drawMinimalClean()
            TemplateStyle.COMPACT_ONE_PAGER -> drawCompactOnePager()
            TemplateStyle.FEDERAL_ATS -> drawFederalAts()
        }
    }

    private fun drawModernExecutive() {
        var cursorY = 0f

        // Top Navy Header Banner
        val headerPaint = Paint().apply { color = colorNavy }
        canvas.drawRect(0f, 0f, width.toFloat(), 110f, headerPaint)

        // Candidate Name (Bold White)
        val namePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val name = resume.fullName.ifEmpty { "Your Name" }
        canvas.drawText(name, 36f, 48f, namePaint)

        // Job Title (Teal Highlight)
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#5EEAD4")
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val title = resume.jobTitle.ifEmpty { resume.targetRole.ifEmpty { "Professional Title" } }
        canvas.drawText(title, 36f, 68f, titlePaint)

        // Contact Information Row (White/Light Slate)
        val contactPaint = TextPaint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val contacts = listOfNotNull(
            resume.email.takeIf { it.isNotEmpty() },
            resume.phone.takeIf { it.isNotEmpty() },
            resume.location.takeIf { it.isNotEmpty() },
            resume.linkedin.takeIf { it.isNotEmpty() }
        ).joinToString("  •  ")
        canvas.drawText(contacts, 36f, 92f, contactPaint)

        cursorY = 135f
        val marginX = 36f
        val contentWidth = width - (marginX * 2)

        // Executive Summary
        if (resume.summary.isNotEmpty()) {
            cursorY = drawSectionHeader("PROFESSIONAL SUMMARY", cursorY, marginX, contentWidth, colorNavy, colorTeal)
            cursorY = drawParagraph(resume.summary, cursorY, marginX, contentWidth, 10f, colorSlateBody)
            cursorY += 12f
        }

        // Work Experience
        if (resume.experiences.isNotEmpty()) {
            cursorY = drawSectionHeader("WORK EXPERIENCE", cursorY, marginX, contentWidth, colorNavy, colorTeal)
            for (exp in resume.experiences) {
                if (cursorY > height - 60f) break
                cursorY = drawExperienceItem(exp, cursorY, marginX, contentWidth)
                cursorY += 8f
            }
            cursorY += 4f
        }

        // Technical Skills
        if (resume.skillCategories.isNotEmpty()) {
            if (cursorY < height - 70f) {
                cursorY = drawSectionHeader("CORE COMPETENCIES & TECHNICAL SKILLS", cursorY, marginX, contentWidth, colorNavy, colorTeal)
                for (cat in resume.skillCategories) {
                    if (cursorY > height - 40f) break
                    val catText = "${cat.categoryName}: ${cat.skills.joinToString(", ")}"
                    cursorY = drawBulletText(catText, cursorY, marginX, contentWidth, isSubBullet = false)
                }
                cursorY += 8f
            }
        }

        // Education
        if (resume.educations.isNotEmpty() && cursorY < height - 60f) {
            cursorY = drawSectionHeader("EDUCATION", cursorY, marginX, contentWidth, colorNavy, colorTeal)
            for (edu in resume.educations) {
                if (cursorY > height - 40f) break
                val eduHeader = "${edu.degree} in ${edu.fieldOfStudy}"
                val eduSub = "${edu.institution}  |  ${edu.graduationYear} ${if (edu.gpaOrHonors.isNotEmpty()) "  •  " + edu.gpaOrHonors else ""}"
                cursorY = drawBoldTitleWithRightText(eduHeader, edu.graduationYear, cursorY, marginX, contentWidth)
                cursorY = drawTextLine(eduSub, cursorY, marginX, 9.5f, colorSlateMuted)
                cursorY += 6f
            }
        }
    }

    private fun drawTechAts() {
        // 100% Single-Column Standard ATS Format
        var cursorY = 36f
        val marginX = 40f
        val contentWidth = width - (marginX * 2)

        // Candidate Name (Centered Bold)
        val namePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val name = resume.fullName.ifEmpty { "YOUR NAME" }
        canvas.drawText(name.uppercase(), width / 2f, cursorY, namePaint)
        cursorY += 18f

        // Contact Row (Centered)
        val contactPaint = TextPaint().apply {
            color = colorSlateBody
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val contacts = listOfNotNull(
            resume.email.takeIf { it.isNotEmpty() },
            resume.phone.takeIf { it.isNotEmpty() },
            resume.location.takeIf { it.isNotEmpty() },
            resume.linkedin.takeIf { it.isNotEmpty() },
            resume.github.takeIf { it.isNotEmpty() }
        ).joinToString(" | ")
        canvas.drawText(contacts, width / 2f, cursorY, contactPaint)
        cursorY += 24f

        // ATS Summary
        if (resume.summary.isNotEmpty()) {
            cursorY = drawAtsHeader("PROFESSIONAL SUMMARY", cursorY, marginX, contentWidth)
            cursorY = drawParagraph(resume.summary, cursorY, marginX, contentWidth, 9.5f, colorSlateBody)
            cursorY += 10f
        }

        // ATS Experience
        if (resume.experiences.isNotEmpty()) {
            cursorY = drawAtsHeader("PROFESSIONAL EXPERIENCE", cursorY, marginX, contentWidth)
            for (exp in resume.experiences) {
                if (cursorY > height - 60f) break
                val title = "${exp.role} - ${exp.company}"
                val dates = "${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}"
                cursorY = drawBoldTitleWithRightText(title, dates, cursorY, marginX, contentWidth)
                if (exp.location.isNotEmpty()) {
                    cursorY = drawTextLine(exp.location, cursorY, marginX, 9f, colorSlateMuted)
                }
                for (b in exp.bullets) {
                    if (cursorY > height - 35f) break
                    cursorY = drawBulletText(b, cursorY, marginX + 10f, contentWidth - 10f, isSubBullet = true)
                }
                cursorY += 6f
            }
        }

        // ATS Technical Skills
        if (resume.skillCategories.isNotEmpty() && cursorY < height - 60f) {
            cursorY = drawAtsHeader("TECHNICAL SKILLS", cursorY, marginX, contentWidth)
            for (cat in resume.skillCategories) {
                if (cursorY > height - 35f) break
                val line = "${cat.categoryName}: ${cat.skills.joinToString(", ")}"
                cursorY = drawBulletText(line, cursorY, marginX, contentWidth, isSubBullet = false)
            }
            cursorY += 8f
        }

        // ATS Education
        if (resume.educations.isNotEmpty() && cursorY < height - 60f) {
            cursorY = drawAtsHeader("EDUCATION", cursorY, marginX, contentWidth)
            for (edu in resume.educations) {
                if (cursorY > height - 35f) break
                val deg = "${edu.degree} in ${edu.fieldOfStudy}"
                cursorY = drawBoldTitleWithRightText(deg, edu.graduationYear, cursorY, marginX, contentWidth)
                cursorY = drawTextLine(edu.institution, cursorY, marginX, 9.5f, colorSlateMuted)
                cursorY += 4f
            }
        }
    }

    private fun drawMinimalClean() {
        var cursorY = 38f
        val marginX = 42f
        val contentWidth = width - (marginX * 2)

        // Name
        val namePaint = TextPaint().apply {
            color = Color.parseColor("#111827")
            textSize = 26f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(resume.fullName.ifEmpty { "Your Name" }, marginX, cursorY, namePaint)
        cursorY += 18f

        // Title
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText(resume.jobTitle.ifEmpty { resume.targetRole }, marginX, cursorY, titlePaint)
        cursorY += 14f

        // Contact info
        val contactPaint = TextPaint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val contacts = listOfNotNull(
            resume.email.takeIf { it.isNotEmpty() },
            resume.phone.takeIf { it.isNotEmpty() },
            resume.location.takeIf { it.isNotEmpty() }
        ).joinToString("  /  ")
        canvas.drawText(contacts, marginX, cursorY, contactPaint)
        cursorY += 18f

        // Divider
        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }
        canvas.drawLine(marginX, cursorY, marginX + contentWidth, cursorY, linePaint)
        cursorY += 18f

        // Summary
        if (resume.summary.isNotEmpty()) {
            cursorY = drawMinimalSectionHeader("ABOUT", cursorY, marginX)
            cursorY = drawParagraph(resume.summary, cursorY, marginX, contentWidth, 9.5f, colorSlateBody)
            cursorY += 14f
        }

        // Experience
        if (resume.experiences.isNotEmpty()) {
            cursorY = drawMinimalSectionHeader("EXPERIENCE", cursorY, marginX)
            for (exp in resume.experiences) {
                if (cursorY > height - 60f) break
                cursorY = drawBoldTitleWithRightText(
                    "${exp.role}, ${exp.company}",
                    "${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}",
                    cursorY, marginX, contentWidth
                )
                for (b in exp.bullets) {
                    if (cursorY > height - 35f) break
                    cursorY = drawBulletText(b, cursorY, marginX + 8f, contentWidth - 8f, isSubBullet = true)
                }
                cursorY += 8f
            }
        }

        // Skills
        if (resume.skillCategories.isNotEmpty() && cursorY < height - 60f) {
            cursorY = drawMinimalSectionHeader("SKILLS", cursorY, marginX)
            for (cat in resume.skillCategories) {
                if (cursorY > height - 35f) break
                cursorY = drawBulletText("${cat.categoryName}: ${cat.skills.joinToString(", ")}", cursorY, marginX, contentWidth, false)
            }
            cursorY += 10f
        }

        // Education
        if (resume.educations.isNotEmpty() && cursorY < height - 60f) {
            cursorY = drawMinimalSectionHeader("EDUCATION", cursorY, marginX)
            for (edu in resume.educations) {
                if (cursorY > height - 35f) break
                cursorY = drawBoldTitleWithRightText("${edu.degree}, ${edu.fieldOfStudy}", edu.graduationYear, cursorY, marginX, contentWidth)
                cursorY = drawTextLine(edu.institution, cursorY, marginX, 9f, colorSlateMuted)
                cursorY += 6f
            }
        }
    }

    private fun drawCompactOnePager() {
        var cursorY = 28f
        val marginX = 32f
        val contentWidth = width - (marginX * 2)

        // Compact Header: Left Name & Title, Right Contacts
        val namePaint = TextPaint().apply {
            color = colorNavy
            textSize = 19f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(resume.fullName.ifEmpty { "Candidate Name" }, marginX, cursorY, namePaint)

        val titlePaint = TextPaint().apply {
            color = colorTeal
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        cursorY += 13f
        canvas.drawText(resume.jobTitle.ifEmpty { resume.targetRole }, marginX, cursorY, titlePaint)

        // Compact Contact Line
        val contactPaint = TextPaint().apply {
            color = colorSlateMuted
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val contacts = listOfNotNull(
            resume.email.takeIf { it.isNotEmpty() },
            resume.phone.takeIf { it.isNotEmpty() },
            resume.location.takeIf { it.isNotEmpty() },
            resume.linkedin.takeIf { it.isNotEmpty() }
        ).joinToString("  |  ")
        cursorY += 12f
        canvas.drawText(contacts, marginX, cursorY, contactPaint)

        // Subtle divider
        cursorY += 6f
        val linePaint = Paint().apply {
            color = colorNavy
            strokeWidth = 1f
        }
        canvas.drawLine(marginX, cursorY, marginX + contentWidth, cursorY, linePaint)
        cursorY += 12f

        // Summary
        if (resume.summary.isNotEmpty()) {
            cursorY = drawCompactSectionHeader("EXECUTIVE PROFILE", cursorY, marginX, contentWidth)
            cursorY = drawParagraph(resume.summary, cursorY, marginX, contentWidth, 8.8f, colorSlateBody)
            cursorY += 6f
        }

        // Skills (Placed High in Compact Style for quick screening)
        if (resume.skillCategories.isNotEmpty() && cursorY < height - 60f) {
            cursorY = drawCompactSectionHeader("TECHNICAL COMPETENCIES", cursorY, marginX, contentWidth)
            for (cat in resume.skillCategories) {
                if (cursorY > height - 40f) break
                val line = "${cat.categoryName}: ${cat.skills.joinToString(", ")}"
                cursorY = drawBulletText(line, cursorY, marginX, contentWidth, false)
            }
            cursorY += 6f
        }

        // Experience
        if (resume.experiences.isNotEmpty()) {
            cursorY = drawCompactSectionHeader("WORK EXPERIENCE", cursorY, marginX, contentWidth)
            for (exp in resume.experiences) {
                if (cursorY > height - 50f) break
                val title = "${exp.role}  —  ${exp.company}"
                val dates = "${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}"
                cursorY = drawBoldTitleWithRightText(title, dates, cursorY, marginX, contentWidth)
                for (b in exp.bullets) {
                    if (cursorY > height - 30f) break
                    cursorY = drawBulletText(b, cursorY, marginX + 6f, contentWidth - 6f, isSubBullet = true)
                }
                cursorY += 4f
            }
        }

        // Education
        if (resume.educations.isNotEmpty() && cursorY < height - 40f) {
            cursorY = drawCompactSectionHeader("EDUCATION", cursorY, marginX, contentWidth)
            for (edu in resume.educations) {
                if (cursorY > height - 25f) break
                val eduHeader = "${edu.degree} in ${edu.fieldOfStudy}  |  ${edu.institution} (${edu.graduationYear})"
                cursorY = drawTextLine(eduHeader, cursorY, marginX, 8.8f, colorSlateBody)
            }
        }
    }

    private fun drawFederalAts() {
        var cursorY = 32f
        val marginX = 38f
        val contentWidth = width - (marginX * 2)

        // Strict Federal Heading Block
        val namePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 21f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val name = resume.fullName.ifEmpty { "CANDIDATE NAME" }
        canvas.drawText(name.uppercase(), marginX, cursorY, namePaint)
        cursorY += 15f

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(resume.jobTitle.ifEmpty { resume.targetRole }.uppercase(), marginX, cursorY, titlePaint)
        cursorY += 13f

        val contactPaint = TextPaint().apply {
            color = Color.parseColor("#374151")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val contactLine1 = listOfNotNull(
            resume.location.takeIf { it.isNotEmpty() }?.let { "LOCATION: $it" },
            resume.phone.takeIf { it.isNotEmpty() }?.let { "PHONE: $it" }
        ).joinToString("   |   ")
        val contactLine2 = listOfNotNull(
            resume.email.takeIf { it.isNotEmpty() }?.let { "EMAIL: $it" },
            resume.linkedin.takeIf { it.isNotEmpty() }?.let { "LINKEDIN: $it" }
        ).joinToString("   |   ")

        canvas.drawText(contactLine1, marginX, cursorY, contactPaint)
        cursorY += 12f
        canvas.drawText(contactLine2, marginX, cursorY, contactPaint)
        cursorY += 16f

        // Federal Summary
        if (resume.summary.isNotEmpty()) {
            cursorY = drawFederalHeader("I. PROFESSIONAL SUMMARY & CORE COMPETENCIES", cursorY, marginX, contentWidth)
            cursorY = drawParagraph(resume.summary, cursorY, marginX, contentWidth, 9.5f, Color.BLACK)
            cursorY += 10f
        }

        // Federal Experience
        if (resume.experiences.isNotEmpty()) {
            cursorY = drawFederalHeader("II. PROFESSIONAL & CIVILIAN WORK EXPERIENCE", cursorY, marginX, contentWidth)
            for (exp in resume.experiences) {
                if (cursorY > height - 60f) break
                val title = "POSITION: ${exp.role.uppercase()} | EMPLOYER: ${exp.company.uppercase()}"
                val dates = "DATES: ${exp.startDate} - ${if (exp.isCurrent) "PRESENT" else exp.endDate}"
                cursorY = drawBoldTitleWithRightText(title, dates, cursorY, marginX, contentWidth)
                if (exp.location.isNotEmpty()) {
                    cursorY = drawTextLine("LOCATION: ${exp.location}", cursorY, marginX, 9f, colorSlateMuted)
                }
                for (b in exp.bullets) {
                    if (cursorY > height - 35f) break
                    cursorY = drawBulletText(b, cursorY, marginX + 8f, contentWidth - 8f, isSubBullet = true)
                }
                cursorY += 8f
            }
        }

        // Federal Technical Skills
        if (resume.skillCategories.isNotEmpty() && cursorY < height - 60f) {
            cursorY = drawFederalHeader("III. TECHNICAL KNOWLEDGE & SYSTEM PROFICIENCIES", cursorY, marginX, contentWidth)
            for (cat in resume.skillCategories) {
                if (cursorY > height - 35f) break
                val line = "${cat.categoryName}: ${cat.skills.joinToString(", ")}"
                cursorY = drawBulletText(line, cursorY, marginX, contentWidth, false)
            }
            cursorY += 8f
        }

        // Federal Education
        if (resume.educations.isNotEmpty() && cursorY < height - 50f) {
            cursorY = drawFederalHeader("IV. FORMAL EDUCATION & ACCREDITATIONS", cursorY, marginX, contentWidth)
            for (edu in resume.educations) {
                if (cursorY > height - 35f) break
                val deg = "${edu.degree} in ${edu.fieldOfStudy} - ${edu.institution}"
                cursorY = drawBoldTitleWithRightText(deg, edu.graduationYear, cursorY, marginX, contentWidth)
            }
        }
    }

    private fun drawCompactSectionHeader(title: String, y: Float, x: Float, width: Float): Float {
        val paint = TextPaint().apply {
            color = colorNavy
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, paint)
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        canvas.drawLine(x, y + 2f, x + width, y + 2f, linePaint)
        return y + 12f
    }

    private fun drawFederalHeader(title: String, y: Float, x: Float, width: Float): Float {
        val paint = TextPaint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, paint)
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1.2f
        }
        canvas.drawLine(x, y + 3f, x + width, y + 3f, linePaint)
        return y + 15f
    }
    private fun drawSectionHeader(
        title: String,
        y: Float,
        x: Float,
        width: Float,
        textColor: Int,
        underlineColor: Int
    ): Float {
        val paint = TextPaint().apply {
            color = textColor
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, paint)

        val linePaint = Paint().apply {
            color = underlineColor
            strokeWidth = 1.5f
        }
        canvas.drawLine(x, y + 4f, x + width, y + 4f, linePaint)
        return y + 18f
    }

    private fun drawAtsHeader(title: String, y: Float, x: Float, width: Float): Float {
        val paint = TextPaint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, paint)
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        canvas.drawLine(x, y + 3f, x + width, y + 3f, linePaint)
        return y + 16f
    }

    private fun drawMinimalSectionHeader(title: String, y: Float, x: Float): Float {
        val paint = TextPaint().apply {
            color = Color.parseColor("#374151")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.08f
        }
        canvas.drawText(title, x, y, paint)
        return y + 14f
    }

    private fun drawExperienceItem(
        exp: com.example.data.model.ExperienceItem,
        y: Float,
        x: Float,
        width: Float
    ): Float {
        var cursor = y
        val title = exp.role
        val companyWithDates = "${exp.company}  |  ${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}"

        cursor = drawBoldTitleWithRightText(title, if (exp.location.isNotEmpty()) exp.location else "", cursor, x, width)
        cursor = drawTextLine(companyWithDates, cursor, x, 9.5f, colorSlateMuted)
        cursor += 2f

        for (bullet in exp.bullets) {
            cursor = drawBulletText(bullet, cursor, x + 8f, width - 8f, isSubBullet = true)
        }
        return cursor
    }

    private fun drawBoldTitleWithRightText(
        leftText: String,
        rightText: String,
        y: Float,
        x: Float,
        width: Float
    ): Float {
        val leftPaint = TextPaint().apply {
            color = colorSlateDark
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(leftText, x, y, leftPaint)

        if (rightText.isNotEmpty()) {
            val rightPaint = TextPaint().apply {
                color = colorSlateMuted
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(rightText, x + width, y, rightPaint)
        }
        return y + 14f
    }

    private fun drawTextLine(text: String, y: Float, x: Float, size: Float, color: Int): Float {
        val paint = TextPaint().apply {
            this.color = color
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(text, x, y, paint)
        return y + 13f
    }

    private fun drawParagraph(
        text: String,
        y: Float,
        x: Float,
        width: Float,
        size: Float,
        color: Int
    ): Float {
        val paint = TextPaint().apply {
            this.color = color
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(x, y - 8f)
        staticLayout.draw(canvas)
        canvas.restore()

        return y + staticLayout.height + 4f
    }

    private fun drawBulletText(
        text: String,
        y: Float,
        x: Float,
        width: Float,
        isSubBullet: Boolean
    ): Float {
        val bulletSymbol = if (isSubBullet) "• " else "▸ "
        val fullText = "$bulletSymbol$text"

        val paint = TextPaint().apply {
            color = colorSlateBody
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val staticLayout = StaticLayout.Builder.obtain(fullText, 0, fullText.length, paint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(x, y - 8f)
        staticLayout.draw(canvas)
        canvas.restore()

        return y + staticLayout.height + 3f
    }
}
