---
# Notilog Design System
# Generated from codebase analysis
# Version: 1.0

name: Notilog
brand:
  name: Notilog
  tagline: "Recover lost notifications, find 2FA codes, search deleted messages"
  description: "Android app for intercepting, storing and organizing notifications"

colors:
  light:
    primary:
      value: "#0059BB"
      name: "Royal Blue"
    onPrimary:
      value: "#FFFFFF"
    primaryContainer:
      value: "#0070EA"
    onPrimaryContainer:
      value: "#FEFCFF"
    secondary:
      value: "#4854BB"
      name: "Indigo Purple"
    onSecondary:
      value: "#FFFFFF"
    secondaryContainer:
      value: "#8692FD"
    onSecondaryContainer:
      value: "#16238E"
    tertiary:
      value: "#A33800"
      name: "Burnt Orange"
    onTertiary:
      value: "#FFFFFF"
    tertiaryContainer:
      value: "#CD4800"
    onTertiaryContainer:
      value: "#FFFBFF"
    error:
      value: "#BA1A1A"
      name: "Crimson Red"
    onError:
      value: "#FFFFFF"
    errorContainer:
      value: "#FFDAD6"
    onErrorContainer:
      value: "#93000A"
    background:
      value: "#F7F9FC"
      name: "Off-White Blue"
    onBackground:
      value: "#191C1E"
    surface:
      value: "#F7F9FC"
    surfaceVariant:
      value: "#E0E3E6"
      name: "Silver Mist"
    onSurface:
      value: "#191C1E"
    onSurfaceVariant:
      value: "#414754"
    outline:
      value: "#717786"
      name: "Slate Grey"
    outlineVariant:
      value: "#C1C6D7"
    surfaceTint:
      value: "#005BC0"
  dark:
    primary:
      value: "#ADC7FF"
      name: "Periwinkle Blue"
    onPrimary:
      value: "#003061"
    primaryContainer:
      value: "#00468A"
    onPrimaryContainer:
      value: "#D8E2FF"
    secondary:
      value: "#BFC2FF"
      name: "Lavender"
    onSecondary:
      value: "#1B277A"
    secondaryContainer:
      value: "#333D91"
    onSecondaryContainer:
      value: "#DFE0FF"
    tertiary:
      value: "#FFB59A"
      name: "Peach"
    onTertiary:
      value: "#5A1900"
    tertiaryContainer:
      value: "#802A00"
    onTertiaryContainer:
      value: "#FFDBCE"
    error:
      value: "#FFB4AB"
      name: "Salmon Pink"
    onError:
      value: "#690005"
    errorContainer:
      value: "#93000A"
    onErrorContainer:
      value: "#FFDAD6"
    background:
      value: "#0F1114"
      name: "Charcoal Black"
    onBackground:
      value: "#E4E7FF"
    surface:
      value: "#0F1114"
    surfaceVariant:
      value: "#414754"
    onSurface:
      value: "#E4E7FF"
    onSurfaceVariant:
      value: "#C1C6D7"
    outline:
      value: "#8B9099"
      name: "Steel Grey"
    outlineVariant:
      value: "#414754"
    inverseSurface:
      value: "#E4E7FF"
    inverseOnSurface:
      value: "#2D3133"
    inversePrimary:
      value: "#0059BB"
    surfaceTint:
      value: "#ADC7FF"
  gradient:
    background:
      light:
        colors:
          - "#F5F0FF"
          - "#EDE5FF"
          - "#F0E8FF"
      dark:
        colors:
          - "#0A0520"
          - "#150A30"
          - "#0A0520"
    blobs:
      light:
        - color: "#0059BB"
          opacity: 0.25
        - color: "#A33800"
          opacity: 0.18
        - color: "#CD4800"
          opacity: 0.14
      dark:
        - color: "#ADC7FF"
          opacity: 0.35
        - color: "#4854BB"
          opacity: 0.25
        - color: "#FFB59A"
          opacity: 0.18

