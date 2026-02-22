package me.IkeaBird132.grafting.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GraftBookCommand implements CommandExecutor {

    public static final String BOOK_NAME = ChatColor.DARK_PURPLE + "Grafting Grimoire";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName(BOOK_NAME);
        book.setItemMeta(meta);

        player.getInventory().addItem(book);
        player.sendMessage(ChatColor.GREEN + "You received the Grafting Grimoire.");

        return true;
    }
}