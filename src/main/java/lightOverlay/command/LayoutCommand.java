package lightOverlay.command;

import lightOverlay.LightOverlay;
import lightOverlay.Settings;
import lightOverlay.autotorch.LayoutType;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.PresetStringParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

public class LayoutCommand extends ModularChatCommand {
    public LayoutCommand() {
        super("lightoverlaylayout", "Set the torch layout", PermissionLevel.USER, false,
                new CmdParameter("layout", new PresetStringParameterHandler(LayoutType.ids())));
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        LayoutType layout = LayoutType.fromID((String) args[0]);
        if (layout == null) {
            commandLog.add("Unknown layout. Use one of: " + String.join(", ", LayoutType.ids()));
            return;
        }
        LightOverlay.settings.layout = layout;
        Settings.save();
        GridInfoCommand.logInfo(client, commandLog);
    }
}