typography:
  fontFamily:
    name: "Plus Jakarta Sans"
    source: "Google Fonts"
    weights:
      - Regular (400)
      - Medium (500)
      - SemiBold (600)
      - ExtraBold (800)
  styles:
    displayLarge:
      size: 34
      lineHeight: 42
      letterSpacing: -0.02
      weight: ExtraBold
    displayMedium:
      size: 28
      lineHeight: 36
      letterSpacing: -0.02
      weight: ExtraBold
    headlineLarge:
      size: 24
      lineHeight: 32
      letterSpacing: -0.02
      weight: Bold
    headlineMedium:
      size: 22
      lineHeight: 28
      letterSpacing: 0
      weight: SemiBold
    headlineSmall:
      size: 20
      lineHeight: 26
      letterSpacing: 0
      weight: SemiBold
    titleLarge:
      size: 18
      lineHeight: 24
      letterSpacing: 0
      weight: SemiBold
    titleMedium:
      size: 16
      lineHeight: 22
      letterSpacing: 0
      weight: SemiBold
    titleSmall:
      size: 14
      lineHeight: 20
      letterSpacing: 0
      weight: Medium
    bodyLarge:
      size: 17
      lineHeight: 24
      letterSpacing: 0
      weight: Regular
    bodyMedium:
      size: 15
      lineHeight: 20
      letterSpacing: 0
      weight: Regular
    bodySmall:
      size: 13
      lineHeight: 18
      letterSpacing: 0
      weight: Regular
    labelLarge:
      size: 14
      lineHeight: 20
      letterSpacing: 0.05
      weight: Bold
    labelMedium:
      size: 12
      lineHeight: 16
      letterSpacing: 0.05
      weight: Bold
    labelSmall:
      size: 12
      lineHeight: 16
      letterSpacing: 0.05
      weight: Bold

spacing:
  baseline: 8
  unit: dp
  values:
    xxs: 4
    xs: 8
    sm: 12
    md: 16
    lg: 24
    xl: 32
    xxl: 48
  screenPadding:
    horizontal: 16
    vertical: 8
  cardPadding: 16
  iconSize:
    small: 20
    medium: 24
    large: 32
    appIcon: 32
    largeAppIcon: 48

elevation:
  levels:
    surface0:
      description: "Background"
      elevation: 0
    surface1:
      description: "Cards"
      elevation: 1
    surface2:
      description: "Search Bar"
      elevation: 2
      shadow: 8
    surface3:
      description: "Dialogs/Popups"
      elevation: 3
  glass:
    light:
      elevation: 0.18
      blur: 16
    dark:
      elevation: 0.1
      blur: 24

motion:
  gradientAnimation:
    duration: 12000
    easing: "FastOutSlowInEasing"
    blobMovement:
      blob1: 40
      blob2: 35
      blob3: 25
  transitions:
    sharedElement: "Container Transform"
    ripple: "Material Ripple"
  microInteractions:
    actions:
      blacklist:
        style: "Button"
        position: "Card trailing edge"
        icon: "Block"
        background: "#DC2626"
      delete:
        style: "Button"
        position: "Card leading edge"
        icon: "Delete"
        background: "#717786"

shape:
  radii:
    small:
      value: 8
      description: "Chips, Badges"
    medium:
      value: 16
      description: "Cards, Inputs"
    large:
      value: 32
      description: "Containers"
    navigation:
      value: 20

glassmorphism:
  light:
    background: "#75FFFFFF"
    border: "#45FFFFFF"
    borderWidth: 1
  dark:
    background: "#18FFFFFF"
    border: "#18FFFFFF"
    borderWidth: 1
  blur:
    android12Plus: 16-24
    below: 0
  ambientGlow:
    light: "#330059BB"
    dark: "#26ADC7FF"

# Components
components:
  navigationBar:
    activeIndicator:
      shape: "Pill"
      containerColor: "Primary Container"
    icons:
      style: "Material Symbols Rounded"
      fill: 1
      weight: 400
      gradeLight: 0
      gradeDark: -25
  cards:
    style: "Elevated with Glass"
    cornerRadius: 16
    elevation: 8
    border: 1
  filterChips:
    style: "Outlined"
    cornerRadius: 8

---

# Notilog Design System

## Overview

Notilog is an Android notification history application designed with a modern, premium aesthetic that balances functional utility with visual sophistication. The design language draws from Material Design 3 while extending it with custom glassmorphism effects and animated gradient backgrounds that create a distinctive, high-end feel.

## Brand Identity

