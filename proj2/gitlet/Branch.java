package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static gitlet.Repository.BRANCHES_DIR;
import static gitlet.Utils.join;

/**
 * 表示 Gitlet 中的指向分支 branch 的指针对象
 *
 * @author Kai Decker
 */

public class Branch implements Serializable {

    /* 分支名字 */
    private final String name;
    /* 当前分支指针指向的提交 uid */
    private String HEAD;

    /**
     * 使用名字和提交的 uid 来实例化一个 branch 对象
     * 若同名分支已经存在，就直接退出并提示错误
     */
    public Branch(String name, String head) {
        if (isExists(name)) {
            Methods.exit("A branch with that name already exists.");
        }
        this.name = name;
        this.HEAD = head;
    }

    /* 判断给定名字的分支是否存在 */
    public static boolean isExists(String name) {
        name = correctName(name);
        List<String> names = Utils.plainFilenamesIn(BRANCHES_DIR);
        return names != null && names.contains(name);
    }

    /* 根据给定的名字来反序列化读取分支 branch 对象 */
    public static Branch readBranch(String name) {
        /* 实际上是调用下方的方法 */
        return readBranch(name, BRANCHES_DIR);
    }

    public static Branch readBranch(String name, File dir) {
        name = correctName(name);
        File b = join(dir, name);
        return !b.exists() ? null : Utils.readObject(b, Branch.class);
    }

    public static String correctName(String name) {
        return name.replace("/", "_");
    }

    /* 更新 HEAD 头指针 */
    public void updateBranch() {
        this.HEAD = Utils.readObject(Repository.HEAD, Branch.class).getHEADAsString();
        String n = this.name;
        n = correctName(n);
        File h = join(BRANCHES_DIR, n);
        Utils.writeObject(h, this);
    }

    /**
     * 移除给定名字的分支 branch
     *
     * @return 文件存在且被删除就返回 true
     */
    public boolean remove(String branchName) {
        File b = join(BRANCHES_DIR, branchName);
        return b.delete();
    }

    /* 修改 HEAD 头指针指向的提交 uid */
    public void setHEADContent(String content) {
        this.HEAD = content;
    }

    /* 获取 HEAD 头指针指向的提交 uid */
    public String getHEADAsString() {
        return this.HEAD;
    }

    /* 当前分支指向的提交 uid 读出来并反序列化成一个 Commit 对象 */
    public Commit getHEADAsCommit() {
        return Methods.toCommit(this.HEAD);
    }

    /* 返回分支 branch 名字 */
    public String getName() {
        return name;
    }

    /**
     * @return 分支 branch 的名字
     */
    @Override
    public String toString() {
        return correctName(name);
    }
}
