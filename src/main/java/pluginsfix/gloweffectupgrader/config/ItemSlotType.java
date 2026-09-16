package pluginsfix.gloweffectupgrader.config;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum ItemSlotType {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    MAIN_HAND;

    @Nullable
    public ItemStack getItem(Player player) {
        return switch (this) {
            case HELMET -> player.getInventory().getHelmet();
            case CHESTPLATE -> player.getInventory().getChestplate();
            case LEGGINGS -> player.getInventory().getLeggings();
            case BOOTS -> player.getInventory().getBoots();
            case MAIN_HAND -> player.getInventory().getItemInMainHand();
        };
    }

    public void setItem(Player player, @Nullable ItemStack item) {
        switch (this) {
            case HELMET -> player.getInventory().setHelmet(item);
            case CHESTPLATE -> player.getInventory().setChestplate(item);
            case LEGGINGS -> player.getInventory().setLeggings(item);
            case BOOTS -> player.getInventory().setBoots(item);
            case MAIN_HAND -> player.getInventory().setItemInMainHand(item);
        }
    }
}
