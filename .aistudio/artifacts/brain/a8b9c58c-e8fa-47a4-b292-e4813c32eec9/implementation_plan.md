# Smooth TV Launcher

Smooth TV Launcher is a lightweight, high-performance, Arabic-first native Android TV and Google TV launcher crafted to faithfully reproduce the cinematic dark-navy and blue-purple reference design while adapting the editable vertical navigation menu to the right side of the widescreen (16:9) display. Every component is engineered for responsive D-pad remote navigation, real installed application discovery, live network validation, and deep local customization of menus, icons, Arabic fonts, and backgrounds without requiring internet access or background bloat.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following product and UX decisions were confirmed during interactive planning and will govern the launcher's behavior:

- **Confirmed Decision 1 — Editable Featured Banner Carousel**: The wide cinematic featured-content carousel on the home screen supports remote navigation between banners, Play/Open actions that launch installed apps or launcher sections, an option to disable automatic rotation, and full banner editing/customization inside Settings.
- **Confirmed Decision 2 — Movies, TV Series, and Files Sections**: Opening the Movies (الأفلام) or TV Series (المسلسلات) sections displays categorized media and streaming applications alongside quick access to local video media, while the Files (الملفات) section provides an integrated local storage and media file browser.
- **Confirmed Decision 3 — Dynamic Installed Application Row**: Rather than showing hardcoded demo placeholders when specific streaming apps (YouTube, Netflix, Prime Video, Disney+, Spotify) are absent, the bottom application row dynamically discovers and populates real installed device applications (prioritizing installed media/streaming apps first) alongside the persistent "كل التطبيقات" (All Applications) tile, with full support for pinning, reordering, and choosing home row apps.

---

### 1. Overview & Core Concept

- **What It Does**: Provides a complete native Android TV home screen experience in Arabic (RTL) featuring a minimal top status bar (real-time clock, Arabic date, and validated internet connectivity indicator), a right-docked vertical navigation menu, a cinematic featured banner carousel, and a customizable bottom row of installed application tiles, backed by an 11-section Settings suite.
- **Target Audience / Persona**: Arabic-speaking Android TV, Google TV, and set-top box users seeking a fast, visually refined, clutter-free television launcher with complete control over navigation items, Arabic typography, icon geometry, and wallpapers.
- **Key Value**: Combines the visual polish of a cinematic widescreen interface with ultra-low resource consumption, zero cloud/account dependencies, instant live-preview customization, and predictable D-pad focus management.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Home Screen & Remote Navigation**: Upon launch, focus starts predictably on the active right-side navigation item (`الرئيسية`) or main content area. Pressing Left moves focus from the right-side vertical menu into the featured banner carousel or bottom application row; pressing Up/Down moves cleanly between the top banner and bottom application tiles; pressing OK launches the selected application or banner action.
  2. **Section Switching**: Selecting items on the right-side vertical navigation menu (`الرئيسية`, `التطبيقات`, `الأفلام`, `المسلسلات`, `المفضلة`, `الملفات`, `الإعدادات`) smoothly switches the left main content pane while keeping the right-side menu visually anchored and consistent.
  3. **Customizing the Right-Side Menu (`تخصيص القائمة الرئيسية` / Customize Home Menu)**: Inside Settings, users can move items up or down with remote-friendly buttons, rename labels in Arabic or custom text, hide items into a Hidden Items list (with one-click Restore and position selection), remove unneeded sections with an optional confirmation dialog, add real device shortcuts (specific installed apps, Display Settings, Network Settings, or media sections), or reset to the default menu.
  4. **Tailoring Icons, Fonts, and Backgrounds**: Users can adjust icon shapes (Original, Square, Rounded Square, Circle, Rounded Rectangular Tile), sizes (Extra Small to Extra Large presets plus a precision slider), spacing, corner radius, opacity, and focus glow/border thickness; switch between 8 bundled Arabic fonts (`Cairo` default, `Amiri`, `Tajawal`, `IBM Plex Sans Arabic`, `Noto Sans Arabic`, `Noto Naskh Arabic`, `Readex Pro`, `Almarai`) with weight and per-element size controls; and choose built-in gradients/landscapes or import custom JPEG/PNG/WebP wallpapers via the system picker with preview and scaling modes (`Fill Screen`, `Fit Image`, `Center Crop`).

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Cinematic midnight-navy television interface with sweeping curved blue-purple ambient waves, frosted glass surfaces, and luminous remote-focus borders that strictly mirror the reference specification.
  - *Color Palette & Mood*:
    - `--background`: Deep Midnight Navy (`#070B19` to `#0D132A`) with subtle curved radial/linear ambient waves in `#1D2564` and `#4A1D88`.
    - `--surface`: Translucent Dark Slate (`#141B34` at 75%–85% opacity) for the right navigation dock and app tile containers.
    - `--primary`: Electric Blue-Purple Gradient (`#6C3CE9` to `#2663EB`) for active navigation pills, carousel indicators, and primary highlights.
    - `--accent`: Luminous Cyan-Violet Focus Glow (`#5CE1E6` to `#8B5CF6`) for active remote focus borders and subtle outer glow effects.
    - `--status-error`: Coral Red (`#EF4444`) for the diagonal slash on the disconnected/unvalidated Wi-Fi indicator.
  - *Typography & Hierarchy*: Bundled local Arabic Google Fonts with `Cairo` as the default family. Clean white primary text (`#FFFFFF`), soft lavender-gray secondary text (`#B4BCD0`), proper Arabic letter shaping, connected cursive rendering, and strict RTL layout flow.
  - *Component Styling & Layout*: Widescreen 16:9 horizontal split with the compact vertical navigation dock on the far right, main content stage on the left, zero top-left branding/logos, no Bluetooth or notification icons in the top bar, generous 24dp–32dp outer margins, and rounded rectangular app cards along the bottom.

