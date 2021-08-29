package com.github.md5sha256.regionshop;

import org.bukkit.plugin.java.JavaPlugin;

public final class RegionShopPlugin extends JavaPlugin {

    private final RegionShop regionShop = new RegionShop();

    @Override
    public void onEnable() {
        // Plugin startup logic
        regionShop.onEnable();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        regionShop.onDisable();
    }
}
