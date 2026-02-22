package me.IkeaBird132.grafting.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModeManager {

    private static final Map<UUID, GraftMode> modes = new HashMap<>();

    public static void setMode(UUID uuid, GraftMode mode) {
        modes.put(uuid, mode);
    }

    public static GraftMode getMode(UUID uuid) {
        return modes.getOrDefault(uuid, GraftMode.NONE);
    }

    public static void clear(UUID uuid) {
        modes.remove(uuid);
    }
}