package com.example.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.Resume
import java.io.File
import java.io.FileOutputStream

class LaTeXGeneratorService(private val context: Context) {

    fun generateLaTeXCode(resume: Resume): String {
        val sb = StringBuilder()

        sb.appendLine("%-------------------------")
        sb.appendLine("% CareerCraft ATS-Optimized LaTeX Resume")
        sb.appendLine("% Target: pdflatex / xelatex / Overleaf")
        sb.appendLine("%-------------------------\n")
        sb.appendLine("\\documentclass[letterpaper,10pt]{article}")
        sb.appendLine("\\usepackage{latexsym}")
        sb.appendLine("\\usepackage[empty]{fullpage}")
        sb.appendLine("\\usepackage{titlesec}")
        sb.appendLine("\\usepackage{marvosym}")
        sb.appendLine("\\usepackage[usenames,dvipsnames]{color}")
        sb.appendLine("\\usepackage{verbatim}")
        sb.appendLine("\\usepackage{enumitem}")
        sb.appendLine("\\usepackage[hidelinks]{hyperref}")
        sb.appendLine("\\usepackage{fancyhdr}")
        sb.appendLine("\\usepackage[english]{babel}")
        sb.appendLine("\\usepackage{tabularx}")
        sb.appendLine("\\usepackage{geometry}")
        sb.appendLine("\\geometry{left=0.6in,top=0.5in,right=0.6in,bottom=0.5in}")
        sb.appendLine("")
        sb.appendLine("\\pagestyle{fancy}")
        sb.appendLine("\\fancyhf{}")
        sb.appendLine("\\renewcommand{\\headrulewidth}{0pt}")
        sb.appendLine("\\renewcommand{\\footrulewidth}{0pt}")
        sb.appendLine("")
        sb.appendLine("\\urlstyle{same}")
        sb.appendLine("\\raggedbottom")
        sb.appendLine("\\raggedright")
        sb.appendLine("\\setlength{\\tabcolsep}{0in}")
        sb.appendLine("")
        sb.appendLine("% Section formatting")
        sb.appendLine("\\titleformat{\\section}{\n  \\vspace{-4pt}\\scshape\\raggedright\\large\\bfseries\n}{}{0em}{}[\\color{black}\\titlerule \\vspace{-5pt}]")
        sb.appendLine("")
        sb.appendLine("\\begin{document}\n")

        // Header
        val name = escape(resume.fullName.ifEmpty { "Candidate Name" })
        val title = escape(resume.jobTitle.ifEmpty { resume.targetRole })
        sb.appendLine("%----------HEADING----------")
        sb.appendLine("\\begin{center}")
        sb.appendLine("    \\textbf{\\Huge \\scshape $name} \\\\ \\vspace{2pt}")
        if (title.isNotEmpty()) {
            sb.appendLine("    \\large \\textbf{$title} \\\\ \\vspace{2pt}")
        }
        val contacts = listOfNotNull(
            resume.phone.takeIf { it.isNotEmpty() }?.let { escape(it) },
            resume.email.takeIf { it.isNotEmpty() }?.let { "\\href{mailto:$it}{\\underline{$it}}" },
            resume.linkedin.takeIf { it.isNotEmpty() }?.let { "\\href{https://$it}{\\underline{LinkedIn}}" },
            resume.github.takeIf { it.isNotEmpty() }?.let { "\\href{https://$it}{\\underline{GitHub}}" },
            resume.location.takeIf { it.isNotEmpty() }?.let { escape(it) }
        ).joinToString(" $|$ ")
        sb.appendLine("    \\small $contacts")
        sb.appendLine("\\end{center}\n")

        // Summary
        if (resume.summary.isNotEmpty()) {
            sb.appendLine("%-----------SUMMARY-----------")
            sb.appendLine("\\section{Professional Summary}")
            sb.appendLine(escape(resume.summary))
            sb.appendLine("\\vspace{4pt}\n")
        }

        // Experience
        if (resume.experiences.isNotEmpty()) {
            sb.appendLine("%-----------EXPERIENCE-----------")
            sb.appendLine("\\section{Experience}")
            for (exp in resume.experiences) {
                val dates = "${escape(exp.startDate)} -- ${if (exp.isCurrent) "Present" else escape(exp.endDate)}"
                sb.appendLine("\\begin{tabular*}{1.0\\textwidth}{l@{\\extracolsep{\\fill}}r}")
                sb.appendLine("    \\textbf{${escape(exp.role)}} & $dates \\\\")
                sb.appendLine("    \\textit{\\small ${escape(exp.company)}} & \\textit{\\small ${escape(exp.location)}} \\\\")
                sb.appendLine("\\end{tabular*}\\vspace{-5pt}")
                if (exp.bullets.isNotEmpty()) {
                    sb.appendLine("\\begin{itemize}[leftmargin=0.15in, label=\\tiny$\\bullet$]")
                    for (b in exp.bullets) {
                        sb.appendLine("    \\item ${escape(b)}")
                    }
                    sb.appendLine("\\end{itemize}\\vspace{-2pt}")
                }
            }
            sb.appendLine("")
        }

        // Skills
        if (resume.skillCategories.isNotEmpty()) {
            sb.appendLine("%-----------TECHNICAL SKILLS-----------")
            sb.appendLine("\\section{Technical Skills}")
            sb.appendLine("\\begin{itemize}[leftmargin=0.15in, label={}]")
            for (cat in resume.skillCategories) {
                val category = escape(cat.categoryName)
                val skillItems = cat.skills.joinToString(", ") { escape(it) }
                sb.appendLine("    \\small{\\item{\\textbf{$category}{: $skillItems}}}")
            }
            sb.appendLine("\\end{itemize}\\vspace{-5pt}\n")
        }

        // Education
        if (resume.educations.isNotEmpty()) {
            sb.appendLine("%-----------EDUCATION-----------")
            sb.appendLine("\\section{Education}")
            for (edu in resume.educations) {
                sb.appendLine("\\begin{tabular*}{1.0\\textwidth}{l@{\\extracolsep{\\fill}}r}")
                sb.appendLine("    \\textbf{${escape(edu.institution)}} & ${escape(edu.graduationYear)} \\\\")
                val degreeLine = "${escape(edu.degree)} in ${escape(edu.fieldOfStudy)}${if (edu.gpaOrHonors.isNotEmpty()) " (GPA/Honors: " + escape(edu.gpaOrHonors) + ")" else ""}"
                sb.appendLine("    \\textit{\\small $degreeLine} & \\\\")
                sb.appendLine("\\end{tabular*}\\vspace{-3pt}")
            }
            sb.appendLine("")
        }

        // Projects
        if (resume.projects.isNotEmpty()) {
            sb.appendLine("%-----------PROJECTS-----------")
            sb.appendLine("\\section{Projects}")
            for (proj in resume.projects) {
                sb.appendLine("\\textbf{${escape(proj.name)}} $|$ \\emph{${escape(proj.techStack)}} \\\\")
                sb.appendLine("${escape(proj.description)} \\vspace{3pt}")
            }
        }

        sb.appendLine("\\end{document}")
        return sb.toString()
    }

    fun generateLatexFile(resume: Resume): File {
        val code = generateLaTeXCode(resume)
        val file = File(context.cacheDir, "resume_${resume.id.take(8)}.tex")
        FileOutputStream(file).use { out ->
            out.write(code.toByteArray())
        }
        return file
    }

    fun shareLatexFile(resume: Resume) {
        val file = generateLatexFile(resume)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/x-tex"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${resume.fullName} - Resume (LaTeX)")
            putExtra(Intent.EXTRA_TEXT, "Attached is the compilable LaTeX .tex source for ${resume.fullName}.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Share LaTeX File").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun escape(text: String): String {
        return text
            .replace("\\", "\\textbackslash{}")
            .replace("&", "\\&")
            .replace("%", "\\%")
            .replace("$", "\\$")
            .replace("#", "\\#")
            .replace("_", "\\_")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("~", "\\textasciitilde{}")
            .replace("^", "\\textasciicircum{}")
    }
}
