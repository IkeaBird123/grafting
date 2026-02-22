package me.IkeaBird132.grafting.handler;

import me.IkeaBird132.grafting.GraftingV1;
import me.IkeaBird132.grafting.manager.GraftMode;
import me.IkeaBird132.grafting.manager.ModeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifeLinkHandler implements Listener {

    private static final Map<UUID, UUID> firstSelection = new HashMap<>();
    private static final Map<UUID, UUID> activeLinks    = new HashMap<>();

    // RMB on entity — select for linking
    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (ModeManager.getMode(player.getUniqueId()) != GraftMode.LIFE_LINK) return;
        if (player.isSneaking()) return; // Ctrl+RMB = cancel, handled below

        if (!(event.getRightClicked() instanceof LivingEntity target)) {
            player.sendMessage(Component.text("You can only link living entities.")
                    .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You cannot link yourself.")
                    .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            return;
        }

        event.setCancelled(true);

        UUID playerUUID = player.getUniqueId();

        if (!firstSelection.containsKey(playerUUID)) {
            firstSelection.put(playerUUID, target.getUniqueId());
            player.sendMessage(Component.text(target.getName() + " has been chosen. Right-click the second entity.")
                    .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        } else {
            UUID firstUUID = firstSelection.get(playerUUID);

            if (firstUUID.equals(target.getUniqueId())) {
                player.sendMessage(Component.text("You cannot link an entity to itself.")
                        .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                return;
            }

            LivingEntity first = findLivingEntity(firstUUID);
            if (first == null) {
                player.sendMessage(Component.text("First entity is no longer valid. Selection reset.")
                        .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                firstSelection.remove(playerUUID);
                return;
            }

            firstSelection.remove(playerUUID);
            player.sendMessage(Component.text(target.getName() + " has been chosen.")
                    .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            player.sendMessage(Component.text("Establishing link in 2 seconds...")
                    .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

            // Capture target for the lambda, unsure if works atm as lifelink kinda dead TO BE FIXED
            LivingEntity second = target;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (first.isDead() || second.isDead()) {
                        player.sendMessage(Component.text("Link failed — an entity died before it could establish.")
                                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        return;
                    }
                    establishLink(player, first, second);
                }
            }.runTaskLater(GraftingV1.getInstance(), 40L);
        }
    }


    // Ctrl+RMB (sneak + right-click) — cancel, return to Stage A

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (ModeManager.getMode(player.getUniqueId()) != GraftMode.LIFE_LINK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return;

        event.setCancelled(true);
        firstSelection.remove(player.getUniqueId());
        ModeManager.setMode(player.getUniqueId(), GraftMode.NONE);
        player.sendMessage(Component.text("Life Link cancelled.")
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        MenuHandler.openMenu(player);
    }


    // Cancel all damage while in Life Link mode
    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (ModeManager.getMode(player.getUniqueId()) != GraftMode.LIFE_LINK) return;
        event.setCancelled(true);
    }

    // Link logic, Doesn't work atm TO BE FIXED
    private void establishLink(Player initiator, LivingEntity e1, LivingEntity e2) {

        activeLinks.put(e1.getUniqueId(), e2.getUniqueId());
        activeLinks.put(e2.getUniqueId(), e1.getUniqueId());

        initiator.sendMessage(Component.text("Life Link established between " + e1.getName() + " and " + e2.getName() + "!")
                .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));

        playLinkParticles(e1, e2);

        new BukkitRunnable() {
            @Override
            public void run() {
                activeLinks.remove(e1.getUniqueId());
                activeLinks.remove(e2.getUniqueId());
                initiator.sendMessage(Component.text("Life Link between " + e1.getName() + " and " + e2.getName() + " has faded.")
                        .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
        }.runTaskLater(GraftingV1.getInstance(), 20L * 180L);
    }

    // Doesn't work atm TO BE FIXED
    private void playLinkParticles(LivingEntity e1, LivingEntity e2) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 60 || e1.isDead() || e2.isDead()) {
                    cancel();
                    return;
                }
                e1.getWorld().spawnParticle(Particle.FIREWORK, e1.getLocation(), 10);
                e2.getWorld().spawnParticle(Particle.FIREWORK, e2.getLocation(), 10);
                drawLine(e1.getLocation(), e2.getLocation());
                ticks++;
            }
        }.runTaskTimer(GraftingV1.getInstance(), 0L, 5L);
    }

    // Doesn't work atm TO BE FIXED
    private void drawLine(Location start, Location end) {
        double distance = start.distance(end);
        if (distance == 0) return;
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        for (double d = 0; d < distance; d += 0.5) {
            Location point = start.clone().add(direction.clone().multiply(d));
            start.getWorld().spawnParticle(Particle.DRAGON_BREATH, point, 1);
        }
    }

    // Doesn't work atm TO BE FIXED
    private LivingEntity findLivingEntity(UUID uuid) {
        var entity = GraftingV1.getInstance().getServer().getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    public static UUID getLinked(UUID uuid) {
        return activeLinks.get(uuid);
    }
}