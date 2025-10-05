package com.jodexindustries.simplegranter;

import com.j256.ormlite.logger.Level;
import com.jodexindustries.simplegranter.api.GranterAPI;
import com.jodexindustries.simplegranter.data.PermissionDriver;
import com.jodexindustries.simplegranter.data.YamlManager;
import com.jodexindustries.simplegranter.database.GranterDataBase;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SimpleGranter extends JavaPlugin {

    private final YamlManager yaml = new YamlManager(this);
    private final GranterAPI api = new GranterAPI(this);

    public static Plugin plugin;

    private Permission perms = null;
    private Chat chat = null;
    private LuckPerms luckPerms = null;

    private boolean sql = false;
    private GranterDataBase mysql = null;

    private PermissionDriver permissionDriver;

    @Override
    public void onEnable() {
        plugin = this;

        MainCommand executor = new MainCommand(this);

        PluginCommand pluginCommand = getCommand("grant");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(executor);
        }

        yaml.load();

        setupVault();
        setupLuckPerms();

        loadDriver();

        setupMySQL();
    }

    @Override
    public void onDisable() {
        if (mysql != null) mysql.close();
    }

    public void loadDriver() {
        permissionDriver = PermissionDriver.valueOf(
                yaml.getConfig().getString("Settings.PermissionDriver", "VAULT").toUpperCase()
        );

        if (permissionDriver == PermissionDriver.VAULT && perms == null) {
            permissionDriver = PermissionDriver.LUCKPERMS;
        }

        if (permissionDriver == PermissionDriver.LUCKPERMS && luckPerms == null) {
            getLogger().severe("Vault and LuckPerms not loaded on server! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Using " + permissionDriver + " driver for permissions.");
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

    public YamlManager yaml() {
        return yaml;
    }

    public GranterAPI api() {
        return api;
    }

    public Permission perms() {
        return perms;
    }

    public Chat chat() {
        return chat;
    }

    public LuckPerms luckPerms() {
        return luckPerms;
    }

    public boolean sql() {
        return sql;
    }

    public GranterDataBase mysql() {
        return mysql;
    }

    public PermissionDriver permissionDriver() {
        return permissionDriver;
    }
}
