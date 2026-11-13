package lightOverlay.autotorch;

import java.awt.Point;

/** All tiles {@code origin + i*u + j*v} for integers i, j. */
public final class Lattice implements TorchLayout {
    private final int ux, uy, vx, vy;
    private final int originX, originY;
    private final long det;

    public Lattice(int ux, int uy, int vx, int vy, int originX, int originY) {
        this.ux = ux;
        this.uy = uy;
        this.vx = vx;
        this.vy = vy;
        this.originX = originX;
        this.originY = originY;
        this.det = (long) ux * vy - (long) uy * vx;
        if (det == 0)
            throw new IllegalArgumentException("basis vectors are parallel");
    }

    /** i * det for the tile, solving tile - origin = i*u + j*v. */
    private long iNum(long dx, long dy) {
        return dx * vy - dy * vx;
    }

    private long jNum(long dx, long dy) {
        return ux * dy - uy * dx;
    }

    @Override
    public boolean isGridPoint(int tileX, int tileY) {
        long dx = tileX - originX, dy = tileY - originY;
        return iNum(dx, dy) % det == 0 && jNum(dx, dy) % det == 0;
    }

    @Override
    public Point nearestGridPoint(int tileX, int tileY) {
        return nearestGridPoint(tileX, tileY, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    /** Nearest grid point with x >= minX and y >= minY (used to keep quadrant layouts inside their quadrant). */
    Point nearestGridPoint(int tileX, int tileY, int minX, int minY) {
        long dx = tileX - originX, dy = tileY - originY;
        long i0 = Math.floorDiv(iNum(dx, dy), det);
        long j0 = Math.floorDiv(jNum(dx, dy), det);
        Point best = null;
        long bestDist = Long.MAX_VALUE;
        for (long i = i0 - 2; i <= i0 + 3; i++) {
            for (long j = j0 - 2; j <= j0 + 3; j++) {
                long px = originX + i * ux + j * vx;
                long py = originY + i * uy + j * vy;
                if (px < minX || py < minY)
                    continue;
                long dist = Math.abs(px - tileX) + Math.abs(py - tileY);
                // Ties go to the smaller x, then y, so cells are stable
                if (dist < bestDist || (dist == bestDist && (px < best.x || (px == best.x && py < best.y)))) {
                    bestDist = dist;
                    best = new Point((int) px, (int) py);
                }
            }
        }
        return best;
    }
}
