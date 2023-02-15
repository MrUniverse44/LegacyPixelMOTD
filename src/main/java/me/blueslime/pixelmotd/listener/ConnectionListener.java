package me.blueslime.pixelmotd.listener;

import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.Configuration;
import me.blueslime.pixelmotd.players.PlayerDatabase;
import me.blueslime.pixelmotd.utils.Extras;
import me.blueslime.pixelmotd.utils.ListType;
import me.blueslime.pixelmotd.utils.WhitelistLocation;
import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import dev.mruniverse.slimelib.logs.SlimeLogs;

import java.util.UUID;

public abstract class ConnectionListener<T, E, S> {

    private final PixelMOTD<T> plugin;

    private boolean isWhitelisted;

    private boolean isBlacklisted;

    public ConnectionListener(PixelMOTD<T> plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        isWhitelisted = plugin.getConfigurationHandler(Configuration.MODES).getStatus("whitelist.global.enabled", false);
        isBlacklisted = plugin.getConfigurationHandler(Configuration.MODES).getStatus("blacklist.global.enabled", false);
    }

    public void update() {
        load();
    }

    public WhitelistLocation getPlace() {
        return WhitelistLocation.fromPlatform(plugin.getServerType());
    }

    public abstract void execute(E event);

    public abstract S colorize(String message);

    public String replace(String message, String key, String username, String uniqueId) {
        ConfigurationHandler settings = getControl();

        return getExtras().replace(
                message.replace("%username%", username)
                    .replace("%nick%", username)
                    .replace("%uniqueId%", uniqueId)
                    .replace("%uuid%", uniqueId)
                    .replace("%reason%", settings.getString(key + ".reason", ""))
                    .replace("%author%", settings.getString(key + ".author", "")),
                plugin.getPlayerHandler().getPlayersSize(),
                plugin.getPlayerHandler().getMaxPlayers(),
                username
        );
    }

    public boolean hasWhitelist() {
        return isWhitelisted;
    }

    public boolean hasBlacklist() {
        return isBlacklisted;
    }

    public ConfigurationHandler getControl() {
        return plugin.getConfigurationHandler(Configuration.MODES);
    }

    public ConfigurationHandler getSettings() {
        return plugin.getConfigurationHandler(Configuration.SETTINGS);
    }

    public boolean checkPlayer(ListType listType, String path, String username) {
        return getControl().getStringList(listType.toString() + "." + path + ".players.by-name").contains(username);
    }

    public boolean checkUUID(ListType listType, String path, UUID uniqueId) {
        return getControl().getStringList(listType.toString() + "." + path + ".players.by-uuid").contains(uniqueId.toString());
    }

    public Extras getExtras() {
        return plugin.getListenerManager().getExtras();
    }

    public SlimeLogs getLogs() {
        return plugin.getLogs();
    }

    public PlayerDatabase getPlayerDatabase() {
        return plugin.getListenerManager().getDatabase();
    }

}
