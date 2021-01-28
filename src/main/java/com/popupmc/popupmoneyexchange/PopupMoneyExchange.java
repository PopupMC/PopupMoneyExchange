package com.popupmc.popupmoneyexchange;

import dev.dbassett.skullcreator.SkullCreator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PopupMoneyExchange extends JavaPlugin {
    @Override
    public void onEnable() {
        plugin = this;

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Commands cmd = new Commands();

        Objects.requireNonNull(this.getCommand("coins")).setExecutor(cmd);

        coin = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTM3ZGNmYjY2YTYxYzUyOWYyYTEyOGFlZWY5M2MyNWY5ZDBiMDQzOWI1OWZiMTk5ZDJiMDRhY2ZlMGI4NWFhYyJ9fX0=");
        ItemMeta meta = coin.getItemMeta();
        meta.setDisplayName("Euro Dollar");

        //https://stackoverflow.com/questions/13395114/how-to-initialize-liststring-object-in-java
        List<String> lore = Collections.singletonList("A physical euro dollar");
        meta.setLore(lore);
        coin.setItemMeta(meta);

        vanillaCoin = new ItemStack(Material.SQUID_SPAWN_EGG);
        emeralds = new ItemStack(Material.EMERALD);

        this.getServer().getPluginManager().registerEvents(cmd, this);

        Objects.requireNonNull(this.getCommand("squids")).setExecutor(new MusicDiscs());
        Objects.requireNonNull(this.getCommand("emeralds")).setExecutor(new Emeralds());

        getLogger().info("PopupMoneyExchange is enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PopupMoneyExchange is disabled");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return true;
    }

    static JavaPlugin plugin;
    public static Economy econ = null;
    static ItemStack coin;
    static ItemStack vanillaCoin;
    static ItemStack emeralds;
    final static float coinWorth = 0.02f;
}
