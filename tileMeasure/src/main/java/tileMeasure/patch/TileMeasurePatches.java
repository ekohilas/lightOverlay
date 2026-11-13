package tileMeasure.patch;

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
import tileMeasure.TileMeasure;

public class TileMeasurePatches {
    /**
     * MainGame passes every input event the forms didn't use to the client's own player here.
     * Right clicks are taken (and the player's own handling skipped) while the debug HUD is active.
     */
    @ModMethodPatch(target = PlayerMob.class, name = "submitInputEvent", arguments = {MainGame.class, InputEvent.class, GameCamera.class})
    public static class SubmitInputEventPatch {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.This PlayerMob player, @Advice.Argument(1) InputEvent event, @Advice.Argument(2) GameCamera camera) {
            try {
                return TileMeasure.onInputEvent(player.getLevel(), event, camera);
            } catch (Throwable e) {
                System.err.println("Tile Measure error:");
                e.printStackTrace();
                return false;
            }
        }
    }

    /** Draws the measurement on top of the debug HUD. */
    @ModMethodPatch(target = HUD.class, name = "draw", arguments = {Level.class, GameCamera.class, PlayerMob.class, TickManager.class})
    public static class HudDrawPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.Argument(0) Level level, @Advice.Argument(1) GameCamera camera) {
            try {
                TileMeasure.draw(level, camera);
            } catch (Throwable e) {
                System.err.println("Tile Measure error:");
                e.printStackTrace();
            }
        }
    }

    /** Appends the tile count to the debug HUD's tile tooltip (this method is only called by the debug HUD). */
    @ModMethodPatch(target = RegionManager.class, name = "addBiomeBlendingDebugTooltips", arguments = {int.class, int.class, StringTooltips.class})
    public static class DebugTooltipPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.Argument(0) int tileX, @Advice.Argument(1) int tileY, @Advice.Argument(2) StringTooltips tooltips) {
            try {
                TileMeasure.addDebugTooltip(tileX, tileY, tooltips);
            } catch (Throwable e) {
                System.err.println("Tile Measure error:");
                e.printStackTrace();
            }
        }
    }
}
