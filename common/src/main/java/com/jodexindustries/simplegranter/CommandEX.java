package com.jodexindustries.simplegranter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jodexindustries.simplegranter.SimpleGranter.api;
import static com.jodexindustries.simplegranter.SimpleGranter.t;
import static com.jodexindustries.simplegranter.SimpleGranter.yaml;

public class CommandEX implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return false;
        }

        // /simplegranter reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("simplegranter.admin")) {
                sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.NoPermissions")));
                return false;
            }

            yaml.reload();
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.ReloadConfig")));
            return false;
        }

        // /simplegranter <player> <group>
        if (args.length < 2) {
            sendHelp(sender);
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(t.rc("&cYou can't grant group from console"));
            return false;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayerExact(args[0]);
        String group = args[1];

        if (target == null) {
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.PlayerNotFound")));
            return false;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.GiveYourself")));
            return false;
        }

        if (!api.isPlayerCanGrantGroups(player)) {
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.CannotGrantAnyOthers")));
            return false;
        }

        if (!api.isGroupExist(group)) {
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.GroupNotFound")));
            return false;
        }

        String senderGroup = api.getPlayerGroup(player);
        String targetGroup = api.getPlayerGroup(target);

        int groupInConfig = api.getGroupInConfig(senderGroup, group);
        int groupCountInData = api.getGroupInData(player, group);
        int targetGroupLevel = api.getGroupLevel(targetGroup);
        int groupLevel = api.getGroupLevel(group);

        if (groupInConfig == 0) {
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.CannotGrant")));
            return false;
        }

        if (groupCountInData >= groupInConfig) {
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.AlreadyIssued")));
            return false;
        }

        if (groupLevel <= targetGroupLevel) {
            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.TargetGroupLevelBigger")));
            return false;
        }

        String giveCommand = yaml.getConfig().getString("Settings.GiveCommand")
                .replaceAll("%group%", group)
                .replaceAll("%target%", target.getName());

        for (String broadcast : yaml.getConfig().getStringList("Settings.GiveBroadCast")) {
            String message = t.rc(broadcast)
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%target%", target.getName())
                    .replaceAll("%group%", group)
                    .replaceAll("%groupprefix%", api.getGroupPrefix(group));

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(message);
            }
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), giveCommand);
        api.setGroupUsages(player, group, groupCountInData + 1);

        return false;
    }

    private void sendHelp(CommandSender sender) {
        List<String> helpList = yaml.getConfig().getStringList("Messages.Help");
        sender.sendMessage(t.rc("&eSimpleGranter &fby &c_Jodex__"));

        for (String line : helpList) {
            if (line.startsWith("$admin")) {
                if (sender.hasPermission("simplegranter.admin")) {
                    sender.sendMessage(t.rc(line.replaceFirst("\\$admin", "")));
                }
            } else {
                sender.sendMessage(t.rc(line));
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

            if (yaml.getConfig().getConfigurationSection("Settings.Groups." + senderGroup) != null) {
                yaml.getConfig()
                        .getConfigurationSection("Settings.Groups." + senderGroup)
                        .getKeys(false)
                        .stream()
                        .filter(group -> group.toLowerCase().startsWith(args[1].toLowerCase()))
                        .forEach(group -> {
                            if (api.getGroupInConfig(senderGroup, group) > api.getGroupInData(player, group)) {
                                result.add(group);
                            }
                        });
            }
            return result;
        }

        return new ArrayList<>();
    }
}
