package com.example.news.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.news.R

/** Poppins for headings, the system font for body text (better readability for long articles). */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val Default = Typography()

private fun TextStyle.poppins(weight: FontWeight) = copy(fontFamily = Poppins, fontWeight = weight)

val Typography = Typography(
    displaySmall = Default.displaySmall.poppins(FontWeight.Bold),
    headlineLarge = Default.headlineLarge.poppins(FontWeight.Bold),
    headlineMedium = Default.headlineMedium.poppins(FontWeight.Bold),
    headlineSmall = Default.headlineSmall.poppins(FontWeight.SemiBold),
    titleLarge = Default.titleLarge.poppins(FontWeight.SemiBold),
    titleMedium = Default.titleMedium.poppins(FontWeight.SemiBold),
    titleSmall = Default.titleSmall.poppins(FontWeight.SemiBold).copy(fontSize = 15.sp, lineHeight = 21.sp),
    bodyLarge = Default.bodyLarge.copy(lineHeight = 26.sp),
    bodyMedium = Default.bodyMedium,
    bodySmall = Default.bodySmall,
    labelLarge = Default.labelLarge.poppins(FontWeight.SemiBold),
    labelMedium = Default.labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = Default.labelSmall,
)
