# Notilog: Android Historical Notification App - Comprehensive Design Spec

## Purpose
An Android application designed to intercept, store, and organize incoming notifications. Its primary use case is searching for specific dismissed notifications, recovering lost 2FA codes, and reading deleted messages from chat apps.

---

## Part 1: User Experience (UX) Design

### 1. Skeleton & Information Architecture
*   **Hierarchical Levels:**
    *   **Level 0 (Root):** Bottom Navigation (Feed, Groups, Settings).
    *   **Level 1 (Browsing):** Search results, Grouped app lists, Category lists.
    *   **Level 2 (Detail):** Individual notification version history (thread view).
*   **Navigation Flow:**
    1.  **Search & Recovery:** User opens app -> Taps Search Bar -> Types keyword -> List filters in real-time -> Taps item -> Views message history.
    2.  **Notification Discovery:** User receives "Message Deleted" -> Opens Notilog -> Filters by "Social" -> Locates original notification record.
    3.  **App Management:** Long-press on any card -> Enters multi-select mode -> Selects multiple apps -> Taps "Blacklist" icon in top bar.

### 2. Prototyping & Interaction Model
*   **Transitions:**
    *   **Shared Element Motion:** When tapping a notification, the App Icon from the list card scales and moves to the header of the Detail View.
    *   **Container Transform:** Cards expand outwards to fill the screen on tap.
*   **Micro-interactions:**
    *   **Ripple Feedback:** Standard Material ripple on all clickable surfaces.
    *   **Swipe Actions:**
        *   **Swipe Right:** Reveals "Blacklist" (Red background with icon).
        *   **Swipe Left:** Reveals "Delete" (Grey background with icon).
    *   **Haptic Feedback:** Subtle vibration on long-press and successful blacklist action.
*   **State Transitions:**
    *   **Loading:** Skeleton Screen Shimmer (simulated card shapes with a moving gradient).
    *   **Empty State:** Custom illustration with text ("No notifications found") when filters yield zero results.

### 3. Platform-Specific UX (Accessibility & System)
*   **Accessibility (a11y):**
    *   **Tap Targets:** All touchable elements (chips, icons) are minimum 48x48dp.
    *   **Content Descriptions:** Meaningful descriptions for screen readers (e.g., "WhatsApp icon", "Add to blacklist").
    *   **Contrast:** Minimum 4.5:1 contrast ratio for all text.
*   **Dark Mode:** Full alternate theme mapping. Surfaces shift to Dark Grey (`#1C1B1F`) with adjusted primary tones for visibility.

---

## Part 2: User Interface (UI) Design

### 1. Visual Design (Aesthetics & Brand)
*   **Brand Personality:** Clean, Reliable, Minimalist, Native.
*   **Color Palette (Material You):**
    *   **Dynamic:** Primary/Secondary derived from user wallpaper (Android 12+).
    *   **Static Fallback:** 
        *   Primary: `#4F46E5` (Indigo)
        *   Error: `#DC2626` (Red)
        *   Neutral: Slate shades for text and borders.
*   **Typography Scale:**
    *   **Headline Large:** *Outfit Bold*, 32sp (Zero-state headers).
    *   **Title Medium:** *Inter SemiBold*, 16sp (Notification Titles/Senders).
    *   **Body Medium:** *Inter Regular*, 14sp (Message content).
    *   **Label Small:** *Inter Medium*, 11sp (Metadata/Timestamps).
*   **Iconography (Variable Material Symbols):** 
    *   **Style:** Material Symbols (Rounded & Filled).
    *   **Implementation:** Utilizes the **Material Symbols Variable Font**. This allows dynamic, screen-aware adjustments of icon properties.
    *   **Variable Properties:**
        *   **Fill:** `1` (Always Filled).
        *   **Weight:** `400` (Standard), auto-adjusted to `500` for emphasized actions.
        *   **Grade:** `-25` for dark mode (to reduce "glow"), `0` for light mode.
        *   **Optical Size:** Auto-scaled (20dp to 48dp) to match the component context and screen density, ensuring stroke consistency.

