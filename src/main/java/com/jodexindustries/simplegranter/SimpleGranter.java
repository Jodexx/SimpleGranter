package com.jodexindustries.simplegranter;

import com.jodexindustries.simplegranter.api.GranterAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SimpleGranter extends JavaPlugin {
    public static Plugin plugin;
    public static Tools t;
    public static GranterAPI api;
    public static YamlManager yaml;
    public static Permission perms = null;
    public static Chat chat = null;



    @Override
    public void onEnable() {
        plugin = this;
        api = new GranterAPI();
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
        if(!setupPermissions()) getLogger().warning("Vault Permission won't load!");
        if(!setupChat()) getLogger().warning("Vault Chat won't load!");
    }
    public static void reloadConfigs() {
        yaml = new YamlManager();
    }
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp != null) {
            perms = rsp.getProvider();
        }
        return perms != null;
    }
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if(rsp != null) chat = rsp.getProvider();
        return chat != null;
    }
}
