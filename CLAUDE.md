# Light Overlay: Necesse mod development brief

Client-side Necesse mod (Java 8 target) that marks tiles where hostile mobs can spawn, shows an optimal torch grid,
and can auto-place torches. Fork of `snoobinoob/lightOverlay` (remote `upstream`, branch `main`); targets game **1.3.3**.

## Getting the game jar (no Steam needed)

Necesse has no maven artifact and Steam can't be used in a cloud container. The free **dedicated server** download
contains the full game code, client rendering classes included, in `Server.jar` (no textures/sounds).

- The `lightOverlay` cloud environment allows `necessegame.com`, `www.necessegame.com` and `maven.quiltmc.org`, and its
  setup script installs the latest server at `~/.local/share/Steam/steamapps/common/Necesse/` with `Server.jar` copied
  to `Necesse.jar`. Gradle's `gamePath = "auto"` finds it there; no edits needed.
- Manually: the zips are listed at `https://necessegame.com/server/`, e.g.
  `/server/download/necesse-server-linux64-1-3-3-24926468.zip`. Run `chmod +x jre/bin/*` after unzipping.
- `github.com` web is blocked; use `git clone https://github.com/...` or `raw.githubusercontent.com`.

## Build and verify

```bash
./gradlew buildModJar          # -> build/jar/Light_Overlay-<gameVersion>-<modVersion>.jar (+ mod.info via the game)
./gradlew decompileToSources   # -> <game dir>/decompiled/Necesse-sources.jar (Quiltflower), ~1 min
```

Never commit decompiled sources (redistribution is prohibited) or a `gamePath` other than `"auto"`.

**Runtime check without a client:** the dedicated server loads mods and applies every `@ModMethodPatch` at startup,
logging `<modid> transformed <Class.method(args)>` per patch. A wrong signature fails loudly here.

```bash
cd ~/.local/share/Steam/steamapps/common/Necesse/necesse-server-*/
(sleep 25; echo stop) | timeout 90 ./jre/bin/java -Xmx2G -jar Server.jar -nogui -mod /home/user/lightOverlay/build/jar -world modtest
```

A real client can't run here (assets only come from Steam, no GPU). Rendering and input behaviour need testing by
the user with `./gradlew runClient`. Say so explicitly instead of claiming it works.

**Quick API lookup** without decompiling: `javap -p -cp Necesse.jar necesse.gfx.drawables.LevelDrawUtils`.

## Reference repos

- `DrFair/ExampleMod`: official example mod by the Necesse dev. Its `build.gradle` `gameVersion` and git history
  show what changed per game version. Check it when updating.
- `snoobinoob/lightOverlay` (upstream): pull its updates (`git fetch upstream`).

## Mod structure

- `settings.gradle`: mod info (id `snoobinoob.lightoverlay`, `modVersion`, `clientside: true`); bump `modVersion`
  per feature. `gradle/main.gradle`: build logic (Gradle 9.6.1, matches ExampleMod).
- `LightOverlay.java`: `@ModEntry`; `init` (controls), `initResources` (textures), `initSettings`, `postInit`
  (client chat commands). Shared helpers `canMobSpawn(level, x, y)` and `getSpawnLightThreshold(level)` use a
  per-thread fake `ZombieMob` (rendering and input ticks can run on different threads).
- `Settings.java`: `ModSettings`; saved to the mod's cfg by the client. Load with the field as the default.
  **Call `Settings.save()` after every change** (commands, hotkeys): leaving the game's settings menu (Escape)
  calls `Settings.loadClientSettings()`, which reloads every mod's settings from disk and undoes unsaved changes.
- `patch/LevelDrawUtilsPatch`: exit advice on private `LevelDrawUtils.addTileBasedDrawProcesses(...)` (15 args;
  arg 1 camera, 2 tile area, 11 `OrderableDrawables`). Adds overlay and grid drawables. **This signature is what
  breaks on game updates**; check it with `javap` first.
- `patch/PlayerMobPatch`: exit advice on `PlayerMob.tickControls(MainGame, boolean, GameCamera)` (runs every frame,
  own player only) and calls `AutoTorch.tick`, wrapped in try/catch.
- `autotorch/`: `LayoutType` (the 5 layouts, optimal distances), `Lattice` and `QuadrantLayout` (pure Java, no game
  classes), `GridSetup` (layout for the current world: origin defaults, per-world key, migrated-world detection),
  `AutoTorch` (placement and relocation). `TorchGridOverlay`: grid drawing. `command/`: chat commands.
- `tools/layout-diagrams/run.sh`: tests the layouts and regenerates `docs/layouts/*.png`. Run it after changing
  layout code, and commit the images.

## Game facts used (verified on 1.3.3)

- **Light:** static light spreads 4-way and drops `max(10, objectLightMod)` per tile (40 through solid,
  non-transparent objects). Torch `lightLevel` = 150. Mobs spawn when static light <= threshold, default 50.
  So a torch covers Manhattan radius 9.
- **Layouts:** light source distance = tiles between neighbouring lights. Optimal for radius r: unaligned diamond
  (lattice (k, k+1), (k+1, -k)) 2r; aligned diamond ((h, h), (h, -h)) 2r - 1; square: step s = r if r is odd,
  else r + 1, so distance s - 1.
  Quadrant layouts mirror the lattice into 4 quadrants whose lines lie just before the center (center 150 gives corner
  tiles 149/150).
- **Worlds migrated from islands (pre-1.0):** `OneWorldMigration` puts the old spawn island at tiles 0-299 and sets
  `worldEntity.spawnTile` to (150, 150); new worlds find a spawn near (512, 512). The client only sees this through
  `client.getLocalServer()` (singleplayer/hosting); remote servers send neither spawn tile nor world ID.
- **Acting as the player from client code:** `player.tryAttack(new PlayerInventorySlot(player.getInv().main, slot), levelX, levelY)`.
  It's the same path as the built-in place-torch hotkey (`PlayerMob.tickControls`); the server validates it, so the
  mod stays client-side.
  - Placing: use a `TorchItem` slot, with `canPlaceTorch` / `getTorchPlaceRange` (pixels).
  - Mining: use a `ToolDamageItem` (pickaxe/axe) slot; check `getTileDamageOption(..., true)` first to confirm the
    target.
  - Only act when `!isAttacking && !onCooldown && getAttackHandler() == null` and no mouse button is down.
- **Tiles:** 32 px; tile center = `tile * 32 + 16`. `level.getObjectID(x, y)` gives layer 0; 0 = air.
- **Drawing:** `GameTexture.fromFile("texture/x")` loads `src/main/resources/texture/x.png`.
  `initDraw().size(w, h).color(r, g, b, a).pos(drawX, drawY)` with `camera.getTileDrawX/Y(tile)`.
  Add a `LevelSortedDrawable` (see `OverlayDrawable`) to the `OrderableDrawables` list.
- **Commands:** `CommandsManager.registerClientCommand`, using `BoolClientCommand` (field toggle) or
  `ModularChatCommand` with `CmdParameter` + parameter handlers.
- **Patches:** `@ModMethodPatch(target, name, arguments)` on a class holding Byte Buddy `@Advice` methods, which
  are inlined into the target. Use `@Advice.This`, `@Advice.Argument(n)`, `@Advice.FieldValue("name")`.

## Conventions

- Develop on the session's assigned branch; commit messages explain the why.
- Keep the mod client-side: no registered items, objects, tiles or packets.
- New player-facing features: config option (off by default if they change the world), chat command, README section.
