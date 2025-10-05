package com.jodexindustries.simplegranter.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "players")
public class PlayersTable {

    @DatabaseField(columnName = "player")
    private String player;

    @DatabaseField(columnName = "group")
    private String group;

    @DatabaseField(defaultValue = "0")
    private int count;

    public void setPlayer(String player) {
        this.player = player;
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
}
