package byow.Core;

import byow.Core.Character.Characters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * 表示游戏当前状态和生成世界时用到的一堆临时数据
 *
 * @author Kai Decker
 */

public class Variables implements Serializable {
    /* 基础世界地图 */
    World world;
    /* 每一步移动都会从 world 克隆一份出来，然后在上面把角色画进去，即当前显示用的世界 */
    World tempWorld;
    /* 管理所有角色的类 */
    Characters characters;
    /* 用于生成随机数 */
    private Random RANDOM;
    /* 存区域之间的连接点 */
    private ArrayList<Point> connections;
    private HashMap<Point, Point> roomAreas;
    private HashMap<Point, Point> root;
    private HashSet<Point> areas;
    private Point mainArea;

    Variables(long seed) {
        connections = new ArrayList<>();
        roomAreas = new HashMap<>();
        root = new HashMap<>();
        areas = new HashSet<>();
        mainArea = null;
        RANDOM = new Random(seed);
    }

    Variables() {
        world = new World(Engine.WIDTH - 3, Engine.HEIGHT - 3);
    }

    /* 以下为各个字段的访问接口 */

    public Random getRANDOM() {
        return RANDOM;
    }

    public ArrayList<Point> getConnections() {
        return connections;
    }

    public Characters getCharacters() {
        return characters;
    }

    public HashMap<Point, Point> getRoomAreas() {
        return roomAreas;
    }

    public HashMap<Point, Point> getRoot() {
        return root;
    }

    public HashSet<Point> getAreas() {
        return areas;
    }

    public Point getMainArea() {
        return mainArea;
    }

    public void setMainArea(Point mainArea) {
        this.mainArea = mainArea;
    }

    public World getTempWorld() {
        return tempWorld;
    }

    public World getWorld() {
        return world;
    }
}
