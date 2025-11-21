package byow.Core;

import byow.Core.Character.Characters;
import edu.princeton.cs.introcs.StdDraw;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 一些工具方法
 *
 * @author Kai Decker
 */

public class Utils {
    /* 当前工作根目录 */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /* 当前存档目录 */
    public static final File saveFile = join(CWD, "savefiles.txt");

    /**
     * 拼凑路径
     *
     * @return 文件路径
     */
    public static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /**
     * 拼凑路径
     *
     * @return 文件路径
     */
    public static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }

    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                        new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    static <T extends Serializable> T readObject(File file, Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /* 序列化的工具方法 */

    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw new RuntimeException("Internal error serializing");
        }
    }

    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /* 引擎的工具方法 */

    /**
     * 清洗和规范化输入字符串
     *
     * @param engine 所用引擎
     * @param input 输入的字符串
     * @return 全大写的有效命令字符串
     */
    public static String fixInputString(Engine engine, String input) {
        StringBuilder split = new StringBuilder();
        /* 加载标识 */
        boolean loadFlag = false;
        /* 新建世界标识 */
        boolean startFlag = false;
        /* 进入移动阶段标识 */
        boolean moveFlag = false;
        char[] in = input.toCharArray();
        for (int i = 0; i < input.length(); i++) {
            switch (in[i]) {
                case 'l', 'L' -> {
                    loadFlag = true;
                    split.append(in[i]);
                    moveFlag = true;
                }
                case 'n', 'N' -> {
                    if (!loadFlag) {
                        startFlag = true;
                        split.append(in[i]);
                    }
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    if (startFlag && !loadFlag) {
                        split.append(in[i]);
                    }
                }
                case 's', 'S' -> {
                    if (startFlag && !loadFlag
                            && (input.length() - input.lastIndexOf('n') > 1
                            || input.length() - input.lastIndexOf('N') > 1)) {
                        split.append(in[i]);
                        startFlag = false;
                        moveFlag = true;
                    } else if (moveFlag || engine.start) {
                        split.append(in[i]);
                    }
                }
                case 'w', 'W', 'a', 'A', 'd', 'D' -> {
                    if (moveFlag || engine.start) {
                        split.append(in[i]);
                    }
                }
                case ':' -> {
                    if (in[i + 1] == 'q' || in[i + 1] == 'Q') {
                        split.append(':');
                        split.append('Q');
                        return split.toString().toUpperCase();
                    }
                }
            }
        }
        return split.toString().toUpperCase();
    }

    /**
     * @param input N#S, #为种子代码
     */
    public static void generateWorld(Variables v, String input) {
        long seed = Long.parseLong(input, 1, input.length() - 1, 10);
        //long seed = LocalTime.now().toNanoOfDay();
        v.world.initializeWorld(seed);
        v.tempWorld = v.world.clone();
        v.characters = new Characters(v.tempWorld);
        v.characters.setCharacters(v.tempWorld, "");
    }

    /**
     * 从键盘读取输入
     * 支持新建世界和读取存档操作
     * 使用 StdDraw
     *
     * @param input 命令字符串
     */
    public static void getStarted(StringBuilder input) {
        /* 标记是否已经按过 N */
        boolean firstN = false;
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                if (!firstN) {
                    if ((ch == 'N' || ch == 'n')) {
                        input.append(ch);
                        firstN = true;
                    } else if (ch == 'l' || ch == 'L') {
                        input.append(ch);
                        return;
                    }
                }
                if (firstN) {
                    if (ch >= '0' && ch <= '9') {
                        input.append(ch);
                    }
                    if (ch == 's' || ch == 'S') {
                        input.append(ch);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 从键盘读取输入
     * 支持移动操作
     * 使用 StdDraw
     *
     * @param input 命令字符串
     */
    public static void inputCommands(StringBuilder input) {
        /* 标记是否退出 */
        boolean quit = false;
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                switch (ch) {
                    case 'w', 'W', 's', 'S', 'a', 'A', 'd', 'D' -> {
                        if (quit) {
                            input.deleteCharAt(0);
                        }
                        input.append(ch);
                        return;
                    }
                    case ':' -> {
                        input.append(ch);
                        quit = true;
                    }
                    case 'q', 'Q' -> {
                        if (quit) {
                            input.append(ch);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * 加载存档
     *
     * @return 一个 Variables 对象
     */
    public static Variables load() {
        return readObject(saveFile, Variables.class);
    }

    /**
     * 保存存档
     *
     * @param v 当前需要存储的 Variables 对象
     */
    public static void save(Variables v) {
        writeObject(saveFile, v);
    }

    /**
     * 退出前存档
     * 即调用上方的 save 方法
     *
     * @param v 当前需要存储的 Variables 对象
     */
    public static void quit(Variables v) {
        save(v);
    }

    /**
     * 实现移动
     *
     * @param v 给定的 Variables 对象
     * @param command 移动的命令
     */
    public static void move(Variables v, String command) {
        for (char s : command.toCharArray()) {
            /* 克隆当前地图， tempWorld 每一步重新从 world 克隆，再把人物绘制上去 */
            v.tempWorld = v.world.clone();
            /* 人物的真正位置记录在 v.characters 里 */
            v.characters.setCharacters(v.tempWorld, String.valueOf(s));
        }
    }
}
