package com.jodexindustries.simplegranter.api;

import com.jodexindustries.simplegranter.SimpleGranter;
import com.jodexindustries.simplegranter.data.PermissionDriver;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class GranterAPI {

    private final SimpleGranter plugin;

    public GranterAPI(SimpleGranter plugin) {
        this.plugin = plugin;
    }

    public String getGroupPrefix(String group) {
        String prefix = null;
        if (plugin.permissionDriver() == PermissionDriver.VAULT) {
            prefix = plugin.chat().getGroupPrefix(Bukkit.getWorlds().get(0), group);
        } else {
            Group lpGroup = plugin.luckPerms().getGroupManager().getGroup(group);
            if (lpGroup != null) prefix = lpGroup.getCachedData().getMetaData().getPrefix();
        }

        return prefix;
    }

    /**
     * Get player group
     *
     * @param player Bukkit Player
     * @return Group name
     */
    public String getPlayerGroup(Player player) {
        String group = "";
        if (plugin.permissionDriver() == PermissionDriver.VAULT) group = plugin.perms().getPrimaryGroup(player);
        if (plugin.permissionDriver() == PermissionDriver.LUCKPERMS)
            group = plugin.luckPerms().getPlayerAdapter(Player.class).getUser(player).getPrimaryGroup();
        return group;
    }

    /**
     * Check if the player can give groups.
     *
     * @param player A player who gives
     * @return Boolean
     */
    public boolean isPlayerCanGrantGroups(Player player) {
        String group = getPlayerGroup(player);
        return plugin.yaml().getConfig().getConfigurationSection("Settings.Groups").getKeys(false).contains(group);
    }

    /**
     * Check if the group exists
     *
     * @param group Group name
     * @return Boolean
     */
    public boolean isGroupExist(String group) {
        switch (plugin.permissionDriver()) {
            case VAULT:
                return Arrays.asList(plugin.perms().getGroups()).contains(group);
            case LUCKPERMS:
                return plugin.luckPerms().getGroupManager().getLoadedGroups()
                        .stream()
                        .map(Group::getName)
                        .anyMatch(group::equals);
        }
        return false;
    }

    /**
     * Get group level
     *
     * @param group Group name
     * @return Group level
     */
    public int getGroupLevel(String group) {
        return plugin.yaml().getConfig().getInt("Settings.Levels." + group, 0);
    }

    /**
     * Get the number of groups a group can give.
     *
     * @param senderGroup The group of the giving player
     * @param group       The group the player wants to give
     * @return Number of groups
     */
    public int getGroupInConfig(String senderGroup, String group) {
        return plugin.yaml().getConfig().getInt("Settings.Groups." + senderGroup + "." + group, 0);
    }

    /**
     * Get the number of groups that the player can still give.
     *
     * @param player A player who gives
     * @param group  The group the player wants to give
     * @return Number of group usage
     */
    public int getGroupInData(String player, String group) {
        return plugin.sql() ? plugin.mysql().getCount(player, group) :
                plugin.yaml().getCount(player, group);
    }

    /**
     * Set group usages for player
     *
     * @param player Player, who used grant
     * @param group  The group the player wants to give
     * @param count  Number of group usage
     */
    public void setGroupUsages(String player, String group, int count) {
        if (!plugin.sql()) {
            plugin.yaml().setCount(player, group, count);
        } else {
            plugin.mysql().setCount(player, group, count);
        }
    }
}
