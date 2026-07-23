package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Attacks a specific player and follows their last known position if they unload. */
public class KillPlayerTask extends AbstractKillEntityTask {

    private static final double LAST_POSITION_REACHED_DISTANCE = 4;

    private final String playerName;
    private final TimerGame localSearchTimer = new TimerGame(30);

    private Vec3d lastKnownPosition;
    private Dimension lastKnownDimension;
    private Task goToLastPositionTask;
    private TimeoutWanderTask localSearchTask;
    private Vec3d goToTaskPosition;
    private boolean searchedLastPosition;
    private boolean localSearchStarted;
    private boolean warnedUnknownTarget;
    private boolean targetDefeated;

    public KillPlayerTask(String playerName) {
        this.playerName = playerName;
    }

    @Override
    protected void onStart() {
        super.onStart();
        localSearchTimer.setInterval(AltoClef.getInstance().getModSettings().getPlayerHuntSearchTimeoutSeconds());
        localSearchTimer.reset();
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        if (mod.getWorld() == null) return Optional.empty();

        if (mod.getPlayer().getName().getString().equalsIgnoreCase(playerName)) {
            targetDefeated = true;
            return Optional.empty();
        }

        Optional<PlayerEntity> tracked = mod.getEntityTracker().getPlayerEntity(playerName);
        if (tracked.isPresent()) {
            PlayerEntity player = tracked.get();
            if (!player.isAlive() || player.isDead()) {
                targetDefeated = true;
                return Optional.empty();
            }
            rememberTarget(player);
            return Optional.of(player);
        }

        // Keep a direct scan for compatibility with tracker updates during the current tick.
        for (PlayerEntity player : mod.getWorld().getPlayers()) {
            if (player.getName().getString().equalsIgnoreCase(playerName)) {
                if (!player.isAlive() || player.isDead()) {
                    targetDefeated = true;
                    return Optional.empty();
                }
                rememberTarget(player);
                return Optional.of(player);
            }
        }

        if (lastKnownPosition == null) {
            mod.getEntityTracker().getPlayerMostRecentPosition(playerName).ifPresent(position -> {
                lastKnownPosition = position;
                lastKnownDimension = WorldHelper.getCurrentDimension();
            });
        }
        return Optional.empty();
    }

    private void rememberTarget(PlayerEntity player) {
        Vec3d currentPosition = player.getPos();
        if (lastKnownPosition == null || lastKnownPosition.squaredDistanceTo(currentPosition) > 4) {
            goToLastPositionTask = null;
            goToTaskPosition = null;
        }
        lastKnownPosition = currentPosition;
        lastKnownDimension = WorldHelper.getCurrentDimension();
        localSearchTask = null;
        searchedLastPosition = false;
        localSearchStarted = false;
        warnedUnknownTarget = false;
    }

    @Override
    protected Task onEntityNotFound(AltoClef mod) {
        if (targetDefeated) {
            setDebugState("Target " + playerName + " is no longer alive");
            return null;
        }
        if (lastKnownPosition == null) {
            if (!warnedUnknownTarget) {
                mod.logWarning("Player \"" + playerName + "\" is outside client tracking range and has no known position. Waiting instead of wandering randomly.");
                warnedUnknownTarget = true;
            }
            setDebugState("Waiting for unseen player " + playerName);
            return null;
        }

        BlockPos target = BlockPosVer.ofFloored(lastKnownPosition);
        if (lastKnownDimension != WorldHelper.getCurrentDimension()
                || mod.getPlayer().getPos().squaredDistanceTo(lastKnownPosition)
                > LAST_POSITION_REACHED_DISTANCE * LAST_POSITION_REACHED_DISTANCE) {
            setDebugState("Going to last known position of " + playerName + ": " + target.toShortString());
            if (goToLastPositionTask == null || goToTaskPosition == null
                    || goToTaskPosition.squaredDistanceTo(lastKnownPosition) > 4) {
                goToTaskPosition = lastKnownPosition;
                goToLastPositionTask = new GetToBlockTask(target, false, lastKnownDimension);
            }
            return goToLastPositionTask;
        }

        if (!searchedLastPosition) {
            if (!localSearchStarted) {
                localSearchStarted = true;
                localSearchTimer.reset();
            }
            int radius = mod.getModSettings().getPlayerHuntSearchRadius();
            if (radius <= 0 || localSearchTimer.elapsed()) {
                searchedLastPosition = true;
            } else {
                setDebugState("Searching within " + radius + " blocks of " + playerName + "'s last known position");
                if (localSearchTask == null) {
                    localSearchTask = new TimeoutWanderTask(radius, false);
                    return localSearchTask;
                }
                if (!localSearchTask.isActive() || !localSearchTask.isFinished()) return localSearchTask;
                searchedLastPosition = true;
            }
        }

        setDebugState("Player " + playerName + " lost; waiting at last known position");
        return null;
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        Item axe = entity instanceof PlayerEntity player && player.isBlocking() ? getBestAxe(mod) : null;
        if (axe != null) {
            if (!mod.getSlotHandler().forceEquipItem(axe)) return null;
        } else if (equipWeapon(mod)) {
            return null;
        }

        if (mod.getPlayer().getAttackCooldownProgress(0) >= 0.95f) {
            LookHelper.lookAt(mod, entity.getEyePos());
            mod.getControllerExtras().attack(entity);
        }
        return null;
    }

    private static Item getBestAxe(AltoClef mod) {
        Item[] axes = {
                Items.NETHERITE_AXE, Items.DIAMOND_AXE, Items.IRON_AXE,
                Items.STONE_AXE, Items.GOLDEN_AXE, Items.WOODEN_AXE
        };
        for (Item axe : axes) {
            if (mod.getItemStorage().hasItem(axe)) return axe;
        }
        return null;
    }

    @Override
    public boolean isFinished() {
        return targetDefeated;
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        if (other instanceof KillPlayerTask task) {
            return Objects.equals(task.playerName.toLowerCase(Locale.ROOT), playerName.toLowerCase(Locale.ROOT));
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Killing player: " + playerName;
    }
}
