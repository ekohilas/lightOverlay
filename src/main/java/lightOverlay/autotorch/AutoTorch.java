package lightOverlay.autotorch;

import lightOverlay.LightOverlay;
import lightOverlay.Settings;
import necesse.engine.input.Control;
import necesse.engine.registries.ObjectRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventory;
import necesse.inventory.PlayerInventorySlot;
import necesse.inventory.item.TorchItem;
import necesse.inventory.item.toolItem.TileDamageOption;
import necesse.inventory.item.toolItem.ToolDamageItem;
import necesse.inventory.item.toolItem.ToolType;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically places regular torches from the player's inventory, the same way the player would by hand (the
 * server validates every placement), so it works in multiplayer without the server having the mod.
 * <ul>
 *     <li>Finds the darkest tile in reach where mobs can spawn and puts a torch on the grid point covering it
 *     ({@link TorchGrid}). If that grid point can't hold a torch (wall, water, furniture...), the torch goes on the dark
 *     tile itself.</li>
 *     <li>Optionally relocates regular torches that are off the grid: a torch is placed on the grid point first, then
 *     the old one is mined with a pickaxe/axe from the inventory. Torches whose grid point is blocked or already
 *     has a torch are left alone, since they are filling a gap the grid can't.</li>
 * </ul>
 * Only acts while the player isn't using an item or holding a mouse button, one action at a time.
 */
public final class AutoTorch {
    private static final String TORCH_ID = "torch";
    private static final long ACTION_INTERVAL_MS = 250;
    private static final long AFTER_PLACE_DELAY_MS = 500;
    /** How long a targeted tile is treated as lit, to give the static light time to update. */
    private static final long TARGET_MEMORY_MS = 3000;
    private static final long RELOCATE_TIMEOUT_MS = 3000;

    private static long nextActionTime;
    private static final Map<Point, Long> recentTargets = new HashMap<>();
    private static Relocation pendingRelocation;

    private AutoTorch() {
    }

    private static class Relocation {
        final Level level;
        final Point from;
        final Point to;
        final long deadline;

        Relocation(Level level, Point from, Point to, long deadline) {
            this.level = level;
            this.from = from;
            this.to = to;
            this.deadline = deadline;
        }
    }

    /** Called every frame from the client's own player controls tick. */
    public static void tick(PlayerMob player) {
        Settings settings = LightOverlay.settings;
        if (settings == null || !settings.autoTorch)
            return;
        long now = System.currentTimeMillis();
        if (now < nextActionTime)
            return;
        Level level = player.getLevel();
        if (level == null || level.lightManager.ambientLightOverride != null)
            return;
        if (player.isAttacking || player.onCooldown || player.getAttackHandler() != null)
            return;
        // Never fight the player for control of their character
        if (Control.MOUSE1.isDown() || Control.MOUSE2.isDown())
            return;

        nextActionTime = now + ACTION_INTERVAL_MS;
        recentTargets.values().removeIf(time -> time < now);

        if (pendingRelocation != null) {
            if (pendingRelocation.level != level || now > pendingRelocation.deadline) {
                pendingRelocation = null;
            } else {
                finishRelocation(player, level, now);
                return;
            }
        }

        int torchSlot = findTorchSlot(player);
        if (torchSlot < 0)
            return;
        InventoryItem torchItem = player.getInv().main.getItem(torchSlot);
        TorchGrid grid = getGrid(level);
        if (grid == null)
            return;
        int placeRange = ((TorchItem) torchItem.item).getTorchPlaceRange(level, torchItem, player);

        if (placeOnDarkestTile(player, level, grid, torchSlot, torchItem, placeRange, now))
            return;
        if (settings.autoTorchRelocate)
            startRelocation(player, level, grid, torchSlot, torchItem, placeRange, now);
    }

    private static TorchGrid getGrid(Level level) {
        int radius = LightOverlay.settings.autoTorchSpacing;
        if (radius <= 0) {
            GameObject torch = ObjectRegistry.getObject(TORCH_ID);
            if (torch == null)
                return null;
            radius = TorchGrid.coverageRadius(torch.lightLevel, LightOverlay.getSpawnLightThreshold(level));
        }
        return radius >= 1 ? new TorchGrid(radius) : null;
    }

    private static boolean placeOnDarkestTile(PlayerMob player, Level level, TorchGrid grid, int torchSlot, InventoryItem torchItem, int placeRange, long now) {
        int playerTileX = player.getTileX();
        int playerTileY = player.getTileY();
        // Dark tiles up to one grid radius outside reach can still be fixed by a grid point within reach
        int scan = placeRange / 32 + grid.radius;
        List<Point> darkTiles = new ArrayList<>();
        for (int x = playerTileX - scan; x <= playerTileX + scan; x++) {
            for (int y = playerTileY - scan; y <= playerTileY + scan; y++) {
                if (LightOverlay.canMobSpawn(level, x, y))
                    darkTiles.add(new Point(x, y));
            }
        }
        if (darkTiles.isEmpty())
            return false;
        darkTiles.sort(Comparator
                .comparingDouble((Point p) -> level.lightManager.getStaticLight(p.x, p.y).getLevel())
                .thenComparingInt(p -> Math.abs(p.x - playerTileX) + Math.abs(p.y - playerTileY)));

        for (Point dark : darkTiles) {
            Point gridPoint = grid.nearestGridPoint(dark.x, dark.y);
            if (recentTargets.containsKey(gridPoint) || recentTargets.containsKey(dark))
                continue; // A torch was just placed for this area; wait for the light to update
            if (!inRange(player, gridPoint, placeRange))
                continue; // Handle it on the grid once the player gets closer
            if (canPlaceAt(player, level, torchItem, gridPoint)) {
                place(player, torchSlot, gridPoint, now);
                return true;
            }
            // Grid point is blocked, or its torch can't reach here (walls): light the dark tile directly
            if (inRange(player, dark, placeRange) && canPlaceAt(player, level, torchItem, dark)) {
                place(player, torchSlot, dark, now);
                return true;
            }
        }
        return false;
    }

