package lightOverlay.command;

import lightOverlay.LightOverlay;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.IntParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.GameMath;

public class TorchSpacingCommand extends ModularChatCommand {
    public TorchSpacingCommand() {
        super("lightoverlaytorchspacing", "Set the auto torch grid radius in tiles (0 = automatic)", PermissionLevel.USER, false,
                new CmdParameter("radius", new IntParameterHandler(0)));
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        int newValue = GameMath.limit((int) args[0], 0, 50);
        LightOverlay.settings.autoTorchSpacing = newValue;
        commandLog.add("Set auto torch grid radius to: " + (newValue == 0 ? "automatic" : newValue + " tiles"));
    }
}
