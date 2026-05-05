package com.notilog.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val offsetX1 = remember { Animatable(0f) }
    val offsetY1 = remember { Animatable(0f) }
    val offsetX2 = remember { Animatable(0f) }
    val offsetY2 = remember { Animatable(0f) }
    val offsetX3 = remember { Animatable(0f) }
    val offsetY3 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val duration = 12000
        offsetX1.animateTo(
            targetValue = 40f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    LaunchedEffect(Unit) {
        offsetY1.animateTo(
            targetValue = -30f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    LaunchedEffect(Unit) {
        offsetX2.animateTo(
            targetValue = -35f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    LaunchedEffect(Unit) {
        offsetY2.animateTo(
            targetValue = 45f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    LaunchedEffect(Unit) {
        offsetX3.animateTo(
            targetValue = 25f,
            animationSpec = infiniteRepeatable(
                animation = tween(18000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    LaunchedEffect(Unit) {
        offsetY3.animateTo(
            targetValue = 35f,
            animationSpec = infiniteRepeatable(
                animation = tween(18000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (darkTheme) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0A0520),
                            Color(0xFF150A30),
                            Color(0xFF0A0520),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF5F0FF),
                            Color(0xFFEDE5FF),
                            Color(0xFFF0E8FF),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                }
            )
    ) {
        AnimatedBlobs(
            offsets = Triple(
                Offset(offsetX1.value, offsetY1.value),
                Offset(offsetX2.value, offsetY2.value),
                Offset(offsetX3.value, offsetY3.value)
            ),
            darkTheme = darkTheme
        )
        content()
    }
}

@Composable
private fun AnimatedBlobs(
    offsets: Triple<Offset, Offset, Offset>,
    darkTheme: Boolean
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(80.dp)
    ) {
        val blobSize = size.minDimension * 0.7f

        drawCircle(
            brush = Brush.radialGradient(
                colors = if (darkTheme) {
                    listOf(
                        Color(0xFFADC7FF).copy(alpha = 0.35f),
                        Color(0xFFADC7FF).copy(alpha = 0.15f),
                        Color.Transparent
                    )
                } else {
                    listOf(
                        Color(0xFF0059BB).copy(alpha = 0.25f),
                        Color(0xFF0059BB).copy(alpha = 0.12f),
                        Color.Transparent
                    )
                },
                center = center,
                radius = blobSize,
            ),
            radius = blobSize,
            center = center + offsets.first,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (darkTheme) {
                    listOf(
                        Color(0xFF4854BB).copy(alpha = 0.25f),
                        Color(0xFF4854BB).copy(alpha = 0.10f),
                        Color.Transparent
                    )
                } else {
                    listOf(
                        Color(0xFFA33800).copy(alpha = 0.18f),
                        Color(0xFFA33800).copy(alpha = 0.08f),
                        Color.Transparent
                    )
                },
            ),
            radius = blobSize * 0.85f,
            center = center + offsets.second,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (darkTheme) {
                    listOf(
                        Color(0xFFFFB59A).copy(alpha = 0.18f),
                        Color(0xFFFFB59A).copy(alpha = 0.06f),
                        Color.Transparent
                    )
                } else {
                    listOf(
                        Color(0xFFCD4800).copy(alpha = 0.14f),
                        Color(0xFFCD4800).copy(alpha = 0.05f),
                        Color.Transparent
                    )
                },
            ),
            radius = blobSize * 0.6f,
            center = center + offsets.third,
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 16.dp,
    borderColor: Color? = null,
    containerColor: Color? = null,
    shadowElevation: Dp = 8.dp,
    borderGlow: Boolean = false,
    content: @Composable () -> Unit
) {
    val glassTokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(cornerRadius)
    val bgColor = containerColor ?: glassTokens.glassBackground
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val shadowColor = if (isDark) Color(0x000000).copy(alpha = 0.4f) else Color(0x000000).copy(alpha = 0.08f)
    val glowBorder = if (borderGlow && isDark) {
        glassTokens.glassBorder.copy(alpha = 0.3f)
    } else if (borderGlow) {
        glassTokens.glassBorder.copy(alpha = 0.5f)
    } else {
        borderColor ?: glassTokens.glassBorder
    }

    Card(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(shape)
            .border(
                width = 1.dp,
                color = glowBorder,
                shape = shape,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
    ) {
        Box(
            modifier = Modifier
                .background(bgColor)
                .then(
                    if (borderGlow) {
                        Modifier.drawBehind {
                            drawRoundRect(
                                color = if (isDark) Color(0xFFADC7FF).copy(alpha = 0.06f) else Color(0xFF0059BB).copy(alpha = 0.04f),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
        ) {
            content()
        }
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val glassTokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(cornerRadius)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = glassTokens.glassBorder.copy(alpha = if (isDark) 0.25f else 0.45f),
                shape = shape,
            )
            .background(glassTokens.glassBackground)
    ) {
        content()
    }
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = shape,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
    }
}

@Composable
fun TimeChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}

@Composable
fun GlassDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color? = null,
) {
    val glassTokens = LocalGlassTokens.current
    val dividerColor = color ?: glassTokens.glassBorder

    Canvas(modifier = modifier) {
        drawLine(
            color = dividerColor,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = thickness.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
        )
    }
}
