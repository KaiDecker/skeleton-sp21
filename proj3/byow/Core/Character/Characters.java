package byow.Core.Character;

import byow.Core.Point;
import byow.Core.World;
import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.util.ArrayList;

public class Characters implements Serializable {
    private final Frisk frisk;
    /* 存储 NPC 对象 */
    private final ArrayList<Point> npc = new ArrayList<>();

    /**
     * 玩家一开始在入口位置
     *
     * @param world 世界对象
     */
    public Characters(World world) {
        Point entry = world.getEntry();
        frisk = new Frisk(entry.getX(), entry.getY());
    }

    /**
     * 控制角色的移动
     *
     * @param world 世界对象
     * @param input 输入的命令
     */
    public void setCharacters(World world, String input) {
        switch (input) {
            case "w", "W" -> frisk.goUp(world);
            case "s", "S" -> frisk.goDown(world);
            case "a", "A" -> frisk.goLeft(world);
            case "d", "D" -> frisk.goRight(world);
        }
        /* 更新世界地图上的当前角色位置，将该位置的瓷砖设置为 Tileset.Frisk ，代表角色位置 */
        world.getTiles()[frisk.getX()][frisk.getY()] = Tileset.FRISK;
    }
}
