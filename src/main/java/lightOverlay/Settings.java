package lightOverlay;

import lightOverlay.autotorch.LayoutType;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

import java.awt.Point;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Settings extends ModSettings {
    public boolean drawOverlay = true;
    public float alpha = 1f;
    public boolean showGrid = false;
    public boolean autoTorch = false;
    public boolean autoTorchRelocate = true;
    public LayoutType layout = LayoutType.DIAMOND;
    /** Tiles between neighbouring light sources. 0 = the optimal distance for the layout. */
    public int lightSourceDistance = 0;
    /** Grid origin (or quadrant center) the player set, per world. Read by the render thread. */
    public final Map<String, Point> gridOrigins = new ConcurrentHashMap<>();

    /**
     * Writes the settings to disk. Commands and hotkeys must call this: leaving the game's settings menu (Escape)
     * reloads every mod's settings from disk to discard unsaved changes, which would otherwise undo them.
     */
    public static void save() {
        necesse.engine.Settings.saveClientSettings();
    }

    @Override
    public void addSaveData(SaveData save) {
        save.addBoolean("drawoverlay", drawOverlay);
        save.addFloat("alpha", alpha, "Float in the range [0, 1]. 0 is clear, 1 fully opaque");
        save.addBoolean("showgrid", showGrid, "Show the torch grid: cell borders and light source spots (green = has a torch, orange = needs one)");
        save.addBoolean("autotorch", autoTorch, "Automatically place torches from your inventory on the torch grid where mobs can spawn");
        save.addBoolean("autotorchrelocate", autoTorchRelocate, "While autotorch is on, move regular torches that are off the grid onto it (needs a pickaxe or axe in your inventory)");
        save.addSafeString("layout", layout.id, "One of: " + String.join(", ", LayoutType.ids()));
        save.addInt("lightsourcedistance", lightSourceDistance, "Tiles between neighbouring light sources. 0 = the largest distance that leaves no spawnable tiles");
        SaveData origins = new SaveData("GRIDORIGINS", "Grid origin (or quadrant center) set per world with /lightoverlayorigin");
        for (Map.Entry<String, Point> entry : gridOrigins.entrySet())
            origins.addPoint(entry.getKey(), entry.getValue());
        save.addSaveData(origins);
    }

    @Override
    public void applyLoadData(LoadData save) {
        if (save == null)
            return;
        drawOverlay = save.getBoolean("drawoverlay", drawOverlay);
        alpha = save.getFloat("alpha", alpha, 0, 1);
        showGrid = save.getBoolean("showgrid", showGrid, false);
        autoTorch = save.getBoolean("autotorch", autoTorch, false);
        autoTorchRelocate = save.getBoolean("autotorchrelocate", autoTorchRelocate, false);
        LayoutType loadedLayout = LayoutType.fromID(save.getSafeString("layout", layout.id, false));
        if (loadedLayout != null)
            layout = loadedLayout;
        lightSourceDistance = save.getInt("lightsourcedistance", lightSourceDistance, 0, 100, false);
        LoadData origins = save.getFirstLoadDataByName("GRIDORIGINS");
        if (origins != null) {
            gridOrigins.clear();
            for (LoadData entry : origins.getLoadData()) {
                Point point = origins.getPoint(entry.getName(), null, false);
                if (point != null)
                    gridOrigins.put(entry.getName(), point);
            }
        }
    }

}
