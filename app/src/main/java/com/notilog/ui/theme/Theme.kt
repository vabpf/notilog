package com.notilog.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.notilog.R

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00428E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0059BB),
    onPrimaryContainer = Color(0xFFC3D5FF),
    secondary = Color(0xFF4854BB),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF8692FD),
    onSecondaryContainer = Color(0xFF16238E),
    tertiary = Color(0xFF7C2900),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA43800),
    onTertiaryContainer = Color(0xFFFFC9B6),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191C22),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF191C22),
    surfaceVariant = Color(0xFFE1E2EB),
    onSurfaceVariant = Color(0xFF424752),
    outline = Color(0xFF727784),
    outlineVariant = Color(0xFFC2C6D5),
    inverseSurface = Color(0xFF2E3037),
    inverseOnSurface = Color(0xFFEFF0F9),
    inversePrimary = Color(0xFFACC7FF),
    surfaceTint = Color(0xFF075BBD),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFACC7FF),
    onPrimary = Color(0xFF001A40),
    primaryContainer = Color(0xFF004492),
    onPrimaryContainer = Color(0xFFD7E2FF),
    secondary = Color(0xFFBDC2FF),
    onSecondary = Color(0xFF000965),
    secondaryContainer = Color(0xFF2E3AA2),
    onSecondaryContainer = Color(0xFFDFE0FF),
    tertiary = Color(0xFFFFB59A),
    onTertiary = Color(0xFF370E00),
    tertiaryContainer = Color(0xFF802A00),
    onTertiaryContainer = Color(0xFFFFDBCE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1114),
    onBackground = Color(0xFFF9F9FF),
    surface = Color(0xFF0F1114),
    onSurface = Color(0xFFF9F9FF),
    surfaceVariant = Color(0xFF424752),
    onSurfaceVariant = Color(0xFFC2C6D5),
    outline = Color(0xFF8B919D),
    outlineVariant = Color(0xFF424752),
    inverseSurface = Color(0xFFF9F9FF),
    inverseOnSurface = Color(0xFF2E3037),
    inversePrimary = Color(0xFF00428E),
    surfaceTint = Color(0xFFACC7FF),
)

val LocalGlassTokens = compositionLocalOf {
    GlassTokens(
        glassBackground = Color(0xB2FFFFFF),
        glassBorder = Color(0x80FFFFFF),
        glassElevation = 4.dp,
        blurAmount = 16.dp,
        cornerRadius = 16.dp,
        ambientGlow = Color(0x0D00428E),
    )
}

data class GlassTokens(
    val glassBackground: Color,
    val glassBorder: Color,
    val glassElevation: Dp,
    val blurAmount: Dp,
    val cornerRadius: Dp,
    val ambientGlow: Color,
)

// Plus Jakarta Sans font family - real assets from res/font
val PlusJakartaSans = FontFamily(
    Font(R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(R.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold),
)

@Composable
fun NotilogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val glassTokens = if (darkTheme) {
        GlassTokens(
            glassBackground = Color(0xB20F1114),
            glassBorder = Color(0x1AFFFFFF),
            glassElevation = 2.dp,
            blurAmount = 16.dp,
            cornerRadius = 16.dp,
            ambientGlow = Color(0x1AACC7FF),
        )
    } else {
        GlassTokens(
            glassBackground = Color(0xB2FFFFFF),
            glassBorder = Color(0x80FFFFFF),
            glassElevation = 4.dp,
            blurAmount = 16.dp,
            cornerRadius = 16.dp,
            ambientGlow = Color(0x0D00428E),
        )
    }

    // Shapes aligned with Ergo-Luxe: large radii for a premium rounded feel
    val ErgoShapes = Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(32.dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NotilogTypography,
        shapes = ErgoShapes,
        content = {
            CompositionLocalProvider(
                LocalGlassTokens provides glassTokens,
                content = content
            )
        }
    )
}

val NotilogTypography = Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.02).sp,
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).sp,
    ),
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).sp,
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
