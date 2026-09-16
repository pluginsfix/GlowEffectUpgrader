package pluginsfix.gloweffectupgrader.config;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public record UpgradeCategory(
        String id,
        List<String> matchPatterns,
        Map<String, EffectDefinition> effects
) {
    public boolean matches(Material material) {
        String name = material.name();
        for (String pattern : matchPatterns) {
            if (pattern.startsWith("*") && name.endsWith(pattern.substring(1))) {
                return true;
            }
            if (pattern.endsWith("*") && name.startsWith(pattern.substring(0, pattern.length() - 1))) {
                return true;
            }
            if (name.equalsIgnoreCase(pattern)) {
                return true;
            }
        }
        return false;
    }
}
