package lightOverlay.autotorch;

import lightOverlay.LightOverlay;
import lightOverlay.Settings;
import necesse.engine.network.client.Client;
import necesse.engine.network.client.network.ClientAddressNetwork;
import necesse.engine.network.server.Server;
import necesse.engine.registries.ObjectRegistry;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;

import java.awt.Point;

/** Works out the torch layout for the current world from the settings. */
public final class GridSetup {
    public static final String TORCH_ID = "torch";
    /**
     * Worlds migrated from the island versions (before 1.0) put the old home island at tiles 0-299, with the world
     * spawn moved to its center. New worlds find a spawn near (512, 512) instead.
     */
    public static final Point ISLAND_CENTER = new Point(150, 150);

    private GridSetup() {
    }

    /** The layout for this level, or null if a torch can't keep anything lit. */
    public static TorchLayout getLayout(Level level) {
        Settings settings = LightOverlay.settings;
        int distance = settings.lightSourceDistance;
        if (distance <= 0) {
            int radius = getCoverageRadius(level);
            if (radius < 1)
                return null;
            distance = settings.layout.optimalDistance(radius);
        }
        return settings.layout.create(distance, getOrigin(level.getClient(), settings.layout));
    }

    /** How many tiles (Manhattan) a regular torch keeps above the spawn threshold. */
    public static int getCoverageRadius(Level level) {
        GameObject torch = ObjectRegistry.getObject(TORCH_ID);
        if (torch == null)
            return 0;
        return LayoutType.coverageRadius(torch.lightLevel, LightOverlay.getSpawnLightThreshold(level));
    }

    /** The origin the player set for this world, else the default for the layout. */
    public static Point getOrigin(Client client, LayoutType layout) {
        String key = getWorldKey(client);
        Point custom = key == null ? null : LightOverlay.settings.gridOrigins.get(key);
        if (custom != null)
            return custom;
        return getDefaultOrigin(client, layout);
    }

    /**
     * Quadrant layouts center on the old island center, so quadrant corners are tiles 149/150. Other layouts start at
     * (0, 0), or at the island center in worlds migrated from the island versions.
     */
    public static Point getDefaultOrigin(Client client, LayoutType layout) {
        if (layout.quadrant || isMigratedWorld(client))
            return ISLAND_CENTER;
        return new Point(0, 0);
    }

    /**
     * Whether this world was migrated from the island versions. Only known when the world runs on this computer
     * (singleplayer or hosting): a remote server doesn't send its spawn tile to clients.
     */
    public static boolean isMigratedWorld(Client client) {
        Server server = client == null ? null : client.getLocalServer();
        if (server == null || server.world == null || server.world.worldEntity == null)
            return false;
        return ISLAND_CENTER.equals(server.world.worldEntity.spawnTile);
    }

    /** Identifies the world for per-world settings: its unique ID when local, else the server address. */
    public static String getWorldKey(Client client) {
        if (client == null)
            return null;
        Server server = client.getLocalServer();
        if (server != null && server.world != null && server.world.worldEntity != null)
            return "world_" + Long.toHexString(server.world.worldEntity.getUniqueID());
        if (client.network instanceof ClientAddressNetwork) {
            ClientAddressNetwork network = (ClientAddressNetwork) client.network;
            return ("server_" + network.addressString + "_" + network.port).replaceAll("[^A-Za-z0-9_]", "_").toLowerCase();
        }
        return "server_other";
    }
}
