# Context Menu Android

## Tech Stack
- **Language**: Kotlin | **UI**: XML Layouts + ViewBinding
- **Min SDK**: 26 (Android 8.0) | **Target SDK**: 34
- **Architecture**: Single Activity with RecyclerView + BottomSheetDialogFragment
- **Design System**: TDM tokens (matching iOS) | **Typography**: Roboto

## Project Structure
```
ContextMenuAndroid/
├── app/src/main/
│   ├── java/com/example/chatscreen/
│   │   ├── data/
│   │   │   └── SampleData.kt              # Sample messages for testing
│   │   ├── model/
│   │   │   └── Message.kt                 # Sealed class + Reaction data class
│   │   ├── ui/
│   │   │   ├── MainActivity.kt            # Chat screen + gesture handlers
│   │   │   ├── adapter/
│   │   │   │   └── MessageAdapter.kt      # RecyclerView adapter + ViewHolders
│   │   │   └── contextmenu/
│   │   │       ├── ContextMenuBottomSheet.kt  # Bottom sheet with reactions & actions
│   │   │       └── MenuAnimationStyle.kt      # Animation style enum
│   │   └── util/
│   │       ├── Constants.kt               # All magic numbers centralized
│   │       ├── CircleOutlineProvider.kt   # Circular avatar clipping
│   │       └── MessageGestureDetector.kt  # Custom gesture detection
│   ├── res/layout/
│   │   ├── activity_main.xml              # Main chat screen
│   │   ├── bottom_sheet_context_menu.xml  # Context menu layout
│   │   └── item_message_*.xml             # Message layouts
│   ├── res/drawable/                      # Icons, shapes, backgrounds
│   ├── res/values/                        # Colors, dimens, strings, themes
│   └── res/values-night/                  # Dark mode overrides
├── build.gradle.kts
└── context.md
```

## Constants (util/Constants.kt)

All magic numbers are centralized for easy maintenance:

```kotlin
// Animation timing
Constants.Animation.QUICK_MS           // 80ms
Constants.Animation.STANDARD_MS        // 150ms
Constants.Animation.MEDIUM_MS          // 200ms

// Bubble press feedback
Constants.BubbleAnimation.PRESSED_SCALE    // 0.96f
Constants.BubbleAnimation.PRESS_DURATION_MS // 80ms

// Haptic feedback
Constants.Haptic.TAP_AMPLITUDE         // 25
Constants.Haptic.LONG_PRESS_AMPLITUDE  // 50

// Quick reaction animation
Constants.QuickReaction.EMOJI_SIZE_SP  // 48f
Constants.QuickReaction.DEFAULT_EMOJI  // "❤️"

// Selection state
Constants.Selection.OVERLAY_COLOR      // "#2A2196F3"
Constants.Selection.CHECKMARK_SIZE_DP  // 28
```

## Context Menu (Bottom Sheet)

Tap on any message row (bubble or surrounding area) to show the context menu bottom sheet.

### Animation System
The context menu features a polished animation sequence:

1. **Haptic Feedback**: Light tick vibration (8ms, amplitude 25) on tap
2. **Bubble Animation**: Spring bounce effect (shrinks to 96% → springs back with overshoot)
3. **Menu Appearance**: Staggered item animations
   - Reactions pop in one by one (150ms duration, 30ms delay between each)
   - Action items slide in from left (150ms duration, 20ms delay between each)

### Animation Styles (MenuAnimationStyle)
```kotlin
enum class MenuAnimationStyle {
    TELEGRAM,   // Fade in with upward translation
    IMESSAGE,   // Scale up with overshoot
    WHATSAPP    // Staggered pop-in for reactions, slide-in for actions
}
```

### Components
- **Reaction bar**: Floating panel above bottom sheet
  - 👍 👎 🔥 👌 🤔 + add button
  - 25dp corner radius, evenly spaced with `layout_weight`
- **Drag handle**: 36x4dp rounded indicator
- **Actions list** (48dp height each):

| Action | Icon | Color |
|--------|------|-------|
| Ответить (Reply) | `ic_reply` | `text_primary_55` |
| Переслать (Forward) | `ic_forward` | `text_primary_55` |
| Прокомментировать (Comment) | `ic_comment` | `text_primary_55` |
| Закрепить (Pin) | `ic_pin` | `text_primary_55` |
| Копировать (Copy) | `ic_copy` | `text_primary_55` |
| Добавить метку (Add label) | `ic_tag` | `text_primary_55` |
| В сохраненное (Save) | `ic_saved` | `text_primary_55` |
| Просмотрено (Viewed) | `ic_read_done` | `text_primary_55` |
| Выбрать (Select) | `ic_check_outline` | `text_primary_55` |
| Удалить (Delete) | `ic_delete` | `system_danger` |

### Usage
```kotlin
// In MessageAdapter - gesture detector calls listener
gestureListener?.onMessageTap(message, message.text, isOutgoing, bubbleView)

// In MainActivity - show context menu
override fun onMessageTap(message: Message, messageText: String?, isOutgoing: Boolean, anchorView: View) {
    vibrate(Constants.Haptic.TAP_DURATION_MS, Constants.Haptic.TAP_AMPLITUDE)
    ContextMenuBottomSheet.newInstance(messageText, isOutgoing, MenuAnimationStyle.WHATSAPP)
        .show(supportFragmentManager, TAG_CONTEXT_MENU)
}
```

## Message Gesture System

Three-gesture system with visual feedback that follows finger state.

