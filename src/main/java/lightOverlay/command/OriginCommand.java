package lightOverlay.command;

import lightOverlay.LightOverlay;
import lightOverlay.Settings;
import lightOverlay.autotorch.GridSetup;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.IntParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.PlayerMob;

import java.awt.Point;

/**
 * Sets this world's grid origin: where the lattice starts, or the quadrant center for quadrant layouts (the quadrant
 * lines run just before it, so center 150 150 gives corner tiles 149 and 150). With no coordinates, uses the tile
 * you're standing on.
 */
public class OriginCommand extends ModularChatCommand {
    public OriginCommand() {
        super("lightoverlayorigin", "Set this world's torch grid origin (default: your tile)", PermissionLevel.USER, false,
                new CmdParameter("tileX", new IntParameterHandler(null), true),
                new CmdParameter("tileY", new IntParameterHandler(null), true));
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        String key = GridSetup.getWorldKey(client);
        if (key == null) {
            commandLog.add("Join a world first");
            return;
        }
        Point origin;
        if (args[0] != null && args[1] != null) {
            origin = new Point((Integer) args[0], (Integer) args[1]);
        } else if (args[0] == null && args[1] == null) {
            PlayerMob player = client.getPlayer();
            if (player == null) {
                commandLog.add("Could not find your position");
                return;
            }
            origin = new Point(player.getTileX(), player.getTileY());
        } else {
            commandLog.add("Give both tileX and tileY, or neither to use your position");
            return;
        }
        LightOverlay.settings.gridOrigins.put(key, origin);
        Settings.save();
        GridInfoCommand.logInfo(client, commandLog);
    }
}
