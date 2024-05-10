package com.omar.pcconnector.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.omar.pcconnector.R


val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_extralight, FontWeight.ExtraLight),
)


val platformStyle = PlatformTextStyle(includeFontPadding = true)

private val defaultTypography = Typography()
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = InterFontFamily, platformStyle = platformStyle),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = InterFontFamily, platformStyle = platformStyle)
)