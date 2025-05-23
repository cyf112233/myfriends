package byd.cxkcxkckx.MyFriends.func;

import byd.cxkcxkckx.MyFriends.MyFriends;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettings {
    private final MyFriends plugin;
    private final Map<UUID, YamlConfiguration> playerConfigs;
    private final File dataFolder;

    public PlayerSettings(MyFriends plugin) {
        this.plugin = plugin;
        this.playerConfigs = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "player_settings");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void loadPlayerSettings(Player player) {
        UUID uuid = player.getUniqueId();
        File configFile = new File(dataFolder, uuid.toString() + ".yml");
        
        if (!configFile.exists()) {
            // 创建默认设置
            YamlConfiguration config = new YamlConfiguration();
            // 可见性设置
            config.set("visibility.show-own-fox", true);
            config.set("visibility.show-others-fox", true);
            config.set("visibility.others-can-see-my-fox", true);
            // 红石设置
            config.set("redstone.can-trigger", false);
            // 效果设置
            config.set("effects.show-teleport-effects", true);
            config.set("effects.show-teleport-message", true);
            config.set("effects.show-storage-message", true);
            config.set("effects.show-death-message", true);
            
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().warning("保存玩家 " + player.getName() + " 的默认设置时出错: " + e.getMessage());
            }
        }
        
        // 加载设置
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        playerConfigs.put(uuid, config);
    }

    public void savePlayerSettings(Player player) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration config = playerConfigs.get(uuid);
        if (config != null) {
            try {
                config.save(new File(dataFolder, uuid.toString() + ".yml"));
            } catch (IOException e) {
                plugin.getLogger().warning("保存玩家 " + player.getName() + " 的设置时出错: " + e.getMessage());
            }
        }
    }

    public void unloadPlayerSettings(Player player) {
        savePlayerSettings(player);
        playerConfigs.remove(player.getUniqueId());
    }

    public boolean getBoolean(Player player, String path, boolean defaultValue) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration config = playerConfigs.get(uuid);
        if (config == null) {
            loadPlayerSettings(player);
            config = playerConfigs.get(uuid);
        }
        return config != null ? config.getBoolean(path, defaultValue) : defaultValue;
    }

    public void setBoolean(Player player, String path, boolean value) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration config = playerConfigs.get(uuid);
        if (config == null) {
            loadPlayerSettings(player);
            config = playerConfigs.get(uuid);
        }
        if (config != null) {
            config.set(path, value);
            savePlayerSettings(player);
        }
    }

    public void reloadAllSettings() {
        // 保存所有在线玩家的设置
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            savePlayerSettings(player);
        }
        playerConfigs.clear();
        
        // 重新加载所有在线玩家的设置
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerSettings(player);
        }
    }
} 