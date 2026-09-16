package pluginsfix.gloweffectupgrader.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

public final class EconomyHook implements Listener {
    private final Plugin plugin;
    private Economy economy;

    public EconomyHook(Plugin plugin) {
        this.plugin = plugin;
        tryHook();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void tryHook() {
        if (this.economy != null) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
        }
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService().equals(Economy.class)) {
            tryHook();
        }
    }

    public Optional<Economy> getEconomy() {
        if (this.economy == null) {
            tryHook();
        }
        return Optional.ofNullable(this.economy);
    }

    public boolean isAvailable() {
        return getEconomy().isPresent();
    }

    public double getBalance(Player player) {
        return getEconomy().map(eco -> eco.getBalance(player)).orElse(0.0);
    }

    public boolean has(Player player, double amount) {
        return getEconomy().map(eco -> eco.has(player, amount)).orElse(false);
    }

    public boolean withdraw(Player player, double amount) {
        return getEconomy()
                .map(eco -> eco.withdrawPlayer(player, amount).transactionSuccess())
                .orElse(false);
    }
}
