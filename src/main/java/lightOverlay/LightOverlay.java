package lightOverlay;

import lightOverlay.command.ModifyOverlayAlphaCommand;
import lightOverlay.command.TorchSpacingCommand;
import necesse.engine.modifiers.Modifier;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.MobSpawnLocation;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.level.maps.Level;
import necesse.engine.input.InputEvent;
import org.lwjgl.glfw.GLFW;
import necesse.engine.commands.CommandsManager;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.clientCommands.BoolClientCommand;
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
    public static Mob fakeHostileMob;

    public static Control toggleOverlayControl;

    public static Settings settings;

    public void init() {
        fakeHostileMob = new ZombieMob();

        toggleOverlayControl =
                Control.addModControl(new Control(GLFW.GLFW_KEY_F8, "toggleoverlay") {
                    @Override
                    public void activate(InputEvent event) {
                        super.activate(event);
                        if (isPressed()) {
                            settings.drawOverlay = !settings.drawOverlay;

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
    }

    public Settings initSettings() {
        settings = new Settings();
        return settings;
    }

    public void postInit() {
        try {
            CommandsManager.registerClientCommand(new BoolClientCommand("lightoverlay",
                    "Toggle light overlay", PermissionLevel.USER,
                    Settings.class.getField("drawOverlay"), settings));
        } catch (NoSuchFieldException | SecurityException e) {
            System.out.println("Could not register lightoverlay command: " + e.getMessage());
            e.printStackTrace();
        }
        CommandsManager.registerClientCommand(new ModifyOverlayAlphaCommand());
        registerBoolCommand("lightoverlayautotorch", "Toggle automatic torch placement", "autoTorch");
        registerBoolCommand("lightoverlayrelocate", "Toggle moving off-grid torches onto the grid", "autoTorchRelocate");
        CommandsManager.registerClientCommand(new TorchSpacingCommand());
    }

    private static void registerBoolCommand(String name, String action, String field) {
        try {
            CommandsManager.registerClientCommand(new BoolClientCommand(name, action, PermissionLevel.USER,
                    Settings.class.getField(field), settings));
        } catch (NoSuchFieldException | SecurityException e) {
            System.out.println("Could not register " + name + " command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** The light level at or below which hostile mobs can spawn (ignoring player buffs). */
    public static int getSpawnLightThreshold(Level level) {
        fakeHostileMob.setLevel(level);
        Modifier<Integer> modifier = BuffModifiers.MOB_SPAWN_LIGHT_THRESHOLD;
        return fakeHostileMob.spawnLightThreshold.limits.applyModifierLimits(
                modifier,
                modifier.finalLimit(modifier.appendManager(modifier.defaultBuffManagerValue, fakeHostileMob.spawnLightThreshold.value))
        );
    }

    /** Whether a hostile mob could spawn on this tile, using the same checks as natural spawning. */
    public static boolean canMobSpawn(Level level, int tileX, int tileY) {
        fakeHostileMob.setLevel(level);
        return new MobSpawnLocation(fakeHostileMob, tileX * 32 + 16, tileY * 32 + 16)
                // null client: use the mob's base spawn light threshold (ignores player buffs)
                .checkStaticLightThreshold((ServerClient) null)
                .checkMobSpawnLocation()
                .validAndApply();
    }
}
