package lightOverlay.patch;

import lightOverlay.measure.TileMeasure;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.InputEvent;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.ui.HUD;
import necesse.level.maps.Level;
import necesse.level.maps.regionSystem.RegionManager;
import net.bytebuddy.asm.Advice;

public class TileMeasurePatches {
    /** Captures right clicks in the world while the debug HUD is active. */
    @ModMethodPatch(target = PlayerMob.class, name = "submitInputEvent", arguments = {MainGame.class, InputEvent.class, GameCamera.class})
    public static class SubmitInputEventPatch {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.This PlayerMob player, @Advice.Argument(1) InputEvent event, @Advice.Argument(2) GameCamera camera) {
            return TileMeasure.onInputEvent(player.getLevel(), event, camera);
        }
    }

    /** Draws the measurement on top of the debug HUD. */
    @ModMethodPatch(target = HUD.class, name = "draw", arguments = {Level.class, GameCamera.class, PlayerMob.class, TickManager.class})
    public static class HudDrawPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.Argument(0) Level level, @Advice.Argument(1) GameCamera camera) {
            TileMeasure.draw(level, camera);
        }
    }

    /** Appends the tile count to the debug HUD tile tooltip (only called by the debug HUD). */
    @ModMethodPatch(target = RegionManager.class, name = "addBiomeBlendingDebugTooltips", arguments = {int.class, int.class, StringTooltips.class})
    public static class DebugTooltipPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.Argument(0) int tileX, @Advice.Argument(1) int tileY, @Advice.Argument(2) StringTooltips tooltips) {
            TileMeasure.addDebugTooltip(tileX, tileY, tooltips);
        }
    }
}
