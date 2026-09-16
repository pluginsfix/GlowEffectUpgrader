package pluginsfix.gloweffectupgrader.config;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public record EffectDefinition(
        String id,
        PotionEffectType type,
        int level,
        int slot,
        Material icon,
        String displayName,
        double priceMoney,
        int pricePoints
) {}
