package com.jodexindustries.simplegranter.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class GranterDataBase {

    private final Dao<PlayersTable, String> playersTables;
    private final JdbcConnectionSource connectionSource;
    private final Plugin plugin;

    public GranterDataBase(Plugin plugin, String database, String port, String host, String user, String password) {
        this.plugin = plugin;

        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=utf8";

            this.connectionSource = new JdbcConnectionSource(url, user, password);
            TableUtils.createTableIfNotExists(connectionSource, PlayersTable.class);
            this.playersTables = DaoManager.createDao(connectionSource, PlayersTable.class);

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the stored count for a player-group pair.
     */
    public int getCount(String player, String group) {
        try {
            List<PlayersTable> results = playersTables.queryBuilder()
                    .where()
                    .eq("player", player)
                    .and()
                    .eq("group", group)
                    .query();

            return results.isEmpty() ? 0 : results.get(0).getCount();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error while fetching count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Set or update the count for a player-group pair asynchronously.
     */
    public void setCount(String player, String group, int count) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<PlayersTable> results = playersTables.queryBuilder()
                        .where()
                        .eq("player", player)
                        .and()
                        .eq("group", group)
                        .query();

                if (results.isEmpty()) {
                    PlayersTable newEntry = new PlayersTable();
                    newEntry.setPlayer(player);
                    newEntry.setGroup(group);
                    newEntry.setCount(count);
                    playersTables.create(newEntry);
                } else {
                    UpdateBuilder<PlayersTable, String> updateBuilder = playersTables.updateBuilder();
                    updateBuilder.updateColumnValue("count", count);
                    updateBuilder.where().eq("player", player).and().eq("group", group);
                    updateBuilder.update();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Error while setting count: " + e.getMessage());
            }
        });
    }

    /**
     * Properly close the connection when plugin disables.
     */
    public void close() {
        try {
            connectionSource.close();
        } catch (Exception e) {
            plugin.getLogger().warning("Error while closing database: " + e.getMessage());
        }
    }
}
