package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;
import java.util.Optional;

/**
 * Kill a specific player by name
 */
public class KillPlayerTask extends AbstractKillEntityTask {

    private final String playerName;

    public KillPlayerTask(String playerName) {
        this.playerName = playerName;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        if (mod.getWorld() == null) return Optional.empty();
        for (PlayerEntity player : mod.getWorld().getPlayers()) {
            if (player.getName().getString().equalsIgnoreCase(playerName)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        if (other instanceof KillPlayerTask task) {
            return Objects.equals(task.playerName, playerName);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Killing player: " + playerName;
    }
}
