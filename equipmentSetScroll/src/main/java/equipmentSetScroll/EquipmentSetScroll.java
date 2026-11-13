package equipmentSetScroll;

import necesse.engine.input.InputEvent;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.screenHudManager.UniqueScreenFloatText;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.Renderer;
import necesse.gfx.gameFont.FontOptions;
import necesse.engine.input.InputPosition;
import necesse.inventory.PlayerEquipmentSetInventoryManager;

@ModEntry
public class EquipmentSetScroll {
    /** Horizontal scroll not yet turned into a set change (trackpads send many small fractions). */
    private static double scrollBuffer;
    private static long cooldownUntil;

    public void init() {
        ItemSetControls.register();
    }

    /**
     * Every frame for the client's own player: handles the next/previous binds when they're
     * bound to a key, mouse button or vertical scroll (horizontal scroll is handled per event).
     */
    public static void tickControls(PlayerMob player) {
        ItemSetControls.ScrollBindControl next = ItemSetControls.next;
        ItemSetControls.ScrollBindControl previous = ItemSetControls.previous;
        if (next == null || previous == null)
            return;
        int step = 0;
        if (!next.isHorizontalScrollBind() && next.isPressed())
            step++;
        if (!previous.isHorizontalScrollBind() && previous.isPressed())
            step--;
        if (step != 0) {
            InputPosition mouse = WindowManager.getWindow().mousePos();
            switchSet(player, step, mouse);
        }
    }

    /**
     * Called with each input event the UI didn't use, for the client's own player.
     * A mostly horizontal wheel event triggers whichever bind is set to that scroll direction.
     */
    public static void onInputEvent(PlayerMob player, InputEvent event) {
        if (event.isUsed() || !ItemSetControls.isHorizontalScroll(event))
            return;
        ItemSetControls.ScrollBindControl next = ItemSetControls.next;
        ItemSetControls.ScrollBindControl previous = ItemSetControls.previous;
        if (next == null || previous == null)
            return;
        if (!next.isHorizontalScrollBind() && !previous.isHorizontalScrollBind())
            return;
        if (player.getInv().equipment.getTotalSets() <= 1)
            return;

        double x = event.getMouseWheelX();
        long now = System.currentTimeMillis();
        if (now < cooldownUntil) {
            event.use();
            return;
        }
        if (scrollBuffer != 0 && Math.signum(scrollBuffer) != Math.signum(x))
            scrollBuffer = 0;
        scrollBuffer += x;
        if (Math.abs(scrollBuffer) < 1) {
            event.use();
            return;
        }

        int key = ItemSetControls.horizontalScrollKey(scrollBuffer);
        scrollBuffer = 0;
        int step = 0;
        if (next.getKey() == key)
            step++;
        if (previous.getKey() == key)
            step--;
        if (step == 0)
            return;
        event.use();
        cooldownUntil = now + ItemSetControls.cooldown.getValue();
        switchSet(player, step, event.pos);
    }

    private static void switchSet(PlayerMob player, int step, InputPosition textPos) {
        PlayerEquipmentSetInventoryManager sets = player.getInv().equipment;
        int total = sets.getTotalSets();
        if (total <= 1)
            return;

        int current = sets.getSelectedSet();
        int next = current + step;
        if (ItemSetControls.wrap.isOn())
            next = Math.floorMod(next, total);
        else
            next = Math.max(0, Math.min(total - 1, next));
        if (next == current)
            return;

        // Same as the set tabs in the inventory and the item set hotkeys
        sets.setSelectedSet(next);
        Client client = player.getClient();
        if (client != null)
            client.sendMovementPacket(false);

        if (ItemSetControls.showText.isOn() && textPos != null && textPos.hudX != Integer.MIN_VALUE) {
            Renderer.hudManager.addElement(new UniqueScreenFloatText(textPos.hudX, textPos.hudY - 16,
                    "Item set " + (next + 1) + "/" + total, new FontOptions(16).outline(), "equipmentsetscroll"));
        }
    }
}
