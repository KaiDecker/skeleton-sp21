package byow.Core.Maps;

import byow.Core.Engine;
import byow.Core.Point;
import byow.Core.Variables;
import byow.Core.World;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Set;

import static com.google.common.primitives.Ints.min;

/**
 * 表示房间的生成
 *
 * @author Kai Decker
 */
public class Room {
    /**
     * 尝试生成房间的次数
     */
    private static final int TIMES = min(Engine.HEIGHT, Engine.WIDTH) / 2;
    /**
     * 房间的最小宽度
     */
    private static final int MIN_WIDTH = 5;
    /**
     * 房间的最小高度
     */
    private static final int MIN_HEIGHT = 5;

    public static void createRooms(World world, Variables v) {
        for (int i = 0; i < TIMES; i++) {
            createRoom(world, v);
        }
    }

    private static void createRoom(World world, Variables v) {
        // 随机生成房间的尺寸
        int h = v.getRANDOM().nextInt(3) * 2 + MIN_HEIGHT;
        int w = v.getRANDOM().nextInt(3) * 2 + MIN_WIDTH;

        // 获取随机的位置
        int x = world.getRandomX(w, v);
        int y = world.getRandomY(h, v);
        int count = 0;
        // 检查区域是否为空和是否与现有房间重叠
        while (!world.isNothing(x, y) || isCoveredRoom(world, x, y, w, h)) {
            x = world.getRandomX(w, v);
            y = world.getRandomY(h, v);
            count++;
            if (count == TIMES) {
                return;
            }
        }

        v.getRoomAreas().put(new Point(x, y), new Point(x + w - 1, y + h - 1));

        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                world.getTiles()[i][j] = Tileset.ROOM;
            }
        }
    }

    /**
     * 判断坐标 {x,y} 是否定位到房间
     * 即房间的重叠检测
     */
    private static boolean isCoveredRoom(World world, int x, int y, int w, int h) {
        if (world.isRoom(x, y)) {
            return true;
        }
        if (x == 0) {
            x++;
        }
        if (y == 0) {
            y++;
        }

        for (int i = x - 1; i < x + w + 1; i++) {
            for (int j = y - 1; j < y + h + 1; j++) {
                if (world.isRoom(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addRoomsToArea(Variables v) {
        Set<Point> keys = v.getRoomAreas().keySet();
        v.getAreas().addAll(keys);
        keys.forEach(r -> v.getRoot().put(r, r));
    }

    /**
     * 通过向左下角遍历找到房间的代表点
     */
    public static Point getBottomLeft(World world, Point unit) {
        int x = unit.getX();
        int y = unit.getY();
        while (world.isRoom(x, y)) {
            x--;
        }
        x++;
        while (world.isRoom(x, y)) {
            y--;
        }
        y++;
        return new Point(x, y);
    }

    public static Point getRandomRoom(Variables v) {
        ArrayList<Point> rooms = new ArrayList<>(v.getRoomAreas().keySet());
        Point room = rooms.get(v.getRANDOM().nextInt(rooms.size()));
        Point mainArea = v.getMainArea();
        if (mainArea != null) {
            while (room.equals(mainArea)) {
                room = rooms.get(v.getRANDOM().nextInt(rooms.size()));
            }
        }
        return room;
    }
}
