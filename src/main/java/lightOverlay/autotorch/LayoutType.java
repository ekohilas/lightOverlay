package lightOverlay.autotorch;

import java.awt.Point;

/**
 * The torch layouts. Spacing is given as the light source distance: how many tiles are between two neighbouring light
 * sources (so 13 means a light every 14th tile along a row).
 * <p>
 * A light source keeps every tile within Manhattan (walking) distance {@code r} of it above the spawn threshold (see
 * {@link #coverageRadius}). Each layout's optimal distance is the largest one that still leaves no spawnable tile on
 * open ground.
 */
public enum LayoutType {
    /** Aligned diamonds mirrored into the four quadrants around the center. */
    DIAMOND_QUADRANT("diamondquadrant", true),
    /** Lights in rows, every other row shifted by half: a 45° rotated square grid. */
    DIAMOND_ALIGNED("diamondaligned", false),
    /** Skewed diamond lattice: every tile is covered exactly once, the fewest lights. */
    DIAMOND("diamond", false),
    /** Square grid mirrored into the four quadrants around the center. */
    SQUARE_QUADRANT("squarequadrant", true),
    /** Lights line up in rows and columns. */
    SQUARE("square", false);

    public final String id;
    public final boolean quadrant;

    LayoutType(String id, boolean quadrant) {
        this.id = id;
        this.quadrant = quadrant;
    }

    public static LayoutType fromID(String id) {
        for (LayoutType type : values())
            if (type.id.equalsIgnoreCase(id))
                return type;
        return null;
    }

    public static String[] ids() {
        LayoutType[] types = values();
        String[] ids = new String[types.length];
        for (int i = 0; i < types.length; i++)
            ids[i] = types[i].id;
        return ids;
    }

    /**
     * Largest Manhattan radius a light source keeps above the spawn threshold on open ground. Static light drops 10
     * per tile, and mobs can spawn when light <= threshold, so a tile at distance d is safe when
     * {@code lightLevel - 10d > threshold}.
     */
    public static int coverageRadius(int lightLevel, int spawnThreshold) {
        int diff = lightLevel - spawnThreshold;
        if (diff <= 0) return 0;
        return (diff + 9) / 10 - 1;
    }

    /** The largest light source distance that leaves no gaps for a light covering radius r. */
    public int optimalDistance(int r) {
        switch (this) {
            case DIAMOND:
                return 2 * r;            // Neighbours (r, r+1) apart: 2r + 1 steps
            case DIAMOND_ALIGNED:
            case DIAMOND_QUADRANT:
                return 2 * r - 1;        // Neighbours (r, r) or (2r, 0) apart: 2r steps
            default:
                return (r % 2 == 1 ? r : r + 1) - 1; // Square cell's far corner is 2 * floor(s / 2) away
        }
    }

    /**
     * @param distance tiles between neighbouring light sources (rounded down where a layout needs an even or odd step)
     * @param origin   lattice origin, or the quadrant center for quadrant layouts
     */
    public TorchLayout create(int distance, Point origin) {
        int step = Math.max(1, distance + 1); // Steps from one light to the next
        int ux, uy, vx, vy;
        switch (this) {
            case DIAMOND: {
                int k = Math.max(1, (step - 1) / 2);
                ux = k; uy = k + 1; vx = k + 1; vy = -k;
                break;
            }
            case DIAMOND_ALIGNED:
            case DIAMOND_QUADRANT: {
                int h = Math.max(1, step / 2);
                ux = h; uy = h; vx = h; vy = -h;
                break;
            }
            default:
                ux = step; uy = 0; vx = 0; vy = step;
        }
        if (quadrant)
            return new QuadrantLayout(new Lattice(ux, uy, vx, vy, 0, 0), origin.x, origin.y);
        return new Lattice(ux, uy, vx, vy, origin.x, origin.y);
    }
}
