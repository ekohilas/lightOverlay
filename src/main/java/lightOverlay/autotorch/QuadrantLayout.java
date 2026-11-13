package lightOverlay.autotorch;

import java.awt.Point;

/**
 * Splits the world into four quadrants around a center and mirrors a layout into each one, starting at the quadrant's
 * corner tile and stopping at the quadrant lines. The lines run between {@code centerX - 1} and {@code centerX}, and
 * between {@code centerY - 1} and {@code centerY}, so with center (150, 150) the corners are 149/150.
 * <p>
 * Every quadrant gets a light source on its corner, so lights pair up across the lines, which keeps a lit path along
 * them.
 */
public final class QuadrantLayout implements TorchLayout {
    private final Lattice base; // Origin (0, 0), in quadrant-local coordinates
    private final int centerX, centerY;

    public QuadrantLayout(Lattice base, int centerX, int centerY) {
        this.base = base;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    private int localX(int tileX) {
        return tileX >= centerX ? tileX - centerX : centerX - 1 - tileX;
    }

    private int localY(int tileY) {
        return tileY >= centerY ? tileY - centerY : centerY - 1 - tileY;
    }

    @Override
    public boolean isGridPoint(int tileX, int tileY) {
        return base.isGridPoint(localX(tileX), localY(tileY));
    }

    @Override
    public Point nearestGridPoint(int tileX, int tileY) {
        Point local = base.nearestGridPoint(localX(tileX), localY(tileY), 0, 0);
        int x = tileX >= centerX ? centerX + local.x : centerX - 1 - local.x;
        int y = tileY >= centerY ? centerY + local.y : centerY - 1 - local.y;
        return new Point(x, y);
    }
}
