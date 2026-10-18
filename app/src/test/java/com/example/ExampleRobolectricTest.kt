package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DefaultResumeData
import com.example.service.LaTeXGeneratorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CareerCraft", appName)
    }

    @Test
    fun `generate valid LaTeX document from resume`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resume = DefaultResumeData.createSampleResume()
        val latexService = LaTeXGeneratorService(context)
        val latex = latexService.generateLaTeXCode(resume)

        assertTrue("LaTeX should have documentclass", latex.contains("\\documentclass"))
        assertTrue("LaTeX should have candidate name", latex.contains(resume.fullName))
        assertTrue("LaTeX should end with end document", latex.contains("\\end{document}"))
    }
}
