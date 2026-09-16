package pluginsfix.gloweffectupgrader.command;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pluginsfix.gloweffectupgrader.domain.AppliedEffect;
import pluginsfix.gloweffectupgrader.domain.EffectService;
import pluginsfix.gloweffectupgrader.text.Messages;

import java.util.Collections;
import java.util.List;

public final class ClearEffectsCommand implements CommandExecutor, TabCompleter {
    private final Messages messages;
    private final EffectService effectService;

    public ClearEffectsCommand(Messages messages, EffectService effectService) {
        this.messages = messages;
        this.effectService = effectService;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission("eup.clear")) {
            messages.send(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            messages.send(player, "air-item");
            return true;
        }

        List<AppliedEffect> appliedEffects = effectService.getAppliedEffects(item);
        if (appliedEffects.isEmpty()) {
            messages.send(player, "no-effects-to-clear");
            return true;
        }

        ItemStack cleared = effectService.removeAllEffects(item);
        player.getInventory().setItemInMainHand(cleared);
        messages.send(player, "item-cleared");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        return List.of();
    }
}
