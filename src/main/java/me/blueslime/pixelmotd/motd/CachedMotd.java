package me.blueslime.pixelmotd.motd;

import me.blueslime.slimelib.file.configuration.ConfigurationHandler;
import me.blueslime.slimelib.file.configuration.TextDecoration;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.utils.internal.players.PlayerModules;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CachedMotd {
    private final ConfigurationHandler configuration;
    private MotdProtocol specifiedProtocol;
    private String protocol;

    private final HashSet<String> validDomains = new HashSet<>();
    private int domainType;

    public CachedMotd(ConfigurationHandler configuration) {
        this.configuration = configuration;
        init();
    }

    private void init() {
        this.specifiedProtocol = MotdProtocol.fromObject(
                configuration.get("protocol.modifier", "1"),
                0
        );

        this.domainType = configuration.getInt("domain-setup.type", -1);
        if (this.domainType != -1) {
            String domainValue = configuration.getString("domain-setup.value", "");
            this.validDomains.addAll(Arrays.asList(domainValue.split(",")));
        }

        this.protocol = configuration.getString("protocol.text", "&fPlayers: &a%online%/1000");

        if (protocol.contains("<before-the-icon>")) {
            this.protocol = protocol.replace("<before-the-icon>", "");

            String[] split = protocol.split("<default>");

            String icon;
            String def;

            if (split.length >= 2) {
                icon = split[0];
                def = split[1];
            } else if (split.length == 1) {
                icon = split[0];
                def = "";
            } else {
                icon = "";
                def = "";
            }

            int max = configuration.getInt("protocol.space-length", 30);

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < max; i++) {
                builder.append(" ");
            }

            this.protocol = icon + builder + def;
        }
    }

    public String getLine1() {
        return configuration.getString("line-1", "");
    }

    public String getLine2() {
        return configuration.getString("line-2", "");
    }

    public MotdProtocol getModifier() {
        return specifiedProtocol;
    }

    public String getProtocolText() {
        return protocol;
    }

    public boolean isDomainAccepted(String domain) {
        if (domain == null || domain.isEmpty()) {
            return true;
        }

        if (domainType == -1) {
            return true;
        }

        boolean contains =  validDomains.contains(domain);

        return (domainType == 0 && contains) || (domainType == 1 && !contains);
    }

    public boolean hasHover() {
        Object type = configuration.get("hover.type", "0");

        if (type instanceof String) {
            String v = (String)type;
            return v.equals("0");
        }
        if (type instanceof Integer) {
            int v = (int)type;
            return v == 0;
        }
        return (boolean)type;
    }

    public List<String> getHover() {
        return configuration.getStringList(TextDecoration.LEGACY, "hover.value");
    }

    public int getOnline(PixelMOTD<?> plugin) {
        return PlayerModules.getOnlinePlayers(
                plugin,
                this
        );
    }

    @SuppressWarnings("unused")
    public int getMax(PixelMOTD<?> plugin) {
        return PlayerModules.getMaximumPlayers(
                plugin,
                this
        );
    }

    public int getMax(PixelMOTD<?> plugin, int online) {
        return PlayerModules.getMaximumPlayers(
                plugin,
                this,
                online
        );
    }

    public boolean hasHex() {
        return configuration.getBoolean("hex-motd", false);
    }

    public boolean hasServerIcon() {
        return "0".equals(configuration.get("icon.type", "0")) || configuration.getInt("icon.type", 0) == 0;
    }

    public String getServerIcon() {
        return generateRandomParameter(
                configuration.getString(
                        "icon.value",
                        "default-icon.png"
                )
        );
    }

    public ConfigurationHandler getConfiguration() {
        return configuration;
    }

    private String generateRandomParameter(String values) {
        if (!values.contains(";")) {
            return values;
        }

        Random random = ThreadLocalRandom.current();

        ArrayList<String> valueList = new ArrayList<>(Arrays.asList(values.split(";")));

        return valueList.get(
                random.nextInt(
                        valueList.size()
                )
        );
    }
}
