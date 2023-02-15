package me.blueslime.pixelmotd.utils;

public class PlayerUtil {

    public static String fromUnknown(String paramString) {
        if (paramString.contains("-")) {
            return "Uuid";
        }
        return "Player";
    }

    public static String getDestinyPath(String paramString) {
        if (paramString.contains("-")) {
            return "by-uuid";
        }
        return "by-name";
    }

}
