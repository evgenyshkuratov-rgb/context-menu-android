# Context Menu Android

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: XML Layouts + ViewBinding
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **Architecture**: Single Activity with RecyclerView
- **Design System**: TDM tokens (matching iOS app)
- **Typography**: Roboto (Regular, Medium, Bold)

## Project Structure
```
ContextMenuAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/chatscreen/
│   │   │   ├── MainActivity.kt           # Single activity with chat UI
│   │   │   ├── Message.kt                # Message data class (sealed class)
│   │   │   ├── MessageAdapter.kt         # RecyclerView adapter with ViewHolders
│   │   │   └── CircleOutlineProvider.kt  # ViewOutlineProvider for circular avatars
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml           # Main chat screen layout
│   │   │   │   ├── item_message_outgoing.xml   # Outgoing text message
│   │   │   │   ├── item_message_incoming.xml   # Incoming text message
│   │   │   │   ├── item_message_voice.xml      # Voice message
│   │   │   │   ├── item_message_image.xml      # Image message with reactions
│   │   │   │   └── item_date_separator.xml     # Date separator pill
│   │   │   ├── drawable/
│   │   │   │   ├── bg_bubble_outgoing.xml      # Gradient bubble shape
│   │   │   │   ├── bg_bubble_incoming.xml      # Solid bubble shape
│   │   │   │   ├── bg_date_pill.xml            # Date separator background
│   │   │   │   ├── bg_reaction.xml             # Reaction bubble gradient
│   │   │   │   ├── bg_play_button.xml          # Play button circle
│   │   │   │   ├── bg_time_badge.xml           # Time badge for images
│   │   │   │   ├── bg_chevron_button.xml       # Chevron button background
│   │   │   │   ├── bg_add_reaction.xml         # Add reaction button
│   │   │   │   ├── ic_*.xml                    # Vector drawable icons
│   │   │   │   └── *.png                       # Image assets
│   │   │   ├── values/
│   │   │   │   ├── colors.xml                  # Light mode colors
│   │   │   │   ├── dimens.xml                  # Spacing & dimensions
│   │   │   │   ├── strings.xml                 # Text strings (Russian)
│   │   │   │   └── themes.xml                  # App theme
│   │   │   ├── values-night/
│   │   │   │   ├── colors.xml                  # Dark mode colors
│   │   │   │   └── themes.xml                  # Dark theme overrides
│   │   │   └── mipmap-*/                       # App launcher icons
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts                            # Root build file
├── settings.gradle.kts
├── gradle.properties                           # Gradle/AndroidX config
├── local.properties                            # SDK location (gitignored)
├── gradlew                                     # Gradle wrapper script
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── context.md                                  # This documentation file
```

## Drawable Assets

### Vector Icons (converted from iOS SVG)

All icons are normalized to a **24x24 viewport** with content centered using `<group>` transforms. This ensures consistent sizing and prevents stretching when icons have non-square original dimensions.

| Asset | Description | Size | Original SVG | Centering |
|-------|-------------|------|--------------|-----------|
| `ic_back` | Back navigation arrow | 24dp | 9x16 | translateX="7.5", translateY="4" |
| `ic_search` | Search icon | 24dp | 18x18 | Native (square) |
| `ic_call` | Phone call icon | 24dp | 18x18 | Native (square) |
| `ic_attach` | Attachment icon | 24dp | 14x22 | translateX="5", translateY="1" |
| `ic_chevron_up` | Chevron up arrow | 24dp | 14x8 | translateX="5", translateY="8" |
| `ic_label` | Label/tag icon | 24dp | 20x20 | translateX="2", translateY="2" |
| `ic_sticker` | Sticker icon | 24dp | 20x20 | translateX="2", translateY="2" |
| `ic_mic` | Microphone icon | 24dp | 18x23 | translateX="3", translateY="0.5" |
| `ic_play` | Play button icon | 24dp | 12x14 | translateX="6", translateY="5" |
| `ic_checkmark_read` | Double checkmark (read) | 16dp | 16x8 | Native |
| `ic_edit` | Edit indicator | 10dp | 10x10 | Native (square) |
| `ic_plus` | Add reaction button | 22dp | 22x22 | Native (square) |

**Icon Normalization Pattern:**
```xml
<vector android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <group android:translateX="X" android:translateY="Y">
        <path android:pathData="..." android:fillColor="#000000" />
    </group>
</vector>
```

### Image Assets (PNG)
| Asset | Description |
|-------|-------------|
| `avatar_1.png` | User avatar (Anna) |
| `avatar_2.png` | User avatar (Maxim) |
| `avatar_header.png` | Header avatar |
| `photo_sample.png` | Sample photo for image message |

### Drawable Shapes
| Asset | Description |
|-------|-------------|
| `bg_bubble_outgoing` | Gradient background for outgoing messages |
| `bg_bubble_incoming` | Solid background for incoming messages |
| `bg_date_pill` | Date separator pill background |
| `bg_reaction` | Reaction bubble with gradient |
| `bg_play_button` | Circular play button background |
| `bg_time_badge` | Semi-transparent time badge |
| `bg_chevron_button` | Chevron button circle |
| `bg_add_reaction` | Add reaction button circle |

## Design System (TDMColors)

Colors auto-adapt to Light/Dark mode via `values/` and `values-night/` resource qualifiers.

