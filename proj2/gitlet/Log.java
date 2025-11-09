package gitlet;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 表示 Gitlet 的日志功能，包括 gitlet-log 和 gitlet-global-log 命令
 *
 * @author Kai Decker
 */

public class Log {
    /**
     * 从当前 HEAD 开始，一直往父提交走，逐个打印日志，直到最初的提交 (parent == null)
     * 忽略了合并提交的第二父提交
     */
    public static void log(Commit c) {
        while (c != null) {
            printLog(c);
            c = Commit.findWithUid(c.getParentAsString());
        }
    }

    /* 打印仓库中所有提交对象的信息 */
    public static void globalLog() {
        /* 逐个反序列化 */
        Commit.findAll().forEach(Log::printLog);
    }

    /**
     * 打印格式
     * <br><br>
     * ===
     * <br>
     * commit {commit id}
     * <br>
     * Date: E MMM dd HH:mm:ss yyyy Z {commit timestamp}
     * <br>
     * {commit message}
     */
    private static void printLog(Commit c) {
        System.out.println("===");
        System.out.println("commit " + c.getUid());
        SimpleDateFormat d = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        System.out.println("Date: " + d.format(c.getDate()));
        System.out.println(c.getLog() + "\n");
    }
}
