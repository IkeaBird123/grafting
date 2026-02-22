package me.IkeaBird132.grafting;

import me.IkeaBird132.grafting.command.GraftBookCommand;
import me.IkeaBird132.grafting.handler.*;
import me.IkeaBird132.grafting.listener.GraftBookListener;
import me.IkeaBird132.grafting.listener_support.LifeLinkDeathListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GraftingV1 extends JavaPlugin {

    private static GraftingV1 instance;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("grafting-book").setExecutor(new GraftBookCommand());

        getServer().getPluginManager().registerEvents(new GraftBookListener(), this);
        getServer().getPluginManager().registerEvents(new MenuHandler(), this);
        getServer().getPluginManager().registerEvents(new LifeLinkHandler(), this);
        getServer().getPluginManager().registerEvents(new LifeLinkDeathListener(), this);
        getServer().getPluginManager().registerEvents(new LocationGraftHandler(), this);
    }

    public static GraftingV1 getInstance() {
        return instance;
    }
}