package com.jodexindustries.simplegranter.data;

import com.jodexindustries.simplegranter.SimpleGranter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class YamlManager {

    private final SimpleGranter plugin;

    private final File configFile;
    private final File dataFile;

    private YamlConfiguration config;
    private YamlConfiguration data;

    public YamlManager(SimpleGranter plugin) {
        this.plugin = plugin;

        this.configFile = saveDefault("config.yml");
        this.dataFile = saveDefault("data.yml");
    }

    private File saveDefault(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }

        return file;
    }

    public void load() {
        config = YamlConfiguration.loadConfiguration(configFile);
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public int getCount(String player, String group) {
        return data.getInt("Players." + player + "." + group, 0);
    }

    public void setCount(String player, String group, int count) {
        data.set("Players." + player + "." + group, count);
        saveData();
    }

    public String getMessage(String path) {
        return config.getString("Messages." + path);
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error with saving data.yml", e);
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
