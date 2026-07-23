package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.ui.AltoClefControlScreen;
import net.minecraft.client.MinecraftClient;

/** Opens the in-game Chinese control panel. */
public class OpenControlPanelCommand extends Command {
    public OpenControlPanelCommand() {
        super("ui", "打开 AltoClef 中文控制面板");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        MinecraftClient.getInstance().setScreen(new AltoClefControlScreen(mod));
        finish();
    }
}
