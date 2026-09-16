package pluginsfix.gloweffectupgrader.domain;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import pluginsfix.gloweffectupgrader.config.EffectDefinition;
import pluginsfix.gloweffectupgrader.config.ItemSlotType;
import pluginsfix.gloweffectupgrader.config.PluginConfig;
import pluginsfix.gloweffectupgrader.hook.EconomyHook;
import pluginsfix.gloweffectupgrader.hook.PlayerPointsHook;
import pluginsfix.gloweffectupgrader.text.Messages;

import java.util.ArrayList;
import java.util.List;

public final class EffectService {
    private final Plugin plugin;
    private final PluginConfig config;
    private final Messages messages;
    private final EconomyHook economyHook;
    private final PlayerPointsHook playerPointsHook;
    private final NamespacedKey effectsKey;

    public EffectService(
            Plugin plugin,
            PluginConfig config,
            Messages messages,
            EconomyHook economyHook,
            PlayerPointsHook playerPointsHook
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.economyHook = economyHook;
        this.playerPointsHook = playerPointsHook;
        this.effectsKey = new NamespacedKey(plugin, "applied_effects");
    }

    public List<AppliedEffect> getAppliedEffects(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return List.of();
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String raw = pdc.get(effectsKey, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        String[] entries = raw.split(";;");
        List<AppliedEffect> result = new ArrayList<>();
        for (String entry : entries) {
            AppliedEffect effect = AppliedEffect.deserialize(entry);
            if (effect != null) {
                result.add(effect);
            }
        }
        return result;
    }

    public boolean hasEffect(ItemStack item, String effectId) {
        List<AppliedEffect> effects = getAppliedEffects(item);
        for (AppliedEffect eff : effects) {
            if (eff.effectId().equalsIgnoreCase(effectId)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack applyEffect(ItemStack item, EffectDefinition definition) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            return item;
        }

        List<AppliedEffect> current = new ArrayList<>(getAppliedEffects(clone));
        current.removeIf(e -> e.effectId().equalsIgnoreCase(definition.id()));
        AppliedEffect newEffect = new AppliedEffect(
                definition.id(),
                definition.type().getName(),
                definition.level(),
                definition.displayName()
        );
        current.add(newEffect);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < current.size(); i++) {
            if (i > 0) {
                sb.append(";;");
            }
            sb.append(current.get(i).serialize());
        }
        meta.getPersistentDataContainer().set(effectsKey, PersistentDataType.STRING, sb.toString());
        rebuildLore(meta, current);
        clone.setItemMeta(meta);
        return clone;
    }

    public ItemStack removeAllEffects(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.getPersistentDataContainer().remove(effectsKey);
        rebuildLore(meta, List.of());
        clone.setItemMeta(meta);
        return clone;
    }

    private void rebuildLore(ItemMeta meta, List<AppliedEffect> effects) {
        List<Component> currentLore = meta.lore();
        List<Component> cleanLore = new ArrayList<>();
        if (currentLore != null) {
            Component headerComp = messages.parseItem(config.itemLoreHeader());
            for (Component line : currentLore) {
                if (line.equals(headerComp)) {
                    break;
                }
                cleanLore.add(line);
            }
        }

        if (!effects.isEmpty()) {
            if (!cleanLore.isEmpty()) {
                cleanLore.add(Component.empty());
            }
            cleanLore.add(messages.parseItem(config.itemLoreHeader()));
            for (AppliedEffect eff : effects) {
                Component effectDisplay = messages.parseItem(eff.displayName());
                Component lineComp = messages.parseItem(
                        config.itemLoreLine(),
                        Placeholder.component("effect_display", effectDisplay)
                );
                cleanLore.add(lineComp);
            }
        }
        meta.lore(cleanLore.isEmpty() ? null : cleanLore);
    }

    public UpgradeResult purchaseUpgrade(
            Player player,
            ItemSlotType slotType,
            EffectDefinition definition,
            boolean payWithMoney
    ) {
        ItemStack item = slotType.getItem(player);
        if (item == null || item.getType() == Material.AIR) {
            return new UpgradeResult.InvalidItem();
        }

        if (hasEffect(item, definition.id())) {
            return new UpgradeResult.AlreadyHasEffect();
        }

        if (payWithMoney) {
            if (!economyHook.isAvailable()) {
                return new UpgradeResult.EconomyUnavailable();
            }
            double price = definition.priceMoney();
            double balance = economyHook.getBalance(player);
            if (!economyHook.has(player, price)) {
                return new UpgradeResult.NotEnoughMoney(price, balance);
            }
            if (!economyHook.withdraw(player, price)) {
                return new UpgradeResult.NotEnoughMoney(price, balance);
            }
        } else {
            if (!playerPointsHook.isAvailable()) {
                return new UpgradeResult.PointsUnavailable();
            }
            int price = definition.pricePoints();
            int balance = playerPointsHook.getPoints(player);
            if (!playerPointsHook.has(player, price)) {
                return new UpgradeResult.NotEnoughPoints(price, balance);
            }
            if (!playerPointsHook.withdraw(player, price)) {
                return new UpgradeResult.NotEnoughPoints(price, balance);
            }
        }

        ItemStack upgraded = applyEffect(item, definition);
        slotType.setItem(player, upgraded);

        AppliedEffect applied = new AppliedEffect(
                definition.id(),
                definition.type().getName(),
                definition.level(),
                definition.displayName()
        );
        return new UpgradeResult.Success(applied, payWithMoney, definition.priceMoney(), definition.pricePoints());
    }

    public PluginConfig config() {
        return config;
    }

    public Messages messages() {
        return messages;
    }
}
