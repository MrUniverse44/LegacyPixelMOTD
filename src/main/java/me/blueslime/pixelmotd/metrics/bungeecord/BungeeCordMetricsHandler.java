package me.blueslime.pixelmotd.metrics.bungeecord;

import me.blueslime.pixelmotd.metrics.MetricsHandler;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCordMetricsHandler extends MetricsHandler<Plugin> {
    public BungeeCordMetricsHandler(Object main) {
        super((Plugin) main, 15578);
    }

    @Override
    public void initialize() {
        new Metrics(getMain(), getId());
    }
}
