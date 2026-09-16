package pluginsfix.gloweffectupgrader.config;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public final class PluginConfig {
    private final Plugin plugin;
    private final File file;

    private int updateIntervalTicks;
    private int effectDurationSeconds;
    private boolean hideParticles;
    private boolean ambient;

    private String mainMenuTitle;
    private int mainMenuSize;
    private Material mainMenuFillerMaterial;
    private String mainMenuFillerName;
    private List<Integer> mainMenuFillerSlots;
    private final Map<ItemSlotType, SlotConfig> slotConfigs = new EnumMap<>(ItemSlotType.class);

    private String upgradeMenuTitle;
    private int upgradeMenuSize;
    private Material upgradeMenuFillerMaterial;
    private String upgradeMenuFillerName;
    private List<Integer> upgradeMenuFillerSlots;
    private boolean backButtonEnabled;
    private int backButtonSlot;
    private Material backButtonMaterial;
    private String backButtonName;
    private List<String> backButtonLore;
    private List<String> effectLoreFormat;
    private String effectAlreadyAppliedLore;

    private String itemLoreHeader;
    private String itemLoreLine;

    private final Map<String, UpgradeCategory> categories = new LinkedHashMap<>();

    public record SlotConfig(
            int slot,
            Material emptyMaterial,
            String emptyName,
            List<String> emptyLore,
            List<String> equippedLore
    ) {}

    public PluginConfig(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        this.updateIntervalTicks = config.getInt("settings.update-interval-ticks", 20);
        this.effectDurationSeconds = config.getInt("settings.effect-duration-seconds", 15);
        this.hideParticles = config.getBoolean("settings.hide-particles", true);
        this.ambient = config.getBoolean("settings.ambient", true);

        this.mainMenuTitle = config.getString("gui.main-menu.title", "<dark_gray>Выберите предмет");
        this.mainMenuSize = config.getInt("gui.main-menu.size", 54);
        this.mainMenuFillerMaterial = Material.matchMaterial(config.getString("gui.main-menu.filler.material", "GRAY_STAINED_GLASS_PANE"));
        if (this.mainMenuFillerMaterial == null) {
            this.mainMenuFillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
        }
        this.mainMenuFillerName = config.getString("gui.main-menu.filler.name", " ");
        this.mainMenuFillerSlots = config.getIntegerList("gui.main-menu.filler.slots");

        loadSlotConfig(config, "gui.main-menu.slots.helmet", ItemSlotType.HELMET, 10);
        loadSlotConfig(config, "gui.main-menu.slots.chestplate", ItemSlotType.CHESTPLATE, 12);
        loadSlotConfig(config, "gui.main-menu.slots.leggings", ItemSlotType.LEGGINGS, 14);
        loadSlotConfig(config, "gui.main-menu.slots.boots", ItemSlotType.BOOTS, 16);
        loadSlotConfig(config, "gui.main-menu.slots.main-hand", ItemSlotType.MAIN_HAND, 22);

        this.upgradeMenuTitle = config.getString("gui.upgrade-menu.title", "<dark_gray>Выберите улучшение");
        this.upgradeMenuSize = config.getInt("gui.upgrade-menu.size", 54);
        this.upgradeMenuFillerMaterial = Material.matchMaterial(config.getString("gui.upgrade-menu.filler.material", "GRAY_STAINED_GLASS_PANE"));
        if (this.upgradeMenuFillerMaterial == null) {
            this.upgradeMenuFillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
        }
        this.upgradeMenuFillerName = config.getString("gui.upgrade-menu.filler.name", " ");
        this.upgradeMenuFillerSlots = config.getIntegerList("gui.upgrade-menu.filler.slots");

        this.backButtonEnabled = config.getBoolean("gui.upgrade-menu.back-button.enabled", true);
        this.backButtonSlot = config.getInt("gui.upgrade-menu.back-button.slot", 49);
        this.backButtonMaterial = Material.matchMaterial(config.getString("gui.upgrade-menu.back-button.material", "ARROW"));
        if (this.backButtonMaterial == null) {
            this.backButtonMaterial = Material.ARROW;
        }
        this.backButtonName = config.getString("gui.upgrade-menu.back-button.name", "<yellow>Назад");
        this.backButtonLore = config.getStringList("gui.upgrade-menu.back-button.lore");

        this.effectLoreFormat = config.getStringList("gui.upgrade-menu.effect-lore-format");
        this.effectAlreadyAppliedLore = config.getString("gui.upgrade-menu.effect-already-applied-lore", "<red>Данное улучшение уже установлено!");

        this.itemLoreHeader = config.getString("item-lore-format.header", "<dark_purple>★ <light_purple>Улучшения:");
        this.itemLoreLine = config.getString("item-lore-format.line", " <gray>▪ <effect_display>");

        this.categories.clear();
        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String categoryKey : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySec = categoriesSection.getConfigurationSection(categoryKey);
                if (categorySec == null) {
                    continue;
                }
                List<String> matchPatterns = categorySec.getStringList("match");
                Map<String, EffectDefinition> effects = new LinkedHashMap<>();

                ConfigurationSection effectsSection = categorySec.getConfigurationSection("effects");
                if (effectsSection != null) {
                    for (String effectKey : effectsSection.getKeys(false)) {
                        ConfigurationSection effSec = effectsSection.getConfigurationSection(effectKey);
                        if (effSec == null) {
                            continue;
                        }
                        String typeName = effSec.getString("type", "");
                        PotionEffectType effectType = resolvePotionEffectType(typeName);
                        if (effectType == null) {
                            continue;
                        }
                        int level = effSec.getInt("level", 1);
                        int slot = effSec.getInt("slot", 10);
                        Material icon = Material.matchMaterial(effSec.getString("icon", "NETHER_STAR"));
                        if (icon == null) {
                            icon = Material.NETHER_STAR;
                        }
                        String displayName = effSec.getString("display-name", "<yellow>" + effectKey);
                        double priceMoney = effSec.getDouble("price-money", 0.0);
                        int pricePoints = effSec.getInt("price-points", 0);

                        effects.put(effectKey, new EffectDefinition(
                                effectKey,
                                effectType,
                                level,
                                slot,
                                icon,
                                displayName,
                                priceMoney,
                                pricePoints
                        ));
                    }
                }
                this.categories.put(categoryKey, new UpgradeCategory(categoryKey, matchPatterns, effects));
            }
        }
    }

    private void loadSlotConfig(FileConfiguration config, String path, ItemSlotType slotType, int defaultSlot) {
        int slot = config.getInt(path + ".slot", defaultSlot);
        Material emptyMaterial = Material.matchMaterial(config.getString(path + ".empty-material", "BARRIER"));
        if (emptyMaterial == null) {
            emptyMaterial = Material.BARRIER;
        }
        String emptyName = config.getString(path + ".empty-name", "<red>Предмет отсутствует");
        List<String> emptyLore = config.getStringList(path + ".empty-lore");
        List<String> equippedLore = config.getStringList(path + ".equipped-lore");

        slotConfigs.put(slotType, new SlotConfig(slot, emptyMaterial, emptyName, emptyLore, equippedLore));
    }

    @SuppressWarnings("deprecation")
    private PotionEffectType resolvePotionEffectType(String typeName) {
        if (typeName.isEmpty()) {
            return null;
        }
        PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(typeName.toLowerCase(Locale.ROOT)));
        if (type != null) {
            return type;
        }
        return PotionEffectType.getByName(typeName.toUpperCase(Locale.ROOT));
    }

    public List<EffectDefinition> getAvailableEffects(Material material) {
        List<EffectDefinition> list = new ArrayList<>();
        for (UpgradeCategory category : categories.values()) {
            if (category.matches(material)) {
                list.addAll(category.effects().values());
            }
        }
        return list;
    }

    public int updateIntervalTicks() {
        return updateIntervalTicks;
    }

    public int effectDurationSeconds() {
        return effectDurationSeconds;
    }

    public boolean hideParticles() {
        return hideParticles;
    }

    public boolean ambient() {
        return ambient;
    }

    public String mainMenuTitle() {
        return mainMenuTitle;
    }

    public int mainMenuSize() {
        return mainMenuSize;
    }

    public Material mainMenuFillerMaterial() {
        return mainMenuFillerMaterial;
    }

    public String mainMenuFillerName() {
        return mainMenuFillerName;
    }

    public List<Integer> mainMenuFillerSlots() {
        return mainMenuFillerSlots;
    }

    public SlotConfig getSlotConfig(ItemSlotType type) {
        return slotConfigs.get(type);
    }

    public String upgradeMenuTitle() {
        return upgradeMenuTitle;
    }

    public int upgradeMenuSize() {
        return upgradeMenuSize;
    }

    public Material upgradeMenuFillerMaterial() {
        return upgradeMenuFillerMaterial;
    }

    public String upgradeMenuFillerName() {
        return upgradeMenuFillerName;
    }

    public List<Integer> upgradeMenuFillerSlots() {
        return upgradeMenuFillerSlots;
    }

    public boolean backButtonEnabled() {
        return backButtonEnabled;
    }

    public int backButtonSlot() {
        return backButtonSlot;
    }

    public Material backButtonMaterial() {
        return backButtonMaterial;
    }

    public String backButtonName() {
        return backButtonName;
    }

    public List<String> backButtonLore() {
        return backButtonLore;
    }

    public List<String> effectLoreFormat() {
        return effectLoreFormat;
    }

    public String effectAlreadyAppliedLore() {
        return effectAlreadyAppliedLore;
    }

    public String itemLoreHeader() {
        return itemLoreHeader;
    }

    public String itemLoreLine() {
        return itemLoreLine;
    }

    public Map<String, UpgradeCategory> categories() {
        return categories;
    }
}
