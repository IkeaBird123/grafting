package me.IkeaBird132.grafting.listener_support;

import me.IkeaBird132.grafting.handler.LifeLinkHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class LifeLinkDeathListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();
        UUID linked = LifeLinkHandler.getLinked(entity.getUniqueId());

        if (linked == null) return;

        if (Bukkit.getEntity(linked) instanceof LivingEntity other) {
            other.setHealth(0);
        }
    }
}