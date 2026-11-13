import lightOverlay.autotorch.*;
import java.awt.Point;
import java.io.*;
import java.util.*;
/** Writes, per layout, a grid of: -1 for a light, else Manhattan distance to the nearest light; plus cell borders. */
public class Dump {
    public static void main(String[] a) throws Exception {
        int x0 = 118, y0 = 128, w = 64, h = 44;
        Point center = new Point(150, 150);
        String[][] configs = {
            {"diamondquadrant", "17"}, {"diamondaligned", "17"}, {"diamond", "18"},
            {"squarequadrant", "8"}, {"square", "8"}, {"diamondquadrant", "13"}};
        for (String[] c : configs) {
            LayoutType t = LayoutType.fromID(c[0]);
            int d = Integer.parseInt(c[1]);
            // Non-quadrant layouts use origin (0, 0) in new worlds
            TorchLayout l = t.create(d, t.quadrant ? center : new Point(0, 0));
            List<Point> pts = new ArrayList<>();
            for (int x = x0 - 40; x < x0 + w + 40; x++) for (int y = y0 - 40; y < y0 + h + 40; y++) if (l.isGridPoint(x, y)) pts.add(new Point(x, y));
            try (PrintWriter out = new PrintWriter(new File(a[0], c[0] + "-" + d + ".txt"))) {
                out.println(x0 + " " + y0 + " " + w + " " + h + " " + (t.quadrant ? 1 : 0));
                for (int y = y0; y < y0 + h; y++) {
                    StringBuilder sb = new StringBuilder();
                    for (int x = x0; x < x0 + w; x++) {
                        int best = Integer.MAX_VALUE;
                        for (Point p : pts) best = Math.min(best, Math.abs(p.x - x) + Math.abs(p.y - y));
                        Point n = l.nearestGridPoint(x, y);
                        int right = l.nearestGridPoint(x + 1, y).equals(n) ? 0 : 1;
                        int down = l.nearestGridPoint(x, y + 1).equals(n) ? 0 : 2;
                        sb.append(l.isGridPoint(x, y) ? -1 : best).append(':').append(right | down).append(' ');
                    }
                    out.println(sb.toString().trim());
                }
            }
        }
    }
}
