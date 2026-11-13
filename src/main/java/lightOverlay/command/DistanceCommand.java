package lightOverlay.command;

import lightOverlay.LightOverlay;
import lightOverlay.Settings;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.IntParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.GameMath;

public class DistanceCommand extends ModularChatCommand {
    public DistanceCommand() {
        super("lightoverlaydistance", "Set the tiles between light sources (0 = optimal)", PermissionLevel.USER, false,
                new CmdParameter("tiles", new IntParameterHandler(0)));
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        LightOverlay.settings.lightSourceDistance = GameMath.limit((int) args[0], 0, 100);
        Settings.save();
        GridInfoCommand.logInfo(client, commandLog);
    }
}