### 2. Surface & Depth (Material 3)
*   **Z-Axis Hierarchy:**
    *   **Surface 0 (Background):** Slate 50 / Dark Grey.
    *   **Surface 1 (Cards):** Tonal Elevation Level 1.
    *   **Surface 2 (Search Bar):** Tonal Elevation Level 2 + 8dp shadow.
    *   **Surface 3 (Dialogs/Popups):** Tonal Elevation Level 3 + Scrim background.
*   **Surface Treatments:**
    *   **Glassmorphism:** Navigation Bar and Top Search Bar utilize `backdrop-filter: blur(20px)` on Android 12+.
    *   **Rounded Corners:** 16dp for Cards, 8dp for Chips.

### 3. Layout & Adaptability
*   **Spacing System:** 8dp Baseline Grid (8, 16, 24, 32). 16dp horizontal padding for all content.
*   **Responsive Breakpoints:**
    *   **Compact (<600dp):** Bottom navigation.
    *   **Expanded (>600dp):** Navigation Rail + List-Detail view (two-pane layout).

### 4. Navigation Component (Bottom Bar)
*   **Implementation:** Material Design 3 `NavigationBar`.
*   **Visuals:**
    *   **Active State:** Icon contained within a pill-shaped `ActiveIndicator` (Primary Container color). Label text becomes Bold and Primary color.
    *   **Inactive State:** Icon only (no container), Label text Regular and Medium Grey.
    *   **Surface:** Level 2 Tonal Elevation with a subtle Glassmorphism blur (Android 12+).
*   **Tabs & Icons (Material Symbols Rounded):**
    1.  **Feed:** 
        *   Icon: `history` or `notifications`.
        *   Label: "Feed".
        *   Destination: Main chronological log.
    2.  **Categories:** 
        *   Icon: `grid_view` or `category`.
        *   Label: "Groups".
        *   Destination: App and category-based clusters.
    3.  **Settings:** 
        *   Icon: `settings`.
        *   Label: "Settings".
        *   Destination: Backup, privacy, and system configuration.

### 5. Screen-by-Screen Definitions

#### Screen A: Feed Screen (Home)
*   **Purpose:** The primary inbox for all incoming notification history.
*   **Top App Bar:** 
    *   Sticky `SearchBar` with "Filter" icon button (right) and "User Profile/Backup Status" icon (left).
*   **Filters Area:** 
    *   Horizontal carousel of `FilterChips` (All, Social, Banking, Shopping, System).
*   **Main List:** 
    *   `LazyColumn` of `ElevatedCards`.
    *   **Card Contents:** 
        *   Left: App Icon (32dp x 32dp).
        *   Center: App Name (Title Small), Title/Sender (Title Medium), Content snippet (Body Medium - max 2 lines).
        *   Right: Timestamp (Label Small) + "Chevron Right" indicator.
*   **Actions:** 
    *   Swipe Card Right: `Blacklist` action (Red background, Icon: `Block`).
    *   Swipe Card Left: `Delete` action (Grey background, Icon: `Delete`).
*   **Bottom Navigation:** `Feed` icon active.

#### Screen B: Notification Detail Screen
*   **Purpose:** View the complete version history of a single notification (e.g., recovered deleted text).
*   **Top App Bar:** Back button, App Name, "Global Blacklist" toggle switch.
*   **Header:** Large App Icon (48dp), App Name, Package Name (e.g., `com.whatsapp`).
*   **Content:** 
    *   Chronological vertical list of "Versions".
    *   Each version card shows:
        *   Timestamp (e.g., "Edited at 10:45 AM").
        *   Full text content (selectable).
        *   Raw data expander (shows system intent extras).
*   **Bottom Action Bar:** "Copy Message", "Share", "Delete All Versions".

#### Screen C: Categories & Groups Screen
*   **Purpose:** Browse notifications grouped by category or app.
*   **Top App Bar:** Title "Categories", Search icon.
*   **Layout:** 
    *   Two-column Grid of `OutlinedCard` categories (Social, Banking, etc.).
    *   Below the grid: "Top Apps" list showing apps with the most notification counts.
*   **Buttons:** 
    *   Each category card shows: Category Icon + Name + "X notifications total".
*   **Navigation:** Tapping a Category/App navigates to a filtered version of the Feed Screen.