**Personality:** Clean, Reliable, Minimalist, Native, Premium

The app targets users who need to recover lost notifications, find 2FA codes, or search deleted messages. The visual identity reflects this reliability—professional yet approachable with subtle polish that doesn't distract from the core functionality.

## Visual Language

### Color Philosophy

The color system uses a sophisticated blue-forward palette with warm accents:

- **Primary (#0059BB):** Royal blue serves as the dominant brand color, conveying trust and reliability
- **Secondary (#4854BB):** Indigo purple provides complementary richness
- **Tertiary (#A33800):** Burnt orange adds warmth for CTAs and accents
- **Error (#BA1A1A):** Crimson red clearly indicates destructive actions

Light mode uses a pale blue-white background (#F7F9FC) creating a clean, clinical feel, while dark mode employs near-black surfaces (#0F1114) for OLED efficiency and reduced eye strain.

### Typography

**Plus Jakarta Sans** serves as the exclusive typeface—a geometric sans-serif with excellent readability across all sizes. The font weight scale progresses from ExtraBold for display text through Regular for body content, with increased letter-spacing on labels for improved legibility at small sizes.

The type scale ranges from 34sp (Display Large) down to 12sp (Labels), with each style carefully tuned line-height ratios for comfortable reading.

### Shape Language

Rounded corners define the UI feel with three tiers:
- **Small (8dp):** Functional elements like chips and badges
- **Medium (16dp):** Cards and input fields
- **Large (32dp):** Major containers

This creates a friendly, approachable aesthetic without sacrificing information density.

## Glassmorphism System

Notilog features a sophisticated glassmorphism implementation that adds depth without obscuring content:

### Light Mode
- White-tinted semi-transparent backgrounds (~53% opacity)
- Subtle white borders (~27% opacity)
- Soft shadows (0.18 elevation)
- 16px blur radius on Android 12+

### Dark Mode  
- Frosted glass effect (~9% white opacity)
- Cool-toned borders matching primary
- Subtle ambient glow effects
- 24px blur radius for depth

The glass effects appear on:
- Navigation bar
- Top search bar
- Floating cards and dialogs

## Background Treatment

The app features animated gradient backgrounds with subtle blob animations:

### Light Mode
- Soft lavender-blue gradient (#F5F0FF → #EDE5FF → #F0E8FF)
- Floating blobs of primary, tertiary, and secondary colors with reduced opacity
- Gentle 12-second animation cycle

### Dark Mode
- Deep purple-black gradient (#0A0520 → #150A30 → #0A0520)
- Cool-toned blob animations using primary/secondary colors
- Slower, more subtle movement

The blobs use radial gradients with transparency falloff, creating soft, organic shapes that float across the screen behind the content.

## Surface Hierarchy

| Level | Surface | Elevation | Shadow |
|-------|---------|-----------|--------|
| 0 | Background | 0dp | None |
| 1 | Cards | 1dp | 2dp |
| 2 | Search Bar | 2dp | 8dp |
| 3 | Dialogs | 3dp | 12dp |

## Interaction Patterns

### Action Buttons
- **Trailing Button:** Blacklist action (red background)
- **Leading Button:** Delete action (grey background)

### Micro-interactions
- Standard Material ripple feedback on all touchables
- Haptic feedback on long-press and blacklist actions
- Shared element transitions for card expansion

### Loading States
- Skeleton screen shimmer effect
- Animated gradient background during load

## Iconography

Uses **Material Symbols Variable Font** with:
- Rounded style
- Always filled (fill: 1)
- Standard weight (400), elevated to 500 for emphasized actions
- Grade -25 for dark mode to reduce glow
- Optical size auto-scaling (20dp-48dp) per context

## Navigation

Bottom navigation with three destinations:
1. **Feed:** Notifications history (icon: history/notifications)
2. **Groups:** Categories and apps (icon: grid_view)
3. **Settings:** Configuration (icon: settings)

Active states show pill-shaped indicators in primary container color with bold labels.

## Accessibility

- Minimum 48x48dp touch targets
- 4.5:1 contrast ratio minimum
- Meaningful content descriptions
- Dynamic color support (Android 12+)

---

*Generated from Theme.kt, GlassComponents.kt, and design specification documentation.*