# Light Overlay

A client-side [Necesse](https://store.steampowered.com/app/1169040/Necesse/) mod that highlights tiles that are dark enough for hostile mobs to spawn.

- Toggle with **F8** (rebindable in controls) or the `/lightoverlay` chat command.
- Change the overlay opacity with `/lightoverlayalpha <0-1>`.

## Building

Requires a JDK 17+ and a Steam install of Necesse. The game directory and game version are detected automatically
(override `gamePath` in `settings.gradle` if detection fails).

```bash
./gradlew buildModJar
```

The mod jar is written to `build/jar/`. Use `./gradlew runClient` to launch the game with the mod.

Check out the [modding wiki page](https://necessewiki.com/Modding) and the
[ExampleMod](https://github.com/DrFair/ExampleMod) for more.
