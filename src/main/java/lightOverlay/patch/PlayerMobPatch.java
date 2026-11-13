package lightOverlay.patch;

import lightOverlay.autotorch.AutoTorch;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.camera.GameCamera;
import net.bytebuddy.asm.Advice;

public class PlayerMobPatch {
    /** tickControls only runs for the client's own player, every frame while in game. */
    @ModMethodPatch(target = PlayerMob.class, name = "tickControls", arguments = {MainGame.class, boolean.class, GameCamera.class})
    public static class TickControlsPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob player) {
            try {
                AutoTorch.tick(player);
            } catch (Throwable e) {
                AutoTorch.onError(e);
            }
        }
    }
}
