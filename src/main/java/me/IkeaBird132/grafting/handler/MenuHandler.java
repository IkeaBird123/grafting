package me.IkeaBird132.grafting.handler;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuHandler implements Listener {

    private static final String TITLE = ChatColor.DARK_PURPLE + "Grafting Modes";

    public static void openMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, 9, TITLE);

        inv.setItem(2, createItem(Material.COOKED_BEEF, ChatColor.GREEN + "Life Link"));
        inv.setItem(4, createItem(Material.GRASS_BLOCK, ChatColor.BLUE + "Location Graft"));
        inv.setItem(6, createItem(Material.OBSIDIAN, ChatColor.DARK_GRAY + "Coming Soon"));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(TITLE)) return;

        event.setCancelled(true); // LOCK ITEMS

        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        switch (event.getCurrentItem().getType()) {

            case COOKED_BEEF:
                player.sendMessage(ChatColor.GREEN + "Life Link mode selected.");
                LifeLinkHandler.enableSelection(player);
                player.closeInventory();
                break;

            case GRASS_BLOCK:
                player.sendMessage(ChatColor.YELLOW + "Location Graft selected.");
                player.closeInventory();
                break;
        }
    }
}