- **Interactive Feedback & Motion**:
  - Responsive D-pad focus scaling and luminous border transitions (with instant/static mode when Reduced Effects / Performance Mode is enabled).
  - Real-time preview and immediate UI updates across all customization screens without requiring an app restart.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Native Jetpack Compose TV Architecture with Custom Canvas & Modifier Focus System**:
  - *Chosen Approach*: Build the UI in Kotlin with Jetpack Compose using lightweight custom `Canvas` paths for the curved midnight-purple background waves, explicit `FocusRequester` trees for deterministic D-pad navigation, and hardware-accelerated clipping/draw modifiers for icon shapes and focus glows.
  - *Why*: Delivers exact visual fidelity to the reference artwork, eliminates heavy external UI framework overhead, keeps APK size minimal, and guarantees smooth focus transitions on television hardware.
  - *Alternatives Considered*: XML Leanback templates (too rigid and replaces the reference design with a generic template) or WebView wrapper (violates native performance and memory goals).

- **Decision 2 — Local JSON & SharedPreferences Persistence Without Heavy Database Overhead**:
  - *Chosen Approach*: Store all menu configurations (order, hidden/removed states, custom labels, shortcuts), icon customization parameters, typography selections, banner items, favorite apps, and background URI/scaling settings in a structured local preference repository backed by `SharedPreferences` and lightweight JSON serialization.
  - *Why*: Loads synchronously/instantly on cold start so the launcher home screen renders immediately on boot without database initialization latency, while surviving app closure and device restarts reliably.
  - *Alternatives Considered*: Cloud storage or heavy ORM layers (unnecessary overhead for local launcher configuration and adds cold-start cost).

- **Decision 3 — Bundled Static Arabic Fonts (`res/font/`) & Subsampled Wallpaper Decoding**:
  - *Chosen Approach*: Bundle all 8 required Arabic font families locally in `res/font/` and decode user-selected background images off the main thread using `BitmapFactory.Options.inSampleSize` capped to display bounds, persisting URI read permissions via `takePersistableUriPermission`.
  - *Why*: Guarantees offline startup, zero runtime font download failures, and low RAM footprint even if a user selects a 4K/8K image from local storage.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Smooth TV Launcher (MainActivity)                    │
│        16:9 Widescreen Stage • RTL LayoutDirection • D-Pad Focus Root        │
└───────────────────────────────────────┬──────────────────────────────────────┘
                                        │
        ┌───────────────────────────────┴───────────────────────────────┐
        ▼                                                               ▼
┌───────────────────────────────────────────────┐       ┌──────────────────────┐
│           Left Main Content Stage             │       │  Right Vertical Nav  │
│ ┌───────────────────────────────────────────┐ │       │  Dock (Editable RTL) │
│ │ Top Status Area (Left-aligned in RTL)     │ │       │                      │
│ │ • Live Clock (12h/24h) & Arabic Date      │ │       │ 1. الرئيسية (Home)   │
│ │ • Real-Time Validated Wi-Fi Indicator     │ │       │ 2. التطبيقات (Apps)  │
│ └───────────────────────────────────────────┘ │       │ 3. الأفلام (Movies)   │
│ ┌───────────────────────────────────────────┐ │◄─────►│ 4. المسلسلات (Series) │
│ │ Active Section Viewport                   │ │ D-Pad │ 5. المفضلة (Favs)    │
│ │ • Home: Featured Banner Carousel +        │ │ Focus │ 6. الملفات (Files)   │
│ │         Dynamic Installed App Row         │ │       │ 7. الإعدادات (Config)│
│ │ • Apps / Movies / Series / Favorites      │ │       │ + Custom Shortcuts   │
│ │ • Local Files & Media Browser             │ │       └──────────────────────┘
│ │ • 11-Section Settings & Live Customizers  │ │
│ └───────────────────────────────────────────┘ │
└───────────────────────┬───────────────────────┘
                        │
        ┌───────────────┼───────────────────────────────┐
        ▼               ▼                               ▼