### Light Mode Colors
| Color | Value | Usage |
|-------|-------|-------|
| `background_base` | #FFFFFF | Main background |
| `background_second` | #F5F5F5 | Secondary background |
| `background_message_screen` | #EFE7DE | Chat background |
| `background_someone_message` | #FFFFFF | Incoming message bubble |
| `text_primary` | #000000 | Primary text |
| `text_secondary` | #8A8A8E | Secondary text |
| `my_message_start` | #FFFAF3 | Outgoing gradient start |
| `my_message_end` | #FFFAF3 | Outgoing gradient end |
| `primary_default` | #FF8C00 | Primary accent color |
| `system_danger` | #E06141 | Delete/error color |

### Dark Mode Colors
| Color | Value | Usage |
|-------|-------|-------|
| `background_base` | #1A1A1A | Main background |
| `background_second` | #313131 | Secondary background |
| `background_message_screen` | #0F0F10 | Chat background |
| `background_someone_message` | #373737 | Incoming message bubble |
| `text_primary` | #FFFFFF | Primary text |
| `text_secondary` | #8A8A8E | Secondary text |
| `my_message_start` | #2B5D95 | Outgoing gradient start |
| `my_message_end` | #284A70 | Outgoing gradient end |

### Avatar Colors (11 colors, index 0-10)
Used for sender name coloring based on user index:
- `avatar_0` through `avatar_10` with distinct colors for each

## Typography

### Font Styles (matching iOS Figma design)
| Style | Font | Size | Line Height |
|-------|------|------|-------------|
| Message text | Roboto Regular | 15sp | 20sp |
| Sender name | Roboto Medium | 16sp | 22sp |
| Time/Caption | Roboto Regular | 12sp | 14sp |
| Header title | Roboto Bold | 16sp | - |
| Header subtitle | Roboto Regular | 14sp | - |
| Date separator | Roboto Regular | 13sp | - |

## Key Dimensions (dp)

| Element | Size |
|---------|------|
| Header height | 56dp |
| Avatar (header) | 40dp |
| Avatar (messages) | 32dp |
| Bubble corner radius | 14dp |
| Bubble max width (outgoing) | 311dp |
| Bubble max width (incoming) | 287dp |
| Bubble padding horizontal | 12dp |
| Bubble padding vertical | 8dp |
| Message spacing | 12dp |
| Play button size | 40dp |
| Input panel height | 80dp |
| Toolbar buttons | 36-40dp |
| Icon size (standard) | 24dp |
| Icon alpha | 55% (0.55) |

## Message Types

Implemented as a Kotlin sealed class:

```kotlin
sealed class Message {
    data class OutgoingText(
        val text: String,
        val time: String,
        val isEdited: Boolean = false,
        val isRead: Boolean = true
    ) : Message()

    data class IncomingText(
        val senderName: String,
        val senderAvatarIndex: Int,
        val text: String,
        val time: String
    ) : Message()

    data class Voice(
        val senderName: String,
        val senderAvatarIndex: Int,
        val duration: String,
        val time: String,
        val waveformHeights: List<Int>
    ) : Message()

    data class Image(
        val senderName: String,
        val senderAvatarIndex: Int,
        val imageResId: Int,
        val time: String,
        val reactions: List<Reaction>? = null
    ) : Message()

    data class DateSeparator(val date: String) : Message()
}
```

## Current State

### Implemented Features
- [x] Chat screen UI with header, message list, input panel
- [x] Multiple message types (outgoing, incoming, voice, image, date separator)
- [x] RecyclerView with multiple ViewHolder types
- [x] ViewBinding for type-safe view access
- [x] Light/dark theme toggle in header
- [x] TDM design system colors (light + dark)
- [x] Roboto typography matching iOS specs
- [x] Vector drawable icons converted from iOS (normalized to 24x24 viewport)
- [x] Gradient backgrounds for outgoing messages (dark mode)
- [x] Circular avatars with ViewOutlineProvider
- [x] Waveform visualization for voice messages
- [x] Reactions display on image messages
- [x] Adaptive launcher icons
- [x] Icon centering fix - all icons properly centered in 24x24 viewport

### Sample Messages
The app displays sample messages matching the iOS version:
1. Date separator: "24 ноября"
2. Incoming (Anna): Long project discussion text
3. Outgoing: "Привет, как дела?"
4. Incoming (Anna): Voice message (0:18)
5. Outgoing (edited): Reply about sending materials
6. Incoming (Maxim): Image with 🔥 reaction

## Key Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Chat screen UI, theme toggle, RecyclerView setup |
| `Message.kt` | Sealed class for message types |
| `MessageAdapter.kt` | RecyclerView adapter with 5 ViewHolder types |
| `CircleOutlineProvider.kt` | ViewOutlineProvider for circular avatar clipping |
| `activity_main.xml` | Main layout with header, RecyclerView, input panel |
| `colors.xml` | Light mode TDM colors |
| `values-night/colors.xml` | Dark mode TDM colors |
| `dimens.xml` | All dimensions and spacing values |
| `themes.xml` | Material3 theme configuration |

## Build & Run

```bash
# From project root
./gradlew assembleDebug

# Install on connected device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk

# Or open in Android Studio
open -a "Android Studio" .
```

## Future Enhancements
- [ ] Add long press gesture for context menu
- [ ] Implement context menu overlay
- [ ] Add message interactions (copy, reply, edit, delete)
- [ ] Add keyboard handling for text input
- [ ] Connect to data layer

## Related Projects
- **iOS Version**: [Context-Menu-Optimization](https://github.com/evgenyshkuratov-rgb/Context-Menu-Optimization)
  - Full context menu implementation with animations
  - Perfect and Simple variants
  - Selection screen

## GitHub Repository
- **Repository**: `evgenyshkuratov-rgb/context-menu-android`
- **URL**: https://github.com/evgenyshkuratov-rgb/context-menu-android
- **Visibility**: Public
