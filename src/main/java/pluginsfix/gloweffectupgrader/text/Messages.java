package pluginsfix.gloweffectupgrader.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Messages {
    private final Plugin plugin;
    private final File file;
    private final MiniMessage miniMessage;
    private final Map<String, String> cache;
    private String prefix;

    public Messages(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        this.miniMessage = MiniMessage.miniMessage();
        this.cache = new HashMap<>();
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        cache.clear();
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                cache.put(key, config.getString(key));
            }
        }
        this.prefix = cache.getOrDefault("prefix", "");
    }

    public Component getComponent(String key, TagResolver... resolvers) {
        String raw = cache.get(key);
        if (raw == null) {
            return miniMessage.deserialize("<red>Missing message key: " + key);
        }
        return miniMessage.deserialize(raw, resolvers);
    }

    public Component getPrefixedComponent(String key, TagResolver... resolvers) {
        String raw = cache.get(key);
        if (raw == null) {
            return miniMessage.deserialize(prefix + "<red>Missing message key: " + key);
        }
        return miniMessage.deserialize(prefix + raw, resolvers);
    }

    public String getRaw(String key) {
        return cache.getOrDefault(key, "");
    }

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        sender.sendMessage(getPrefixedComponent(key, resolvers));
    }

    public Component parseItem(String text, TagResolver... resolvers) {
        return miniMessage.deserialize("<!italic>" + text, resolvers).decoration(TextDecoration.ITALIC, false);
    }

    public MiniMessage miniMessage() {
        return miniMessage;
    }
}
