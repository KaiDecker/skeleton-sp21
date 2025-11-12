package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Merge.findAllAncestors;
import static gitlet.Repository.OBJECTS_DIR;
import static gitlet.Utils.*;

/**
 * 表示 gitlet 远程仓库对象
 *
 * @author Kai Decker
 */

public class Remote implements Serializable {

    /**
     * KEY 为远程仓库的名字
     * VALUE 为远程仓库的目录路径
     */
    private final Map<String, File> remotes;

    Remote() {
        /* 初始化映射 */
        remotes = new HashMap<>();
    }

    /**
     * 将给定分支中的所有新对象
     * 从该分支所在的仓库移动到目标仓库
     */
    private static void moveObjects(File sourceObjectsDir, File targetObjectsDir,
                                    Branch branch, Set<String> ancestors) {
        String branchHEAD = branch.getHEADAsString();
        for (String commit : ancestors) {
            moveObject(sourceObjectsDir, targetObjectsDir, commit);
            Methods.toCommit(commit, targetObjectsDir)
                    .getBlobs()
                    .values()
                    .forEach(objID -> moveObject(sourceObjectsDir, targetObjectsDir, objID));
            if (commit.equals(branchHEAD)) {
                break;
            }
        }
    }

    /**
     * 将对象（提交或快照）从源仓库移动到目标仓库
     *
     * @param id 移动的对象的 id
     */
    private static void moveObject(File sourceObjectsDir, File targetObjectsDir, String id) {
        String dir = id.substring(0, 2);
        String name = id.substring(2);
        File targetDir = join(targetObjectsDir, dir);
        targetDir.mkdir();
        File sourcePath = join(sourceObjectsDir, dir, name);
        writeContents(join(targetDir, name), readContents(sourcePath));
    }

    /**
     * 从远程 Gitlet 仓库将提交下载到本地 Gitlet 仓库
     * 基本上，此操作会将远程仓库中给定分支的所有提交和数据块，即当前仓库中尚不存在的，复制到本地 .gitlet 中
     * 且名为[远程名称]/[远程分支名称]，并将 [远程名称]/[远程分支名称] 指向头指针提交
     * 如果该分支在本地仓库中先前不存在，则会创建它
     */
    public void fetch(String remoteName, Branch branch) {
        File sourceRepo = remotes.get(remoteName);
        /* 若调用者传入的“想 fetch 的远程分支”不存在，报错退出 */
        if (branch == null) {
            Methods.exit("That remote does not have that branch.");
        }

        /*
        * 获取远程活动分支中的所有提交
        * 定位远程对象库
        */
        File sourceObjectsDir = join(sourceRepo, "objects");
        /* 读取远程对象的 HEAD */
        Commit sourceHEAD = Methods.toCommit(
                readObject(join(sourceRepo, "HEAD"), Branch.class).getHEADAsString(),
                sourceObjectsDir);
        /* 收集远端当前活动分支的所有祖先提交的 id，插入顺序保留 */
        Set<String> ancestors = new LinkedHashSet<>();
        findAllAncestors(sourceHEAD, ancestors);

        /* 把这些提交及其快照 blob 从远端复制到本地对象库 */
        moveObjects(sourceObjectsDir, OBJECTS_DIR, branch, ancestors);
        String branchName = remoteName + "/" + branch;
        Branch nb;
        if (!Branch.isExists(branchName)) {
            nb = new Branch(branchName, branch.getHEADAsString());
        } else {
            nb = Branch.readBranch(branchName);
            nb.setHEADContent(branch.getHEADAsString());
        }
        /* 完成更新 */
        writeObject(join(Repository.BRANCHES_DIR, nb.toString()), nb);
    }

    /* 尝试将当前分支的提交追加到给定远程仓库中指定分支的末尾 */
    public void push(String remoteName, Branch branch) {
        /* 获取本地的当前分支提交 */
        File target = remotes.get(remoteName);
        Set<String> ancestors = new LinkedHashSet<>();
        Commit currentHEAD = Methods.readHEADAsCommit();
        findAllAncestors(currentHEAD, ancestors);
        String branchHEAD = branch.getHEADAsString();
        if (!ancestors.contains(branchHEAD)) {
            Methods.exit("Please pull down remote changes before pushing.");
        }

        /* 移动到远程仓库 */
        moveObjects(OBJECTS_DIR, join(target, "objects"), branch, ancestors);
        Methods.setHEAD(currentHEAD, Methods.readHEADAsBranch(), target);
    }

    /**
     * 将给定的登录信息保存在指定的远程名称下，即远程仓库
     * 后续尝试从该远程名称进行推送或拉取操作时，将尝试使用此 .gitlet 目录。
     */
    public boolean addRemote(String name, File path) {
        if (isExists(name)) {
            return false;
        }
        remotes.put(name, path);
        save();
        return true;
    }

    /**
     * 移除与指定远程名称关联的信息，即远程仓库
     * 如果您想要更改已添加的远程仓库
     * 必须先将其移除，然后重新添加
     */
    public boolean removeRemote(String name) {
        if (!isExists(name)) {
            return false;
        }
        remotes.remove(name);
        save();
        return true;
    }

    /* 判断该名字的远程仓库是否存在 */
    public boolean isExists(String name) {
        return remotes.containsKey(name);
    }

    public File getRemote(String name) {
        return remotes.get(name);
    }

    /* 序列化远程仓库对象 */
    public void save() {
        Utils.writeObject(Repository.REMOTES, this);
    }
}