    private static void startRelocation(PlayerMob player, Level level, TorchGrid grid, int torchSlot, InventoryItem torchItem, int placeRange, long now) {
        int toolSlot = findToolSlot(player);
        if (toolSlot < 0)
            return;
        InventoryItem toolItem = player.getInv().main.getItem(toolSlot);
        int mineRange = ((ToolDamageItem) toolItem.item).getMiningRange(toolItem, player);
        int torchID = ObjectRegistry.getObjectID(TORCH_ID);

        int playerTileX = player.getTileX();
        int playerTileY = player.getTileY();
        int scan = mineRange / 32 + 1;
        for (int x = playerTileX - scan; x <= playerTileX + scan; x++) {
            for (int y = playerTileY - scan; y <= playerTileY + scan; y++) {
                if (level.getObjectID(x, y) != torchID || grid.isGridPoint(x, y))
                    continue;
                Point torch = new Point(x, y);
                if (!inRange(player, torch, mineRange))
                    continue;
                Point gridPoint = grid.nearestGridPoint(x, y);
                if (recentTargets.containsKey(gridPoint) || !inRange(player, gridPoint, placeRange))
                    continue;
                if (!canPlaceAt(player, level, torchItem, gridPoint))
                    continue; // Grid point blocked or already lit: this torch is filling a gap, keep it
                place(player, torchSlot, gridPoint, now);
                pendingRelocation = new Relocation(level, torch, gridPoint, now + RELOCATE_TIMEOUT_MS);
                return;
            }
        }
    }

    /** Second half of a relocation: once the new torch is down, mine the old one. */
    private static void finishRelocation(PlayerMob player, Level level, long now) {
        Relocation relocation = pendingRelocation;
        int torchID = ObjectRegistry.getObjectID(TORCH_ID);
        if (level.getObjectID(relocation.to.x, relocation.to.y) != torchID)
            return; // New torch not placed yet, wait (until the deadline)
        pendingRelocation = null;
        if (level.getObjectID(relocation.from.x, relocation.from.y) != torchID)
            return; // Old torch already gone

        int toolSlot = findToolSlot(player);
        if (toolSlot < 0)
            return;
        InventoryItem toolItem = player.getInv().main.getItem(toolSlot);
        ToolDamageItem tool = (ToolDamageItem) toolItem.item;
        int levelX = relocation.from.x * 32 + 16;
        int levelY = relocation.from.y * 32 + 16;
        // Make sure the swing would hit exactly that torch and nothing else (e.g. an overlapping tree or wall)
        TileDamageOption hit = tool.getTileDamageOption(level, levelX, levelY, player, null, toolItem, true);
        if (hit == null || hit.layerID != 0 || hit.tileX != relocation.from.x || hit.tileY != relocation.from.y)
            return;
        player.tryAttack(new PlayerInventorySlot(player.getInv().main, toolSlot), levelX, levelY);
        nextActionTime = now + AFTER_PLACE_DELAY_MS;
    }

    private static void place(PlayerMob player, int torchSlot, Point tile, long now) {
        if (player.tryAttack(new PlayerInventorySlot(player.getInv().main, torchSlot), tile.x * 32 + 16, tile.y * 32 + 16)) {
            recentTargets.put(tile, now + TARGET_MEMORY_MS);
            nextActionTime = now + AFTER_PLACE_DELAY_MS;
        }
    }

    /** Only places on empty tiles, so it never replaces furniture or lands on a table/holder. */
    private static boolean canPlaceAt(PlayerMob player, Level level, InventoryItem torchItem, Point tile) {
        if (level.getObjectID(tile.x, tile.y) != 0)
            return false;
        return ((TorchItem) torchItem.item).canPlaceTorch(level, tile.x * 32 + 16, tile.y * 32 + 16, torchItem, player);
    }

    private static boolean inRange(PlayerMob player, Point tile, int range) {
        return player.getPositionPoint().distance(tile.x * 32 + 16, tile.y * 32 + 16) <= range;
    }

    private static int findTorchSlot(PlayerMob player) {
        PlayerInventory main = player.getInv().main;
        for (int i = 0; i < main.getSize(); i++) {
            InventoryItem item = main.getItem(i);
            if (item != null && item.item instanceof TorchItem && TORCH_ID.equals(item.item.getStringID()))
                return i;
        }
        return -1;
    }

    private static int findToolSlot(PlayerMob player) {
        PlayerInventory main = player.getInv().main;
        for (int i = 0; i < main.getSize(); i++) {
            InventoryItem item = main.getItem(i);
            if (item != null && item.item instanceof ToolDamageItem) {
                ToolType type = ((ToolDamageItem) item.item).getToolType(item);
                if (type == ToolType.PICKAXE || type == ToolType.AXE || type == ToolType.ALL)
                    return i;
            }
        }
        return -1;
    }

    /** Logs a failure once and turns the feature off, rather than crashing the game's input handling. */
    public static void onError(Throwable e) {
        System.err.println("Light Overlay auto torch failed and has been disabled:");
        e.printStackTrace();
        if (LightOverlay.settings != null)
            LightOverlay.settings.autoTorch = false;
        pendingRelocation = null;
    }
}
