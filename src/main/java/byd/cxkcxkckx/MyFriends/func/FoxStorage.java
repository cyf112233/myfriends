package byd.cxkcxkckx.MyFriends.func;

import byd.cxkcxkckx.MyFriends.MyFriends;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoxStorage {
    private final MyFriends plugin;
    private final Map<UUID, ItemStack[]> storageMap = new HashMap<>(); // 玩家UUID -> 存储物品（缓存）
    private static final int INVENTORY_SIZE = 9; // 一行9格
    private static final NamespacedKey STORAGE_KEY;

    static {
        STORAGE_KEY = new NamespacedKey(MyFriends.getInstance(), "fox_storage");
    }

    public FoxStorage(MyFriends plugin) {
        this.plugin = plugin;
    }

    private void loadPlayerStorage(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (!container.has(STORAGE_KEY, PersistentDataType.TAG_CONTAINER)) {
            return;
        }

        PersistentDataContainer storageContainer = container.get(STORAGE_KEY, PersistentDataType.TAG_CONTAINER);
        if (storageContainer == null) {
            return;
        }

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            NamespacedKey slotKey = new NamespacedKey(plugin, "slot_" + i);
            if (storageContainer.has(slotKey, PersistentDataType.STRING)) {
                String itemData = storageContainer.get(slotKey, PersistentDataType.STRING);
                if (itemData != null) {
                    items[i] = deserializeItem(itemData);
                }
            }
        }
        storageMap.put(player.getUniqueId(), items);
    }

    private void savePlayerStorage(Player player) {
        ItemStack[] items = storageMap.get(player.getUniqueId());
        if (items == null) {
            return;
        }

        PersistentDataContainer container = player.getPersistentDataContainer();
        PersistentDataContainer storageContainer = container.getAdapterContext().newPersistentDataContainer();

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (items[i] != null) {
                NamespacedKey slotKey = new NamespacedKey(plugin, "slot_" + i);
                String itemData = serializeItem(items[i]);
                storageContainer.set(slotKey, PersistentDataType.STRING, itemData);
            }
        }

        container.set(STORAGE_KEY, PersistentDataType.TAG_CONTAINER, storageContainer);
    }

    private String serializeItem(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
    }

    private ItemStack deserializeItem(String data) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(data);
            return config.getItemStack("item");
        } catch (Exception e) {
            plugin.getLogger().warning("无法反序列化物品数据：" + e.getMessage());
            return null;
        }
    }

    public void openStorage(Player player) {
        // 如果缓存中没有数据，尝试从玩家存档加载
        if (!storageMap.containsKey(player.getUniqueId())) {
            loadPlayerStorage(player);
        }

        // 获取或创建玩家的存储空间
        ItemStack[] items = storageMap.computeIfAbsent(player.getUniqueId(), k -> new ItemStack[INVENTORY_SIZE]);
        
        // 创建存储界面
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, 
            plugin.getLanguageManager().getMessage("fox.storage.title"));
        
        // 设置物品
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (items[i] != null) {
                inv.setItem(i, items[i]);
            }
        }
        
        // 打开界面
        player.openInventory(inv);
    }

    public void saveStorage(Player player, Inventory inventory) {
        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            items[i] = inventory.getItem(i);
        }
        storageMap.put(player.getUniqueId(), items);
        savePlayerStorage(player);
    }

    public void clearStorage(Player player) {
        storageMap.remove(player.getUniqueId());
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.remove(STORAGE_KEY);
    }

    // 当玩家加入服务器时调用此方法
    public void onPlayerJoin(Player player) {
        loadPlayerStorage(player);
    }

    // 当玩家退出服务器时调用此方法
    public void onPlayerQuit(Player player) {
        savePlayerStorage(player);
        storageMap.remove(player.getUniqueId());
    }
} 