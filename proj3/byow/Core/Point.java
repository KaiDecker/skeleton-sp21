package byow.Core;

import byow.Core.Maps.Room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * 表示地图上的一个坐标 (x, y)
 * 搭配 rank 字段，用作并查集里的节点（按 rank 合并）
 *
 * @author Kai Decker
 */

public class Point implements Serializable {
    /* 表示坐标 */
    private final int x;
    private final int y;
    /* 并查集中的树深度 */
    private int rank;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        rank = 0;
    }

    /**
     * 返回上下左右 4 个相邻格子
     *
     * @param p 坐标点
     * @return 4 个相邻格子坐标
     */
    public static ArrayList<Point> getFourWaysPoints(Point p) {
        return getFourWaysPoints(p.x, p.y);
    }

    /**
     * 返回上下左右 4 个相邻格子
     * 即调用 getEightWaysPoints ，返回前 4 个
     *
     * @param x 坐标点 X 坐标
     * @param y 坐标点 Y 坐标
     * @return 4 个相邻格子坐标
     */
    public static ArrayList<Point> getFourWaysPoints(int x, int y) {
        return new ArrayList<>(getEightWaysPoints(x, y).subList(0, 4));
    }

    /**
     * 返回右、下、左、上、右上、右下、左下、左上 8 个相邻格子
     * @param x 坐标点 X 坐标
     * @param y 坐标点 Y 坐标
     * @return 8 个相邻格子坐标
     */
    public static ArrayList<Point> getEightWaysPoints(int x, int y) {
        ArrayList<Point> ret = new ArrayList<>();
        final int[][] direction =
                new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        for (int i = 0; i < 8; i++) {
            ret.add(new Point(x + direction[i][0], y + direction[i][1]));
        }
        return ret;
    }

    /**
     * 找到连接点两侧的真实房间或道路的坐标
     *
     * @param world 世界对象
     * @param connection 连接点
     * @return 两个坐标点，表示连接点两侧的单位格子（房间或道路）
     */
    public static Point[] getNearPoint(World world, Point connection) {
        int x1 = connection.x + 1;
        int y1 = connection.y;
        int x2 = connection.x - 1;
        int y2 = connection.y;
        if (!world.isRoad(x1, y1) && !world.isRoom(x1, y1)) {
            x1 = connection.x;
            y1 = connection.y + 1;
            x2 = connection.x;
            y2 = connection.y - 1;
        }
        return new Point[]{new Point(x1, y1), new Point(x2, y2)};
    }

    /**
     * @return 秩
     */
    public int getRank() {
        return rank;
    }

    /**
     * 秩加 1
     */
    public void addRank() {
        rank++;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * 把房间内的任意一个格子统一映射到代表房间的一个点，方便用并查集、哈希表存储房间的区域
     *
     * @param world 世界对象
     * @return 如果是房间，返回房间的左下角代表点，否则返回自己
     */
    public Point getCorrectPoint(World world) {
        if (world.isRoom(x, y)) {
            return Room.getBottomLeft(world, this);
        }
        return this;
    }

    /**
     * 看当前点的四个相邻格子中，是否有任何一个格子是主区域的一部分
     *
     * @param world 世界对象
     * @param v Variables 对象
     * @return 如果有一个邻居属于主区域，就返回 true
     */
    public boolean isNearMain(World world, Variables v) {
        for (Point near : getFourWaysPoints(this)) {
            /* 把它变成房间的代表点 */
            if (near.getCorrectPoint(world).isInMainArea(v)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInMainArea(Variables v) {
        HashMap<Point, Point> root = v.getRoot();
        Point mainArea = v.getMainArea();
        return Objects.equals(root.get(this), mainArea)
                || Objects.equals(root.get(root.get(this)), mainArea);
    }

    @Override
    public boolean equals(Object p) {
        if (this == p) {
            return true;
        }
        if (p == null || p.getClass() != this.getClass()) {
            return false;
        }
        return ((Point) p).x == this.x && ((Point) p).y == this.y;
    }

    /**
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return x * 114514 + y;
    }
}