┌───────────────┐ ┌─────────────────────────────┐ ┌────────────────────────────┐
│ AppDiscovery  │ │ LauncherPreferencesRepo     │ │ System Monitor Services    │
│ & LaunchMgr   │ │ • Menu Order/Hidden/Labels  │ │ • ConnectivityManager      │
│ • PackageMgr  │ │ • Icon Shape/Size/Glow/Gap  │ │   NetworkCallback          │
│ • Leanback &  │ │ • Arabic Font & Weight/Size │ │   (NET_CAPABILITY_VALIDATED│
│   Main Intents│ │ • Wallpaper URI & ScaleMode │ │    real-time flow)         │
│ • Icon Cache  │ │ • Featured Banner Configs   │ │ • Lifecycle-aware Clock    │
│ • Safe Launch │ │ • Reduced Effects & Layout  │ │   (ACTION_TIME_TICK)       │
└───────────────┘ └─────────────────────────────┘ └────────────────────────────┘
```

- **Data Model & State**:
  - `NavMenuItem`: Represents each right-side navigation entry (`id`, `defaultTitleAr`, `customTitle`, `iconType`, `itemType` [BuiltInSection, AppShortcut, SystemSettingShortcut], `targetPackageOrAction`, `visibilityState` [Visible, Hidden, Removed], `orderIndex`).
  - `AppItem`: Represents an installed Android application discovered via `PackageManager` (`packageName`, `label`, `iconDrawable`, `isBannerOrTvApp`, `category`, `isFavorite`, `homeRowOrder`).
  - `FeaturedBannerItem`: Represents a slide in the home featured carousel (`id`, `title`, `subtitle`, `backgroundPresetOrUri`, `actionLabel`, `targetType` [LaunchApp, OpenSection], `targetValue`).
  - `IconConfig`: Stores shape (`ORIGINAL`, `SQUARE`, `ROUNDED_SQUARE`, `CIRCLE`, `ROUNDED_RECT`), size preset and exact dp slider value (`48dp`–`140dp`), show/hide label toggle, label font size, inter-icon spacing, corner radius, tile background opacity, focus glow toggle, and focus border thickness.
  - `TypographyConfig`: Stores selected font family (`CAIRO`, `AMIRI`, `TAJAWAL`, `IBM_PLEX_SANS_ARABIC`, `NOTO_SANS_ARABIC`, `NOTO_Naskh_ARABIC`, `READEX_PRO`, `ALMARAI`), font weight, global scale, app label size, clock/date sizes, and navigation menu text size.
  - `BackgroundConfig`: Stores background type (`BUILT_IN_PRESET` across Dark Navy, Blue-Purple, Natural Landscape, Night City, and Minimal Abstract, or `CUSTOM_URI`), scaling mode (`FILL_SCREEN`, `FIT_IMAGE`, `CENTER_CROP`), and cached decoded bitmap state.
  - `NetworkStatus`: Sealed state (`ConnectedValidated`, `ConnectedNoInternet`, `Disconnected`) driven by `ConnectivityManager.NetworkCallback`.

- **Interactive Component & State Mapping**:
  - **Home Screen & Banner Carousel**: Left/Right D-pad buttons cycle through featured banners (or auto-advance when rotation is enabled); pressing Select on the banner button executes its configured action (opening a target section or launching an installed app); Down moves focus to the dynamic installed application row where users can press Select to launch or Long-Press / Menu to favorite, reorder, or pin/unpin from the home row.
  - **Customize Home Menu Screen**: Provides direct interactive buttons per item for Move Up (`تحريك لأعلى`), Move Down (`تحريك لأسفل`), Rename (`إعادة تسمية` via Arabic dialog), Hide (`إخفاء`), and Remove (`إزالة` with optional confirmation dialog), plus a Hidden Items drawer with Restore (`استعادة` with position picker), an Add Shortcut modal (`إضافة اختصار` for installed apps, Display Settings, Network Settings, or media sections), and Restore Default Menu (`استعادة القائمة الافتراضية`).
  - **11-Section Settings Suite**: Houses dedicated, live-updating sub-screens for General Settings (`الإعدادات العامة`), Customize Home Menu (`تخصيص القائمة الرئيسية`), Icon Customization (`تخصيص الأيقونات`), Fonts and Typography (`الخطوط والطباعة`), Background and Appearance (`الخلفية والمظهر`), Clock and Date (`الساعة والتاريخ`), Home Screen Layout (`تخطيط الشاشة الرئيسية`), Remote Navigation (`التحكم عن بعد`), Performance Options (`خيارات الأداء` including low-effects mode), Restore Default Settings (`استعادة الإعدادات الافتراضية`), and Application Information (`معلومات التطبيق` including TV launcher activation guidance and manufacturer notes).
