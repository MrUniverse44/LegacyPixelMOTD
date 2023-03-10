package me.blueslime.pixelmotd.motd.builder.hover;

import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import dev.mruniverse.slimelib.file.configuration.TextDecoration;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.PluginModule;
import me.blueslime.pixelmotd.utils.placeholders.PluginPlaceholders;

import java.util.List;

public abstract class HoverModule<T> extends PluginModule {

    public HoverModule(PixelMOTD<?> plugin) {
        super(plugin);
    }

    public List<T> generate(ConfigurationHandler configuration, String path, String user, int online, int max) {
        return generate(
                configuration.getStringList(TextDecoration.LEGACY, path),
                user,
                online,
                max
        );
    }
    public abstract List<T> generate(List<String> lines, String user, int online, int max);

    public abstract T[] convert(List<T> list);

    public PluginPlaceholders getExtras() {
        return getPlugin().getListenerManager().getExtras();
    }

    public boolean hasPlayers() {
        return getPlugin().getListenerManager().isPlayer();
    }
}
