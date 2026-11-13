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

public class OriginResetCommand extends ModularChatCommand {
    public OriginResetCommand() {
        super("lightoverlayoriginreset", "Reset this world's torch grid origin to the default", PermissionLevel.USER, false);
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        String key = GridSetup.getWorldKey(client);
        if (key != null)
            LightOverlay.settings.gridOrigins.remove(key);
        Settings.save();
        GridInfoCommand.logInfo(client, commandLog);
    }
}
