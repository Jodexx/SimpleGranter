package com.jodexindustries.simplegranter.api;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jodexindustries.simplegranter.SimpleGranter.*;

public class GranterAPI {
    /**
     * Check if the player can give groups.
     * @param player A player who gives
     * @return Boolean
     */
    public boolean isPlayerCanGrantGroups(Player player) {
        String Group = perms.getPrimaryGroup(player);
        return yaml.getConfig().getConfigurationSection("Settings.Groups").getKeys(false).contains(Group);
    }

    /**
     * Check if the group exists
     * @param group Group name
     * @return Boolean
     */
    public boolean isGroupExist(String group) {
        List<String> groupsList = new ArrayList<>();
        Collections.addAll(groupsList, perms.getGroups());
        return groupsList.contains(group);
    }

    /**
     * Get group level
     * @param group Group name
     * @return Group level
     */
    public int getGroupLevel(String group) {
        return yaml.getConfig().getInt("Settings.Levels." + group, 0);
    }

    /**
     * Get the number of groups a group can give.
     * @param senderGroup The group of the giving player
     * @param group The group the player wants to give
     * @return Number of groups
     */
    public int getGroupInConfig(String senderGroup, String group) {
        return yaml.getConfig().getInt("Settings.Groups." + senderGroup + "." + group,0);
    }

    /**
     * Get the number of groups that the player can still give.
     * @param player A player who gives
     * @param group The group the player wants to give
     * @return Number of group usage
     */
    public int getGroupInData(Player player, String group) {
        if(!sql) {
            return yaml.getData().getInt("Players." + player.getName() + "." + group, 0);
        } else {
            return mysql.getCount(player.getName(), group);
        }
    }

    /**
     * Set group usages for player
     * @param player Player, who used grant
     * @param group The group the player wants to give
     * @param count Number of group usage
     */
    public void setGroupUsages(Player player, String group, int count) {
        if(!sql) {
            yaml.getData().set("Players." + player.getName() + "." + group, count);
            yaml.saveData();
        } else {
            mysql.setCount(player.getName(), group, count);
        }
    }
}
