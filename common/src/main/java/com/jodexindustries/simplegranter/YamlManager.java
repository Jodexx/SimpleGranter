package com.jodexindustries.simplegranter;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static com.jodexindustries.simplegranter.SimpleGranter.plugin;

public class YamlManager {

    private final File configFile;
    private final File dataFile;

    private YamlConfiguration config;
    private YamlConfiguration data;

    public YamlManager() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        data = YamlConfiguration.loadConfiguration(dataFile);
        SimpleGranter.reloadConfigs();
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public YamlConfiguration getData() {
        return data;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
