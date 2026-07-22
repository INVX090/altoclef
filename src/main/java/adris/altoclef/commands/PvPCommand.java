package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;

public class PvPCommand extends Command {
    public PvPCommand() {
        super("pvp", "Toggle PvP mode — auto-attack all nearby players");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        boolean now = !mod.getMobDefenseChain().isPvPMode();
        mod.getMobDefenseChain().setPvPMode(now);
        mod.log("PvP mode " + (now ? "ON" : "OFF"));
        finish();
    }
}
