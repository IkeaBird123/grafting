package me.IkeaBird132.grafting.handler;

import me.IkeaBird132.grafting.manager.GraftMode;
import me.IkeaBird132.grafting.manager.ModeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuHandler implements Listener {

    public static final Component TITLE = Component.text("Grafting Modes")
            .color(NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false);

    public static void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE);

        inv.setItem(2, createItem(Material.COOKED_BEEF,   text("Life Link",       NamedTextColor.GREEN)));
        inv.setItem(4, createItem(Material.GRASS_BLOCK,   text("Location Graft",  NamedTextColor.BLUE)));
        inv.setItem(6, createItem(Material.SPLASH_POTION, text("Attribute Graft", NamedTextColor.RED)));

        fillGlass(inv);
        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static Component text(String s, NamedTextColor color) {
        return Component.text(s).color(color).decoration(TextDecoration.ITALIC, false);
    }

    private static void fillGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.text(" "));
        glass.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!event.getView().title().equals(TITLE)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        switch (event.getCurrentItem().getType()) {

            case COOKED_BEEF -> {
                ModeManager.setMode(player.getUniqueId(), GraftMode.LIFE_LINK);
                player.closeInventory();
                player.sendMessage(Component.text("Life Link selected. Right-click two entities to link them. Ctrl+Right-click to cancel.")
                        .color(NamedTextColor.GREEN));
            }

            case GRASS_BLOCK -> {
                ModeManager.setMode(player.getUniqueId(), GraftMode.LOCATION_GRAFT);
                player.closeInventory();
                LocationGraftHandler.openStageC(player);
            }

            case SPLASH_POTION ->
                    player.sendMessage(Component.text("Attribute Graft is coming soon.")
                            .color(NamedTextColor.RED));

            default -> { /* filler glass */ }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().title().equals(TITLE)) {
            event.setCancelled(true);
        }
    }
}