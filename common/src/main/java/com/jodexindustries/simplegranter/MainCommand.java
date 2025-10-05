package com.jodexindustries.simplegranter;

import com.jodexindustries.simplegranter.api.GranterAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {
    
    private final SimpleGranter plugin;
    private final GranterAPI api;
    
    public MainCommand(SimpleGranter plugin) {
        this.plugin = plugin;
        this.api = plugin.api();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return false;
        }

        // /simplegranter reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("simplegranter.admin")) {
                sender.sendMessage(rc(plugin.yaml().getMessage("NoPermissions")));
                return false;
            }

            plugin.yaml().load();
            plugin.loadDriver();
            sender.sendMessage(rc(plugin.yaml().getMessage("ReloadConfig")));
            return false;
        }

        // /simplegranter <player> <group>
        if (args.length < 2) {
            sendHelp(sender);
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(rc("&cYou can't grant group from console"));
            return false;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayerExact(args[0]);
        String group = args[1];

        if (target == null) {
            sender.sendMessage(rc(plugin.yaml().getMessage("PlayerNotFound")));
            return false;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage(rc(plugin.yaml().getMessage("GiveYourself")));
            return false;
        }

        if (!api.isPlayerCanGrantGroups(player)) {
            sender.sendMessage(rc(plugin.yaml().getMessage("CannotGrantAnyOthers")));
            return false;
        }

        if (!api.isGroupExist(group)) {
            sender.sendMessage(rc(plugin.yaml().getMessage("GroupNotFound")));
            return false;
        }

        String senderGroup = api.getPlayerGroup(player);
        String targetGroup = api.getPlayerGroup(target);

        int groupInConfig = api.getGroupInConfig(senderGroup, group);
        int groupCountInData = api.getGroupInData(player.getName(), group);
        int targetGroupLevel = api.getGroupLevel(targetGroup);
        int groupLevel = api.getGroupLevel(group);

        if (groupInConfig == 0) {
            sender.sendMessage(rc(plugin.yaml().getMessage("CannotGrant")));
            return false;
        }

        if (groupCountInData >= groupInConfig) {
            sender.sendMessage(rc(plugin.yaml().getMessage("AlreadyIssued")));
            return false;
        }

        if (groupLevel <= targetGroupLevel) {
            sender.sendMessage(rc(plugin.yaml().getMessage("TargetGroupLevelBigger")));
            return false;
        }

        String giveCommand = plugin.yaml().getConfig().getString("Settings.GiveCommand")
                .replaceAll("%group%", group)
                .replaceAll("%target%", target.getName());

        for (String broadcast : plugin.yaml().getConfig().getStringList("Settings.GiveBroadCast")) {
            String message = rc(broadcast)
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%target%", target.getName())
                    .replaceAll("%group%", group)
                    .replaceAll("%groupprefix%", api.getGroupPrefix(group));

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(message);
            }
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), giveCommand);
        api.setGroupUsages(player.getName(), group, groupCountInData + 1);

        return false;
    }

    private static String rc(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private void sendHelp(CommandSender sender) {
        List<String> helpList = plugin.yaml().getConfig().getStringList("Messages.Help");
        sender.sendMessage(rc("&eSimpleGranter &fby &c_Jodex__"));

        for (String line : helpList) {
            if (line.startsWith("$admin")) {
                if (sender.hasPermission("simplegranter.admin")) {
                    sender.sendMessage(rc(line.replaceFirst("\\$admin", "")));
                }
            } else {
                sender.sendMessage(rc(line));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            String senderGroup = api.getPlayerGroup(player);
            List<String> result = new ArrayList<>();

            if (plugin.yaml().getConfig().getConfigurationSection("Settings.Groups." + senderGroup) != null) {
                plugin.yaml().getConfig()
                        .getConfigurationSection("Settings.Groups." + senderGroup)
                        .getKeys(false)
                        .stream()
                        .filter(group -> group.toLowerCase().startsWith(args[1].toLowerCase()))
                        .forEach(group -> {
                            if (api.getGroupInConfig(senderGroup, group) > api.getGroupInData(player.getName(), group)) {
                                result.add(group);
                            }
                        });
            }
            return result;
        }

        return new ArrayList<>();
    }
}
