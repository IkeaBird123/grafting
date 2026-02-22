package me.IkeaBird132.grafting.listener_support;

import me.IkeaBird132.grafting.handler.LifeLinkHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class LifeLinkDeathListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        LivingEntity dead = event.getEntity();
        UUID linkedUUID = LifeLinkHandler.getLinked(dead.getUniqueId());

        if (linkedUUID == null) return;

        var linkedEntity = Bukkit.getEntity(linkedUUID);
        if (linkedEntity instanceof LivingEntity other && !other.isDead()) {
            other.setHealth(0);
        }
    }
}