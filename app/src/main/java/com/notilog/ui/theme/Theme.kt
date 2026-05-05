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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.notilog.R

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0059BB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0070EA),
    onPrimaryContainer = Color(0xFFFefcff),
    secondary = Color(0xFF4854BB),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF8692FD),
    onSecondaryContainer = Color(0xFF16238E),
    tertiary = Color(0xFFA33800),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCD4800),
    onTertiaryContainer = Color(0xFFFFFBFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFF7F9FC),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFE0E3E6),
    onSurfaceVariant = Color(0xFF414754),
    outline = Color(0xFF717786),
    outlineVariant = Color(0xFFC1C6D7),
    inverseSurface = Color(0xFF2D3133),
    inverseOnSurface = Color(0xFFEFF1F4),
    inversePrimary = Color(0xFFADC7FF),
    surfaceTint = Color(0xFF005BC0),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFADC7FF),
    onPrimary = Color(0xFF003061),
    primaryContainer = Color(0xFF00468A),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFBFC2FF),
    onSecondary = Color(0xFF1B277A),
    secondaryContainer = Color(0xFF333D91),
    onSecondaryContainer = Color(0xFFDFE0FF),
    tertiary = Color(0xFFFFB59A),
    onTertiary = Color(0xFF5A1900),
    tertiaryContainer = Color(0xFF802A00),
    onTertiaryContainer = Color(0xFFFFDBCE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1114),
    onBackground = Color(0xFFE4E7FF),
    surface = Color(0xFF0F1114),
    onSurface = Color(0xFFE4E7FF),
    surfaceVariant = Color(0xFF414754),
    onSurfaceVariant = Color(0xFFC1C6D7),
    outline = Color(0xFF8B9099),
    outlineVariant = Color(0xFF414754),
    inverseSurface = Color(0xFFE4E7FF),
    inverseOnSurface = Color(0xFF2D3133),
    inversePrimary = Color(0xFF0059BB),
    surfaceTint = Color(0xFFADC7FF),
)

val LocalGlassTokens = compositionLocalOf {
    GlassTokens(
        glassBackground = Color(0x80FFFFFF),
        glassBorder = Color(0x33FFFFFF),
        glassElevation = 0.15f,
        blurAmount = 16f,
        cornerRadius = 16f,
        ambientGlow = Color(0x330059BB),
    )
}

data class GlassTokens(
    val glassBackground: Color,
    val glassBorder: Color,
    val glassElevation: Float,
    val blurAmount: Float,
    val cornerRadius: Float,
    val ambientGlow: Color,
)

// Plus Jakarta Sans font family - real assets from res/font
val PlusJakartaSans = FontFamily(
    Font(R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(R.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold),
)
val InterFont = PlusJakartaSans

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
            glassBackground = Color(0x18FFFFFF),
            glassBorder = Color(0x18FFFFFF),
            glassElevation = 0.1f,
            blurAmount = 24f,
            cornerRadius = 20f,
            ambientGlow = Color(0x26ADC7FF),
        )
    } else {
        GlassTokens(
            glassBackground = Color(0x75FFFFFF),
            glassBorder = Color(0x45FFFFFF),
            glassElevation = 0.18f,
            blurAmount = 16f,
            cornerRadius = 20f,
            ambientGlow = Color(0x330059BB),
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

val NotilogTypography = androidx.compose.material3.Typography(
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
        letterSpacing = 0.05.sp,
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp,
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp,
    ),
)
