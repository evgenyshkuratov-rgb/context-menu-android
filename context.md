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
│   │   ├── MainActivity.kt              # Chat UI, theme toggle, context menu trigger
│   │   ├── Message.kt                   # Sealed class for message types
│   │   ├── MessageAdapter.kt            # RecyclerView adapter with click handling
│   │   ├── ContextMenuBottomSheet.kt    # Bottom sheet with reactions & actions
│   │   └── CircleOutlineProvider.kt     # Circular avatar clipping
│   ├── res/layout/
│   │   ├── activity_main.xml            # Main chat screen
│   │   ├── bottom_sheet_context_menu.xml # Context menu layout
│   │   └── item_message_*.xml           # Message layouts (outgoing, incoming, voice, image, date)
│   ├── res/drawable/                    # Icons, shapes, backgrounds
│   ├── res/values/                      # Colors, dimens, strings, themes
│   └── res/values-night/                # Dark mode overrides
├── build.gradle.kts
└── context.md
```

## Context Menu (Bottom Sheet)

Tap on any message row (bubble or surrounding area) to show the context menu bottom sheet.

### Animation System
The context menu features a polished animation sequence:

1. **Haptic Feedback**: Light tick vibration (20ms) on tap
2. **Bubble Animation**: Spring bounce effect (shrinks to 88% → springs back with overshoot)
3. **Menu Appearance**: Staggered item animations
   - Reactions pop in one by one (40ms delay between each, overshoot interpolator)
   - Action items slide in from left (30ms delay between each)

**Dependencies**: `androidx.dynamicanimation:dynamicanimation:1.0.0` for SpringAnimation

**Permissions**: `android.permission.VIBRATE` for haptic feedback

### Components
- **Reaction bar**: Separate floating panel above bottom sheet (transparent dialog background)
  - Rounded container (25dp radius) with `background_sheet_or_modal` color
  - Full width with 8dp horizontal margins
  - 👍 👎 🔥 👌 🤔 + add button (evenly spaced with `layout_weight`)
  - Add button has circular `background_second` background
  - 12dp horizontal padding, 6dp vertical padding, 8dp gap below panel
  - Ripple effect on press (no visible default backgrounds on emojis)
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

### Drawables
| Asset | Description |
|-------|-------------|
| `bg_bottom_sheet` | Rounded top corners (20dp) for main sheet, extends to bottom with 24dp padding |
| `bg_reaction_bar` | Rounded container (25dp) for reaction panel |
| `bg_drag_handle` | Gray pill indicator |
| `bg_reaction_item` | Ripple effect for emoji buttons (transparent default, oval mask) |
| `bg_add_reaction_button` | Circular background (`background_second`) with ripple for plus button |
| `bg_action_item` | Pressed state for action rows |

### Usage
```kotlin
// In MessageAdapter - click listener on root for larger tap area
binding.root.setOnClickListener {
    onMessageClickListener?.onMessageClick(message, message.text, isOutgoing, binding.bubbleContainer)
}

// In MainActivity - animate bubble then show menu
override fun onMessageClick(message: Message, messageText: String?, isOutgoing: Boolean, anchorView: View) {
    animateBubble(anchorView) {
        val bottomSheet = ContextMenuBottomSheet.newInstance(messageText, isOutgoing, MenuAnimationStyle.WHATSAPP)
        bottomSheet.show(supportFragmentManager, "ContextMenuBottomSheet")
    }
}

