package equipmentSetScroll;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class Settings extends ModSettings {
    public boolean enabled = true;
    public boolean invert = false;
    public boolean wrap = true;
    public boolean showText = true;
    public int cooldownMs = 150;

    @Override
    public void addSaveData(SaveData save) {
        save.addBoolean("enabled", enabled, "Switch equipment sets with horizontal scrolling");
        save.addBoolean("invert", invert, "false: scroll right = next set. true: scroll right = previous set");
        save.addBoolean("wrap", wrap, "Wrap from the last set to the first and back");
        save.addBoolean("showtext", showText, "Show the selected set's number next to the cursor when switching");
        save.addInt("cooldownms", cooldownMs, "Milliseconds after a switch during which horizontal scrolling is ignored, so one trackpad swipe moves one set");
    }

    @Override
    public void applyLoadData(LoadData save) {
        if (save == null)
            return;
        enabled = save.getBoolean("enabled", enabled, false);
        invert = save.getBoolean("invert", invert, false);
        wrap = save.getBoolean("wrap", wrap, false);
        showText = save.getBoolean("showtext", showText, false);
        cooldownMs = save.getInt("cooldownms", cooldownMs, 0, 2000, false);
    }
}
