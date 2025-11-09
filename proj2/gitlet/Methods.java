package gitlet;

import java.io.File;
import java.util.List;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/**
 * 提供一些辅助方法
 * 主要用于文件的操作，可以复用的通用的方法
 * @author Kai Decker
 */
public class Methods {

    /* 检查 .gitlet 目录是否存在，如果不存在，则返回错误信息 */
    public static void exitUnlessRepoExists() {
        File repo = join(CWD, ".gitlet");
        if (!repo.exists()) {
            exit("Not in an initialized Gitlet directory.");
        }
    }

    /* 检查命令行参数是否等于指定的数量，如果不正确则 exit(0) */
    public static void judgeOperands(String[] args, int num) {
        /* 对下面的 judgeOperands 的调用 */
        judgeOperands(num, num, args);
    }

    /* 检查参数数量是否在指定的范围内，如果不正确则 exit(0) */
    public static void judgeOperands(int min, int max, String[] args) {
        if (args.length < min + 1 || args.length > max + 1) {
            exit("Incorrect operands.");
        }
    }

    /**
     * 根据给定的 uid (长度为 8 或者 40) 来返回对应的 Commit 对象
     *
     * @param uid commit 的 uid
     * @return 存在的给定 uid 的 commit
     */
    public static Commit toCommit(String uid) {
        /* 对下面的 toCommit 的调用 */
        return toCommit(uid, OBJECTS_DIR);
    }

    /**
     * 根据给定的 uid (长度为 8 或者 40) 来返回对应的 Commit 对象
     *
     * @param uid       commit 的 uid
     * @param targetDir commit 的位置
     * @return 存在的给定 uid 的 commit
     */
    public static Commit toCommit(String uid, File targetDir) {
        File c = getObject(uid, targetDir);
        if (c == null) {
            return null;
        }
        return c.exists() ? readObject(c, Commit.class) : null;
    }

    /**
     * 根据 id (长度为 40) 创建一个 blob
     *
     * @param uid blob 的 uid
     * @return 存在的给定 uid 的 blob
     */
    public static Blob toBlob(String uid) {
        File b = getObject(uid, OBJECTS_DIR);
        if (b == null) {
            return null;
        }
        return b.exists() ? readObject(b, Blob.class) : null;
    }

    /**
     * 通过 uid 来获取对象文件 (Commit、Blob等) 在 Gitlet 对象存储中的路径
     * 根据 uid 的前 2 位和后 38 位构造文件路径，并返回该文件路径
     * @return 对象文件路径
     */
    private static File getObject(String uid, File objectsDir) {
        if (uid == null || uid.isEmpty()) {
            return null;
        }
        File obj = join(objectsDir, uid.substring(0, 2));
        String rest = getObjectName(uid);
        if (uid.length() == 8) {
            List<String> objects = plainFilenamesIn(obj);
            if (objects == null) {
                return null;
            }
            for (String commit : objects) {
                if (commit.substring(0, 6).equals(rest)) {
                    obj = join(obj, commit);
                    break;
                }
            }
        } else {
            obj = join(obj, rest);
        }
        return obj;
    }

    /* 更新 HEAD 指针，指向给定的 commit 并且更新当前 branch 信息 */
    public static void setHEAD(Commit commit, Branch b) {
        setHEAD(commit, b, GITLET_DIR);
    }

    /* 更新 HEAD 指针，指向给定的 commit 并且更新当前 Branch 信息 */
    public static void setHEAD(Commit commit, Branch b, File remote) {
        b.setHEADContent(commit.getUid());
        writeObject(join(remote, "HEAD"), b);
        b.updateBranch();
    }

    /**
     * @return 当前 HEAD 指针所指向的 branch
     */
    public static Branch readHEADAsBranch() {
        return readObject(HEAD, Branch.class);
    }

    /**
     * @return 当前 HEAD 指针所指向的 commit
     */
    public static Commit readHEADAsCommit() {
        String uid = readHEADContent();
        return Methods.toCommit(uid);
    }

    /**
     * @return 当前 HEAD 指针所指向的 id
     */
    public static String readHEADContent() {
        return readHEADAsBranch().getHEADAsString();
    }

    /**
     * 检查当前是否有未跟踪的文件
     * 如果有未跟踪的文件，便打印错误信息并退出程序
     */
    public static void untrackedExist() {
        if (!Status.getUntrackedFilesNames().isEmpty()) {
            exit("There is an untracked file in the way; delete it,"
                    + " or add and commit it first.");
        }
    }

    /**
     * 读取 index 的配置
     * @return index 对象
     */
    public static Index readStagingArea() {
        return readObject(INDEX, Index.class);
    }

    /**
     * 读取 remote 的配置
     * @return remote 对象
     */

    public static Remote readRemotes() {
        return readObject(REMOTES, Remote.class);
    }

    /* 程序执行中退出，并输出错误信息 */
    public static void exit(String message) {
        if (message != null) {
            System.out.println(message);
        }
        System.exit(0);
    }

    /* 结合之前的方法判断 */
    public static void judgeCommand(String[] args, int num) {
        exitUnlessRepoExists();
        judgeOperands(args, num);
    }

    /**
     * 根据操作系统的路径分隔符修正文件路径
     * @return 在不同系统中正确的路径
     */
    public static File correctPath(String path) {
        path = path.replace("/", File.separator);
        return join(path);
    }
}
