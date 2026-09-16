package pluginsfix.gloweffectupgrader.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pluginsfix.gloweffectupgrader.config.EffectDefinition;
import pluginsfix.gloweffectupgrader.config.ItemSlotType;

import java.util.HashMap;
import java.util.Map;

public final class UpgradeSelectHolder implements InventoryHolder {
    private final ItemSlotType slotType;
    private final ItemStack targetItem;
    private final Map<Integer, EffectDefinition> effectSlots = new HashMap<>();
    private Inventory inventory;

    public UpgradeSelectHolder(ItemSlotType slotType, ItemStack targetItem) {
        this.slotType = slotType;
        this.targetItem = targetItem;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void addEffectSlot(int slot, EffectDefinition definition) {
        effectSlots.put(slot, definition);
    }

    public EffectDefinition getEffectAt(int slot) {
        return effectSlots.get(slot);
    }

    public ItemSlotType slotType() {
        return slotType;
    }

    public ItemStack targetItem() {
        return targetItem;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
