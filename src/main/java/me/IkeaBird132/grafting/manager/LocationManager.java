package me.IkeaBird132.grafting.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocationManager {

    // Each player has exactly 3 save slots (index 0, 1, 2)
    private static final Map<UUID, Location[]> playerSaves = new HashMap<>();
    private static final Map<UUID, Integer> selectedSlot   = new HashMap<>();

    private static Location[] getSlots(Player player) {
        return playerSaves.computeIfAbsent(player.getUniqueId(), k -> new Location[3]);
    }


    // Save the player's current location to a specific slot.
    // Does NOT migrate saves — each slot is independent.

    public static void saveLocation(Player player, int slot) {
        if (slot < 0 || slot > 2) return;
        Location[] slots = getSlots(player);
        slots[slot] = player.getLocation().clone();
    }

    // Remove the save at a specific slot. Other slots are unaffected.
    public static void removeLocation(Player player, int slot) {
        if (slot < 0 || slot > 2) return;
        getSlots(player)[slot] = null;
    }


    // Get the saved location at a specific slot, or null if empty.
    public static Location getLocation(Player player, int slot) {
        if (slot < 0 || slot > 2) return null;
        return getSlots(player)[slot];
    }

    //Get all 3 save slots (elements may be null if empty).

    public static Location[] getAll(Player player) {
        return getSlots(player);
    }

    public static void setSelected(Player player, int slot) {
        selectedSlot.put(player.getUniqueId(), slot);
    }

    // Returns the selected slot index, or -1 if none selected.

    public static int getSelected(Player player) {
        return selectedSlot.getOrDefault(player.getUniqueId(), -1);
    }

    public static void clear(UUID uuid) {
        playerSaves.remove(uuid);
        selectedSlot.remove(uuid);
    }
}