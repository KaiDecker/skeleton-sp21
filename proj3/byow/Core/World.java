package byow.Core;

import byow.Core.Maps.Road;
import byow.Core.Maps.Room;
import byow.Core.Maps.Wall;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;

/**
 * 表现世界的对象
 *
 * @author Kai Decker
 */

public class World implements Serializable {
    /* 世界的宽和高 */
    private final int width;
    private final int height;

    /* 二维数组，表示地图上的格子 */
    private TETile[][] tiles;
    /* 入口和出口坐标 */
    private Point entry;
    private Point exit;

    /**
     * 根据给定宽高创建一个空的 TETile[][]
     *
     * @param w     给定的宽度
     * @param h     给定的高度
     */
    World(int w, int h) {
        width = w;
        height = h;
        tiles = new TETile[w][h];
    }

    public void initializeWorld(long seed) {
        Variables v = new Variables(seed);
        /* 先将世界填为空白 */
        fillWithNothing();
        /* 以下为使用随机构造世界 */
        Room.createRooms(this, v);
        Wall.createWalls(this);
        Road.createRoad(this, v);

        initializeAreas(v);
        Wall.findConnection(this, v);
        Wall.connectAreas(this, v);
        Road.removeDeadEnds(this);
        Wall.buildWallNearUnit(this);
        Wall.creatEntryAndExit(this, v);
    }

    private void initializeAreas(Variables v) {
        v.getRoot().clear();
        Road.addRoadsToArea(this, v);
        Room.addRoomsToArea(v);
        v.setMainArea(Room.getRandomRoom(v));
    }

    public void fillWithNothing() {
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /**
     * 克隆世界
     *
     * @return 克隆的世界
     */
    @Override
    public World clone() {
        World ret = new World(width, height);
        ret.tiles = TETile.copyOf(tiles);
        ret.entry = entry;
        ret.exit = exit;
        return ret;
    }

    /**
     * @return 入口坐标
     */
    public Point getEntry() {
        return entry;
    }

    /**
     * 修改入口坐标
     *
     * @param entry 入口坐标
     */
    public void setEntry(Point entry) {
        this.entry = entry;
    }

    /**
     * @return 出口坐标
     */
    public Point getExit() {
        return exit;
    }

    /**
     * 修改出口坐标
     *
     * @param exit 出口坐标
     */
    public void setExit(Point exit) {
        this.exit = exit;
    }

    /**
     * @return 宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return 高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return TETile[][] 数组
     */
    public TETile[][] getTiles() {
        return tiles;
    }

    /**
     * @param w 宽度
     * @param v Variables 对象
     * @return 随机 X 坐标
     */
    public int getRandomX(int w, Variables v) {
        return v.getRANDOM().nextInt((width - w - 1) / 2) * 2 + 1;
    }

    /**
     * @param h 高度
     * @param v Variables 对象
     * @return 随机 Y 坐标
     */
    public int getRandomY(int h, Variables v) {
        return v.getRANDOM().nextInt((height - h - 1) / 2) * 2 + 1;
    }

    /* 以下为地形判断方法 */

    public boolean isNothing(int x, int y) {
        return tiles[x][y].equals(Tileset.NOTHING);
    }

    public boolean isWall(int x, int y) {
        return tiles[x][y].equals(Tileset.WALL);
    }

    public boolean isRoom(int x, int y) {
        return tiles[x][y].equals(Tileset.ROOM);
    }

    public boolean isRoad(int x, int y) {
        return tiles[x][y].equals(Tileset.FLOOR);
    }

    public boolean isUnit(int x, int y) {
        return isRoom(x, y) || isRoad(x, y) || isDoor(x, y);
    }

    public boolean isDoor(int x, int y) {
        return tiles[x][y].equals(Tileset.UNLOCKED_DOOR);
    }
}
