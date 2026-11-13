package lightOverlay.command;

import lightOverlay.LightOverlay;
import lightOverlay.Settings;
import lightOverlay.autotorch.GridSetup;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.level.maps.Level;

import java.awt.Point;

public class GridInfoCommand extends ModularChatCommand {
    public GridInfoCommand() {
        super("lightoverlaygridinfo", "Show the torch layout, light source distance and grid origin", PermissionLevel.USER, false);
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        logInfo(client, commandLog);
    }

    static void logInfo(Client client, CommandLog log) {
        Settings settings = LightOverlay.settings;
        log.add("Torch layout: " + settings.layout.id);

        Level level = client == null ? null : client.getLevel();
        int radius = level == null ? 0 : GridSetup.getCoverageRadius(level);
        String optimal = radius < 1 ? "?" : String.valueOf(settings.layout.optimalDistance(radius));
        log.add("Light source distance: " + (settings.lightSourceDistance <= 0
                ? optimal + " tiles (optimal)"
                : settings.lightSourceDistance + " tiles (optimal is " + optimal + ")"));

        Point origin = GridSetup.getOrigin(client, settings.layout);
        String key = GridSetup.getWorldKey(client);
        boolean custom = key != null && settings.gridOrigins.containsKey(key);
        String kind = settings.layout.quadrant ? "Quadrant center" : "Grid origin";
        log.add(kind + ": " + origin.x + ", " + origin.y + (custom ? " (set for this world)"
                : GridSetup.isMigratedWorld(client) ? " (default, migrated island world)" : " (default)"));
    }
}
