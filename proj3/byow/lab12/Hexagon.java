package byow.lab12;

import org.junit.Test;

/**
 * 六边形的实现
 *
 * @author Kai Decker
 */
public class Hexagon {

    /**
     * 可以将字符串形成的六边形看作两部分，上下两部分相同
     *
     * @param s 六边形的边长
     * @return 字符串构成的六边形
     */
    public static String[] addHexagon(int s) {
        /* 边长为 s 的六边形的行数为 2 * s ，故构造一个字符串数组 */
        String[] hex = new String[2 * s];
        for (int i = 0; i < s; i++) {
            /* 左边空白 */
            String line = " ".repeat(s - i - 1) +
                    /* 中间部分，每往下一行，a 的数量增加 2 个 */
                    "a".repeat(s + 2 * i) +
                    /* 右边空白 */
                    " ".repeat(s - i - 1);
            /* 放入上半部分 */
            hex[i] = line;
            /* 放入下半部分，与上方对称 */
            hex[2 * s - i - 1] = line;
        }
        return hex;
    }

    /**
     * 仅仅是测试
     */
    @Test
    public void print() {
        for (int i = 1; i < 5; i++) {
            for (String l : addHexagon(i)) {
                System.out.println(l);
            }
        }
    }
}
