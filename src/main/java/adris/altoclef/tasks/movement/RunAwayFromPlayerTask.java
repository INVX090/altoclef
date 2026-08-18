package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import adris.altoclef.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.stream.Collectors;

public class RunAwayFromPlayerTask extends CustomBaritoneGoalTask {

    private final double distanceToRun;

    public RunAwayFromPlayerTask(double distance) {
        distanceToRun = distance;
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        // We want to run away NOW
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        return new GoalRunAwayFromPlayers(mod, distanceToRun);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RunAwayFromPlayerTask task) {
            return Math.abs(task.distanceToRun - distanceToRun) < 1;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Running away from player, distance=" + distanceToRun;
    }

    private class GoalRunAwayFromPlayers extends GoalRunAwayFromEntities {

        public GoalRunAwayFromPlayers(AltoClef mod, double distance) {
            super(mod, distance, false, 0.8);
        }

        @Override
        protected List<Entity> getEntities(AltoClef mod) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                return mod.getEntityTracker().getLoadedPlayers().stream()
                        .filter(player -> !player.equals(mod.getPlayer()) && player.isAlive())
                        .collect(Collectors.toList());
            }
        }
    }
}
