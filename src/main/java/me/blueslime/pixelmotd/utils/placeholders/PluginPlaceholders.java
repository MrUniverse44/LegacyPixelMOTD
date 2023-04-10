package me.blueslime.pixelmotd.utils.placeholders;

import me.blueslime.pixelmotd.Configuration;
import me.blueslime.pixelmotd.PixelMOTD;
import me.blueslime.pixelmotd.initialization.bungeecord.BungeeMOTD;
import me.blueslime.pixelmotd.initialization.velocity.VelocityMOTD;
import me.blueslime.pixelmotd.servers.BungeeServerHandler;
import me.blueslime.pixelmotd.servers.Server;
import me.blueslime.pixelmotd.servers.VelocityServerHandler;
import me.blueslime.pixelmotd.status.StatusChecker;
import dev.mruniverse.slimelib.file.configuration.ConfigurationHandler;
import dev.mruniverse.slimelib.file.configuration.TextDecoration;
import me.blueslime.pixelmotd.utils.OnlineList;
import me.blueslime.pixelmotd.utils.internal.events.EventFormatEnum;
import me.blueslime.pixelmotd.utils.internal.storage.PluginStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginPlaceholders {

    private final PluginStorage<String, List<String>> serversMap = PluginStorage.initAsConcurrentHash();
    private final PluginStorage<String, OnlineList> onlineMap = PluginStorage.initAsConcurrentHash();
    private final static Pattern PLAYER_PATTERN = Pattern.compile("%player_(\\d)+%");
    private final boolean IS_VELOCITY_PLATFORM;
    private final boolean IS_BUNGEE_PLATFORM;
    private final PixelMOTD<?> plugin;
    private String prefix;
    private final int max;

    public PluginPlaceholders(PixelMOTD<?> plugin) {
        this.IS_VELOCITY_PLATFORM = plugin.getServerHandler() instanceof VelocityServerHandler;
        this.IS_BUNGEE_PLATFORM = plugin.getServerHandler() instanceof BungeeServerHandler;

        this.plugin = plugin;
        this.max    = plugin.getPlayerHandler().getMaxPlayers();


        load();
    }

    public void update() {
        load();
    }

    private void load() {
        serversMap.clear();
        onlineMap.clear();

        ConfigurationHandler settings = plugin.getConfiguration(Configuration.SETTINGS);

        String path = "settings.online-variables";

        prefix = settings.getString(path + ".prefix", "custom_online");

        if (settings.getStatus(path + ".enabled", false)) {
            for (String key : settings.getContent(path, false)) {
                if (!key.equalsIgnoreCase("prefix") && !key.equalsIgnoreCase("enabled")) {

                    OnlineList mode = OnlineList.fromText(
                            key,
                            settings.getString(path + "." + key + ".mode", "")
                    );

                    List<String> values = settings.getStringList(path + "." + key + ".values");

                    serversMap.set(key, values);
                    onlineMap.set(key, mode);
                }
            }
        }
    }

    public String replace(String message, int online, int max, String username) {

        return replaceServers(
                message.replace("%online%", "" + plugin.getPlayerHandler().getPlayersSize())
                        .replace("%max%","" + this.max)
                        .replace("%fake_online%", "" + online)
                        .replace("%plugin_author%", "MrUniverse44")
                        .replace("%whitelist_author%", getWhitelistAuthor())
                        .replace("%user%", username)
                        .replace("%fake_max%", "" + max)
                        .replace("[box]", "▇")
        );
    }

    private String replaceServers(String message) {
        if (message.contains("%" + prefix + "_")) {
            if (onlineMap.size() != 0) {

                List<Server> serverList = plugin.getServerHandler().getServers();

                for (String key : onlineMap.getKeys()) {
                    int online = 0;

                    switch (onlineMap.get(key)) {
                        case NAME:
                            online = getOnlineByNames(serverList, serversMap.get(key));
                            break;
                        case CONTAINS:
                            online = getOnlineByContains(serverList, serversMap.get(key));
                    }

                    message = message.replace("%" + prefix + "_" + key + "%", "" + online);
                }
            }
        }

        if (message.contains("%online_") || message.contains("%status_")) {

            StatusChecker checker = null;

            if (IS_VELOCITY_PLATFORM) {
                checker = VelocityMOTD.getInstance().getChecker();
            }

            if (IS_BUNGEE_PLATFORM) {
                checker = BungeeMOTD.getInstance().getChecker();
            }

            for (Server server : plugin.getServerHandler().getServers()) {
                message = message.replace("%online_" + server.getName() + "%", server.getOnline() + "");

                if (checker != null) {
                    message = message.replace(
                            "%status_" + server.getName() + "%",
                            checker.getServerStatus(server.getName())
                    );
                }
            }
        }
        return replaceEvents(message);
    }

    private String replaceEvents(String message) {
        ConfigurationHandler events = plugin.getConfiguration(Configuration.EVENTS);

        if (events.getStatus("events-toggle", false)) {
            if (message.contains("%event_")) {
                Date currentDate = new Date();
                for (String event : events.getContent("events", false)) {

                    String timeLeft;

                    Date date = getSpecifiedEvent(events, event);

                    if (date == null) {
                        continue;
                    }

                    long difference = date.getTime() - currentDate.getTime();

                    String path = "events." + event + ".";

                    EventFormatEnum format = EventFormatEnum.fromText(events.getString(path + "format-type", "FIRST"));

                    if (difference >= 0L) {
                        timeLeft = replaceEvent(
                                format,
                                events,
                                difference
                        );
                    } else {
                        timeLeft = events.getString(
                                TextDecoration.LEGACY,
                                path + "end-message",
                                "&cThe event finished."
                        );
                    }

                    String simplifiedPrefix = "%event_" + event + "_";

                    String zone = events.getString(path + "time-zone", "12/21/24 23:59:00");
                    String name = events.getString(path + "name", "Example Event 001");

                    message = message.replace(
                            simplifiedPrefix + "name%", name
                    ).replace(
                            simplifiedPrefix + "Name%", name
                    ).replace(
                            simplifiedPrefix + "TimeZone%", zone
                    ).replace(
                            simplifiedPrefix + "timeZone%" , zone
                    ).replace(
                            simplifiedPrefix + "Timezone", zone
                    ).replace(
                            simplifiedPrefix + "timezone%", zone
                    ).replace(
                            simplifiedPrefix + "TimeLeft%", timeLeft
                    ).replace(
                            simplifiedPrefix + "timeLeft%", timeLeft
                    ).replace(
                            simplifiedPrefix + "timeleft%", timeLeft
                    ).replace(
                            simplifiedPrefix + "Timeleft%", timeLeft
                    ).replace(
                            simplifiedPrefix + "left%", timeLeft
                    );
                }
            }
            return message;
        }
        return message;
    }

    //BIG MENTAL SKILL ISSUE
    private String replaceEvent(EventFormatEnum format, ConfigurationHandler events, long time) {

        String separator = events.getString("timer.separator", ",");

        StringJoiner joiner = new StringJoiner("");

        String prefix;

        long seconds;

        int number;


        switch (format) {
            case FIRST:
                seconds = time / 1000;

                number = math(seconds, TimeUnit.DAYS, 7);
                if (number > 0) {
                    seconds %= TimeUnit.DAYS.toSeconds(7);

                    if (number == 1) {
                        prefix = events.getString("timer.week", "week");
                    } else {
                        prefix = events.getString("timer.weeks", "weeks");
                    }

                    joiner.add(number + " " + prefix);
                }

                number = math(seconds, TimeUnit.DAYS, 1);
                if (number > 0) {
                    seconds %= TimeUnit.DAYS.toSeconds(1);

                    if (number == 1) {
                        prefix = events.getString("timer.day", "day");
                    } else {
                        prefix = events.getString("timer.days", "days");
                    }

                    joiner.add(number + " " + prefix);
                }

                number = math(seconds, TimeUnit.HOURS, 1);
                if (number > 0) {
                    seconds %= TimeUnit.HOURS.toSeconds(1);

                    if (number == 1) {
                        prefix = events.getString("timer.hour", "hour");
                    } else {
                        prefix = events.getString("timer.hours", "hours");
                    }

                    joiner.add(number + " " + prefix);
                }

                number = math(seconds, TimeUnit.MINUTES, 1);
                if (number > 0) {
                    seconds %= TimeUnit.MINUTES.toSeconds(1);

                    if (number == 1) {
                        prefix = events.getString("timer.minute", "minute");
                    } else {
                        prefix = events.getString("timer.minutes", "minutes");
                    }

                    joiner.add(number + " " + prefix);
                }

                number = math(seconds, TimeUnit.SECONDS, 1);
                if (seconds > 0 || joiner.length() == 0) {
                    seconds %= TimeUnit.SECONDS.toSeconds(1);
                    if (number == 1) {
                        prefix = events.getString("timer.second", "second");
                    } else {
                        prefix = events.getString("timer.seconds", "seconds");
                    }

                    joiner.add(number + " " + prefix);
                }
                return joiner.toString();
            case SECOND:
                seconds = time / 1000;

                number = math(seconds, TimeUnit.DAYS, 7);
                if (number > 0) {
                    seconds %= TimeUnit.DAYS.toSeconds(7);
                    joiner.add(number + ":");
                }

                number = math(seconds, TimeUnit.DAYS, 1);
                if (number > 0) {
                    seconds %= TimeUnit.DAYS.toSeconds(1);
                    joiner.add(number + ":");
                }

                number = math(seconds, TimeUnit.HOURS, 1);
                if (number > 0) {
                    seconds %= TimeUnit.HOURS.toSeconds(1);
                    joiner.add(number + ":");
                }

                number = math(seconds, TimeUnit.MINUTES, 1);
                if (number > 0) {
                    seconds %= TimeUnit.MINUTES.toSeconds(1);
                    joiner.add(number + ":");
                }

                number = math(seconds, TimeUnit.SECONDS, 1);
                if (seconds > 0 || joiner.length() == 0) {
                    seconds %= TimeUnit.SECONDS.toSeconds(1);
                    joiner.add(number + "");
                }
                return joiner.toString().replace(" ", "");
            case THIRD:
                seconds = time / 1000;

                number = math(seconds, TimeUnit.DAYS, 7);
                if (number > 0) {
                    seconds %= TimeUnit.DAYS.toSeconds(7);

                    prefix = events.getString("timer.w", "w");

                    joiner.add(number + prefix + separator);
                }

                number = math(seconds, TimeUnit.DAYS, 1);
                if (number > 0) {
                    seconds %= TimeUnit.DAYS.toSeconds(1);

                    prefix = events.getString("timer.d", "d");

                    joiner.add(number + prefix + separator);
                }

                number = math(seconds, TimeUnit.HOURS, 1);
                if (number > 0) {
                    seconds %= TimeUnit.HOURS.toSeconds(1);

                    prefix = events.getString("timer.h", "h");

                    joiner.add(number + prefix + separator);
                }

                number = math(seconds, TimeUnit.MINUTES, 1);
                if (number > 0) {
                    seconds %= TimeUnit.MINUTES.toSeconds(1);

                    prefix = events.getString("timer.m", "m");

                    joiner.add(number + prefix + separator);
                }

                number = math(seconds, TimeUnit.SECONDS, 1);
                if (seconds > 0 || joiner.length() == 0) {
                    seconds %= TimeUnit.SECONDS.toSeconds(1);

                    prefix = events.getString("timer.s", "s");

                    joiner.add(number + prefix);
                }
                return joiner.toString();
        }
        return "";
    }

    private Date getSpecifiedEvent(ConfigurationHandler control, String event) {
        SimpleDateFormat format = new SimpleDateFormat(
                control.getString("pattern","MM/dd/yy HH:mm:ss")
        );

        format.setTimeZone(
                TimeZone.getTimeZone(
                        control.getString("events." + event + ".time-zone")
                )
        );

        try {
            return format.parse(
                    control.getString("events." + event + ".date")
            );
        } catch (ParseException ignored) {
            return null;
        }
    }

    private String replaceSpecifiedPlayer(String message) {
        Matcher matcher = PLAYER_PATTERN.matcher(message);

        if (plugin.getPlayerHandler().getPlayersSize() >= 1) {
            List<String> players = new ArrayList<>(plugin.getPlayerHandler().getPlayersNames());
            while (matcher.find()) {

                int number = Integer.parseInt(matcher.group(1));

                if (players.size() >= number && number != 0) {
                    message = message.replace("%player_" + number + "%", players.get(number - 1));
                } else {
                    message = message.replace("%player_" + number + "%","%canNotFindX02_" + number + "%");
                }

            }
        } else {
            message = message.replace("%player_", "%canNotFindX02_");
        }
        return message;
    }

    public List<String> replaceHoverLine(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> array = new ArrayList<>();
        int showedPlayers = 0;
        for (String line : lines) {
            if (line.contains("<hasOnline>") || line.contains("<hasMoreOnline>")) {

                int size = plugin.getPlayerHandler().getPlayersSize();

                if (line.contains("<hasOnline>") && size >= 1) {
                    line = line.replace("<hasOnline>", "");

                    String replaceOnlineVariable = replaceSpecifiedPlayer(line);

                    if (!replaceOnlineVariable.contains("%canNotFindX02_")) {
                        array.add(replaceOnlineVariable);
                        showedPlayers++;
                    }

                    continue;
                }
                if (size > showedPlayers && showedPlayers != 0 && size >= 1) {

                    int fixedSize = size - showedPlayers;

                    line = line.replace("<hasMoreOnline>","")
                            .replace("%more_online%","" + fixedSize);

                    array.add(line);
                }
                continue;
            }
            array.add(line);
        }
        return array;
    }

    private int math(long seconds, TimeUnit unit, int number) {
        return Math.toIntExact(seconds / unit.toSeconds(number));
    }

    private String getWhitelistAuthor() {
        ConfigurationHandler whitelist = plugin.getConfiguration(Configuration.WHITELIST);

        return whitelist.getString("author", "Console");
    }


    private int getOnlineByNames(List<Server> serverList, List<String> values) {
        int count = 0;
        for (Server server : serverList) {
            if (values.contains(server.getName())) {
                count = count + server.getOnline();
            }
        }
        return count;
    }

    private int getOnlineByContains(List<Server> serverList, List<String> values) {
        int count = 0;
        for (Server server : serverList) {
            count = count + containServer(server,values);
        }
        return count;
    }

    private int containServer(Server server,List<String> values) {
        for (String value : values) {
            if (server.getName().contains(value)) {
                return server.getOnline();
            }
        }
        return 0;
    }
}
