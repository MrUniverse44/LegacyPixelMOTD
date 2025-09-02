package me.blueslime.pixelmotd.motd;

import java.util.Locale;

public enum MotdProtocol {
    ALWAYS_POSITIVE,
    ALWAYS_NEGATIVE(-1),
    DEFAULT;

    MotdProtocol() {
        code = 0;
    }

    MotdProtocol(int code) {
        this.code = code;
    }

    private int code;

    public MotdProtocol setCode(int code) {
        this.code = code;
        return this;
    }

    public int getCode() {
        return code;
    }

    @Deprecated
    public static MotdProtocol getFromText(String paramText, int code) {
        return fromString(paramText, code);
    }

    public static MotdProtocol fromOther(MotdProtocol protocol) {
        for (MotdProtocol p : values()) {
            if (p == protocol) {
                return p;
            }
        }
        return protocol;
    }

    public static MotdProtocol fromObject(Object object, int code) {
        if (object instanceof String) {
            return fromString(
                    (String)object,
                    code
            );
        }
        if (object instanceof Integer) {
            return fromInteger(
                    (int)object,
                    code
            );
        }
        return DEFAULT.setCode(code);
    }

    public static MotdProtocol fromInteger(int parameter, int code) {
        switch (parameter) {
            default:
            case -1:
            case 0:
                return DEFAULT.setCode(code);
            case 1:
                return ALWAYS_POSITIVE.setCode(code);
            case 2:
                return ALWAYS_NEGATIVE;
        }
    }

    public static MotdProtocol fromString(String paramText, int code) {
        paramText = paramText.toLowerCase(Locale.ENGLISH);

        return switch (paramText) {
            case "always_negative", "negative", "2" -> ALWAYS_NEGATIVE;
            case "default", "0", "-1" -> DEFAULT.setCode(code);
            default -> ALWAYS_POSITIVE.setCode(code);
        };
    }
}
