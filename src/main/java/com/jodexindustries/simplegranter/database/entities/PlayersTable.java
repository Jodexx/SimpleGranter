package com.jodexindustries.simplegranter.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "players")
public class PlayersTable {
    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @DatabaseField(columnName = "player")
    private String player;
    @DatabaseField(columnName = "group")
    private String group;
    @DatabaseField(defaultValue = "0")
    private int count;
}
