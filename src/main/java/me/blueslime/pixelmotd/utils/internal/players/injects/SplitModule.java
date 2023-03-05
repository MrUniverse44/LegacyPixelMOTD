package me.blueslime.pixelmotd.utils.internal.players.injects;

import me.blueslime.pixelmotd.utils.internal.players.PlayerModule;

public class SplitModule extends PlayerModule {

    public static final SplitModule INSTANCE = new SplitModule();

    @Override
    public int execute(int online, String values) {
        if (values.contains(";")) {
            return online / generateRandomParameter(values);
        }
        return online / Integer.parseInt(values);
    }
}
