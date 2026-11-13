import lightOverlay.autotorch.*;
import java.awt.Point;
import java.util.*;
public class LayoutTest {
    // Max over tiles in region of min Manhattan distance to an actual grid point (brute force)
    static int[] coverage(TorchLayout l, int x0, int y0, int x1, int y1) {
        List<Point> pts = new ArrayList<>();
        for (int x = x0 - 30; x <= x1 + 30; x++) for (int y = y0 - 30; y <= y1 + 30; y++) if (l.isGridPoint(x, y)) pts.add(new Point(x, y));
        int max = 0;
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) {
            int best = Integer.MAX_VALUE;
            for (Point p : pts) best = Math.min(best, Math.abs(p.x - x) + Math.abs(p.y - y));
            max = Math.max(max, best);
            Point n = l.nearestGridPoint(x, y);
            if (!l.isGridPoint(n.x, n.y)) throw new AssertionError("nearest not grid " + n);
        }
        int count = 0; for (Point p : pts) if (p.x >= x0 && p.x <= x1 && p.y >= y0 && p.y <= y1) count++;
        return new int[]{max, count};
    }
    public static void main(String[] a) {
        int r = LayoutType.coverageRadius(150, 50);
        if (r != 9) throw new AssertionError();
        int x0 = 100, y0 = 100, x1 = 199, y1 = 199; // spans the 149|150 quadrant lines
        Point center = new Point(150, 150);
        for (LayoutType t : LayoutType.values()) {
            int d = t.optimalDistance(r);
            int[] c = t.create(d, center).nearestGridPoint(0,0) == null ? null : coverage(t.create(d, center), x0, y0, x1, y1);
            int[] over = coverage(t.create(d + 1, center), x0, y0, x1, y1);
            if (over[0] <= r) over = coverage(t.create(d + 2, center), x0, y0, x1, y1); // d + 1 can round down to d
            System.out.printf("%-16s optimal distance %2d: max dark distance %d (<= %d), %5.1f tiles per light | next spacing up -> %d%n",
                    t.id, d, c[0], r, 10000.0 / c[1], over[0]);
            if (c[0] > r) throw new AssertionError("gap at optimal");
            if (over[0] <= r) throw new AssertionError("not optimal: +1 still covers");
        }
        // Quadrant: corners are lights, mirrored, nearest stays in quadrant
        for (LayoutType t : new LayoutType[]{LayoutType.DIAMOND_QUADRANT, LayoutType.SQUARE_QUADRANT}) {
            TorchLayout l = t.create(13, center);
            for (int[] p : new int[][]{{149,149},{150,149},{149,150},{150,150}}) if (!l.isGridPoint(p[0], p[1])) throw new AssertionError("corner " + p[0]+","+p[1]);
            for (int x = 120; x < 180; x++) for (int y = 120; y < 180; y++) {
                if (l.isGridPoint(x, y) != l.isGridPoint(299 - x, y)) throw new AssertionError("mirror x");
                if (l.isGridPoint(x, y) != l.isGridPoint(x, 299 - y)) throw new AssertionError("mirror y");
                Point n = l.nearestGridPoint(x, y);
                if ((n.x >= 150) != (x >= 150) || (n.y >= 150) != (y >= 150)) throw new AssertionError("crossed quadrant");
            }
        }
        // Nice preset: diamond quadrant, distance 13 -> 14 apart in a row, rows 7 apart
        TorchLayout nice = LayoutType.DIAMOND_QUADRANT.create(13, center);
        if (!nice.isGridPoint(164, 150) || nice.isGridPoint(163, 150) || !nice.isGridPoint(157, 157) || !nice.isGridPoint(150, 164)) throw new AssertionError("nice spacing");
        System.out.println("nice (diamondquadrant 13): max dark distance " + coverage(nice, x0, y0, x1, y1)[0]);
        // Non-quadrant layouts use the origin
        if (!LayoutType.DIAMOND.create(18, new Point(0,0)).isGridPoint(0,0) || !LayoutType.SQUARE.create(8, new Point(5,7)).isGridPoint(14,16)) throw new AssertionError("origin");
        System.out.println("OK");
    }
}
