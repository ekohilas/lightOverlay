package equipmentSetScroll;

import necesse.engine.commands.CommandsManager;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.clientCommands.BoolClientCommand;
import necesse.engine.input.InputEvent;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.screenHudManager.UniqueScreenFloatText;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.Renderer;
import necesse.gfx.gameFont.FontOptions;
import necesse.inventory.PlayerEquipmentSetInventoryManager;

@ModEntry
public class EquipmentSetScroll {
    public static Settings settings;

    /** Horizontal scroll not yet turned into a set change (trackpads send many small fractions). */
    private static double scrollBuffer;
    private static long cooldownUntil;

    public Settings initSettings() {
        settings = new Settings();
        return settings;
    }

    public void postInit() {
        registerBoolCommand("equipsetscroll", "Toggle switching equipment sets with horizontal scrolling", "enabled");
        registerBoolCommand("equipsetscrollinvert", "Toggle inverting the horizontal scroll direction for equipment sets", "invert");
        registerBoolCommand("equipsetscrollwrap", "Toggle wrapping around between the first and last equipment set", "wrap");
    }

    private static void registerBoolCommand(String name, String action, String field) {
        try {
            CommandsManager.registerClientCommand(new BoolClientCommand(name, action, PermissionLevel.USER,
                    Settings.class.getField(field), settings));
        } catch (NoSuchFieldException | SecurityException e) {
            System.out.println("Could not register " + name + " command: " + e.getMessage());
        }
    }

    /**
     * Called with each input event the UI didn't use, for the client's own player.
     * A mostly horizontal wheel event selects the next or previous equipment set.
     */
    public static void onInputEvent(PlayerMob player, InputEvent event) {
        if (settings == null || !settings.enabled || event.isUsed() || !event.isMouseWheelEvent())
            return;
        double x = event.getMouseWheelX();
        double y = event.getMouseWheelY();
        if (x == 0 || Math.abs(x) <= Math.abs(y))
            return;

        PlayerEquipmentSetInventoryManager sets = player.getInv().equipment;
        int total = sets.getTotalSets();
        if (total <= 1)
            return;

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

        // GLFW reports scrolling left as positive x, so right (negative) means next set by default
        int step = scrollBuffer < 0 ? 1 : -1;
        if (settings.invert)
            step = -step;
        scrollBuffer = 0;
        cooldownUntil = now + settings.cooldownMs;
        event.use();

        int current = sets.getSelectedSet();
        int next = current + step;
        if (settings.wrap)
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

        if (settings.showText && event.pos != null && event.pos.hudX != Integer.MIN_VALUE) {
            Renderer.hudManager.addElement(new UniqueScreenFloatText(event.pos.hudX, event.pos.hudY - 16,
                    "Equipment set " + (next + 1) + "/" + total, new FontOptions(16).outline(), "equipmentsetscroll"));
        }
    }
}