#### Screen D: Settings Screen
*   **Purpose:** Manage app behavior, backup, and privacy.
*   **Layout:** Standard Material Settings list.
*   **Sections & Buttons:**
    *   **Cloud Backup:** "Connect Google Drive" button, "Sync Now" button, Last sync timestamp.
    *   **Data Retention:** "Auto-cleanup" toggle, Dropdown for "30 / 60 / 90 days".
    *   **Privacy:** "Manage Blacklist" button (navigates to list of blacklisted packages).
    *   **System:** "Check Permissions" button (verifies Notification Access).

---

## Part 3: Backend & Technical Architecture

### 1. Tech Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (MVVM + Clean Architecture)
*   **Database:** Room (SQLite) with WAL (Write-Ahead Logging) enabled for concurrent service/UI access.
*   **Service:** `NotificationListenerService`
*   **Asynchrony:** Coroutines & Flow for reactive UI updates.
*   **DI:** Hilt
*   **Cloud SDKs:** Google Play Services (Drive), Microsoft Authentication Library (MSAL) for Android.

### 2. Database Schema (Room)
*   **Table: `notifications`**
    *   `id` (Long, PK): Unique identifier.
    *   `system_id` (Int, Indexed): Android notification ID for version grouping.
    *   `tag` (String, Nullable): System tag for unique identification.
    *   `package_name`, `app_name`, `title`, `text_content`.
    *   `post_time` (Long, Indexed): Unix timestamp.
    *   `is_dismissed` (Boolean).
    *   `category` (String).
*   **Table: `blacklisted_apps`**
    *   `package_name` (String, PK).
*   **Table: `category_overrides`** (New)
    *   `package_name` (String, PK), `manual_category` (String).

### 3. Cloud Backup & Authentication Logic

#### A. Google Drive Authentication (OAuth 2.0)
*   **Client Setup:** App registers in Google Cloud Console with Android package name and SHA-1 certificate.
*   **Auth Flow:**
    1.  User taps "Connect Google Drive" -> App triggers `GoogleSignInClient.getSignInIntent()`.
    2.  User selects account and grants `DriveScopes.DRIVE_APPDATA` (access restricted to a private hidden folder Notilog creates).
    3.  App receives `GoogleSignInAccount` containing an **Auth Code**.
    4.  App exchanges Code for a **Refresh Token** (stored securely in EncryptedSharedPreferences).
*   **Silent Sign-In:** On subsequent backups, app uses `GoogleSignInClient.silentSignIn()` to refresh the **Access Token** without user interaction.

#### B. OneDrive Authentication (MSAL)
*   **Client Setup:** App registers in Microsoft Entra (Azure AD).
*   **Auth Flow:**
    1.  App initializes `PublicClientApplication` with a JSON configuration.
    2.  User triggers `acquireToken()` with `Files.ReadWrite.AppFolder` scope.
    3.  MSAL handles the webview/browser handshake and returns an `IAuthenticationResult`.
    4.  **Token Persistence:** MSAL automatically caches tokens in a secure internal storage; the app queries the cache via `acquireTokenSilent()` for background backup jobs.

#### C. Backup Execution Profile
1.  **Checkpoint:** Run `PRAGMA wal_checkpoint(FULL)` to merge `-shm` and `-wal` files into the main `.db`.
2.  **Compression:** Archive the `.db` file using `GZIPOutputStream` to reduce upload bandwidth.
3.  **Upload:** Use the respective REST API (Drive `files.create` or OneDrive `uploadSession`) to transmit the archive to the **App-Specific Hidden Folder**.

### 4. Core Logic & Services
*   **`NotilogListenerService`**:
    *   **Versioning:** Every `onNotificationPosted` event creates a new entry.
    *   **Deduplication:** Before insert, query the latest entry for the same `system_id` + `tag`. If `title` and `text_content` match exactly, discard the update to prevent DB bloat.
*   **Category Engine**:
    *   Loads `categories_map.json` into a Memory Cache (HashMap) on app start.
    *   Prioritizes `category_overrides` table -> then `categories_map.json` -> defaults to "Uncategorized".
*   **Auto-cleanup Worker**: 
    *   A periodic `WorkManager` job (runs at 3 AM).
    *   Query: `DELETE FROM notifications WHERE post_time < :threshold` where threshold is (CurrentTime - UserPrefDays).
