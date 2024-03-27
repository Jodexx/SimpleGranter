package com.jodexindustries.simplegranter.api;

import com.jodexindustries.simplegranter.fields.PermissionDriver;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.jodexindustries.simplegranter.SimpleGranter.*;

public class GranterAPI {
    /**
     * Get player group
     * @param player Bukkit Player
     * @return Group name
     */
    public String getPlayerGroup(Player player) {
        String group = "";
        if(permissionDriver == PermissionDriver.vault) group = perms.getPrimaryGroup(player);
        if(permissionDriver == PermissionDriver.luckperms) group = luckPerms.getPlayerAdapter(Player.class).getUser(player).getPrimaryGroup();
        return group;
    }
    /**
     * Check if the player can give groups.
     * @param player A player who gives
     * @return Boolean
     */
    public boolean isPlayerCanGrantGroups(Player player) {
        String group = getPlayerGroup(player);
        return yaml.getConfig().getConfigurationSection("Settings.Groups").getKeys(false).contains(group);
    }

    /**
     * Check if the group exists
     * @param group Group name
     * @return Boolean
     */
    public boolean isGroupExist(String group) {
        List<String> groupsList = new ArrayList<>();
        if(permissionDriver == PermissionDriver.vault) Collections.addAll(groupsList, perms.getGroups());
        if(permissionDriver == PermissionDriver.luckperms) luckPerms.getGroupManager().getLoadedGroups().stream().map(Group::getName).collect(Collectors.toList());
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
