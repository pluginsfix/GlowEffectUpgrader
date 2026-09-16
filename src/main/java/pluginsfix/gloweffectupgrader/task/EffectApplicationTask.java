package pluginsfix.gloweffectupgrader.task;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pluginsfix.gloweffectupgrader.config.PluginConfig;
import pluginsfix.gloweffectupgrader.domain.AppliedEffect;
import pluginsfix.gloweffectupgrader.domain.EffectService;

import java.util.*;

public final class EffectApplicationTask implements Runnable {
    private final PluginConfig config;
    private final EffectService effectService;

    public EffectApplicationTask(PluginConfig config, EffectService effectService) {
        this.config = config;
        this.effectService = effectService;
    }

    @Override
    public void run() {
        int durationTicks = config.effectDurationSeconds() * 20;
        boolean ambient = config.ambient();
        boolean particles = !config.hideParticles();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isValid() || player.isDead()) {
                continue;
            }

            Map<PotionEffectType, Integer> maxLevels = new HashMap<>();

            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack item : armor) {
                collectEffects(item, maxLevels);
            }

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            collectEffects(mainHand, maxLevels);

            for (Map.Entry<PotionEffectType, Integer> entry : maxLevels.entrySet()) {
                PotionEffectType type = entry.getKey();
                int amplifier = Math.max(0, entry.getValue() - 1);
                player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, ambient, particles, true));
            }
        }
    }

    private void collectEffects(ItemStack item, Map<PotionEffectType, Integer> maxLevels) {
        if (item == null) {
            return;
        }
        List<AppliedEffect> effects = effectService.getAppliedEffects(item);
        for (AppliedEffect eff : effects) {
            PotionEffectType type = resolveType(eff.typeName());
            if (type != null) {
                maxLevels.merge(type, eff.level(), Math::max);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private PotionEffectType resolveType(String name) {
        PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
        if (type != null) {
            return type;
        }
        return PotionEffectType.getByName(name.toUpperCase(Locale.ROOT));
    }
}
