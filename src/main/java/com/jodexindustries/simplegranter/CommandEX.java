package com.jodexindustries.simplegranter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.jodexindustries.simplegranter.SimpleGranter.*;

public class CommandEX implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sendHelp(sender);
        } else {
            if(args[0].equalsIgnoreCase("reload")) {
                if(sender.hasPermission("simplegranter.admin")) {
                    yaml.reload();
                    sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.ReloadConfig")));
                } else {
                    sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.NoPermissions")));
                }
            } else {
                if(args.length >= 2) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Player target = Bukkit.getPlayerExact(args[0]);
                        String group = args[1];
                        String senderGroup = perms.getPrimaryGroup(player);
                        String targetGroup = perms.getPrimaryGroup(target);
                        int groupInConfig = yaml.getConfig().getInt("Settings.Groups." + senderGroup + "." + group,0);
                        int groupCountInData = yaml.getData().getInt("Players." + player.getName() + "." + group, 0);
                        int targetGroupLevel = yaml.getConfig().getInt("Settings.Levels." + targetGroup, 0);
                        int groupLevel = yaml.getConfig().getInt("Settings.Levels." + group, 0);
                        List<String> groupsList = new ArrayList<>();
                        Collections.addAll(groupsList, perms.getGroups());
                        if (target != null) {
                            if(target.getUniqueId() != player.getUniqueId()) {
                                if (yaml.getConfig().getConfigurationSection("Settings.Groups").getKeys(false).contains(senderGroup)) {
                                    // check for group?
                                    if(groupsList.contains(group)) {
                                        if (groupInConfig != 0) {
                                            if (groupCountInData < groupInConfig) {
                                                if(groupLevel > targetGroupLevel) {
                                                    String giveCommand = yaml.getConfig().getString("Settings.GiveCommand")
                                                            .replaceAll("%group%", group)
                                                            .replaceAll("%target%", target.getName());
                                                    String giveBroadCast = yaml.getConfig().getString("Settings.GiveBroadCast")
                                                            .replaceAll("%player%", player.getName())
                                                            .replaceAll("%target%", target.getName())
                                                            .replaceAll("%group%", group)
                                                            .replaceAll("%groupprefix%", chat.getGroupPrefix(player.getWorld(), group));
                                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), giveCommand);
                                                    if (!giveBroadCast.equals("")) {
                                                        Bukkit.broadcastMessage(t.rc(giveBroadCast));
                                                    }
                                                    yaml.getData().set("Players." + player.getName() + "." + group, groupCountInData + 1);
                                                    yaml.saveData();
                                                } else {
                                                    sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.TargetGroupLevelBigger")));
                                                }
                                            } else {
                                                // ліміт видачі вже закінчився
                                                sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.AlreadyIssued")));
                                            }

                                        } else {
                                            // ваша група не може видати таку привілею
                                            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.CannotGrant")));
                                        }
                                    } else {
                                        // такої групи не існує
                                        sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.GroupNotFound")));
                                    }
                                } else {
                                    // ваша група не може видавати ніякі інші
                                    sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.CannotGrantAnyOthers")));
                                }
                            } else {
                                sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.GiveYourself")));
                            }
                        } else {
                            sender.sendMessage(t.rc(yaml.getConfig().getString("Messages.PlayerNotFound")));
                        }
                    } else {
                        // from console
                        sender.sendMessage(t.rc("&cYou can't grant group from console"));
                    }
                } else {
                    sendHelp(sender);
                }
            }
        }
        return false;
    }

    private void sendHelp(CommandSender sender) {
        List<String> stringList = yaml.getConfig().getStringList("Messages.Help");
        sender.sendMessage(t.rc("&eSimpleGranter &fby &c_Jodex__"));
        for(String line : stringList) {
            if(line.startsWith("$admin")) {
                if (sender.hasPermission("simplegranter.admin")) {
                    sender.sendMessage(t.rc(line).replaceFirst("\\$admin", ""));
                }
            } else {
                sender.sendMessage(t.rc(line));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(sender instanceof Player) {
            List<String> list = new ArrayList<>();
            if(args.length == 1) {
                for (Player player: Bukkit.getOnlinePlayers().stream().filter((px) -> px.getName().startsWith(args[0])).collect(Collectors.toList())) {
                    list.add(player.getName());
                }
                return list;
            }
            if (args.length == 2) {
                if(!args[0].equalsIgnoreCase("reload")) {
                    Player player = (Player) sender;
                    String senderGroup = perms.getPrimaryGroup(player);
                    List<String> groups = new ArrayList<>();
                    for (String group : yaml.getConfig().getConfigurationSection("Settings.Groups." + senderGroup).getKeys(false).stream().filter((px) -> px.startsWith(args[1])).collect(Collectors.toList())) {
                        if (yaml.getConfig().getInt("Settings.Groups." + senderGroup + "." + group) > yaml.getData().getInt("Players." + player.getName() + "." + group, 0)) {
                            groups.add(group);
                        }
                    }
                    return groups;
                }
            } else if (args.length >= 3) {
                return new ArrayList<>();
            }
        }
        return null;
    }
}
