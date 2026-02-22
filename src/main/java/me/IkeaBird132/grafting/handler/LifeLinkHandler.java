package me.IkeaBird132.grafting.handler;

import me.IkeaBird132.grafting.GraftingV1;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifeLinkHandler implements Listener {

    // First entity selection per player
    private static final Map<Player, LivingEntity> firstSelection = new HashMap<>();

    // Active links (entity UUID -> linked entity UUID)
    private static final Map<UUID, UUID> activeLinks = new HashMap<>();

    /*
     * Called when player selects Life Link mode
     */
    public static void enableSelection(Player player) {
        player.sendMessage(ChatColor.GRAY + "Select first entity.");
        firstSelection.put(player, null);
    }

    /*
     * Detect entity right-click
     */
    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();

        // Only proceed if player is in selection mode
        if (!firstSelection.containsKey(player)) return;

        // Only allow living entities
        if (!(clicked instanceof LivingEntity living)) {
            player.sendMessage(ChatColor.RED + "You can only link living entities.");
            return;
        }

        event.setCancelled(true);

        // First selection
        if (firstSelection.get(player) == null) {
            firstSelection.put(player, living);
            player.sendMessage(ChatColor.GREEN + living.getName() + " has been selected.");
            player.sendMessage(ChatColor.GRAY + "Select second entity.");
            return;
        }

        LivingEntity first = firstSelection.get(player);

        // Prevent linking same entity
        if (first.getUniqueId().equals(living.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot link an entity to itself.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + living.getName() + " has been selected.");
        player.sendMessage(ChatColor.YELLOW + "Establishing Life Link...");

        // 2 second delay before establishing link
        new BukkitRunnable() {
            @Override
            public void run() {
                establishLink(first, living);
                player.sendMessage(ChatColor.GREEN + "Life link established.");
            }
        }.runTaskLater(GraftingV1.getInstance(), 40);

        firstSelection.remove(player);
    }

    /*
     * Establish the link
     */
    private static void establishLink(LivingEntity e1, LivingEntity e2) {

        activeLinks.put(e1.getUniqueId(), e2.getUniqueId());
        activeLinks.put(e2.getUniqueId(), e1.getUniqueId());

        playParticles(e1, e2);

        // Remove after 3 minutes (180 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                activeLinks.remove(e1.getUniqueId());
                activeLinks.remove(e2.getUniqueId());

                if (!e1.isDead())
                    e1.sendMessage(ChatColor.GRAY + "Life Link faded.");
                if (!e2.isDead())
                    e2.sendMessage(ChatColor.GRAY + "Life Link faded.");
            }
        }.runTaskLater(GraftingV1.getInstance(), 20 * 180);
    }

    /*
     * Particle visuals
     */
    private static void playParticles(LivingEntity e1, LivingEntity e2) {

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (ticks > 60) { // lasts a few seconds
                    cancel();
                    return;
                }

                if (e1.isDead() || e2.isDead()) {
                    cancel();
                    return;
                }

                // Firework circle effect
                e1.getWorld().spawnParticle(Particle.FIREWORK, e1.getLocation(), 15);
                e2.getWorld().spawnParticle(Particle.FIREWORK, e2.getLocation(), 15);

                // Green connecting line
                drawLine(e1.getLocation(), e2.getLocation());

                ticks++;
            }

        }.runTaskTimer(GraftingV1.getInstance(), 0, 5);
    }

    /*
     * Draw particle line between entities
     */
    private static void drawLine(Location start, Location end) {

        double distance = start.distance(end);
        Vector vector = end.toVector().subtract(start.toVector()).normalize();

        for (double i = 0; i < distance; i += 0.5) {
            Location point = start.clone().add(vector.clone().multiply(i));
            start.getWorld().spawnParticle(Particle.DRAGON_BREATH, point, 1);
        }
    }

    /*
     * Used by Death Listener
     */
    public static UUID getLinked(UUID uuid) {
        return activeLinks.get(uuid);
    }
}