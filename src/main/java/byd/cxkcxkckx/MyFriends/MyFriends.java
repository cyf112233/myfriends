package byd.cxkcxkckx.MyFriends;
        
import byd.cxkcxkckx.MyFriends.func.FoxModule;
import byd.cxkcxkckx.MyFriends.func.LanguageManager;
import byd.cxkcxkckx.MyFriends.command.FoxCommand;
import byd.cxkcxkckx.MyFriends.func.PlayerSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import byd.cxkcxkckx.MyFriends.placeholder.FoxPlaceholder;

public class MyFriends extends JavaPlugin implements Listener {
    private FoxModule foxModule;
    private LanguageManager languageManager;
    private FoxCommand foxCommand;
    private PlayerSettings playerSettings;

    public static MyFriends getInstance() {
        return (MyFriends) JavaPlugin.getPlugin(MyFriends.class);
    }

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化语言管理器
        languageManager = new LanguageManager(this);
        
        // 初始化玩家设置管理器
        playerSettings = new PlayerSettings(this);
        
        // 初始化狐狸模块
        foxModule = new FoxModule(this);
        
        // 注册命令
        foxCommand = new FoxCommand(this, foxModule);
        getCommand("fox").setExecutor(foxCommand);
        getCommand("fox").setTabCompleter(foxCommand);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(foxModule, this);
        
        // 清除所有现有的伙伴狐狸
        foxModule.removeAllFoxes();
        
        // 加载所有在线玩家的设置
        for (Player player : getServer().getOnlinePlayers()) {
            playerSettings.loadPlayerSettings(player);
        }
        
        // 注册 PlaceholderAPI 扩展
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FoxPlaceholder(this).register();
            getLogger().info("已注册 PlaceholderAPI 扩展");
        }
        
        getLogger().info("MyFriends 插件已启用！");
    }

    @Override
    public void onDisable() {
        // 清除所有伙伴狐狸
        foxModule.removeAllFoxes();
        
        // 保存所有在线玩家的设置和存储数据
        for (Player player : getServer().getOnlinePlayers()) {
            playerSettings.unloadPlayerSettings(player);
            foxModule.getStorage().onPlayerQuit(player);
        }
        
        getLogger().info("MyFriends 插件已禁用！");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerSettings.loadPlayerSettings(player);
        foxModule.getStorage().onPlayerJoin(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerSettings.unloadPlayerSettings(player);
        foxModule.getStorage().onPlayerQuit(player);
    }

    public NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }

    public FoxModule getFoxModule() {
        return foxModule;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }
}
