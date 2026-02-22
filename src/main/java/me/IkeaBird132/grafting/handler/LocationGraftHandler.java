package me.IkeaBird132.grafting.handler;

import me.IkeaBird132.grafting.manager.GraftMode;
import me.IkeaBird132.grafting.manager.ModeManager;
import me.IkeaBird132.grafting.manager.LocationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LocationGraftHandler implements Listener {

    public static final Component TITLE_C = Component.text("Location Graft")
            .color(NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false);
    public static final Component TITLE_F = Component.text("Save Manager")
            .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);

    // -------------------------------------------------------------------------
    // Stage C
    // -------------------------------------------------------------------------
    public static void openStageC(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE_C);

        inv.setItem(2, createItem(Material.DIAMOND,     txt("Teleport Yourself", NamedTextColor.AQUA),  "Teleports you to your selected save."));
        inv.setItem(4, createItem(Material.FIRE_CHARGE, txt("Teleport Entity",   NamedTextColor.GOLD),  "Right-click an entity to send it to your selected save."));
        inv.setItem(6, createItem(Material.MAP,         txt("Manage Saves",      NamedTextColor.GREEN), "View, save and select locations."));
        inv.setItem(0, createBack());

        fillGlass(inv);
        player.openInventory(inv);
    }

    // -------------------------------------------------------------------------
    // Stage F
    // -------------------------------------------------------------------------
    public static void openStageF(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE_F);

        Location[] saves     = LocationManager.getAll(player);
        int        selected  = LocationManager.getSelected(player);

        for (int i = 0; i < 3; i++) {
            inv.setItem(2 + (i * 2), buildPaperItem(i, saves[i], selected == i));
        }

        inv.setItem(0, createBack());
        fillGlass(inv);
        player.openInventory(inv);
    }

    // Inventory clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        Component title = event.getView().title();
        boolean isC = title.equals(TITLE_C);
        boolean isF = title.equals(TITLE_F);
        if (!isC && !isF) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        Material type = event.getCurrentItem().getType();

        // ── Stage C ─────────────────────────────────────────────────────────
        if (isC) {
            switch (type) {

                case DIAMOND -> {
                    int sel      = LocationManager.getSelected(player);
                    Location dest = LocationManager.getLocation(player, sel);
                    if (dest == null) {
                        player.sendMessage(txt("No save selected. Open Manage Saves first.", NamedTextColor.RED));
                        return;
                    }
                    player.closeInventory();
                    player.teleport(dest);
                    player.sendMessage(txt("Teleported to Save " + (sel + 1) + ".", NamedTextColor.AQUA));
                }

                case FIRE_CHARGE -> {
                    int sel = LocationManager.getSelected(player);
                    if (LocationManager.getLocation(player, sel) == null) {
                        player.sendMessage(txt("No save selected. Open Manage Saves first.", NamedTextColor.RED));
                        return;
                    }
                    player.closeInventory();
                    ModeManager.setMode(player.getUniqueId(), GraftMode.LOCATION_ENTITY_TELEPORT);
                    player.sendMessage(txt("Entity Teleport active. Right-click an entity to teleport it. Ctrl+Right-click to cancel.", NamedTextColor.GOLD));
                }

                case MAP -> openStageF(player);

                case RED_STAINED_GLASS_PANE -> {
                    player.closeInventory();
                    ModeManager.setMode(player.getUniqueId(), GraftMode.NONE);
                    MenuHandler.openMenu(player);
                }

                default -> { /* filler */ }
            }
        }

        // ── Stage F ─────────────────────────────────────────────────────────
        if (isF) {

            if (type == Material.RED_STAINED_GLASS_PANE) {
                openStageC(player);
                return;
            }

            if (type != Material.PAPER) return;

            int saveIndex = (event.getSlot() - 2) / 2;
            if (saveIndex < 0 || saveIndex > 2) return;

            Location[] saves = LocationManager.getAll(player);

            if (event.isShiftClick()) {

                if (event.isRightClick()) {
                    LocationManager.removeLocation(player, saveIndex);
                    player.sendMessage(txt("Save " + (saveIndex + 1) + " cleared.", NamedTextColor.RED));
                } else {
                    if (saves[saveIndex] == null) {
                        player.sendMessage(txt("That slot has no location saved.", NamedTextColor.RED));
                        return;
                    }
                    LocationManager.setSelected(player, saveIndex);
                    player.sendMessage(txt("Save " + (saveIndex + 1) + " selected.", NamedTextColor.YELLOW));
                }
                openStageF(player); // loop back

            } else if (event.isLeftClick()) {
                if (saves[saveIndex] != null) {
                    player.sendMessage(txt("Slot already occupied. Shift+RMB to delete it first.", NamedTextColor.RED));
                    return;
                }
                LocationManager.saveLocation(player, saveIndex);
                Location saved = player.getLocation();
                player.sendMessage(txt("Saved to slot " + (saveIndex + 1)
                        + "  X:" + (int) saved.getX()
                        + " Y:" + (int) saved.getY()
                        + " Z:" + (int) saved.getZ(), NamedTextColor.GREEN));
                openStageF(player);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Component title = event.getView().title();
        if (title.equals(TITLE_C) || title.equals(TITLE_F)) {
            event.setCancelled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Stage E: RMB on entity → teleport to selected save
    // -------------------------------------------------------------------------
    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (ModeManager.getMode(player.getUniqueId()) != GraftMode.LOCATION_ENTITY_TELEPORT) return;
        if (player.isSneaking()) return; // Ctrl+RMB = cancel

        event.setCancelled(true);

        Entity target = event.getRightClicked();
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(txt("Use the Diamond to teleport yourself.", NamedTextColor.RED));
            return;
        }

        int sel       = LocationManager.getSelected(player);
        Location dest = LocationManager.getLocation(player, sel);
        if (dest == null) {
            player.sendMessage(txt("No save selected.", NamedTextColor.RED));
            return;
        }

        target.teleport(dest);
        player.sendMessage(txt(target.getName() + " teleported to Save " + (sel + 1) + ".", NamedTextColor.GOLD));
    }

    // -------------------------------------------------------------------------
    // Stage E: Ctrl+RMB → cancel, return to Stage C
    // -------------------------------------------------------------------------
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (ModeManager.getMode(player.getUniqueId()) != GraftMode.LOCATION_ENTITY_TELEPORT) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return;

        event.setCancelled(true);
        ModeManager.setMode(player.getUniqueId(), GraftMode.LOCATION_GRAFT);
        openStageC(player);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Component txt(String s, NamedTextColor color) {
        return Component.text(s).color(color).decoration(TextDecoration.ITALIC, false);
    }

    private static ItemStack createItem(Material mat, Component name, String loreText) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        meta.displayName(name);
        meta.lore(List.of(txt(loreText, NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildPaperItem(int index, Location loc, boolean selected) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta   = paper.getItemMeta();

        Component nameComp = Component.text("Save " + (index + 1))
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(selected
                        ? Component.text("  ★ Selected").color(NamedTextColor.YELLOW)
                        : Component.empty());
        meta.displayName(nameComp);

        List<Component> lore = new ArrayList<>();
        if (loc != null) {
            lore.add(txt("X: " + (int) loc.getX() + "  Y: " + (int) loc.getY() + "  Z: " + (int) loc.getZ(), NamedTextColor.GRAY));
            lore.add(txt("World: " + loc.getWorld().getName(), NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Shift+LMB").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                    .append(txt(" → Select this save", NamedTextColor.WHITE)));
            lore.add(Component.text("Shift+RMB").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
                    .append(txt(" → Delete this save", NamedTextColor.WHITE)));
        } else {
            lore.add(txt("(empty)", NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("LMB").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                    .append(txt(" → Save your current location here", NamedTextColor.WHITE)));
        }
        meta.lore(lore);
        paper.setItemMeta(meta);
        return paper;
    }

    private static ItemStack createBack() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta  = item.getItemMeta();
        meta.displayName(txt("Back", NamedTextColor.RED));
        item.setItemMeta(meta);
        return item;
    }

    private static void fillGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta   = glass.getItemMeta();
        meta.displayName(Component.text(" "));
        glass.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
    }
}