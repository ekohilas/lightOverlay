package tileMeasure;

import necesse.engine.input.Control;
import necesse.engine.input.InputEvent;
import necesse.gfx.Renderer;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.ui.HUD;
import necesse.level.maps.Level;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Tile distance measuring tool, only active while the debug HUD (F2) is shown.
 * <p>
 * Right click a tile to select it. While a single tile is selected, a line is drawn from it to the
 * hovered tile. Right clicking a second tile locks the line between the two. Right clicking a selected
 * tile unselects it (keeping the other one), and right clicking any other tile unselects both.
 * <p>
 * Every tile on the line is outlined; the selected tiles get a thicker, yellow outline.
 */
public class TileMeasure {
    public static Point first;
    public static Point second;
    private static Level level;
    private static boolean consumedPress;

    /**
     * Called before the player handles a world input event.
     *
     * @return true if the event was handled here and the player should not handle it.
     */
    public static boolean onInputEvent(Level currentLevel, InputEvent event, GameCamera camera) {
        if (event.getID() != Control.MOUSE2.defaultKey) // Right mouse button
            return false;
        if (!event.state) {
            // Also swallow the release of a right click we consumed
            if (consumedPress) {
                consumedPress = false;
                event.use();
                return true;
            }
            return false;
        }
        if (!HUD.debugActive || camera == null)
            return false;
        syncLevel(currentLevel);
        select(new Point(camera.getMouseLevelTilePosX(event), camera.getMouseLevelTilePosY(event)));
        consumedPress = true;
        event.use();
        return true;
    }

    public static void select(Point tile) {
        if (first == null) {
            first = tile;
        } else if (second == null) {
            if (tile.equals(first))
                first = null;
            else
                second = tile;
        } else if (tile.equals(first)) {
            first = second;
            second = null;
        } else if (tile.equals(second)) {
            second = null;
        } else {
            clear();
        }
    }

    public static void clear() {
        first = null;
        second = null;
    }

    private static void syncLevel(Level currentLevel) {
        if (currentLevel != level) {
            level = currentLevel;
            clear();
        }
    }

    /** The tile the line ends on: the second selection, or the hovered tile if there is none. */
    private static Point getEnd(int hoverTileX, int hoverTileY) {
        return second != null ? second : new Point(hoverTileX, hoverTileY);
    }

    /** Outlines every tile on the line between the selected tile and the end tile. */
    public static void draw(Level currentLevel, GameCamera camera) {
        if (!HUD.debugActive)
            return;
        syncLevel(currentLevel);
        if (first == null)
            return;
        Point end = getEnd(camera.getMouseLevelTilePosX(), camera.getMouseLevelTilePosY());
        for (Point tile : getLine(first, end)) {
            boolean selected = tile.equals(first) || tile.equals(second);
            drawOutline(camera, tile, selected ? SELECTED_COLOR : LINE_COLOR, selected ? 2 : 1);
        }
    }

    private static final float[] LINE_COLOR = {1f, 1f, 1f, 0.9f};
    private static final float[] SELECTED_COLOR = {1f, 0.85f, 0.1f, 1f};

    /**
     * Draws a border just inside the tile: one dark pixel on the edge (so it shows on bright ground),
     * then {@code thickness} pixels in the given colour.
     */
    private static void drawOutline(GameCamera camera, Point tile, float[] color, int thickness) {
        int x = camera.getTileDrawX(tile.x);
        int y = camera.getTileDrawY(tile.y);
        drawBorder(x, y, 32, 1, 0f, 0f, 0f, 0.6f);
        drawBorder(x + 1, y + 1, 30, thickness, color[0], color[1], color[2], color[3]);
    }

    /** Draws a square border of the given size and thickness, with its top left corner at (x, y). */
    private static void drawBorder(int x, int y, int size, int t, float r, float g, float b, float a) {
        Renderer.initQuadDraw(size, t).color(r, g, b, a).draw(x, y); // top
        Renderer.initQuadDraw(size, t).color(r, g, b, a).draw(x, y + size - t); // bottom
        Renderer.initQuadDraw(t, size - 2 * t).color(r, g, b, a).draw(x, y + t); // left
        Renderer.initQuadDraw(t, size - 2 * t).color(r, g, b, a).draw(x + size - t, y + t); // right
    }

    /** Adds the measurement line to the debug HUD tooltip. */
    public static void addDebugTooltip(int hoverTileX, int hoverTileY, StringTooltips tooltips) {
        if (first == null)
            return;
        Point end = getEnd(hoverTileX, hoverTileY);
        tooltips.add("Tile count: " + getTileCount(first, end)
                + " (" + first.x + "," + first.y + ") -> (" + end.x + "," + end.y + ")");
    }

    /** Number of tiles on the line between the two tiles, both ends inclusive. */
    public static int getTileCount(Point start, Point end) {
        return Math.max(Math.abs(end.x - start.x), Math.abs(end.y - start.y)) + 1;
    }

    /** Tiles on the line between the two tiles (Bresenham), both ends inclusive. */
    public static List<Point> getLine(Point start, Point end) {
        List<Point> tiles = new ArrayList<>(getTileCount(start, end));
        int x = start.x, y = start.y;
        int dx = Math.abs(end.x - x), dy = -Math.abs(end.y - y);
        int sx = x < end.x ? 1 : -1, sy = y < end.y ? 1 : -1;
        int err = dx + dy;
        while (true) {
            tiles.add(new Point(x, y));
            if (x == end.x && y == end.y)
                break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y += sy;
            }
        }
        return tiles;
    }
}
