package byd.cxkcxkckx.MyFriends.func;

import byd.cxkcxkckx.MyFriends.MyFriends;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventPriority;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FoxModule implements Listener {
    private final MyFriends plugin;
    private final Map<UUID, UUID> playerFoxMap = new HashMap<>(); // 玩家UUID -> 狐狸UUID
    private final FoxStorage storage;
    private final int TELEPORT_DISTANCE;
    private final int MAX_SEARCH_RADIUS;
    private int taskId = -1;

    public FoxModule(MyFriends plugin) {
        this.plugin = plugin;
        this.storage = new FoxStorage(plugin);
        // 从配置文件加载设置
        this.TELEPORT_DISTANCE = plugin.getConfig().getInt("fox.teleport-distance", 15);
        this.MAX_SEARCH_RADIUS = plugin.getConfig().getInt("fox.search-radius", 10);
        // 启动定时任务，每2秒检查一次狐狸位置
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::checkFoxLocations, 20L, 40L).getTaskId();
    }

    private void checkFoxLocations() {
        for (Map.Entry<UUID, UUID> entry : new HashMap<>(playerFoxMap).entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            Fox fox = (Fox) Bukkit.getEntity(entry.getValue());
            if (fox == null || fox.isDead()) {
                cleanupDuplicateFoxes(player);
                if (!playerFoxMap.containsKey(player.getUniqueId())) {
                    spawnFox(player);
                }
                continue;
            }

            if (!fox.getLocation().getChunk().isLoaded()) {
                cleanupDuplicateFoxes(player);
                if (!playerFoxMap.containsKey(player.getUniqueId())) {
                    spawnFox(player);
                }
                continue;
            }

            double distance = fox.getLocation().distance(player.getLocation());
            
            // 如果距离超过传送距离，直接传送
            if (distance > TELEPORT_DISTANCE) {
                Location safeLoc = findSafeLocation(player.getLocation());
                fox.teleport(safeLoc);
                
                // 显示传送效果
                if (plugin.getConfig().getBoolean("fox.show-teleport-effects", true)) {
                    fox.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, safeLoc, 20, 0.5, 0.5, 0.5, 0);
                    fox.getWorld().playSound(safeLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
                }
                
                // 显示传送消息
                if (plugin.getConfig().getBoolean("fox.show-teleport-message", true)) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                     plugin.getLanguageManager().getMessage("fox.teleport.particle"));
                }
            }
        }
    }

    private void spawnFox(Player player) {
        // 新增：如果玩家设置为不显示自己的狐狸，直接不生成
        if (!plugin.getPlayerSettings().getBoolean(player, "visibility.show-own-fox", true)) {
            return;
        }
        // 在生成新狐狸之前，先检查并清理重复的狐狸
        cleanupDuplicateFoxes(player);
        
        Location loc = player.getLocation();
        Fox fox = loc.getWorld().spawn(loc, Fox.class);
        
        // 设置狐狸属性
        fox.setCustomName(plugin.getLanguageManager().getMessage("fox.name.own"));
        fox.setCustomNameVisible(true);
        if (fox instanceof Tameable) {
            ((Tameable) fox).setTamed(true);
            ((Tameable) fox).setOwner(player);
        }
        fox.setInvulnerable(true);
        fox.setAI(true); // 启用AI，允许自由移动
        fox.setCanPickupItems(false);
        
        // 设置最大生命值
        if (fox.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            fox.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        }
        fox.setHealth(20.0);
        
        // 设置狐狸的AI
        setupFoxAI(fox);
        
        // 保存狐狸和玩家的绑定关系
        fox.getPersistentDataContainer().set(plugin.getKey("owner"), PersistentDataType.STRING, player.getUniqueId().toString());
        playerFoxMap.put(player.getUniqueId(), fox.getUniqueId());
        
        // 发送消息
        player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                          plugin.getLanguageManager().getMessage("fox.spawn"));
    }

    private void setupFoxAI(Fox fox) {
        // 设置基本属性
        fox.setAware(true); // 让狐狸感知周围环境
        fox.setCanPickupItems(false); // 禁止拾取物品
        
        // 设置 AI 目标
        fox.setAI(true); // 启用 AI
        
        // 设置属性
        AttributeInstance followRange = fox.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
        if (followRange != null) {
            followRange.setBaseValue(32.0); // 设置跟随范围
        }
        
        // 标记为被动实体
        NamespacedKey passiveKey = new NamespacedKey(plugin, "passive_fox");
        fox.getPersistentDataContainer().set(passiveKey, PersistentDataType.BYTE, (byte) 1);
    }

    private Location findSafeLocation(Location center) {
        List<Location> safeLocations = new ArrayList<>();
        
        // 在玩家周围搜索安全位置
        for (int x = -MAX_SEARCH_RADIUS; x <= MAX_SEARCH_RADIUS; x++) {
            for (int z = -MAX_SEARCH_RADIUS; z <= MAX_SEARCH_RADIUS; z++) {
                Location loc = center.clone().add(x, 0, z);
                // 从当前位置向下搜索地面
                Location ground = findGround(loc);
                if (ground != null && isSafeLocation(ground)) {
                    safeLocations.add(ground);
                }
            }
        }

        if (safeLocations.isEmpty()) {
            // 如果找不到安全位置，返回玩家位置
            return center;
        }

        // 找到距离玩家最近的安全位置
        Location nearest = safeLocations.get(0);
        double minDistance = nearest.distanceSquared(center);
        
        for (Location loc : safeLocations) {
            double distance = loc.distanceSquared(center);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = loc;
            }
        }

        return nearest;
    }

    private Location findGround(Location loc) {
        // 从当前位置向下搜索地面
        for (int y = 0; y > -10; y--) {
            Location check = loc.clone().add(0, y, 0);
            Block block = check.getBlock();
            if (!block.getType().isAir() && 
                !block.getType().name().contains("WATER") && 
                !block.getType().name().contains("LAVA")) {
                // 找到地面，返回上方一格的位置
                return check.add(0, 1, 0);
            }
        }
        return null;
    }

    private boolean isSafeLocation(Location loc) {
        Block block = loc.getBlock();
        Block blockAbove = loc.clone().add(0, 1, 0).getBlock();
        Block blockBelow = loc.clone().add(0, -1, 0).getBlock();
        
        // 检查是否是安全的位置（不是液体，上方有空间，下方是固体）
        return !block.getType().name().contains("WATER") && 
               !block.getType().name().contains("LAVA") &&
               !blockAbove.getType().name().contains("WATER") && 
               !blockAbove.getType().name().contains("LAVA") &&
               blockAbove.getType().isAir() && 
               !blockBelow.getType().isAir() && 
               !blockBelow.getType().name().contains("WATER") &&
               !blockBelow.getType().name().contains("LAVA");
    }

    public void onDisable() {
        // 取消定时任务
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 如果玩家已经有狐狸伙伴，检查并清理重复的狐狸
        if (playerFoxMap.containsKey(player.getUniqueId())) {
            cleanupDuplicateFoxes(player);
            // 更新所有狐狸的名称显示
            updateFoxNames();
            return;
        }
        spawnFox(player);
        // 更新所有狐狸的名称显示
        updateFoxNames();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID foxUUID = playerFoxMap.get(player.getUniqueId());
        if (foxUUID != null) {
            // 玩家退出时移除狐狸
            Fox fox = (Fox) Bukkit.getEntity(foxUUID);
            if (fox != null) {
                fox.remove();
            }
            playerFoxMap.remove(player.getUniqueId());
            // 清除存储数据
            storage.clearStorage(player);
            // 更新剩余狐狸的名称显示
            updateFoxNames();
        }
    }

    @EventHandler
    public void onFoxDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Fox)) return;
        Fox fox = (Fox) event.getEntity();
        // 检查是否是玩家的伙伴狐狸
        if (fox.getPersistentDataContainer().has(plugin.getKey("owner"), PersistentDataType.STRING)) {
            event.setCancelled(true); // 取消伤害
        }
    }

    @EventHandler
    public void onFoxDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Fox)) return;
        Fox fox = (Fox) event.getEntity();
        String ownerUUID = fox.getPersistentDataContainer().get(plugin.getKey("owner"), PersistentDataType.STRING);
        if (ownerUUID != null) {
            event.setDroppedExp(0);
            event.getDrops().clear();
            
            Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUID));
            if (owner != null && owner.isOnline() && 
                plugin.getConfig().getBoolean("fox.show-death-message", true)) {
                owner.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                plugin.getLanguageManager().getMessage("fox.death.respawn"));
            }
            
            // 延迟1秒后复活狐狸
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player owner2 = Bukkit.getPlayer(UUID.fromString(ownerUUID));
                if (owner2 != null && owner2.isOnline()) {
                    spawnFox(owner2);
                    if (plugin.getConfig().getBoolean("fox.show-death-message", true)) {
                        owner2.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                         plugin.getLanguageManager().getMessage("fox.death.respawned"));
                    }
                }
            }, 20L);
        }
    }

    @EventHandler
    public void onFoxInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Fox)) return;
        Fox fox = (Fox) event.getRightClicked();
        Player player = event.getPlayer();
        
        // 检查是否是玩家的伙伴狐狸
        String ownerUUID = fox.getPersistentDataContainer().get(plugin.getKey("owner"), PersistentDataType.STRING);
        if (ownerUUID != null && ownerUUID.equals(player.getUniqueId().toString())) {
            event.setCancelled(true);
            // 打开存储界面
            storage.openStorage(player);
            if (plugin.getConfig().getBoolean("fox.show-storage-message", true)) {
                player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                                 plugin.getLanguageManager().getMessage("fox.storage.open"));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!event.getView().getTitle().equals(plugin.getLanguageManager().getMessage("fox.storage.title"))) return;
        
        Player player = (Player) event.getPlayer();
        // 保存存储内容
        storage.saveStorage(player, event.getInventory());
        if (plugin.getConfig().getBoolean("fox.show-storage-message", true)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + 
                             plugin.getLanguageManager().getMessage("fox.storage.close"));
        }
    }

    @EventHandler
    public void onFoxTriggerRedstone(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Fox)) return;
        Fox fox = (Fox) event.getEntity();
        String ownerUUID = fox.getPersistentDataContainer().get(plugin.getKey("owner"), PersistentDataType.STRING);
        if (ownerUUID != null) {
            Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUID));
            if (owner != null && owner.isOnline()) {
                // 从玩家配置读取红石设置
                if (!plugin.getPlayerSettings().getBoolean(owner, "redstone.can-trigger", false)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFoxVisibility(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof Fox)) return;
        Fox fox = (Fox) event.getEntity();
        String ownerUUID = fox.getPersistentDataContainer().get(plugin.getKey("owner"), PersistentDataType.STRING);
        if (ownerUUID != null) {
            Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUID));
            if (owner != null && owner.isOnline()) {
                // 从玩家配置读取可见性设置
                if (!plugin.getPlayerSettings().getBoolean(owner, "visibility.show-own-fox", true)) {
                    event.setCancelled(true);
                    return;
                }
                
                // 检查其他玩家是否可以看到这个狐狸
                if (event.getTarget() instanceof Player) {
                    Player target = (Player) event.getTarget();
                    if (!target.getUniqueId().equals(owner.getUniqueId())) {
                        // 从玩家配置读取可见性设置
                        if (!plugin.getPlayerSettings().getBoolean(owner, "visibility.others-can-see-my-fox", true) ||
                            !plugin.getPlayerSettings().getBoolean(target, "visibility.show-others-fox", true)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoxTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Fox) {
            Fox fox = (Fox) event.getEntity();
            // 检查是否是玩家的狐狸
            if (fox.getPersistentDataContainer().has(new NamespacedKey(plugin, "passive_fox"), PersistentDataType.BYTE)) {
                event.setCancelled(true); // 取消目标事件，防止狐狸攻击其他实体
            }
        }
    }

    // 添加一个新方法来检查和清理重复的狐狸
    private void cleanupDuplicateFoxes(Player player) {
        String ownerUUID = player.getUniqueId().toString();
        List<Fox> duplicateFoxes = new ArrayList<>();
        
        // 遍历所有世界中的所有狐狸
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Fox) {
                    Fox fox = (Fox) entity;
                    String foxOwner = fox.getPersistentDataContainer().get(plugin.getKey("owner"), PersistentDataType.STRING);
                    if (ownerUUID.equals(foxOwner)) {
                        duplicateFoxes.add(fox);
                    }
                }
            }
        }
        
        // 如果找到多只狐狸，保留最新生成的一只（即UUID最大的那只）
        if (duplicateFoxes.size() > 1) {
            // 按UUID排序，保留最新的
            duplicateFoxes.sort((f1, f2) -> f2.getUniqueId().compareTo(f1.getUniqueId()));
            
            // 删除除了最新的一只之外的所有狐狸
            for (int i = 1; i < duplicateFoxes.size(); i++) {
                duplicateFoxes.get(i).remove();
            }
            
            // 更新玩家-狐狸映射
            if (!duplicateFoxes.isEmpty()) {
                playerFoxMap.put(player.getUniqueId(), duplicateFoxes.get(0).getUniqueId());
            }
        }
    }

    // 添加一个新方法来更新所有在线玩家看到的狐狸名称
    private void updateFoxNames() {
        for (Map.Entry<UUID, UUID> entry : playerFoxMap.entrySet()) {
            Fox fox = (Fox) Bukkit.getEntity(entry.getValue());
            if (fox == null || fox.isDead()) continue;

            // 对每个在线玩家更新狐狸名称
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId().equals(entry.getKey())) {
                    // 如果是狐狸的主人，显示"我的伙伴"
                    fox.setCustomName(plugin.getLanguageManager().getMessage("fox.name.own"));
                } else {
                    // 如果是其他玩家，显示"xxx的伙伴"
                    Player owner = Bukkit.getPlayer(entry.getKey());
                    if (owner != null) {
                        String name = plugin.getLanguageManager().getMessage("fox.name.other")
                            .replace("%player%", owner.getName());
                        fox.setCustomName(name);
                    }
                }
            }
        }
    }

    public Fox getPlayerFox(Player player) {
        UUID foxUUID = playerFoxMap.get(player.getUniqueId());
        if (foxUUID != null) {
            return (Fox) Bukkit.getEntity(foxUUID);
        }
        return null;
    }

    public FoxStorage getStorage() {
        return storage;
    }

    public void removeAllFoxes() {
        // 遍历所有世界中的所有狐狸
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Fox) {
                    Fox fox = (Fox) entity;
                    // 检查是否是玩家的伙伴狐狸
                    if (fox.getPersistentDataContainer().has(plugin.getKey("owner"), PersistentDataType.STRING)) {
                        // 移除狐狸
                        fox.remove();
                    }
                }
            }
        }
        
        // 清空所有映射
        playerFoxMap.clear();
    }
} 