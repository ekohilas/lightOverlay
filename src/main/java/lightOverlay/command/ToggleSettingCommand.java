package lightOverlay.command;

import lightOverlay.Settings;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.clientCommands.BoolClientCommand;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Toggles (or sets with 1/0) a boolean setting, then saves it so it survives settings reloads. */
public class ToggleSettingCommand extends BoolClientCommand {
    public ToggleSettingCommand(String name, String action, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(name, action, PermissionLevel.USER, (client, log, result) -> {
            boolean value = result.result(getter.getAsBoolean());
            setter.accept(value);
            Settings.save();
            log.add(label + ": " + (value ? "on" : "off"));
        });
    }
}