private fun animateBubble(view: View, onComplete: () -> Unit) {
    // Haptic feedback
    vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

    // Spring bounce animation
    view.animate().scaleX(0.88f).scaleY(0.88f).setDuration(100).withEndAction {
        SpringAnimation(view, SpringAnimation.SCALE_X, 1f).apply {
            spring.stiffness = SpringForce.STIFFNESS_LOW
            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            start()
        }
        SpringAnimation(view, SpringAnimation.SCALE_Y, 1f).apply {
            spring.stiffness = SpringForce.STIFFNESS_LOW
            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            start()
        }
        view.postDelayed({ onComplete() }, 150)
    }.start()
}
```

## Vector Icons

All icons normalized to **24x24 viewport**. Non-square icons use `<group>` transforms for centering.

### Chat UI Icons
| Icon | Description | Notes |
|------|-------------|-------|
| `ic_back`, `ic_search`, `ic_call` | Header navigation | Centered with translateX/Y |
| `ic_attach`, `ic_label`, `ic_sticker`, `ic_mic` | Input toolbar | `fillType="evenOdd"` for outlines |
| `ic_play`, `ic_plus` | UI controls | |
| `ic_checkmark_read` (16dp), `ic_edit` (10dp) | Message indicators | Native size |
| `ic_theme_light`, `ic_theme_dark` | Theme toggle | Sun/moon icons |

### Context Menu Icons
| Icon | Description |
|------|-------------|
| `ic_reply` | Reply arrow |
| `ic_forward` | Forward arrow |
| `ic_comment` | Comment bubbles |
| `ic_pin` | Pin/tack |
| `ic_copy` | Copy squares |
| `ic_tag` | Label tag (stroke) |
| `ic_saved` | Bookmark (stroke) |
| `ic_read_done` | Double checkmark |
| `ic_check_outline` | Circle checkmark |
| `ic_delete` | Trash bin (red #E06141) |

### Image Assets
`avatar_1.png`, `avatar_2.png`, `avatar_header.png`, `photo_sample.png`

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
| `text_primary_08` | 8% black | 8% white | Pressed states |
| `my_message_start/end` | #FFFAF3 | #2B5D95/#284A70 | Outgoing gradient |
| `system_danger` | #E06141 | #E06141 | Delete actions |

### Typography
| Style | Size | Font |
|-------|------|------|
| Message text | 15sp + 2sp line spacing | `sans-serif` |
| Sender name | 16sp | `sans-serif-medium` |
| Time/Caption | 12sp | `sans-serif` |
| Context menu action | 15sp | `sans-serif` |

## Key Dimensions
| Element | Size |
|---------|------|
| Header height | 56dp |
| Avatar (header/messages) | 40dp / 32dp |
| Bubble max width | 311dp (out) / 287dp (in) |
| Bubble padding | 12dp H / 8dp V |
| Icon size / alpha | 24dp / 55% |
| Context menu action height | 48dp |
| Reaction button | 44dp |

## Message Types
```kotlin
sealed class Message {
    data class OutgoingText(text: String, time: String, isEdited: Boolean, isRead: Boolean)
    data class IncomingText(senderName: String, senderAvatarIndex: Int, text: String, time: String)
    data class Voice(senderName: String, senderAvatarIndex: Int, duration: String, time: String, waveformHeights: List<Int>)
    data class Image(senderName: String, senderAvatarIndex: Int, imageResId: Int, time: String, reactions: List<Reaction>?)
    data class DateSeparator(date: String)
}
```

## Implemented Features
- [x] Chat screen UI (header, messages, input panel)
- [x] Message types: outgoing, incoming, voice, image, date separator
- [x] RecyclerView with ViewBinding
- [x] Light/dark theme toggle
- [x] TDM design system colors
- [x] Vector icons from iOS (24x24 normalized)
- [x] Edge-to-edge display with safe area handling
- [x] Circular avatars, waveform visualization, reactions
- [x] **Context menu bottom sheet** (tap on any message row)
- [x] Transparent dialog background (reaction bar and sheet are separate visual elements)
- [x] Reaction bar: full-width, evenly spaced emojis, circular add button with background
- [x] Bottom sheet extends to screen bottom with internal padding
- [x] 10 action items with icons and press states
- [x] Copy action copies text to clipboard
- [x] Simplified input toolbar (removed chevron button)
- [x] **Expanded tap area** (entire message row, not just bubble)
- [x] **Haptic feedback** (light tick vibration on tap)
- [x] **Spring bounce bubble animation** (shrink → spring back)
- [x] **Staggered menu animations** (reactions pop in, actions slide in)

## Build & Run
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.chatscreen/.MainActivity
```

## Future Enhancements
- [ ] Long press gesture (currently uses tap)
- [ ] Action handlers (reply, forward, pin, etc.)
- [ ] Keyboard handling for text input
- [ ] Connect to data layer
- [ ] Two-state context menu (primary/secondary actions with toggle)
- [ ] Message snapshot in context menu overlay

## Testing
Tested on:
- **Physical device**: Samsung SM-G960F (Android 10)
- **Emulator**: Pixel 6 (Android 16 / API 36)

## Related
- **iOS Version**: Full context menu with animations, Apple Photos Lift style
- **Repository**: https://github.com/evgenyshkuratov-rgb/context-menu-android
