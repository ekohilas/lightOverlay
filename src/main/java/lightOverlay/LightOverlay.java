package lightOverlay;

import lightOverlay.command.*;
import necesse.engine.modifiers.Modifier;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.MobSpawnLocation;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.level.maps.Level;
import necesse.engine.input.InputEvent;
import org.lwjgl.glfw.GLFW;
import necesse.engine.commands.CommandsManager;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.input.Control;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.GlobalData;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.hostile.ZombieMob;
import necesse.gfx.gameTexture.GameTexture;

@ModEntry
public class LightOverlay {
    public static GameTexture lowLightTexture;
    public static GameTexture torchOutlineTexture;
    public static GameTexture whiteTexture;
    /** One per thread: the overlay is built during rendering, auto torch runs in the input tick. */
    private static final ThreadLocal<Mob> fakeHostileMob = ThreadLocal.withInitial(ZombieMob::new);

    public static Control toggleOverlayControl;

    public static Settings settings;

    public void init() {
        toggleOverlayControl =
                Control.addModControl(new Control(GLFW.GLFW_KEY_F8, "toggleoverlay") {
                    @Override
                    public void activate(InputEvent event) {
                        super.activate(event);
                        if (isPressed()) {
                            settings.drawOverlay = !settings.drawOverlay;
                            Settings.save();

                            MainGame mainGame = (GlobalData.getCurrentState() instanceof MainGame) ? (MainGame)GlobalData.getCurrentState() : null;
                            if (mainGame != null) {
                              //mainGame.getClient().chat.addMessage("LightOverlay is " + (settings.drawOverlay ? "on.":"off."));
                              mainGame.getClient().chat.addMessage(Localization.translate("controls", "togglemessage") + " " + Localization.translate("controls", (settings.drawOverlay ? "on":"off")));
                            }
                        }
                    }
                });
    }

    public void initResources() {
        lowLightTexture = GameTexture.fromFile("texture/lowlight");
        torchOutlineTexture = GameTexture.fromFile("texture/torchoutline");
        whiteTexture = GameTexture.fromFile("texture/white");
    }

    public Settings initSettings() {
        settings = new Settings();
        return settings;
    }

    public void postInit() {
        CommandsManager.registerClientCommand(new ToggleSettingCommand("lightoverlay", "Toggle light overlay",
                "Light overlay", () -> settings.drawOverlay, v -> settings.drawOverlay = v));
        CommandsManager.registerClientCommand(new ModifyOverlayAlphaCommand());
        CommandsManager.registerClientCommand(new ToggleSettingCommand("lightoverlaygrid", "Toggle showing the torch grid",
                "Torch grid", () -> settings.showGrid, v -> settings.showGrid = v));
        CommandsManager.registerClientCommand(new ToggleSettingCommand("lightoverlayautotorch", "Toggle automatic torch placement",
                "Auto torch", () -> settings.autoTorch, v -> settings.autoTorch = v));
        CommandsManager.registerClientCommand(new ToggleSettingCommand("lightoverlayrelocate", "Toggle moving off-grid torches onto the grid",
                "Relocate off-grid torches", () -> settings.autoTorchRelocate, v -> settings.autoTorchRelocate = v));
        CommandsManager.registerClientCommand(new LayoutCommand());
        CommandsManager.registerClientCommand(new DistanceCommand());
        CommandsManager.registerClientCommand(new OriginCommand());
        CommandsManager.registerClientCommand(new OriginResetCommand());
        CommandsManager.registerClientCommand(new NiceCommand());
        CommandsManager.registerClientCommand(new GridInfoCommand());
    }

    /** The light level at or below which hostile mobs can spawn (ignoring player buffs). */
    public static int getSpawnLightThreshold(Level level) {
        Mob mob = fakeHostileMob.get();
        mob.setLevel(level);
        Modifier<Integer> modifier = BuffModifiers.MOB_SPAWN_LIGHT_THRESHOLD;
        return mob.spawnLightThreshold.limits.applyModifierLimits(
                modifier,
                modifier.finalLimit(modifier.appendManager(modifier.defaultBuffManagerValue, mob.spawnLightThreshold.value))
        );
    }

    /** Whether a hostile mob could spawn on this tile, using the same checks as natural spawning. */
    public static boolean canMobSpawn(Level level, int tileX, int tileY) {
        Mob mob = fakeHostileMob.get();
        mob.setLevel(level);
        return new MobSpawnLocation(mob, tileX * 32 + 16, tileY * 32 + 16)
                // null client: use the mob's base spawn light threshold (ignores player buffs)
                .checkStaticLightThreshold((ServerClient) null)
                .checkMobSpawnLocation()
                .validAndApply();
    }
}
