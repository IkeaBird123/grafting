package me.IkeaBird132.grafting.listener;

import me.IkeaBird132.grafting.GraftingV1;
import me.IkeaBird132.grafting.command.GraftBookCommand;
import me.IkeaBird132.grafting.handler.MenuHandler;
import me.IkeaBird132.grafting.manager.GraftMode;
import me.IkeaBird132.grafting.manager.ModeManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class GraftBookListener implements Listener {

    // Match exactly the titles set in MenuHandler and LocationGraftHandler
    private static final Component TITLE_MENU = MenuHandler.TITLE;
    private static final Component TITLE_C    = me.IkeaBird132.grafting.handler.LocationGraftHandler.TITLE_C;
    private static final Component TITLE_F    = me.IkeaBird132.grafting.handler.LocationGraftHandler.TITLE_F;

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!GraftBookCommand.isGraftBook(item)) return;

        Player player = event.getPlayer();
        if (ModeManager.getMode(player.getUniqueId()) != GraftMode.NONE) return;

        event.setCancelled(true);
        MenuHandler.openMenu(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player player)) return;

        Component title = event.getView().title();
        if (!title.equals(TITLE_MENU) && !title.equals(TITLE_C) && !title.equals(TITLE_F)) return;

        // Wait 1 tick — if no inventory is open it was a genuine Escape, reset to NONE
        GraftingV1.getInstance().getServer().getScheduler().runTaskLater(
                GraftingV1.getInstance(),
                () -> {
                    if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                        ModeManager.setMode(player.getUniqueId(), GraftMode.NONE);
                    }
                },
                1L
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GraftBookCommand.removeBookFromPlayer(player);
        ModeManager.clear(player.getUniqueId());
    }
}