package me.blueslime.pixelmotd.listener.bungeecord.events.abstracts;

import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.listener.ConnectionListener;
import me.blueslime.pixelmotd.utils.ListType;
import me.blueslime.pixelmotd.utils.ListUtil;
import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.SocketAddress;
import java.util.UUID;

public class AbstractLoginListener extends ConnectionListener<Plugin, LoginEvent, TextComponent> implements Listener {

    public AbstractLoginListener(PixelMOTD<Plugin> plugin) {
        super(plugin);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void execute(LoginEvent event) {
        if (event == null || event.isCancelled()) {
            return;
        }

        final PendingConnection connection = event.getConnection();

        if (connection == null || connection.getUniqueId() == null) {
            getLogs().info("PixelMOTD will not frame the connection, because the connection is null or the UUID is null.");
            event.setCancelled(true);
            event.setCancelReason(
                    colorize("&cBlocking you from joining to the server, reason: &6Unframed connection&c, if this is a error, contact to the server staff")
            );
            return;
        }

        final SocketAddress address = connection.getSocketAddress();

        final String username = connection.getName();

        final UUID uuid = connection.getUniqueId();

        getPlayerDatabase().fromSocket(
                address.toString(),
                username
        );

        ConfigurationHandler whitelist = getWhitelist();
        ConfigurationHandler blacklist = getBlacklist();

        if (hasWhitelist()) {
            if (!checkPlayer(ListType.WHITELIST, "global", username) && !checkUUID(ListType.WHITELIST, "global", uuid)) {
                String reason = ListUtil.ListToString(whitelist.getStringList("kick-message.global"));

                event.setCancelReason(
                        colorize(
                                replace(
                                        reason,
                                        true,
                                        "global",
                                        username,
                                        uuid.toString()
                                )
                        )
                );

                event.setCancelled(true);
                return;
            }
        }

        if (hasBlacklist()) {
            if (checkPlayer(ListType.BLACKLIST, "global", username) || checkUUID(ListType.BLACKLIST, "global", uuid)) {
                String reason = ListUtil.ListToString(blacklist.getStringList("kick-message.global"));

                event.setCancelReason(
                        colorize(
                                replace(
                                        reason,
                                        false,
                                        "global",
                                        username,
                                        uuid.toString()
                                )
                        )
                );

                event.setCancelled(true);
            }
        }



    }

    @Override
    public TextComponent colorize(String message) {
        return new TextComponent(
                ChatColor.translateAlternateColorCodes(
                        '&',
                        message
                )
        );
    }
}
