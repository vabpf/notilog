# Notilog UI/UX Finalization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finalize the UI and UX of the Notilog application to match the comprehensive design specification, including app icons, enhanced detail actions, and blacklist management.

**Architecture:** MVVM with Jetpack Compose. Uses Android Package Manager for icon loading and standard Android Intent system for sharing.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Hilt.

---

### Task 1: App Icon Loading Support

**Files:**
- Modify: `app/src/main/java/com/notilog/ui/feed/FeedScreen.kt`
- Modify: `app/src/main/java/com/notilog/ui/detail/DetailScreen.kt`

- [ ] **Step 1: Add App Icon to NotificationCard in FeedScreen**
    *   Load icon using `LocalContext.current.packageManager.getApplicationIcon(packageName)`.
    *   Display in a 32dp x 32dp container on the left side of the card.
- [ ] **Step 2: Add Large App Icon to DetailScreen Header**
    *   Display 48dp x 48dp icon in the header section.
- [ ] **Step 3: Commit**

```bash
git commit -m "ui: add app icon loading to feed and detail screens"
```

---

### Task 4: Blacklist Management Screen

**Files:**
- Create: `app/src/main/java/com/notilog/ui/settings/BlacklistScreen.kt`
- Create: `app/src/main/java/com/notilog/ui/settings/BlacklistViewModel.kt`
- Modify: `app/src/main/java/com/notilog/ui/navigation/NotilogNavGraph.kt`
- Modify: `app/src/main/java/com/notilog/ui/settings/SettingsScreen.kt`

- [ ] **Step 1: Create BlacklistViewModel**
    *   Expose Flow of blacklisted apps.
    *   Provide `removeFromBlacklist(packageName)` method.
- [ ] **Step 2: Implement BlacklistScreen UI**
    *   List of blacklisted apps with "Remove" icon button.
- [ ] **Step 3: Add Blacklist route to NavGraph**
- [ ] **Step 4: Add "Manage Blacklist" button to SettingsScreen**
- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add blacklist management screen"
```

---

### Task 3: Enhanced Detail Screen Actions

**Files:**
- Modify: `app/src/main/java/com/notilog/ui/detail/DetailScreen.kt`
- Modify: `app/src/main/java/com/notilog/ui/detail/DetailViewModel.kt`

- [ ] **Step 1: Add "Global Blacklist" toggle to DetailScreen TopAppBar**
    *   Linked to `isBlacklisted` state in ViewModel.
- [ ] **Step 2: Add "Copy" and "Share" buttons to VersionCard**
    *   Implement "Copy to Clipboard" and "Android Share Intent".
- [ ] **Step 3: Implement "Delete All Versions" action**
- [ ] **Step 4: Commit**

```bash
git commit -m "ui: add enhanced actions to detail screen"
```

---

### Task 4: UI Polish & Polish

**Files:**
- Modify: `app/src/main/java/com/notilog/ui/feed/FeedScreen.kt`

- [ ] **Step 1: Add Filter icon to SearchBar**
- [ ] **Step 2: Improve Empty State UI with icon**
- [ ] **Step 3: Commit**

```bash
git commit -m "ui: final polish of feed screen elements"
```
