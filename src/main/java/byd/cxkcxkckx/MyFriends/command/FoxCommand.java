package byd.cxkcxkckx.MyFriends.command;

import byd.cxkcxkckx.MyFriends.MyFriends;
import byd.cxkcxkckx.MyFriends.func.FoxModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FoxCommand implements CommandExecutor, TabCompleter {
    private final MyFriends plugin;
    private final FoxModule foxModule;
    private static final List<String> MAIN_COMMANDS = Arrays.asList("storage", "teleport", "settings");
    private static final List<String> SETTINGS_COMMANDS = Arrays.asList("visibility", "redstone");

    public FoxCommand(MyFriends plugin, FoxModule foxModule) {
        this.plugin = plugin;
        this.foxModule = foxModule;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                             plugin.getLanguageManager().getMessage("command.player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("myfriends.fox")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                             plugin.getLanguageManager().getMessage("command.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "storage":
                if (!player.hasPermission("myfriends.fox.storage")) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                     plugin.getLanguageManager().getMessage("command.no-permission"));
                    return true;
                }
                foxModule.getStorage().openStorage(player);
                break;

            case "teleport":
                if (!player.hasPermission("myfriends.fox.teleport")) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                     plugin.getLanguageManager().getMessage("command.no-permission"));
                    return true;
                }
                teleportFox(player);
                break;

            case "settings":
                if (!player.hasPermission("myfriends.fox.settings")) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                     plugin.getLanguageManager().getMessage("command.no-permission"));
                    return true;
                }
                if (args.length == 1) {
                    sendSettingsHelp(player);
                } else {
                    handleSettingsCommand(player, args);
                }
                break;

            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void handleSettingsCommand(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "visibility":
                if (!player.hasPermission("myfriends.fox.settings.visibility")) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                     plugin.getLanguageManager().getMessage("command.no-permission"));
                    return;
                }
                if (args.length == 2) {
                    sendVisibilitySettingsHelp(player);
                } else {
                    handleVisibilitySetting(player, args);
                }
                break;

            case "redstone":
                if (!player.hasPermission("myfriends.fox.settings.redstone")) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                     plugin.getLanguageManager().getMessage("command.no-permission"));
                    return;
                }
                if (args.length == 2) {
                    sendRedstoneSettingsHelp(player);
                } else {
                    handleRedstoneSetting(player, args);
                }
                break;

            default:
                sendSettingsHelp(player);
                break;
        }
    }

    private void handleVisibilitySetting(Player player, String[] args) {
        if (args.length != 4 || !args[2].equals("set")) {
            sendVisibilitySettingsHelp(player);
            return;
        }

        String setting = args[3].toLowerCase();
        boolean value;
        switch (setting) {
            case "show-own":
            case "show-others":
            case "others-can-see":
                value = !plugin.getPlayerSettings().getBoolean(player, "visibility." + setting, true);
                plugin.getPlayerSettings().setBoolean(player, "visibility." + setting, value);
                player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                 plugin.getLanguageManager().getMessage("menu.settings.saved"));
                break;
            default:
                sendVisibilitySettingsHelp(player);
                break;
        }
    }

    private void handleRedstoneSetting(Player player, String[] args) {
        if (args.length != 4 || !args[2].equals("set")) {
            sendRedstoneSettingsHelp(player);
            return;
        }

        String setting = args[3].toLowerCase();
        if (setting.equals("can-trigger")) {
            boolean value = !plugin.getPlayerSettings().getBoolean(player, "redstone.can-trigger", false);
            plugin.getPlayerSettings().setBoolean(player, "redstone.can-trigger", value);
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                             plugin.getLanguageManager().getMessage("menu.settings.saved"));
        } else {
            sendRedstoneSettingsHelp(player);
        }
    }

    private void teleportFox(Player player) {
        Fox fox = foxModule.getPlayerFox(player);
        if (fox != null) {
            fox.teleport(player.getLocation());
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                             plugin.getLanguageManager().getMessage("menu.teleport.success"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                             plugin.getLanguageManager().getMessage("menu.teleport.no-fox"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== 狐狸伙伴命令帮助 ===");
        player.sendMessage("§6/fox §7- 显示此帮助信息");
        player.sendMessage("§6/fox storage §7- 打开狐狸的存储空间");
        player.sendMessage("§6/fox teleport §7- 将狐狸传送到你身边");
        player.sendMessage("§6/fox settings §7- 显示设置帮助");
    }

    private void sendSettingsHelp(Player player) {
        player.sendMessage("§6=== 狐狸伙伴设置帮助 ===");
        player.sendMessage("§6/fox settings visibility §7- 修改可见性设置");
        player.sendMessage("§6/fox settings redstone §7- 修改红石设置");
    }

    private void sendVisibilitySettingsHelp(Player player) {
        player.sendMessage("§6=== 可见性设置帮助 ===");
        player.sendMessage("§6/fox settings visibility set show-own §7- 切换显示自己的狐狸");
        player.sendMessage("§6/fox settings visibility set show-others §7- 切换显示他人的狐狸");
        player.sendMessage("§6/fox settings visibility set others-can-see §7- 切换允许他人看到我的狐狸");
    }

    private void sendRedstoneSettingsHelp(Player player) {
        player.sendMessage("§6=== 红石设置帮助 ===");
        player.sendMessage("§6/fox settings redstone set can-trigger §7- 切换允许狐狸触发红石");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return new ArrayList<>();
        if (!sender.hasPermission("myfriends.fox")) return new ArrayList<>();

        if (args.length == 1) {
            return MAIN_COMMANDS.stream()
                .filter(cmd -> sender.hasPermission("myfriends.fox." + cmd))
                .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("settings")) {
            if (!sender.hasPermission("myfriends.fox.settings")) return new ArrayList<>();
            return SETTINGS_COMMANDS.stream()
                .filter(cmd -> sender.hasPermission("myfriends.fox.settings." + cmd))
                .filter(cmd -> cmd.startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("settings")) {
            if (!sender.hasPermission("myfriends.fox.settings." + args[1].toLowerCase())) return new ArrayList<>();
            return Arrays.asList("set");
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("settings") && args[2].equalsIgnoreCase("set")) {
            switch (args[1].toLowerCase()) {
                case "visibility":
                    if (!sender.hasPermission("myfriends.fox.settings.visibility")) return new ArrayList<>();
                    return Arrays.asList("show-own", "show-others", "others-can-see");
                case "redstone":
                    if (!sender.hasPermission("myfriends.fox.settings.redstone")) return new ArrayList<>();
                    return Arrays.asList("can-trigger");
            }
        }

        return new ArrayList<>();
    }
} 