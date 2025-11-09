package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

/**
 * 表示 gitlet-checkout 和 gitlet-reset 命令的功能，即检出
 * 把提交的文件还原到工作区
 *
 * @author Kai Decker
 */

public class Checkout {
    /**
     * 提取 HEAD 指针所指提交中的文件版本，并将其放入工作目录
     * 如果该位置已存在同名文件，则直接覆盖
     * 恢复的文件不会自动进入暂存区，需要手动执行才能重新暂存
     */
    public static void checkoutFile(File file) {
        /* 调用下方的方法 */
        checkoutFile(Methods.readHEADAsCommit(), file);
    }

    /**
     * 从指定的提交中提取文件的版本，并将其放入工作目录，
     * 如果该位置已存在同名文件，则直接覆盖。
     * 恢复的文件不会被添加到暂存区。
     */
    public static void checkoutFile(Commit commit, File file) {
        /* 从快照 blob 提取 */
        String oldBlob = commit.getBlob(file);
        if (oldBlob == null) {
            Methods.exit("File does not exist in that commit.");
        }
        /* 重写文件，反序列化 */
        File checkFrom = join(Repository.makeObjectDir(oldBlob));
        reStoreBlob(file, checkFrom);
    }

    /**
     * 提取给定分支 HEAD 指针所指提交中的所有文件，并将它们放入工作目录
     * 如果工作目录中已存在同名文件，则直接覆盖
     * 在这之后，给定的分支将成为当前分支，即 HEAD 指针指向
     * 清理工作目录中不属于目标分支的文件
     * 暂存区将被清空，除非被检出的分支就是当前分支
     */
    public static void checkoutBranch(String name) {
        /* 检查分支是否存在 */
        if (!Branch.isExists(name)) {
            Methods.exit("No such branch exists.");
        }
        /* 判断是否和当前分支相同 */
        Branch currentBranch = Methods.readHEADAsBranch();
        if (currentBranch.toString().equals(name)) {
            Methods.exit("No need to checkout the current branch.");
        }
        /* 检查是否有未跟踪的文件 */
        Methods.untrackedExist();
        /* 清空工作区 */
        Repository.clean(Repository.CWD);
        Branch branchToSwitch = Branch.readBranch(name);

        Commit commitToSwitch = branchToSwitch.getHEADAsCommit();
        HashMap<String, String> old = commitToSwitch.getBlobs();
        for (String oldFile : old.keySet()) {
            String branchName = old.get(oldFile);
            reStoreBlob(join(oldFile), join(Repository.makeObjectDir(branchName)));
        }

        Methods.readStagingArea().cleanStagingArea();
        Methods.setHEAD(commitToSwitch, branchToSwitch);
    }

    /**
     * 检出给定提交所跟踪的所有文件
     * 删除在该提交中不存在但当前已被跟踪的文件
     * 同时将当前分支的头指针移动到该提交节点
     * [commit id] 可以像 checkout 命令中那样使用缩写形式
     * 暂存区将被清空
     * 该命令本质上是对任意提交的检出操作
     * 同时还会改变当前分支的头指针位置
     */
    public static void reset(Commit commit) {
        Repository.clean(Repository.CWD);
        Methods.readStagingArea().cleanStagingArea();
        Map<String, String> olds = commit.getBlobs();
        olds.keySet().forEach(f -> checkoutFile(commit, join(f)));
        Methods.setHEAD(commit, Methods.readHEADAsBranch());
    }

    /**
     * 从快照 blob 里读取文件内容
     * 之后写入文件
     *
     * @param file      将要检出的文件
     * @param checkFrom 指向文件的快照 blob
     */
    private static void reStoreBlob(File file, File checkFrom) {
        Blob oldBlob = readObject(checkFrom, Blob.class);
        writeContents(file, oldBlob.getContent());
    }
}
