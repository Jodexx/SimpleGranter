package com.jodexindustries.simplegranter;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SimpleGranter extends JavaPlugin {
    public static Plugin plugin;
    public static Tools t;
    public static YamlManager yaml;
    public static Permission perms = null;
    public static Chat chat = null;



    @Override
    public void onEnable() {
        plugin = this;
        t = new Tools();
        if(!new File(this.getDataFolder(), "data.yml").exists()) {
            saveResource("data.yml", false);
        }
        if(!new File(this.getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        reloadConfigs();
        getCommand("grant").setExecutor(new CommandEX());
        getCommand("grant").setTabCompleter(new CommandEX());
        setupPermissions();
        setupChat();
    }
    public static void reloadConfigs() {
        yaml = new YamlManager();
    }
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
}
