package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ConfigHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

/** Chinese in-game control panel. It reports actual active task states; it does not fabricate AI reasoning. */
public class AltoClefControlScreen extends Screen {
    private static final int PANEL_WIDTH = 330;
    private static final int LINE_HEIGHT = 13;
    private final AltoClef mod;

    public AltoClefControlScreen(AltoClef mod) {
        super(Text.literal("AltoClef 控制台"));
        this.mod = mod;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int y = height - 52;
        Settings settings = mod.getModSettings();

        addDrawableChild(ButtonWidget.builder(Text.literal(mod.isPaused() ? "继续机器人" : "暂停机器人"), button -> {
            mod.setPaused(!mod.isPaused());
            rebuild();
        }).dimensions(left, y, 104, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("停止全部任务"), button -> {
            mod.stopTasks();
            rebuild();
        }).dimensions(left + 112, y, 104, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("关闭"), button -> close())
                .dimensions(left + 224, y, 106, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("玩家 PvP：" + onOff(mod.getMobDefenseChain().isPvPMode())), button -> {
            mod.getMobDefenseChain().setPvPMode(!mod.getMobDefenseChain().isPvPMode());
            rebuild();
        }).dimensions(left, y - 24, 160, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("任务 HUD：" + onOff(settings.shouldShowTaskChain())), button -> {
            settings.setShowTaskChains(!settings.shouldShowTaskChain());
            saveSettings();
            rebuild();
        }).dimensions(left + 170, y - 24, 160, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("投射物闪避：" + onOff(settings.isDodgeProjectiles())), button -> {
            settings.setDodgeProjectiles(!settings.isDodgeProjectiles());
            saveSettings();
            rebuild();
        }).dimensions(left, y - 48, 160, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("生存防御：" + onOff(settings.isMobDefense())), button -> {
            settings.setMobDefense(!settings.isMobDefense());
            saveSettings();
            rebuild();
        }).dimensions(left + 170, y - 48, 160, 20).build());
    }

    private void rebuild() {
        clearChildren();
        init(MinecraftClient.getInstance(), width, height);
    }

    private void saveSettings() {
        ConfigHelper.saveConfig(Settings.SETTINGS_PATH, mod.getModSettings());
    }

    private static String onOff(boolean value) {
        return value ? "开启" : "关闭";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int left = (width - PANEL_WIDTH) / 2;
        int top = 24;
        int bottom = height - 112;
        context.fill(left - 8, top - 8, left + PANEL_WIDTH + 8, bottom, 0xE0181C25);
        context.drawBorder(left - 8, top - 8, PANEL_WIDTH + 16, bottom - top + 8, 0xFF4F6580);

        context.drawText(textRenderer, "AltoClef 中文控制台", left, top, 0xFFFFFFFF, true);
        context.drawText(textRenderer, "实时状态（来自实际任务树，不生成虚构思维）", left, top + 16, 0xFFB7D9FF, false);
        context.drawText(textRenderer, "运行器：" + (mod.getTaskRunner().isActive() ? "运行中" : "空闲")
                + "   暂停：" + onOff(mod.isPaused()), left, top + 32, 0xFFE0E0E0, false);
        context.drawText(textRenderer, "决策状态：" + mod.getTaskRunner().statusReport, left, top + 48, 0xFFFFD98A, false);

        List<Task> tasks = Collections.emptyList();
        if (mod.getTaskRunner().getCurrentTaskChain() != null) {
            tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
        }
        int y = top + 70;
        context.drawText(textRenderer, "当前任务链：", left, y, 0xFFFFFFFF, true);
        y += LINE_HEIGHT + 2;
        if (tasks.isEmpty()) {
            context.drawText(textRenderer, "- 当前没有自动化任务", left + 8, y, 0xFFAAAAAA, false);
        } else {
            int maxLines = Math.max(1, (bottom - y - 8) / LINE_HEIGHT);
            int start = Math.max(0, tasks.size() - maxLines);
            if (start > 0) {
                context.drawText(textRenderer, "... 省略 " + start + " 层父任务", left + 8, y, 0xFFAAAAAA, false);
                y += LINE_HEIGHT;
            }
            for (int i = start; i < tasks.size() && y < bottom - 4; i++) {
                Task task = tasks.get(i);
                String text = "- " + task.toString();
                context.drawText(textRenderer, text, left + 8, y, 0xFFFFFFFF, false);
                y += LINE_HEIGHT;
            }
        }

        context.drawText(textRenderer, "提示：@ui 可重新打开；配置会立即保存。", left, height - 102, 0xFF9BC6A6, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
