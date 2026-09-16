package pluginsfix.gloweffectupgrader.gui;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pluginsfix.gloweffectupgrader.config.EffectDefinition;
import pluginsfix.gloweffectupgrader.config.ItemSlotType;
import pluginsfix.gloweffectupgrader.config.PluginConfig;
import pluginsfix.gloweffectupgrader.domain.EffectService;
import pluginsfix.gloweffectupgrader.domain.UpgradeResult;
import pluginsfix.gloweffectupgrader.text.Messages;

import java.text.DecimalFormat;

public final class GuiListener implements Listener {
    private final PluginConfig config;
    private final Messages messages;
    private final EffectService effectService;
    private final GuiManager guiManager;
    private final DecimalFormat moneyFormat = new DecimalFormat("#.##");

    public GuiListener(
            PluginConfig config,
            Messages messages,
            EffectService effectService,
            GuiManager guiManager
    ) {
        this.config = config;
        this.messages = messages;
        this.effectService = effectService;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        if (!(holder instanceof MainSelectHolder || holder instanceof UpgradeSelectHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inventory)) {
            return;
        }

        int slot = event.getSlot();

        if (holder instanceof MainSelectHolder) {
            handleMainMenuClick(player, slot);
            return;
        }

        if (holder instanceof UpgradeSelectHolder upgradeHolder) {
            handleUpgradeMenuClick(player, upgradeHolder, slot, event.isLeftClick(), event.isRightClick());
        }
    }

    private void handleMainMenuClick(Player player, int slot) {
        for (ItemSlotType slotType : ItemSlotType.values()) {
            PluginConfig.SlotConfig slotConfig = config.getSlotConfig(slotType);
            if (slotConfig != null && slotConfig.slot() == slot) {
                guiManager.openUpgradeMenu(player, slotType);
                return;
            }
        }
    }

    private void handleUpgradeMenuClick(
            Player player,
            UpgradeSelectHolder holder,
            int slot,
            boolean isLeftClick,
            boolean isRightClick
    ) {
        if (config.backButtonEnabled() && slot == config.backButtonSlot()) {
            guiManager.openMainMenu(player);
            return;
        }

        EffectDefinition definition = holder.getEffectAt(slot);
        if (definition == null) {
            return;
        }

        if (!isLeftClick && !isRightClick) {
            return;
        }

        UpgradeResult result = effectService.purchaseUpgrade(player, holder.slotType(), definition, isLeftClick);

        switch (result) {
            case UpgradeResult.Success success -> {
                if (success.byMoney()) {
                    String formattedPrice = (success.priceMoney() == (long) success.priceMoney())
                            ? String.format("%d", (long) success.priceMoney())
                            : moneyFormat.format(success.priceMoney());
                    messages.send(
                            player,
                            "upgrade-success-money",
                            Placeholder.component("effect", messages.miniMessage().deserialize(success.effect().displayName())),
                            Placeholder.parsed("price", formattedPrice)
                    );
                } else {
                    messages.send(
                            player,
                            "upgrade-success-points",
                            Placeholder.component("effect", messages.miniMessage().deserialize(success.effect().displayName())),
                            Placeholder.parsed("points", String.valueOf(success.pricePoints()))
                    );
                }
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                guiManager.openUpgradeMenu(player, holder.slotType());
            }
            case UpgradeResult.AlreadyHasEffect ignored -> {
                messages.send(player, "already-has-effect");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            case UpgradeResult.NotEnoughMoney notEnoughMoney -> {
                String formattedPrice = moneyFormat.format(notEnoughMoney.required());
                String formattedBalance = moneyFormat.format(notEnoughMoney.current());
                messages.send(
                        player,
                        "not-enough-money",
                        Placeholder.parsed("price", formattedPrice),
                        Placeholder.parsed("balance", formattedBalance)
                );
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            case UpgradeResult.NotEnoughPoints notEnoughPoints -> {
                messages.send(
                        player,
                        "not-enough-points",
                        Placeholder.parsed("points", String.valueOf(notEnoughPoints.required())),
                        Placeholder.parsed("balance", String.valueOf(notEnoughPoints.current()))
                );
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            case UpgradeResult.EconomyUnavailable ignored -> messages.send(player, "economy-disabled");
            case UpgradeResult.PointsUnavailable ignored -> messages.send(player, "points-disabled");
            case UpgradeResult.InvalidItem ignored -> {
                messages.send(player, "slot-empty");
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof MainSelectHolder || holder instanceof UpgradeSelectHolder) {
            event.setCancelled(true);
        }
    }
}
