# Equipment Set Scroll

A client-side [Necesse](https://store.steampowered.com/app/1169040/Necesse/) mod that switches between your
equipment sets (the numbered tabs above the armor slots) by scrolling horizontally: tilt the mouse wheel, or
swipe sideways on a trackpad.

- Scroll **right** for the next set, **left** for the previous one. Wraps from the last set back to the first.
- The new set's number pops up next to the cursor.
- Does nothing while you have only one set, and scrolling over a scrollable menu still scrolls the menu.
- One swipe moves one set: after a switch, sideways scrolling is ignored for a moment (150 ms by default).

The switch goes through the same path as the inventory set tabs and the item set hotkeys, so it works in multiplayer
without the server needing the mod.

## Settings

Everything is in **Settings > Controls > Item sets**, below the "Item set 1-4" hotkeys:

| Row | Default | What it does |
| --- | --- | --- |
| Next item set | SCROLLRIGHT | Click, then scroll sideways or press any key or mouse button to rebind. Set to NOT SET (Esc) to turn it off |
| Previous item set | SCROLLLEFT | Same. Swap the two binds to invert the direction |
| Wrap around item sets | ON | Click to toggle |
| Show item set number | ON | Click to toggle |
| Item set scroll cooldown | 150 MS | Click to cycle through 0, 50, 100, 150, 250, 400, 600 and 1000 ms |

The reset button next to a row, and "Restore all default binds", put that row back to its default.
Values are saved in the mod's config file with the rest of the mod's controls.

## Install

Copy `dist/Equipment_Set_Scroll-1.3.3-1.1.jar` from the repository root into Necesse's `mods` folder.

## Building

Shares the build logic in `../gradle/main.gradle` with Light Overlay. Requires a JDK 17+ and Necesse (detected
automatically). From the repository root:

```bash
./gradlew -p equipmentSetScroll buildModJar
```

The jar is written to `equipmentSetScroll/build/jar/`. `./gradlew -p equipmentSetScroll runClient` launches the
game with the mod.
