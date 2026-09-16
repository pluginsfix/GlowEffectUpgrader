package pluginsfix.gloweffectupgrader.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pluginsfix.gloweffectupgrader.config.PluginConfig;
import pluginsfix.gloweffectupgrader.gui.GuiManager;
import pluginsfix.gloweffectupgrader.text.Messages;

import java.util.Collections;
import java.util.List;

public final class SmithCommand implements CommandExecutor, TabCompleter {
    private final PluginConfig config;
    private final Messages messages;
    private final GuiManager guiManager;

    public SmithCommand(PluginConfig config, Messages messages, GuiManager guiManager) {
        this.config = config;
        this.messages = messages;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            if (!sender.hasPermission("eup.use")) {
                messages.send(sender, "no-permission");
                return true;
            }
            if (!(sender instanceof Player player)) {
                messages.send(sender, "player-only");
                return true;
            }
            guiManager.openMainMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("eup.admin")) {
                messages.send(sender, "no-permission");
                return true;
            }
            config.reload();
            messages.reload();
            messages.send(sender, "reloaded");
            return true;
        }

        messages.send(sender, "command-usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1 && sender.hasPermission("eup.admin")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
        }
        return List.of();
    }
}
