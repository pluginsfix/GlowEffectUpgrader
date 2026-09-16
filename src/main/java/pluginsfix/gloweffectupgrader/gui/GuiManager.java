package pluginsfix.gloweffectupgrader.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsfix.gloweffectupgrader.config.EffectDefinition;
import pluginsfix.gloweffectupgrader.config.ItemSlotType;
import pluginsfix.gloweffectupgrader.config.PluginConfig;
import pluginsfix.gloweffectupgrader.domain.EffectService;
import pluginsfix.gloweffectupgrader.text.Messages;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class GuiManager {
    private final PluginConfig config;
    private final Messages messages;
    private final EffectService effectService;
    private final DecimalFormat moneyFormat = new DecimalFormat("#.##");

    public GuiManager(PluginConfig config, Messages messages, EffectService effectService) {
        this.config = config;
        this.messages = messages;
        this.effectService = effectService;
    }

    public void openMainMenu(Player player) {
        MainSelectHolder holder = new MainSelectHolder();
        Component title = messages.miniMessage().deserialize(config.mainMenuTitle());
        Inventory inventory = Bukkit.createInventory(holder, config.mainMenuSize(), title);
        holder.setInventory(inventory);

        ItemStack fillerItem = createFillerItem(config.mainMenuFillerMaterial(), config.mainMenuFillerName());
        for (int slot : config.mainMenuFillerSlots()) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, fillerItem);
            }
        }

        for (ItemSlotType slotType : ItemSlotType.values()) {
            PluginConfig.SlotConfig slotConfig = config.getSlotConfig(slotType);
            if (slotConfig == null) {
                continue;
            }
            ItemStack equippedItem = slotType.getItem(player);
            ItemStack displayItem;
            if (equippedItem == null || equippedItem.getType() == Material.AIR) {
                displayItem = createEmptySlotItem(slotConfig);
            } else {
                displayItem = createEquippedSlotItem(equippedItem, slotConfig);
            }
            inventory.setItem(slotConfig.slot(), displayItem);
        }

        player.openInventory(inventory);
    }

    public void openUpgradeMenu(Player player, ItemSlotType slotType) {
        ItemStack item = slotType.getItem(player);
        if (item == null || item.getType() == Material.AIR) {
            messages.send(player, "slot-empty");
            return;
        }

        List<EffectDefinition> available = config.getAvailableEffects(item.getType());
        if (available.isEmpty()) {
            messages.send(player, "no-upgrades-available");
            return;
        }

        UpgradeSelectHolder holder = new UpgradeSelectHolder(slotType, item);
        Component title = messages.miniMessage().deserialize(config.upgradeMenuTitle());
        Inventory inventory = Bukkit.createInventory(holder, config.upgradeMenuSize(), title);
        holder.setInventory(inventory);

        ItemStack fillerItem = createFillerItem(config.upgradeMenuFillerMaterial(), config.upgradeMenuFillerName());
        for (int slot : config.upgradeMenuFillerSlots()) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, fillerItem);
            }
        }

        if (config.backButtonEnabled()) {
            ItemStack backButton = createBackButton();
            inventory.setItem(config.backButtonSlot(), backButton);
        }

        for (EffectDefinition def : available) {
            ItemStack iconItem = createEffectIconItem(item, def);
            holder.addEffectSlot(def.slot(), def);
            inventory.setItem(def.slot(), iconItem);
        }

        player.openInventory(inventory);
    }

    private ItemStack createFillerItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(messages.miniMessage().deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEmptySlotItem(PluginConfig.SlotConfig slotConfig) {
        ItemStack item = new ItemStack(slotConfig.emptyMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(messages.miniMessage().deserialize(slotConfig.emptyName()));
            List<Component> lore = new ArrayList<>();
            for (String line : slotConfig.emptyLore()) {
                lore.add(messages.miniMessage().deserialize(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEquippedSlotItem(ItemStack original, PluginConfig.SlotConfig slotConfig) {
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : slotConfig.equippedLore()) {
                lore.add(messages.miniMessage().deserialize(line));
            }
            List<Component> existingLore = meta.lore();
            if (existingLore != null && !existingLore.isEmpty()) {
                lore.add(Component.empty());
                lore.addAll(existingLore);
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(config.backButtonMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(messages.miniMessage().deserialize(config.backButtonName()));
            List<Component> lore = new ArrayList<>();
            for (String line : config.backButtonLore()) {
                lore.add(messages.miniMessage().deserialize(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEffectIconItem(ItemStack targetItem, EffectDefinition def) {
        ItemStack item = new ItemStack(def.icon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(messages.miniMessage().deserialize(def.displayName()));
            List<Component> lore = new ArrayList<>();
            boolean alreadyApplied = effectService.hasEffect(targetItem, def.id());

            String formattedPrice = (def.priceMoney() == (long) def.priceMoney())
                    ? String.format("%d", (long) def.priceMoney())
                    : moneyFormat.format(def.priceMoney());

            for (String line : config.effectLoreFormat()) {
                lore.add(messages.miniMessage().deserialize(
                        line,
                        Placeholder.parsed("price", formattedPrice),
                        Placeholder.parsed("points", String.valueOf(def.pricePoints()))
                ));
            }

            if (alreadyApplied) {
                lore.add(Component.empty());
                lore.add(messages.miniMessage().deserialize(config.effectAlreadyAppliedLore()));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
