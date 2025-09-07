package me.blueslime.pixelmotd.listener.sponge.server;

import me.blueslime.pixelmotd.listener.sponge.SpongeListener;
import me.blueslime.pixelmotd.motd.setup.MotdSetup;
import me.blueslime.slimelib.file.configuration.ConfigurationHandler;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.utils.ping.Ping;
import me.blueslime.pixelmotd.listener.type.SpongePluginListener;
import me.blueslime.pixelmotd.motd.builder.PingBuilder;
import me.blueslime.pixelmotd.motd.builder.favicon.platforms.SpongeFavicon;
import me.blueslime.pixelmotd.motd.builder.hover.platforms.SpongeHover;
import me.blueslime.pixelmotd.motd.platforms.SpongePing;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.StatusClient;
import org.spongepowered.api.util.Tristate;

import java.net.SocketAddress;

public class ServerPingListener extends SpongePluginListener implements Ping {

    private final SpongePing builder;

    private String unknown;

    public ServerPingListener(PixelMOTD<Server> plugin) {
        super(plugin, SpongeListener.SERVER_PING);
        register();

        builder = new SpongePing(
                getBasePlugin(),
                new SpongeFavicon(
                        getBasePlugin()
                ),
                new SpongeHover(
                        getBasePlugin()
                )
        );

        reload();
    }

    @Override
    public void reload() {
        ConfigurationHandler settings = getSettings();

        if (settings != null) {
            this.unknown = settings.getString("settings.unknown-player", "unknown#1");
        } else {
            this.unknown = "unknown#1";
        }
    }

    @IsCancelled(value = Tristate.UNDEFINED)
    @Listener
    public void onClientPingServer(ClientPingServerEvent event) {
        if (event == null || event.isCancelled()) {
            return;
        }

        final StatusClient connection = event.client();

        final SocketAddress address = connection.address();

        final String userName = getPlayerDatabase().getPlayer(
                address.toString(), unknown
        );

        ClientPingServerEvent.Response response = event.response();
        ClientPingServerEvent.Response.Version version = response.version();
        int protocol = version.protocolVersion();

        MotdSetup setup = new MotdSetup(
            getBlacklist().getStringList("players.by-name").contains(userName),
            "",
            userName,
            protocol
        );

        builder.execute(event, setup);
    }

    public PingBuilder<?, ?, ?, ?> getPingBuilder() {
        return builder;
    }
}
