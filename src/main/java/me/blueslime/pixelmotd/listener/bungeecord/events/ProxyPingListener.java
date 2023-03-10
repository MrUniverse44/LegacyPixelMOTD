package me.blueslime.pixelmotd.listener.bungeecord.events;

import me.blueslime.pixelmotd.motd.MotdType;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.Configuration;
import me.blueslime.pixelmotd.listener.Ping;
import me.blueslime.pixelmotd.motd.builder.PingBuilder;
import me.blueslime.pixelmotd.motd.builder.favicon.platforms.BungeeFavicon;
import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import me.blueslime.pixelmotd.motd.builder.hover.platforms.BungeeHover;
import me.blueslime.pixelmotd.motd.platforms.BungeePing;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.net.SocketAddress;

public class ProxyPingListener implements Listener, Ping {
    private final PixelMOTD<Plugin> plugin;

    private final BungeePing builder;

    private boolean hasOutdatedClient;

    private boolean hasOutdatedServer;

    private boolean isWhitelisted;

    private boolean isBlacklisted;

    private int MIN_PROTOCOL;

    private int MAX_PROTOCOL;

    private String unknown;

    public ProxyPingListener(PixelMOTD<Plugin> plugin) {
        this.plugin = plugin;

        this.builder = new BungeePing(
                plugin,
                new BungeeFavicon(
                        plugin
                ),
                new BungeeHover(
                        plugin
                )
        );
        load();
    }

    public void update() {
        load();
        builder.update();
    }

    private void load() {

        final ConfigurationHandler control = plugin.getSettings();

        if (control != null) {
            this.unknown = control.getString("settings.unknown-player", "unknown#1");
        } else {
            this.unknown = "unknown#1";
        }

        ConfigurationHandler whitelist = plugin.getConfiguration(Configuration.WHITELIST);
        ConfigurationHandler blacklist = plugin.getConfiguration(Configuration.BLACKLIST);


        this.isWhitelisted = whitelist.getStatus("enabled") &&
                whitelist.getStatus("motd");

        this.isBlacklisted = blacklist.getStatus("enabled") &&
                blacklist.getStatus("motd");

        if (control != null) {
            hasOutdatedClient = control.getStatus("settings.outdated-client-motd", true);
            hasOutdatedServer = control.getStatus("settings.outdated-server-motd", true);

            MAX_PROTOCOL = control.getInt("settings.max-server-protocol", 756);
            MIN_PROTOCOL = control.getInt("settings.min-server-protocol", 47);
        } else {
            hasOutdatedClient = true;
            hasOutdatedServer = true;

            MAX_PROTOCOL = 758;
            MIN_PROTOCOL = 47;
        }
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        final ServerPing ping = event.getResponse();

        if (ping == null || event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
            return;
        }

        final PendingConnection connection = event.getConnection();

        final SocketAddress address = connection.getSocketAddress();

        final int protocol = connection.getVersion();

        final String userName = getPlayerDatabase().getPlayer(
                address.toString(), unknown
        );

        if (isBlacklisted && plugin.getConfiguration(Configuration.BLACKLIST).getStringList("players.by-name").contains(userName)) {
            builder.execute(MotdType.BLACKLIST, ping, protocol, userName);
            return;
        }

        if (isWhitelisted) {
            builder.execute(MotdType.WHITELIST, ping, protocol, userName);
            return;
        }

        if (!hasOutdatedClient && !hasOutdatedServer || protocol >= MIN_PROTOCOL && protocol <= MAX_PROTOCOL) {
            builder.execute(MotdType.NORMAL, ping, protocol, userName);
            return;
        }
        if (MAX_PROTOCOL < protocol && hasOutdatedServer) {
            builder.execute(MotdType.OUTDATED_SERVER, ping, protocol, userName);
            return;
        }
        if (MIN_PROTOCOL > protocol && hasOutdatedClient) {
            builder.execute(MotdType.OUTDATED_CLIENT, ping, protocol, userName);
        }

    }

    public PingBuilder<?, ?, ?, ?> getPingBuilder() {
        return builder;
    }

}
