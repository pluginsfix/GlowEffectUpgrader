package pluginsfix.gloweffectupgrader.domain;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import pluginsfix.gloweffectupgrader.config.UpgradeCategory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeCategoryTest {

    @Test
    void testPatternMatching() {
        UpgradeCategory category = new UpgradeCategory(
                "helmet",
                List.of("*_HELMET", "TURTLE_HELMET"),
                Map.of()
        );

        assertThat(category.matches(Material.DIAMOND_HELMET)).isTrue();
        assertThat(category.matches(Material.NETHERITE_HELMET)).isTrue();
        assertThat(category.matches(Material.TURTLE_HELMET)).isTrue();
        assertThat(category.matches(Material.DIAMOND_CHESTPLATE)).isFalse();
    }
}
