package lightOverlay;

import lightOverlay.autotorch.AutoTorch;
import lightOverlay.autotorch.TorchGrid;
import necesse.engine.registries.ObjectRegistry;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.Level;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws the auto torch grid: the border of each grid cell (the diamond one torch keeps lit) and a marker on each grid
 * point, coloured by status:
 * <ul>
 *     <li>green: a torch is already there</li>
 *     <li>orange: no torch, and mobs can spawn somewhere in its cell (in view), so a torch belongs here</li>
 *     <li>white: no torch needed, the cell is already lit</li>
 * </ul>
 */
public final class TorchGridOverlay {
    private static final int LINE_WIDTH = 2;

    private TorchGridOverlay() {
    }

    /**
     * @param spawnable spawnable[x - startX][y - startY] for every tile in the area
     */
    public static void addDrawables(Level level, GameCamera camera, int startX, int startY, int endX, int endY,
                                    boolean[][] spawnable, OrderableDrawables list) {
        TorchGrid grid = AutoTorch.getGrid(level);
        if (grid == null)
            return;
        int width = endX - startX + 1;
        int height = endY - startY + 1;

        // Which cell each tile belongs to, with a 1 tile margin so borders at the edge of view are known
        Point[][] cells = new Point[width + 1][height + 1];
        for (int x = 0; x <= width; x++)
            for (int y = 0; y <= height; y++)
                cells[x][y] = grid.nearestGridPoint(startX + x, startY + y);

        Map<Point, Boolean> cellNeedsTorch = new HashMap<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (spawnable[x][y])
                    cellNeedsTorch.put(cells[x][y], true);
            }
        }

        float lineAlpha = 0.6f * LightOverlay.settings.alpha;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int tileX = startX + x;
                int tileY = startY + y;
                int drawX = camera.getTileDrawX(tileX);
                int drawY = camera.getTileDrawY(tileY);
                if (!cells[x][y].equals(cells[x + 1][y])) // Border on the right edge of this tile
                    list.add(new OverlayDrawable(tileX, tileY, LightOverlay.whiteTexture.initDraw()
                            .size(LINE_WIDTH, 32).color(1, 1, 1, lineAlpha).pos(drawX + 32 - LINE_WIDTH / 2, drawY)));
                if (!cells[x][y].equals(cells[x][y + 1])) // Border on the bottom edge of this tile
                    list.add(new OverlayDrawable(tileX, tileY, LightOverlay.whiteTexture.initDraw()
                            .size(32, LINE_WIDTH).color(1, 1, 1, lineAlpha).pos(drawX, drawY + 32 - LINE_WIDTH / 2)));
            }
        }

        int torchID = ObjectRegistry.getObjectID("torch");
        float markerAlpha = Math.max(0.3f, LightOverlay.settings.alpha);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int tileX = startX + x;
                int tileY = startY + y;
                if (!grid.isGridPoint(tileX, tileY))
                    continue;
                float r, g, b;
                if (level.getObjectID(tileX, tileY) == torchID) {
                    r = 0.3f; g = 1f; b = 0.3f;
                } else if (cellNeedsTorch.containsKey(new Point(tileX, tileY))) {
                    r = 1f; g = 0.6f; b = 0.1f;
                } else {
                    r = 1f; g = 1f; b = 1f;
                }
                list.add(new OverlayDrawable(tileX, tileY, LightOverlay.gridPointTexture.initDraw()
                        .color(r, g, b, markerAlpha).pos(camera.getTileDrawX(tileX), camera.getTileDrawY(tileY))));
            }
        }
    }
}
