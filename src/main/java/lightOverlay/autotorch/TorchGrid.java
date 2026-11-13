package lightOverlay.autotorch;

import java.awt.Point;

/**
 * The optimal torch grid.
 * <p>
 * Static light spreads to the 4 neighbouring tiles and loses at least 10 levels per tile, so on open ground a torch
 * keeps every tile within Manhattan (walking) distance {@code r} above the spawn threshold. Those diamond-shaped areas
 * tile the plane perfectly when torches sit on the lattice spanned by {@code (r, r+1)} and {@code (r+1, -r)}: every
 * tile is covered by exactly one torch, using one torch per {@code 2r² + 2r + 1} tiles (181 tiles for a regular torch),
 * versus {@code r²} for a plain square grid.
 * <p>
 * The lattice is anchored at tile (0, 0), so all grid points are fixed for a given radius.
 */
public final class TorchGrid {
    public final int radius;
    private final int cellSize;

    public TorchGrid(int radius) {
        if (radius < 1) throw new IllegalArgumentException("radius must be >= 1");
        this.radius = radius;
        this.cellSize = 2 * radius * radius + 2 * radius + 1;
    }

    /** Lattice coordinate a of a tile, scaled by cellSize: tile = a*(r, r+1) + b*(r+1, -r). */
    private long aNum(int x, int y) {
        return (long) radius * x + (long) (radius + 1) * y;
    }

    private long bNum(int x, int y) {
        return (long) (radius + 1) * x - (long) radius * y;
    }

    private Point fromLattice(long a, long b) {
        return new Point((int) (a * radius + b * (radius + 1)), (int) (a * (radius + 1) - b * radius));
    }

    public boolean isGridPoint(int tileX, int tileY) {
        return Math.floorMod(aNum(tileX, tileY), cellSize) == 0 && Math.floorMod(bNum(tileX, tileY), cellSize) == 0;
    }

    /** The grid point whose diamond covers this tile (the nearest by Manhattan distance). */
    public Point nearestGridPoint(int tileX, int tileY) {
        long a0 = Math.floorDiv(aNum(tileX, tileY), cellSize);
        long b0 = Math.floorDiv(bNum(tileX, tileY), cellSize);
        Point best = null;
        int bestDist = Integer.MAX_VALUE;
        for (long a = a0 - 1; a <= a0 + 2; a++) {
            for (long b = b0 - 1; b <= b0 + 2; b++) {
                Point p = fromLattice(a, b);
                int dist = Math.abs(p.x - tileX) + Math.abs(p.y - tileY);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = p;
                }
            }
        }
        return best;
    }

    /**
     * Largest Manhattan radius a light source keeps above the spawn threshold on open ground.
     * A tile at distance d gets {@code lightLevel - 10d} light, and mobs can spawn when light <= threshold.
     */
    public static int coverageRadius(int lightLevel, int spawnThreshold) {
        int diff = lightLevel - spawnThreshold;
        if (diff <= 0) return 0;
        return (diff + 9) / 10 - 1;
    }
}
