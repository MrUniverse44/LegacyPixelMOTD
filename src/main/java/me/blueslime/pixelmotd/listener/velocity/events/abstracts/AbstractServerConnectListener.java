package me.blueslime.pixelmotd.listener.velocity.events.abstracts;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.listener.ConnectionListener;
import me.blueslime.pixelmotd.utils.ListType;
import me.blueslime.pixelmotd.utils.ListUtil;
import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;
import java.util.UUID;

public class AbstractServerConnectListener extends ConnectionListener<ProxyServer, ServerPreConnectEvent, Component> {

    public AbstractServerConnectListener(PixelMOTD<ProxyServer> plugin) {
        super(plugin);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    @Subscribe
    public void execute(ServerPreConnectEvent event) {
        if (event == null) {
            return;
        }

        final Player connection = event.getPlayer();

        final ServerInfo previous = event.getOriginalServer().getServerInfo();
        final Optional<RegisteredServer> serverOptional   = event.getResult().getServer();

        final ServerInfo target;

        if (!serverOptional.isPresent()) {
            return;
        }

        target = serverOptional.get().getServerInfo();

        if (previous.getName().equalsIgnoreCase(target.getName())) {
            return;
        }

        if (connection == null) {
            return;
        }

        if (connection.getUniqueId() == null) {

            getLogs().info("The plugin will not apply the whitelist or blacklist check for a null-ip and will block player from joining to the server");
            event.setResult(
                    deny()
            );
            connection.sendMessage(
                    colorize("&cBlocking you from joining to the server, reason: &6Null IP&c, if this is a error, contact to the server staff")
            );
            return;

        }

        final String username = connection.getUsername();

        final UUID uuid = connection.getUniqueId();

        final String serverName = target.getName();

        ConfigurationHandler settings = getControl();

        String path = getPlace().toStringLowerCase() + "." + serverName;

        if (settings.getStatus("whitelist." + path + ".enabled", false)) {
            if (!checkPlayer(ListType.WHITELIST, path, username) && !checkUUID(ListType.WHITELIST, path, uuid)) {
                String reason = ListUtil.ListToString(settings.getStringList("kick-message.whitelist"));

                connection.sendMessage(
                        colorize(
                                replace(
                                        reason,
                                        "whitelist." + path,
                                        username,
                                        uuid.toString()
                                )
                        )
                );

                event.setResult(
                        deny()
                );
                return;
            }
        }

        if (settings.getStatus("blacklist." + path + ".enabled", false)) {
            if (checkPlayer(ListType.BLACKLIST, path, username) || checkUUID(ListType.BLACKLIST, path, uuid)) {
                String reason = ListUtil.ListToString(settings.getStringList("kick-message.blacklist"));

                connection.sendMessage(
                        colorize(
                                replace(
                                        reason,
                                        "blacklist." + path,
                                        username,
                                        uuid.toString()
                                )
                        )
                );

                event.setResult(
                        deny()
                );
            }
        }
    }

    private ServerPreConnectEvent.ServerResult deny() {
        return ServerPreConnectEvent.ServerResult.denied();
    }

    @Override
    public Component colorize(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }
}