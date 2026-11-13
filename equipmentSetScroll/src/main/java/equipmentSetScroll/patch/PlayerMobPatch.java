package equipmentSetScroll.patch;

import equipmentSetScroll.EquipmentSetScroll;
import necesse.engine.input.InputEvent;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.camera.GameCamera;
import net.bytebuddy.asm.Advice;

public class PlayerMobPatch {
    /**
     * MainGame passes every input event the forms didn't use to the client's own player here,
     * so scrolling over a scrollable UI list still scrolls the list instead.
     */
    @ModMethodPatch(target = PlayerMob.class, name = "submitInputEvent", arguments = {MainGame.class, InputEvent.class, GameCamera.class})
    public static class SubmitInputEventPatch {
        @Advice.OnMethodEnter
        static void onEnter(@Advice.This PlayerMob player, @Advice.Argument(1) InputEvent event) {
            try {
                EquipmentSetScroll.onInputEvent(player, event);
            } catch (Throwable e) {
                System.err.println("Equipment Set Scroll error:");
                e.printStackTrace();
            }
        }
    }

    /** Runs every frame for the client's own player, where the game checks its own item set hotkeys. */
    @ModMethodPatch(target = PlayerMob.class, name = "tickControls", arguments = {MainGame.class, boolean.class, GameCamera.class})
    public static class TickControlsPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob player) {
            try {
                EquipmentSetScroll.tickControls(player);
            } catch (Throwable e) {
                System.err.println("Equipment Set Scroll error:");
                e.printStackTrace();
            }
        }
    }
}
