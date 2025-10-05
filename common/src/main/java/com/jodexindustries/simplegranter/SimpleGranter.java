package com.jodexindustries.simplegranter;

import com.j256.ormlite.logger.Level;
import com.jodexindustries.simplegranter.api.GranterAPI;
import com.jodexindustries.simplegranter.database.GranterDataBase;
import com.jodexindustries.simplegranter.fields.PermissionDriver;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class SimpleGranter extends JavaPlugin {

    public static Plugin plugin;
    public static GranterAPI api;
    public static YamlManager yaml;

    public static Permission perms = null;
    public static Chat chat = null;
    public static LuckPerms luckPerms = null;

    public static boolean sql = false;
    public static GranterDataBase mysql = null;

    public static PermissionDriver permissionDriver;

    @Override
    public void onEnable() {
        plugin = this;
        api = new GranterAPI();

        saveDefaultConfigFile("data.yml");
        saveDefaultConfigFile("config.yml");

        getCommand("grant").setExecutor(new CommandEX());
        getCommand("grant").setTabCompleter(new CommandEX());

        setupVault();
        setupLuckPerms();

        reloadConfigs();

        setupMySQL();
    }

    @Override
    public void onDisable() {
        if (mysql != null) mysql.close();
    }

    public static void reloadConfigs() {
        yaml = new YamlManager();
        permissionDriver = PermissionDriver.valueOf(
                yaml.getConfig().getString("Settings.PermissionDriver", "VAULT").toUpperCase()
        );

        if (permissionDriver == PermissionDriver.VAULT && perms == null) {
            permissionDriver = PermissionDriver.LUCKPERMS;
        }

        if (permissionDriver == PermissionDriver.LUCKPERMS && luckPerms == null) {
            plugin.getLogger().severe("Vault and LuckPerms not loaded on server! Disabling...");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        plugin.getLogger().info("Using " + permissionDriver + " driver for permissions.");
    }

    private void saveDefaultConfigFile(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }

    private void setupVault() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            return;
        }

        if (!setupVaultPermissions()) {
            getLogger().warning("Vault Permission won't load!");
        }

        if (!setupVaultChat()) {
            getLogger().warning("Vault Chat won't load!");
        }
    }

    private boolean setupVaultPermissions() {
        RegisteredServiceProvider<Permission> rsp =
                getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            perms = rsp.getProvider();
        }
        return perms != null;
    }

    private boolean setupVaultChat() {
        RegisteredServiceProvider<Chat> rsp =
                getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            chat = rsp.getProvider();
        }
        return chat != null;
    }

    private void setupLuckPerms() {
        if (!getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            return;
        }

        RegisteredServiceProvider<LuckPerms> provider =
                Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
    }

    private void setupMySQL() {
        sql = yaml.getConfig().getBoolean("Settings.MySql.Enabled");
        if (!sql) return;

        String base = yaml.getConfig().getString("Settings.MySql.DataBase");
        String port = yaml.getConfig().getString("Settings.MySql.Port");
        String host = yaml.getConfig().getString("Settings.MySql.Host");
        String user = yaml.getConfig().getString("Settings.MySql.User");
        String password = yaml.getConfig().getString("Settings.MySql.Password");

        new BukkitRunnable() {
            @Override
            public void run() {
                mysql = new GranterDataBase(plugin, base, port, host, user, password);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 12000L);

        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.WARNING);
    }
}
