package lightOverlay;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class Settings extends ModSettings {
    public boolean drawOverlay = true;
    public float alpha = 1f;
    public boolean autoTorch = false;
    public boolean autoTorchRelocate = true;
    public int autoTorchSpacing = 0;

    @Override
    public void addSaveData(SaveData save) {
        save.addBoolean("drawoverlay", drawOverlay);
        save.addFloat("alpha", alpha, "Float in the range [0, 1]. 0 is clear, 1 fully opaque");
        save.addBoolean("autotorch", autoTorch, "Automatically place torches from your inventory on the darkest spawnable tiles in reach, on an optimal grid");
        save.addBoolean("autotorchrelocate", autoTorchRelocate, "While autotorch is on, move regular torches that are off the grid onto it (needs a pickaxe or axe in your inventory)");
        save.addInt("autotorchspacing", autoTorchSpacing, "Grid radius in tiles. 0 = automatic (9 for regular torches, the most a torch can keep lit)");
    }

    @Override
    public void applyLoadData(LoadData save) {
        if (save == null)
            return;
        drawOverlay = save.getBoolean("drawoverlay", drawOverlay);
        alpha = save.getFloat("alpha", alpha, 0, 1);
        autoTorch = save.getBoolean("autotorch", autoTorch, false);
        autoTorchRelocate = save.getBoolean("autotorchrelocate", autoTorchRelocate, false);
        autoTorchSpacing = save.getInt("autotorchspacing", autoTorchSpacing, 0, 50, false);
    }

}
