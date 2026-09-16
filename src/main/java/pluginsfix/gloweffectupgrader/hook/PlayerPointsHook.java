package pluginsfix.gloweffectupgrader.hook;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerPointsHook {

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("PlayerPoints");
    }

    private PlayerPointsAPI getApi() {
        if (!isAvailable()) {
            return null;
        }
        return PlayerPoints.getInstance().getAPI();
    }

    public int getPoints(Player player) {
        PlayerPointsAPI api = getApi();
        if (api == null) {
            return 0;
        }
        return api.look(player.getUniqueId());
    }

    public boolean has(Player player, int amount) {
        return getPoints(player) >= amount;
    }

    public boolean withdraw(Player player, int amount) {
        PlayerPointsAPI api = getApi();
        if (api == null) {
            return false;
        }
        return api.take(player.getUniqueId(), amount);
    }
}
