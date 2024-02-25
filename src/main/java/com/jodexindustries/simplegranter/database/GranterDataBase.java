package com.jodexindustries.simplegranter.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.TableUtils;
import com.jodexindustries.simplegranter.database.entities.PlayersTable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class GranterDataBase {
    private Dao<PlayersTable, String> playersTables;
    private JdbcConnectionSource connectionSource;
    private final Plugin instance;

    public GranterDataBase(Plugin instance, String database, String port, String host, String user, String password) {
        this.instance = instance;
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
            connectionSource = new JdbcConnectionSource(url, user, password);
            TableUtils.createTableIfNotExists(connectionSource, PlayersTable.class);
            playersTables = DaoManager.createDao(connectionSource, PlayersTable.class);
        } catch (SQLException e) {
            instance.getLogger().warning(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(instance);
        }
    }

    public int getCount(String player, String group) {
        try {
            List<PlayersTable> results = playersTables.queryBuilder()
                    .where()
                    .eq("player", player)
                    .and()
                    .eq("group", group)
                    .query();

            if (!results.isEmpty()) {
                return results.get(0).getCount();
            }
        } catch (SQLException e) {
            instance.getLogger().warning(e.getMessage());
        }
        return 0;
    }

    public void setCount(String player, String group, int count) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () ->{

            try {
                List<PlayersTable> results = playersTables.queryBuilder()
                        .where()
                        .eq("player", player)
                        .and()
                        .eq("group", group)
                        .query();
                PlayersTable playersTable = null;
                if(!results.isEmpty()) playersTable = results.get(0);
                if (playersTable == null) {
                    playersTable = new PlayersTable();
                    playersTable.setPlayer(player);
                    playersTable.setCount(count);
                    playersTable.setGroup(group);
                    playersTables.create(playersTable);
                } else {
                    UpdateBuilder<PlayersTable, String> updateBuilder = playersTables.updateBuilder();
                    updateBuilder.updateColumnValue("count", count);
                    updateBuilder.where().eq("player", player).and().eq("group", group);
                    updateBuilder.update();
                }
            } catch (SQLException e) {
                instance.getLogger().warning(e.getMessage());
            }
        });
    }

    public void delAllKey() {
        try {
            playersTables.deleteBuilder().delete();
        } catch (SQLException e) {
            instance.getLogger().warning(e.getMessage());
        }
    }

    public void close() {
        try {
            connectionSource.close();
        } catch (Exception e) {
            instance.getLogger().warning(e.getMessage());
        }
    }

}
