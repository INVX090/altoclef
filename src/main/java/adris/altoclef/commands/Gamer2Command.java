package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;

public class Gamer2Command extends Command {
    public Gamer2Command() {
        super("gamer2", "Beats the game with PvP mode enabled");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.getMobDefenseChain().setPvPMode(true);
        mod.runUserTask(new BeatMinecraftTask(mod), () -> {
            mod.getMobDefenseChain().setPvPMode(false);
            finish();
        });
    }
}