### Gesture Types
| Gesture | Action | Visual Feedback | Haptic |
|---------|--------|-----------------|--------|
| **Single tap** (200ms delay) | Open context menu | Scale down → bounce back | 8ms, amplitude 25 |
| **Double tap** | Quick reaction (❤️) | Heart emoji floats up | 5ms, amplitude 20 |
| **Long press** (~400ms) | Select message | Blue overlay + checkmark badge | 15ms, amplitude 50 |

### MessageGestureDetector
Custom gesture detector that separates visual feedback from gesture recognition:

```kotlin
class MessageGestureDetector(context: Context, listener: OnGestureListener) {
    interface OnGestureListener {
        fun onTouchDown()    // Finger pressed - scale down
        fun onTouchUp()      // Finger lifted - scale back
        fun onLongPress()    // Long press detected - select
        fun onDoubleTap()    // Double tap detected - react
        fun onSingleTap()    // Single tap confirmed - open menu
    }
}
```

### Selection State
When message is selected via long press:
- **Blue overlay** (16% opacity primary color) covers the bubble
- **Checkmark badge** (28dp circle) appears at bottom-right corner
- Badge animates in with overshoot interpolator
- Long press again to deselect (with fade-out animation)

### Quick Reaction Animation
Double tap triggers floating emoji:
1. Create 48sp emoji TextView at bubble center
2. Scale from 0.3 → 1.0 with fade in (150ms)
3. Hold for 200ms
4. Scale to 1.5 + translate up 100px + fade out (400ms)
5. Remove view

## Design System

Colors auto-adapt via `values/` and `values-night/` qualifiers.

### Key Colors
| Color | Light | Dark | Usage |
|-------|-------|------|-------|
| `background_base` | #FFFFFF | #1A1A1A | Main background |
| `background_sheet_or_modal` | #FFFFFF | #232325 | Bottom sheet |
| `background_message_screen` | #EFE7DE | #0F0F10 | Chat background |
| `text_primary` | #000000 | #FFFFFF | Primary text |
| `text_primary_55` | 55% black | 55% white | Icons |
| `my_message_start/end` | #FFFAF3 | #2B5D95/#284A70 | Outgoing gradient |
| `system_danger` | #E06141 | #E06141 | Delete actions |

## Message Types (model/Message.kt)
```kotlin
sealed class Message {
    data class OutgoingText(text: String, time: String, isEdited: Boolean, isRead: Boolean)
    data class IncomingText(senderName: String, senderAvatarIndex: Int, text: String, time: String)
    data class Voice(senderName: String, senderAvatarIndex: Int, duration: String, time: String, waveformHeights: List<Int>)
    data class Image(senderName: String, senderAvatarIndex: Int, imageResId: Int, time: String, reactions: List<Reaction>?)
    data class DateSeparator(date: String)
}

data class Reaction(val emoji: String, val count: Int)
```

## Key Files
| File | Purpose |
|------|---------|
| `ui/MainActivity.kt` | Chat UI, gesture handlers, selection/reaction logic |
| `ui/adapter/MessageAdapter.kt` | RecyclerView adapter with 5 ViewHolders |
| `ui/contextmenu/ContextMenuBottomSheet.kt` | Bottom sheet with reactions + actions |
| `ui/contextmenu/MenuAnimationStyle.kt` | Animation style enum |
| `util/Constants.kt` | All magic numbers centralized |
| `util/MessageGestureDetector.kt` | Custom gesture detection with touch state |
| `model/Message.kt` | Sealed class for message types |
| `data/SampleData.kt` | Sample messages for testing |

## Code Architecture

### Package Organization
- **data/**: Data sources (SampleData, future repositories)
- **model/**: Data classes (Message, Reaction)
- **ui/**: Activities, adapters, UI components
- **util/**: Utilities (Constants, gesture detector, outline provider)

### Performance Optimizations
- RecyclerView: `setHasFixedSize(true)` + `itemAnimator = null`
- Single shared `CircleOutlineProvider` instance
- Lazy initialization of `Vibrator` service
- Extracted gesture detector setup to reduce duplication

### Code Quality
- All magic numbers in `Constants.kt`
- String resources for all user-visible text
- Proper imports (no fully-qualified names in code)
- Extracted helper methods for readability

## Implemented Features
- [x] Chat screen UI (header, messages, input panel)
- [x] Message types: outgoing, incoming, voice, image, date separator
- [x] RecyclerView with ViewBinding
- [x] Light/dark theme toggle
- [x] TDM design system colors
- [x] Vector icons (24x24 normalized)
- [x] Edge-to-edge display with safe area handling
- [x] **Context menu bottom sheet** (tap on any message row)
- [x] **Three-gesture system** (tap, double-tap, long-press)
- [x] **Touch-based bubble animation** (follows finger)
- [x] **Message selection** with visual feedback
- [x] **Quick reaction** with floating emoji
- [x] **Haptic feedback** for all gestures
- [x] **Staggered menu animations** (WhatsApp style)
- [x] **Centralized constants** (Constants.kt)
- [x] **Clean package structure** (ui/, model/, util/, data/)

## Build & Run
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.chatscreen/.ui.MainActivity
```

## Future Enhancements
- [ ] Persist selection state across scroll
- [ ] Multi-select mode with action bar
- [ ] Reaction picker (full emoji set)
- [ ] Action handlers (reply, forward, pin, etc.)
- [ ] Two-state context menu (primary/secondary actions with toggle)
- [ ] Message snapshot in context menu overlay

## Testing
Tested on:
- **Physical device**: Samsung SM-G960F (Android 10)
- **Emulator**: Pixel 6 (Android 16 / API 36)

## Related
- **iOS Version**: Full context menu with animations, Apple Photos Lift style
- **Repository**: https://github.com/evgenyshkuratov-rgb/context-menu-android
