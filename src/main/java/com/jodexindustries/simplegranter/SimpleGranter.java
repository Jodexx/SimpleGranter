package com.jodexindustries.simplegranter;

import com.j256.ormlite.logger.Level;
import com.jodexindustries.simplegranter.api.GranterAPI;
import com.jodexindustries.simplegranter.database.GranterDataBase;
import com.jodexindustries.simplegranter.fields.PermissionDriver;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public final class SimpleGranter extends JavaPlugin {
    public static Plugin plugin;
    public static Tools t;
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
        t = new Tools();
        if(!new File(this.getDataFolder(), "data.yml").exists()) {
            saveResource("data.yml", false);
        }
        if(!new File(this.getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        loadLibraries();
        getCommand("grant").setExecutor(new CommandEX());
        getCommand("grant").setTabCompleter(new CommandEX());
        if(getServer().getPluginManager().isPluginEnabled("Vault")) {
            if (!setupVaultPermissions()) getLogger().warning("Vault Permission won't load!");
            if (!setupVaultChat()) getLogger().warning("Vault Chat won't load!");
        }
        if(getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            setupLuckPerms();
        }
        reloadConfigs();
        setupMySQL();
    }
    public static void reloadConfigs() {
        yaml = new YamlManager();
        permissionDriver = PermissionDriver.valueOf(yaml.getConfig().getString("Settings.PermissionDriver", "Vault").toLowerCase());
        if(permissionDriver == PermissionDriver.vault && perms == null) {
            permissionDriver = PermissionDriver.luckperms;
        }
        if(permissionDriver == PermissionDriver.luckperms && luckPerms == null) {
            plugin.getLogger().severe("Vault and LuckPerms not loaded on server! Disabling...");
            Bukkit.getServer().getPluginManager().disablePlugin(SimpleGranter.plugin);
        }
        plugin.getLogger().info("Using " + permissionDriver + " driver for permissions.");
    }
    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();

        }
    }
    private boolean setupVaultPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp != null) {
            perms = rsp.getProvider();
        }
        return perms != null;
    }
    private boolean setupVaultChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if(rsp != null) chat = rsp.getProvider();
        return chat != null;
    }
    private void loadLibraries() {
        Library lib = Library.builder()
                .groupId("com{}j256{}ormlite")
                .artifactId("ormlite-jdbc")
                .version("6.1")
                .id("ormlite")
                .build();
        BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);
        bukkitLibraryManager.addMavenCentral();
        bukkitLibraryManager.loadLibrary(lib);

    }
    private void setupMySQL() {
        sql = yaml.getConfig().getBoolean("Settings.MySql.Enabled");
        if (sql) {
            String base = yaml.getConfig().getString("Settings.MySql.DataBase");
            String port = yaml.getConfig().getString("Settings.MySql.Port");
            String host = yaml.getConfig().getString("Settings.MySql.Host");
            String user = yaml.getConfig().getString("Settings.MySql.User");
            String password = yaml.getConfig().getString("Settings.MySql.Password");
            (new BukkitRunnable() {
                public void run() {
                    mysql = new GranterDataBase(plugin, base, port, host, user, password);
                }
            }).runTaskTimerAsynchronously(plugin, 0L, 12000L);
            com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.WARNING);
        }
    }
}
