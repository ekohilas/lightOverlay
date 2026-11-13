# Equipment Set Scroll

A client-side [Necesse](https://store.steampowered.com/app/1169040/Necesse/) mod that switches between your
equipment sets (the numbered tabs above the armor slots) by scrolling horizontally: tilt the mouse wheel, or
swipe sideways on a trackpad.

- Scroll **right** for the next set, **left** for the previous one. Wraps from the last set back to the first.
- The new set's number pops up next to the cursor.
- Does nothing while you have only one set, and scrolling over a scrollable menu still scrolls the menu.
- One swipe moves one set: after a switch, horizontal scrolling is ignored for 150 ms (`cooldownms` in the config).

The switch goes through the same path as the inventory set tabs and the item set hotkeys, so it works in multiplayer
without the server needing the mod.

## Commands and config

| Command | Config key | Default | Effect |
| --- | --- | --- | --- |
| `/equipsetscroll` | `enabled` | on | Turn the mod on or off |
| `/equipsetscrollinvert` | `invert` | off | Scroll right for the previous set instead |
| `/equipsetscrollwrap` | `wrap` | on | Wrap around between the first and last set |
| | `showtext` | on | Show the set number next to the cursor |
| | `cooldownms` | 150 | Pause after a switch, in milliseconds (0-2000) |

## Install

Copy `dist/Equipment_Set_Scroll-1.3.3-1.0.jar` from the repository root into Necesse's `mods` folder.

## Building

Shares the build logic in `../gradle/main.gradle` with Light Overlay. Requires a JDK 17+ and Necesse (detected
automatically). From the repository root:

```bash
./gradlew -p equipmentSetScroll buildModJar
```

The jar is written to `equipmentSetScroll/build/jar/`. `./gradlew -p equipmentSetScroll runClient` launches the
game with the mod.
