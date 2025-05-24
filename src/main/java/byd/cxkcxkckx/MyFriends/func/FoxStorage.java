package byd.cxkcxkckx.MyFriends.func;

import byd.cxkcxkckx.MyFriends.MyFriends;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoxStorage {
    private final MyFriends plugin;
    private final Map<UUID, ItemStack[]> storageMap = new HashMap<>(); // 玩家UUID -> 存储物品（缓存）
    private static final int INVENTORY_SIZE = 9; // 一行9格
    private final File storageFolder;

    public FoxStorage(MyFriends plugin) {
        this.plugin = plugin;
        this.storageFolder = new File(plugin.getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }

    private File getStorageFile(UUID uuid) {
        return new File(storageFolder, uuid.toString() + ".yml");
    }

    // 将物品数组序列化为Base64字符串
    private String itemStackArrayToBase64(ItemStack[] items) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        
        // 写入物品数量
        dataOutput.writeInt(items.length);
        
        // 写入每个物品
        for (ItemStack item : items) {
            dataOutput.writeObject(item);
        }
        
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    // 将Base64字符串反序列化为物品数组
    private ItemStack[] itemStackArrayFromBase64(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        
        // 读取物品数量
        int size = dataInput.readInt();
        ItemStack[] items = new ItemStack[size];
        
        // 读取每个物品
        for (int i = 0; i < size; i++) {
            items[i] = (ItemStack) dataInput.readObject();
        }
        
        dataInput.close();
        return items;
    }

    private void loadPlayerStorage(Player player) {
        File storageFile = getStorageFile(player.getUniqueId());
        if (!storageFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(storageFile);
        if (!config.contains("items")) {
            return;
        }

        try {
            String itemsData = config.getString("items");
            if (itemsData != null) {
                ItemStack[] items = itemStackArrayFromBase64(itemsData);
                storageMap.put(player.getUniqueId(), items);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("加载玩家 " + player.getName() + " 的存储数据时出错: " + e.getMessage());
        }
    }

    private void savePlayerStorage(Player player) {
        ItemStack[] items = storageMap.get(player.getUniqueId());
        if (items == null) {
            return;
        }

        File storageFile = getStorageFile(player.getUniqueId());
        YamlConfiguration config = new YamlConfiguration();
        
        try {
            String itemsData = itemStackArrayToBase64(items);
            config.set("items", itemsData);
            config.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().warning("保存玩家 " + player.getName() + " 的存储数据时出错: " + e.getMessage());
        }
    }

    public void openStorage(Player player) {
        // 如果缓存中没有数据，尝试从文件加载
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
        File storageFile = getStorageFile(player.getUniqueId());
        if (storageFile.exists()) {
            storageFile.delete();
        }
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