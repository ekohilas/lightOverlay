package lightOverlay.autotorch;

import java.awt.Point;

/** Where light sources go. Coordinates are tiles. */
public interface TorchLayout {
    boolean isGridPoint(int tileX, int tileY);

    /** The grid point whose cell this tile is in: the nearest one by Manhattan distance (within its quadrant, for quadrant layouts). */
    Point nearestGridPoint(int tileX, int tileY);
}
