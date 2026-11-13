# Tile Measure

A client-side [Necesse](https://store.steampowered.com/app/1169040/Necesse/) mod for measuring distances in tiles.
It only does anything while the debug HUD is open (**F2**).

- **Right click** a tile to select it. Its colours are inverted.
- While one tile is selected, a line is drawn from it to the tile under the cursor: every tile on the line is inverted.
- **Right click** a second tile to lock the line between the two.
- **Right click** a selected tile to unselect it. The other one stays selected and the line follows the cursor again.
- With two tiles selected, **right click** any other tile to unselect both.

The F2 tile tooltip gets an extra line:

```
Tile count: n (x1,y1) -> (x2,y2)
```

`n` is the number of tiles on the line, counting both ends, so the same tile twice is 1, and (0,0) -> (3,3) is 4.
While only one tile is selected, (x2,y2) is the tile under the cursor.

While F2 is open, right clicks in the world are used for measuring, so they don't open doors, chests and so on.
Right clicks on menus work as usual. The selection is cleared when you change level.

## Install

Copy `dist/Tile_Measure-1.3.3-1.0.jar` from the repository root into Necesse's `mods` folder.

## Building

Shares the build logic in `../gradle/main.gradle` with Light Overlay. Requires a JDK 17+ and Necesse (detected
automatically). From the repository root:

```bash
./gradlew -p tileMeasure buildModJar
```

The jar is written to `tileMeasure/build/jar/`. `./gradlew -p tileMeasure runClient` launches the game with the mod.
