package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kai Decker
 */

public class Repository implements Serializable {

    /** 当前项目的根目录 */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** .gitlet 的根目录 */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** 存储 Gitlet 引用信息的目录 */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    /** 存储各个分支信息的目录 */
    public static final File BRANCHES_DIR = join(REFS_DIR, "heads");

    /** 存储所有提交 ID 的文件 */
    public static final File COMMITS = join(REFS_DIR, "commits");

    /** 远程仓库信息的存储目录 */
    public static final File REMOTES = join(REFS_DIR, "remotes");

    /** 存储 Gitlet 对象（比如 blobs 和 commits 等）的目录 */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /** Gitlet 的 HEAD 指针，指向当前分支 */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    /** 存储当前暂存区（已添加文件和已删除文件）的索引对象 */
    public static final File INDEX = join(GITLET_DIR, "index");

    /** 在当前的目录创建一个新的 Gitlet 版本控制系统 */
    public static void initializeRepo() {
        /* 创建目录的列表 */
        List<File> dirs = List.of(GITLET_DIR, REFS_DIR, OBJECTS_DIR, BRANCHES_DIR);
        /* 创建目录 */
        dirs.forEach(File::mkdir);
        /* 创建默认分支即 master */
        Branch h = new Branch("master", "");
        /* 将分支写入 HEAD 即使用序列化方法 */
        writeObject(HEAD, h);
        /* 更新分支 */
        h.updateBranch();
        /* 创建空的暂存区索引 */
        writeObject(INDEX, new Index());
        /* 创建空的远程仓库 */
        writeObject(REMOTES, new Remote());
        /* 创建空的提交历史 */
        writeContents(COMMITS, "");
    }

    /** 删除 DIR 目录里的所有文件 */
    public static void clean(File dir) {
        /* 获取目录中的文件名 */
        List<String> files = plainFilenamesIn(dir);
        if (files != null) {
            /* 如果文件存在，则删除 */
            files.forEach(n -> join(dir, n).delete());
        }
    }

    /** 根据给定的 commit ID 或者 blob ID
     * 获取其对应的存储目录，其中包括ID的前两个字符 */
    public static File getObjectsDir(String id) {
        return join(OBJECTS_DIR, id.substring(0, 2));
    }

    /**
     * 根据给定的 commit ID 或者 blob ID 获取文件名
     * 它从 ID 中截取最后38个字符作为文件名
     */
    public static String getObjectName(String id) {
        return id.substring(2);
    }

    /** 使用对象的 id 创建一个新的 commit 对象或者 blob 对象的存储目录 */
    public static File makeObjectDir(String id) {
        File out = getObjectsDir(id);
        out.mkdir();
        return join(out, getObjectName(id));
    }

    /** 获取远程仓库的分支目录 */
    public static File getRemoteBranchDir(String name) {
        return join(Methods.readRemotes().getRemote(name), "refs", "heads");
    }
}
