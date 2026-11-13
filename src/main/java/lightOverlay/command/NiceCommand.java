package lightOverlay.command;

import lightOverlay.LightOverlay;
import lightOverlay.Settings;
import lightOverlay.autotorch.LayoutType;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

/** The "nice" preset: diamonds aligned to the quadrants, 13 tiles between lights (a light every 14th tile). */
public class NiceCommand extends ModularChatCommand {
    public static final int NICE_DISTANCE = 13;

    public NiceCommand() {
        super("lightoverlaynice", "Use the nice torch layout: quadrant diamonds, 13 tiles apart", PermissionLevel.USER, false);
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        LightOverlay.settings.layout = LayoutType.DIAMOND_QUADRANT;
        LightOverlay.settings.lightSourceDistance = NICE_DISTANCE;
        Settings.save();
        GridInfoCommand.logInfo(client, commandLog);
    }
}
