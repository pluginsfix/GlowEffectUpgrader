package pluginsfix.gloweffectupgrader;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsfix.gloweffectupgrader.command.ClearEffectsCommand;
import pluginsfix.gloweffectupgrader.command.SmithCommand;
import pluginsfix.gloweffectupgrader.config.PluginConfig;
import pluginsfix.gloweffectupgrader.domain.EffectService;
import pluginsfix.gloweffectupgrader.gui.GuiListener;
import pluginsfix.gloweffectupgrader.gui.GuiManager;
import pluginsfix.gloweffectupgrader.hook.EconomyHook;
import pluginsfix.gloweffectupgrader.hook.PlayerPointsHook;
import pluginsfix.gloweffectupgrader.task.EffectApplicationTask;
import pluginsfix.gloweffectupgrader.text.Messages;

public final class GlowEffectUpgrader extends JavaPlugin {
    private PluginConfig pluginConfig;
    private Messages messages;
    private EconomyHook economyHook;
    private PlayerPointsHook playerPointsHook;
    private EffectService effectService;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        this.pluginConfig = new PluginConfig(this);
        this.messages = new Messages(this);
        this.economyHook = new EconomyHook(this);
        this.playerPointsHook = new PlayerPointsHook();
        this.effectService = new EffectService(this, pluginConfig, messages, economyHook, playerPointsHook);
        this.guiManager = new GuiManager(pluginConfig, messages, effectService);

        getServer().getPluginManager().registerEvents(
                new GuiListener(pluginConfig, messages, effectService, guiManager),
                this
        );

        registerCommands();

        Bukkit.getScheduler().runTaskTimer(
                this,
                new EffectApplicationTask(pluginConfig, effectService),
                20L,
                pluginConfig.updateIntervalTicks()
        );
    }

    private void registerCommands() {
        PluginCommand smithCommand = getCommand("smith");
        if (smithCommand != null) {
            SmithCommand executor = new SmithCommand(pluginConfig, messages, guiManager);
            smithCommand.setExecutor(executor);
            smithCommand.setTabCompleter(executor);
        }

        PluginCommand clearCommand = getCommand("cleareffects");
        if (clearCommand != null) {
            ClearEffectsCommand executor = new ClearEffectsCommand(messages, effectService);
            clearCommand.setExecutor(executor);
            clearCommand.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public Messages messages() {
        return messages;
    }

    public EffectService effectService() {
        return effectService;
    }

    public GuiManager guiManager() {
        return guiManager;
    }
}
