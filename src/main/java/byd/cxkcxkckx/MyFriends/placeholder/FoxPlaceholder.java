package byd.cxkcxkckx.MyFriends.placeholder;

import byd.cxkcxkckx.MyFriends.MyFriends;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FoxPlaceholder extends PlaceholderExpansion {
    private final MyFriends plugin;

    public FoxPlaceholder(MyFriends plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "myfriends";
    }

    @Override
    public @NotNull String getAuthor() {
        return "cxkcxkckx";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // 获取玩家的狐狸
        Fox fox = plugin.getFoxModule().getPlayerFox(player);
        if (fox == null) {
            return plugin.getLanguageManager().getMessage("fox.status.none");
        }

        // 获取玩家的设置
        boolean showOwnFox = plugin.getPlayerSettings().getBoolean(player, "visibility.show-own-fox", true);
        boolean showOthersFox = plugin.getPlayerSettings().getBoolean(player, "visibility.show-others-fox", true);
        boolean othersCanSeeMyFox = plugin.getPlayerSettings().getBoolean(player, "visibility.others-can-see-my-fox", true);
        boolean canTriggerRedstone = plugin.getPlayerSettings().getBoolean(player, "redstone.can-trigger", false);

        // 根据参数返回不同的状态
        switch (params.toLowerCase()) {
            case "show_own":
                // 是否显示自己的狐狸
                return showOwnFox ? 
                    plugin.getLanguageManager().getMessage("fox.status.enabled") : 
                    plugin.getLanguageManager().getMessage("fox.status.disabled");

            case "show_others":
                // 是否显示他人的狐狸
                return showOthersFox ? 
                    plugin.getLanguageManager().getMessage("fox.status.enabled") : 
                    plugin.getLanguageManager().getMessage("fox.status.disabled");

            case "others_see":
                // 是否允许他人看到自己的狐狸
                return othersCanSeeMyFox ? 
                    plugin.getLanguageManager().getMessage("fox.status.enabled") : 
                    plugin.getLanguageManager().getMessage("fox.status.disabled");

            case "redstone":
                // 是否允许触发红石
                return canTriggerRedstone ? 
                    plugin.getLanguageManager().getMessage("fox.status.enabled") : 
                    plugin.getLanguageManager().getMessage("fox.status.disabled");

            default:
                return "";
        }
    }
} 