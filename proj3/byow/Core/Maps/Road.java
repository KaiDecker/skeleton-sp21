package byow.Core.Maps;

import byow.Core.Point;
import byow.Core.Variables;
import byow.Core.World;
import byow.TileEngine.Tileset;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

/**
 * 表示道路的生成
 *
 * @author Kai Decker
 */

public class Road {

    /**
     * 初始化道路
     *
     * @param world 世界对象
     * @param v Variables 对象
     */
    public static void createRoad(World world, Variables v) {
        for (int x = 1; x < world.getWidth() - 1; x++) {
            for (int y = 1; y < world.getHeight() - 1; y++) {
                /* 如果当前位置是道路或者是墙  */
                if (world.isRoad(x, y) || world.isWall(x, y)) {
                    /* 将该位置作为一个点存入 */
                    Point p = new Point(x, y);
                    v.getRoot().put(p, p);
                }
            }
        }
        /* 生成道路 */
        findPath(world, v);
    }

    /**
     * 使用 Kruskal 算法随机生成道路
     *
     * @param world 世界对象
     * @param v Variables 对象
     */
    private static void findPath(World world, Variables v) {
        /* 获取世界中的所有墙壁位置 */
        ArrayList<Point> walls = Wall.getAllWalls(world);
        while (!walls.isEmpty()) {
            int idx = v.getRANDOM().nextInt(walls.size());
            Point wall = walls.get(idx);
            int x = wall.getX();
            int y = wall.getY();
            int x1 = x % 2 == 1 ? x : x + 1;
            int x2 = x % 2 == 1 ? x : x - 1;
            int y1 = x % 2 == 1 ? y + 1 : y;
            int y2 = x % 2 == 1 ? y - 1 : y;
            /* 根据墙壁的位置，选择该墙周围的两个邻近的点 */
            Point unit1 = new Point(x1, y1);
            Point unit2 = new Point(x2, y2);

            var root = v.getRoot();
            /* 尝试将它们连接起来 */
            if (world.isRoad(x1, y1) && world.isRoad(x2, y2)
                    && !isIntersected(unit1, unit2, root)) {
                kruskalUnion(unit1, unit2, root);
                root.put(wall, unit1);
                world.getTiles()[x][y] = Tileset.FLOOR;
            }
            walls.remove(idx);
        }
    }

    /**
     * 查找给定点的根，并进行路径压缩
     */
    private static Point kruskalFind(Point unit, HashMap<Point, Point> root) {
        if (root.get(unit) != unit) {
            root.put(unit, kruskalFind(root.get(unit), root));
        }
        return root.get(unit);
    }

    /**
     * 合并两个集合，并根据树的秩来优化合并操作
     */
    private static void kruskalUnion(Point unit1, Point unit2, HashMap<Point, Point> root) {
        Point root1 = kruskalFind(unit1, root);
        Point root2 = kruskalFind(unit2, root);
        if (unit1.getRank() <= root2.getRank()) {
            root.put(root1, root2);
        } else {
            root.put(root2, root1);
        }
        if (unit1.getRank() == root2.getRank() && !root1.equals(root2)) {
            root2.addRank();
        }
    }

    private static boolean isIntersected(Point p1, Point p2, HashMap<Point, Point> root) {
        return kruskalFind(p1, root).equals(kruskalFind(p2, root));
    }

    /**
     * 将道路添加到指定区域
     * 遍历世界中的每个点，如果该点是道路且未被访问过，则进行广度优先搜索（BFS）
     * 来找出与该点连接的所有道路，并将它们标记为访问过
     *
     * @param world 世界对象
     * @param v Variables 对象
     */
    public static void addRoadsToArea(World world, Variables v) {
        int w = world.getWidth();
        int h = world.getHeight();
        boolean[][] path = new boolean[w][h];

        var areas = v.getAreas();
        var root = v.getRoot();

        for (int x = 1; x < w - 1; x++) {
            for (int y = 1; y < h - 1; y++) {
                if (world.isRoad(x, y) && !path[x][y]) {
                    Point p = new Point(x, y);
                    path[x][y] = true;
                    areas.add(p);
                    root.put(p, p);

                    Point rootP = p;
                    Queue<Point> queue = new ArrayDeque<>();
                    queue.add(p);
                    while (!queue.isEmpty()) {
                        p = queue.poll();

                        for (Point near : Point.getFourWaysPoints(p)) {
                            int nearX = near.getX();
                            int nearY = near.getY();
                            if (world.isRoad(nearX, nearY) && !path[nearX][nearY]) {
                                queue.add(near);
                                root.put(near, rootP);
                                path[nearX][nearY] = true;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 移除死路
     *
     * @param world 世界对象
     */
    public static void removeDeadEnds(World world) {
        boolean done = false;
        while (!done) {
            done = true;
            for (int x = 1; x < world.getWidth() - 1; x++) {
                for (int y = 1; y < world.getHeight() - 1; y++) {
                    if (world.isRoad(x, y) && isDeadEnd(world, x, y)) {
                        world.getTiles()[x][y] = Tileset.NOTHING;
                        done = false;
                    }
                }
            }
        }
    }

    private static boolean isDeadEnd(World world, int x, int y) {
        int count = 0;
        for (Point p : Point.getFourWaysPoints(x, y)) {
            if (world.getTiles()[p.getX()][p.getY()] == Tileset.WALL
                    || world.isNothing(p.getX(), p.getY())) {
                count++;
            }
        }
        return count == 3;
    }
}
