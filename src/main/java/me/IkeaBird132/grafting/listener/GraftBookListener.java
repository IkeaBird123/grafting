package me.IkeaBird132.grafting.listener;

import me.IkeaBird132.grafting.command.GraftBookCommand;
import me.IkeaBird132.grafting.handler.MenuHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GraftBookListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (!item.hasItemMeta()) return;

        if (item.getItemMeta().getDisplayName().equals(GraftBookCommand.BOOK_NAME)) {
            event.setCancelled(true);
            MenuHandler.openMenu(event.getPlayer());
        }
    }
}