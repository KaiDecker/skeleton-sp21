package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gitlet.Methods.readHEADAsCommit;
import static gitlet.Methods.readStagingArea;
import static gitlet.Utils.join;
import static gitlet.Utils.plainFilenamesIn;

/**
 * 表示 gitlet-status
 *
 * @author Kai Decker
 */

public class Status {

    /**
     * 显示当前存在的所有分支，
     * 并在当前分支前用 * 标记。
     * 同时显示已暂存等待添加或删除的文件。
     */
    public static void printStatus() {
        Index idx = Methods.readStagingArea();
        printFilenames("=== Branches ===", getBranchesNames());
        printFilenames("\n=== Staged Files ===", idx.getAddedFilenames());
        printFilenames("\n=== Removed Files ===", idx.getRemovedFilenames());
        printFilenames("\n=== Modifications Not Staged For Commit ===",
                getModifiedButNotStagedFilesNames());
        printFilenames("\n=== Untracked Files ===", getUntrackedFilesNames());
        System.out.println();
    }

    /* 打印一个信息和文件名 */
    private static void printFilenames(String msg, List<String> names) {
        System.out.println(msg);
        if (names != null) {
            names.forEach(System.out::println);
        }
    }

    /* 打印一个信息和文件名 */
    private static void printFilenames(String msg, Set<String> names) {
        printFilenames(msg, new ArrayList<>(names));
    }

    /**
     * 获取所有分支名
     * 当前分支打 * 号并放首位
     * 比如 *master
     */
    private static List<String> getBranchesNames() {
        List<String> bs = plainFilenamesIn(Repository.BRANCHES_DIR);
        if (bs == null) {
            return null;
        }
        List<String> branches = new ArrayList<>(bs);
        String name = Methods.readHEADAsBranch().toString();
        branches.remove(name);
        branches.add(0, "*" + name);
        return branches;

    }

    /**
     * 工作目录中的文件在以下情况下被视为"已修改但未暂存"：
     * <br>
     * 在当前提交中被跟踪，在工作目录中被更改，但未暂存
     * <br>
     * 已暂存等待添加，但工作目录中的内容与暂存版本不同
     * <br>
     * 已暂存等待添加，但在工作目录中被删除
     * <br>
     * 未暂存等待移除，但在当前提交中被跟踪且已从工作目录中删除
     */
    private static Set<String> getModifiedButNotStagedFilesNames() {
        Index judge = readStagingArea();
        Set<String> ret = new HashSet<>();
        Commit h = readHEADAsCommit();
        /* 仅对当前提交追踪的文件逐一检查 */
        for (String filePath : h.getBlobs().keySet()) {
            File f = join(filePath);
            String filename = f.getName();
            boolean exists = f.exists();
            boolean staged = judge.isStaged(f);
            boolean removed = judge.isRemoved(f);
            boolean tracked = judge.isTracked(f, h);
            boolean modified = Index.isModified(f, h);
            if (!exists && (staged || (!removed && tracked))) {
                ret.add(filename + " (deleted)");
            } else if (exists && modified && (tracked || staged)) {
                ret.add(filename + " (modified)");
            }

        }
        return ret;
    }

    /**
     * 存在于工作目录中，但既未暂存等待添加也未被版本控制跟踪的文件
     * 这包括那些已被标记为待删除，但随后又在 Gitlet 不知情的情况下重新创建的文件
     *
     * @return 未跟踪的文件名集合，如果模式不正确则返回空集合
     */
    public static Set<String> getUntrackedFilesNames() {
        Set<String> ret = new HashSet<>();
        Commit currentCommit = Methods.readHEADAsCommit();
        List<String> files = plainFilenamesIn(Repository.CWD);
        if (files == null) {
            return ret;
        }
        Index idx = readStagingArea();
        for (String f : files) {
            File file = join(Repository.CWD, f);
            boolean flag = idx.isTracked(file, currentCommit);
            if (!flag) {
                ret.add(file.getName());
            }
        }
        return ret;
    }
}
