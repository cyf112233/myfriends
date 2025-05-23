package byd.cxkcxkckx.MyFriends.func;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;

public class PathFinder {
    private static final int MAX_SEARCH_DISTANCE = 20; // 最大搜索距离
    private static final int MAX_SEARCH_ITERATIONS = 1000; // 最大搜索迭代次数
    private static final double JUMP_HEIGHT = 1.0; // 最大跳跃高度
    private static final double STEP_HEIGHT = 0.6; // 最大台阶高度

    private final World world;
    private final Location start;
    private final Location target;
    private final Set<Location> closedSet = new HashSet<>();
    private final PriorityQueue<PathNode> openSet = new PriorityQueue<>();
    private final Map<Location, PathNode> nodeMap = new HashMap<>();

    // 方向数组，用于 JPS 搜索
    private static final int[][] DIRECTIONS = {
        {0, 1}, {1, 0}, {0, -1}, {-1, 0},  // 直线方向
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // 对角线方向
    };

    public PathFinder(Location start, Location target) {
        this.world = start.getWorld();
        this.start = start.clone();
        this.target = target.clone();
        // 确保起点和终点在同一世界
        if (!start.getWorld().equals(target.getWorld())) {
            throw new IllegalArgumentException("起点和终点必须在同一世界");
        }
    }

    public List<Location> findPath() {
        // 如果距离太远，直接返回空
        if (start.distance(target) > MAX_SEARCH_DISTANCE) {
            return Collections.emptyList();
        }

        // 初始化起点
        PathNode startNode = new PathNode(start, null, 0, getHeuristic(start));
        openSet.add(startNode);
        nodeMap.put(start, startNode);

        int iterations = 0;
        while (!openSet.isEmpty() && iterations < MAX_SEARCH_ITERATIONS) {
            iterations++;
            PathNode current = openSet.poll();
            
            // 如果到达目标
            if (current.location.distance(target) < 1.5) {
                return reconstructPath(current);
            }

            closedSet.add(current.location);
            
            // 使用 JPS 寻找跳点
            for (int[] dir : DIRECTIONS) {
                Location jumpPoint = jump(current.location, dir[0], dir[1]);
                if (jumpPoint != null && !closedSet.contains(jumpPoint)) {
                    double newCost = current.gCost + current.location.distance(jumpPoint);
                    PathNode neighborNode = nodeMap.get(jumpPoint);
                    
                    if (neighborNode == null) {
                        neighborNode = new PathNode(jumpPoint, current, newCost, getHeuristic(jumpPoint));
                        nodeMap.put(jumpPoint, neighborNode);
                        openSet.add(neighborNode);
                    } else if (newCost < neighborNode.gCost) {
                        neighborNode.parent = current;
                        neighborNode.gCost = newCost;
                        neighborNode.fCost = newCost + neighborNode.hCost;
                        // 重新排序
                        openSet.remove(neighborNode);
                        openSet.add(neighborNode);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private Location jump(Location current, int dx, int dz) {
        int x = current.getBlockX() + dx;
        int z = current.getBlockZ() + dz;
        int y = current.getBlockY();
        
        // 检查是否超出搜索范围
        if (current.distance(new Location(world, x, y, z)) > MAX_SEARCH_DISTANCE) {
            return null;
        }

        // 检查是否是目标点
        Location next = new Location(world, x + 0.5, y, z + 0.5);
        if (next.distance(target) < 1.5) {
            return next;
        }

        // 检查是否是跳点
        if (hasForcedNeighbor(x, y, z, dx, dz)) {
            return next;
        }

        // 如果是直线移动，继续搜索
        if (dx != 0 && dz != 0) {
            // 对角线移动时，检查两个直线方向
            if (jump(next, dx, 0) != null || jump(next, 0, dz) != null) {
                return next;
            }
        }

        // 继续沿当前方向搜索
        return jump(next, dx, dz);
    }

    private boolean hasForcedNeighbor(int x, int y, int z, int dx, int dz) {
        // 检查是否有强制邻居（障碍物后的可行走点）
        if (dx != 0 && dz != 0) {
            // 对角线移动
            boolean walkable1 = isWalkable(x + dx, y, z) && isWalkable(x + dx, y + 1, z);
            boolean walkable2 = isWalkable(x, y, z + dz) && isWalkable(x, y + 1, z + dz);
            boolean blocked1 = !isWalkable(x + dx, y, z + dz) || !isWalkable(x + dx, y + 1, z + dz);
            boolean blocked2 = !isWalkable(x + dx, y, z - dz) || !isWalkable(x + dx, y + 1, z - dz);
            
            return (walkable1 && blocked1) || (walkable2 && blocked2);
        } else {
            // 直线移动
            if (dx != 0) {
                boolean walkable1 = isWalkable(x, y, z + 1) && isWalkable(x, y + 1, z + 1);
                boolean walkable2 = isWalkable(x, y, z - 1) && isWalkable(x, y + 1, z - 1);
                boolean blocked1 = !isWalkable(x + dx, y, z + 1) || !isWalkable(x + dx, y + 1, z + 1);
                boolean blocked2 = !isWalkable(x + dx, y, z - 1) || !isWalkable(x + dx, y + 1, z - 1);
                
                return (walkable1 && blocked1) || (walkable2 && blocked2);
            } else {
                boolean walkable1 = isWalkable(x + 1, y, z) && isWalkable(x + 1, y + 1, z);
                boolean walkable2 = isWalkable(x - 1, y, z) && isWalkable(x - 1, y + 1, z);
                boolean blocked1 = !isWalkable(x + 1, y, z + dz) || !isWalkable(x + 1, y + 1, z + dz);
                boolean blocked2 = !isWalkable(x - 1, y, z + dz) || !isWalkable(x - 1, y + 1, z + dz);
                
                return (walkable1 && blocked1) || (walkable2 && blocked2);
            }
        }
    }

    private boolean isWalkable(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        Material type = block.getType();
        return type.isAir() || 
               type.name().contains("CARPET") || 
               type.name().contains("SLAB") || 
               type.name().contains("STAIRS");
    }

    private double getHeuristic(Location loc) {
        return loc.distance(target);
    }

    private List<Location> reconstructPath(PathNode endNode) {
        List<Location> path = new ArrayList<>();
        PathNode current = endNode;
        while (current != null) {
            path.add(0, current.location);
            current = current.parent;
        }
        return path;
    }

    private static class PathNode implements Comparable<PathNode> {
        final Location location;
        PathNode parent;
        double gCost; // 从起点到当前节点的成本
        final double hCost; // 从当前节点到终点的估计成本
        double fCost; // 总成本 (gCost + hCost)

        PathNode(Location location, PathNode parent, double gCost, double hCost) {
            this.location = location;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }

        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.fCost, other.fCost);
        }
    }
} 