# Light Overlay

A client-side [Necesse](https://store.steampowered.com/app/1169040/Necesse/) mod that highlights tiles that are dark enough for hostile mobs to spawn.

- Toggle with **F8** (rebindable in controls) or the `/lightoverlay` chat command.
- Change the overlay opacity with `/lightoverlayalpha <0-1>`.

## Auto torch

Off by default. Turn it on with `/lightoverlayautotorch` (or `autotorch = true` in the mod's config).
While it's on, regular torches from your inventory are placed for you, one at a time, whenever you aren't using an item:

- It finds the darkest tile in reach where mobs can spawn and places a torch on the grid point that covers it.
  If that grid point can't hold a torch (wall, water, furniture...), the torch goes on the dark tile itself.
- **Optimal grid:** on open ground a torch keeps every tile within 9 steps (Manhattan distance) lit, and those
  diamond-shaped areas tile perfectly with torches offset by (9, 10) and (10, -9). That's one torch per 181 tiles,
  less than half of a 9×9 square grid. The grid is fixed to the world, so it lines up wherever you go.
- **Relocation** (`/lightoverlayrelocate`, on by default while auto torch is on): regular torches that are off the grid
  get moved onto it. The new torch is placed first, then the old one is mined with a pickaxe or axe from your
  inventory. Torches whose grid point is blocked or already has a torch are left alone, since they're filling a gap.
- `/lightoverlaytorchspacing <radius>` overrides the grid radius in tiles (0 = automatic).
- `/lightoverlaygrid` toggles showing the grid (works with auto torch off too): white borders mark each torch's
  cell, and each grid point gets a ring: **green** = torch is there, **orange** = mobs can spawn in that cell,
  so a torch belongs here, **white** = cell is already lit.

Placements and mining go through the normal player actions, so the server checks them like your own and they work in multiplayer.
Only plain torches are placed or moved; wall torches, lanterns and other lights are never touched.

## Building

Requires a JDK 17+ and a Steam install of Necesse. The game directory and game version are detected automatically
(override `gamePath` in `settings.gradle` if detection fails).

```bash
./gradlew buildModJar
```

The mod jar is written to `build/jar/`. Use `./gradlew runClient` to launch the game with the mod.

Check out the [modding wiki page](https://necessewiki.com/Modding) and the
[ExampleMod](https://github.com/DrFair/ExampleMod) for more.
