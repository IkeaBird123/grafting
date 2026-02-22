package me.IkeaBird132.grafting.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GraftBookCommand implements CommandExecutor {

    // The book's display name as a Component — used for identity checks
    public static final Component BOOK_NAME = Component.text("Grafting Grimoire")
            .color(NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false);

    private static final Set<UUID> bookHolders = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) return true;

        removeBookFromPlayer(player);

        ItemStack book = createBook();
        player.getInventory().addItem(book);
        bookHolders.add(player.getUniqueId());
        player.sendMessage(Component.text("You received the Grafting Grimoire.")
                .color(NamedTextColor.GREEN));

        return true;
    }

    public static ItemStack createBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.displayName(BOOK_NAME);
        book.setItemMeta(meta);
        return book;
    }

    public static boolean isGraftBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Component name = item.getItemMeta().displayName();
        return BOOK_NAME.equals(name);
    }

    public static void removeBookFromPlayer(Player player) {
        player.getInventory().forEach(item -> {
            if (isGraftBook(item)) player.getInventory().remove(item);
        });
        bookHolders.remove(player.getUniqueId());
    }
}