package byd.cxkcxkckx.MyFriends.func;

import byd.cxkcxkckx.MyFriends.MyFriends;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final MyFriends plugin;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private String defaultLanguage = "zh_CN";
    private String currentLanguage = "zh_CN";

    public LanguageManager(MyFriends plugin) {
        this.plugin = plugin;
        loadLanguages();
    }

    private void loadLanguages() {
        // 加载默认语言文件
        loadLanguage("zh_CN");
        loadLanguage("en_US");
        
        // 从配置中读取当前语言设置
        String configLang = plugin.getConfig().getString("language", defaultLanguage);
        if (languages.containsKey(configLang)) {
            currentLanguage = configLang;
        }
    }

    private void loadLanguage(String lang) {
        try {
            // 首先尝试从jar中加载默认语言文件
            InputStream defaultStream = plugin.getResource("lang/" + lang + ".yml");
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
                );
                languages.put(lang, defaultConfig);
            }

            // 然后尝试从插件文件夹加载自定义语言文件
            File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
            if (langFile.exists()) {
                YamlConfiguration customConfig = YamlConfiguration.loadConfiguration(langFile);
                // 合并自定义配置到默认配置
                if (languages.containsKey(lang)) {
                    for (String key : customConfig.getKeys(true)) {
                        languages.get(lang).set(key, customConfig.get(key));
                    }
                } else {
                    languages.put(lang, customConfig);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("加载语言文件 " + lang + " 时出错: " + e.getMessage());
        }
    }

    public String getMessage(String key) {
        return getMessage(key, currentLanguage);
    }

    public String getMessage(String key, String lang) {
        YamlConfiguration langConfig = languages.getOrDefault(lang, languages.get(defaultLanguage));
        String message = langConfig.getString(key);
        if (message == null) {
            plugin.getLogger().warning("未找到语言键: " + key + " 在语言: " + lang);
            return key;
        }
        return message;
    }

    public void setLanguage(String lang) {
        if (languages.containsKey(lang)) {
            currentLanguage = lang;
            plugin.getConfig().set("language", lang);
            plugin.saveConfig();
        }
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
} 