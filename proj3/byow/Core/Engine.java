package byow.Core;

import byow.Core.HUD.Framework;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import static byow.Core.Utils.*;
import static byow.Core.Utils.generateWorld;

/**
 * @author Kai Decker
 */

public class Engine {
    /* 绘制世界的渲染器 */
    TERenderer ter = new TERenderer();
    /* 宽度和高度可以自由更改 */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    /* 用来标记是不是第一次输入，即是否已经初始化了世界 */
    boolean start = false;
    /* 用来保存一个世界的状态 */
    Variables v = new Variables();
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT, 2, 2);

        Framework f = new Framework();
        f.drawMenu();

        while (true) {
            StringBuilder input = new StringBuilder();
            if (!start) {
                getStarted(input);
            } else {
                inputCommands(input);
            }
            TETile[][] tiles = interactWithInputString(input.toString());
            if (tiles == null) {
                return;
            }
            f.drawFramework(ter, tiles);
        }
    }

    /**
     * 接收一串输入指令字符串来进行互动
     *
     * @param input 传入给程序的输入字符串
     * @return 表示当前世界状态的 2D TETile[][] 数组
     */
    public TETile[][] interactWithInputString(String input) {
        /* 首先预处理字符串 */
        input = fixInputString(this, input);
        /* 当世界还没有初始化 */
        if (!start) {
            /* 加载存档 */
            if (input.contains("L")) {
                /* 调用 load 读取之前保存的状态 */
                v = load();
                /* 将 L 字符去掉 */
                input = input.substring(1);
            /* 新建世界 */
            } else {
                /*int end = input.indexOf('S') + 1;
                String s = input.substring(0, end);
                long seed = Long.parseLong(s, 1, s.length() - 1, 10);
                v.world = new World(seed, WIDTH, HEIGHT);
                v.world.initializeWorld(seed);
                v.tempWorld = v.world.clone();
                v.characters = new Characters(v.tempWorld);
                v.characters.setCharacters(v.tempWorld, "");
                input = input.substring(end);*/
                int end = input.indexOf('S') + 1;
                generateWorld(v, input.substring(0, end));
                input = input.substring(end);
            }
            /* 标记完成初始化 */
            start = true;
        }
        /* 将剩下的字符串当作移动命令 */
        move(v, input);
        /* 如果字符串中出现了 ":q" 则退出 */
        if (input.indexOf(':') > -1) {
            quit(v);
        }
        /* 返回当前世界的瓦片矩阵 */
        return v.tempWorld.getTiles();
    }
}
