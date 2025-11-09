package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/**
 * 表示 Gitlet 的提交对象
 * 包括其提交信息，uid，时间戳
 * 父提交的指针，文件指针
 *
 * @author Kai Decker
 */
public class Commit implements Serializable {

    /* 提交信息 */
    private String log;

    /* 这个提交的父提交 */
    private String parent;

    /* 合并提交时的第二父提交 */
    private String secondParent;

    /* 提交的时间戳 */
    private Date date;

    /**
     * 这个提交的文件快照映射
     * keys 为工作区文件的绝对路径
     * values 为该文件对应 blob 的 id ，即 BLOB_DIR/shortCommitUid
     */
    private HashMap<String, String> blobs;

    /* 本次提交的 40 位 SHA-1 */
    private String uid;

    /**
     * 使用指定的提交信息和父提交 uid 实例化一个提交对象
     * 第一条提交信息为 "initial commit" 的提交没有父提交
     */
    public Commit(String message, String parent) {
        instantiateCommit(message, parent, null);
    }

    public Commit(String message, String parent, String secondParent) {
        instantiateCommit(message, parent, secondParent);
    }

    /* 根据 uid 找提交对象 */
    public static Commit findWithUid(String id) {
        if (id == null) {
            return null;
        }
        Commit ret = Methods.toCommit(id);
        return ret != null ? ret : Methods.toCommit(id.substring(0, 8));
    }

    /**
     * 根据给定的提交信息来查找提交的 uid
     *
     * @return 提交的 uid 列表
     */
    public static List<String> findWithMessage(String message) {
        Set<Commit> commits = findAll();
        List<String> ids = new ArrayList<>();
        for (Commit c : commits) {
            if (c.log.equals(message)) {
                ids.add(c.uid);
            }
        }
        return ids;
    }

    /**
     * @return 所有曾经建立的提交，包括没有父提交的提交
     */
    public static Set<Commit> findAll() {
        Set<Commit> commits = new HashSet<>();
        /* 读取文件内容 */
        String cs = readContentsAsString(Repository.COMMITS);
        while (!cs.isEmpty()) {
            /* 截取字符串的前 40 个字符，即一个完整的 SHA-1 哈希值 */
            commits.add(Methods.toCommit(cs.substring(0, 40)));
            /* 继续处理剩下的部分 */
            cs = cs.substring(40);
        }
        return commits;
    }

    private void instantiateCommit(String message, String first, String second) {
        this.log = message;
        this.parent = first;
        this.secondParent = second;
        /* 如果为初始提交，即无父提交 */
        if (first == null) {
            this.date = new Date(0);
        /* 普通提交则设置为当前时间 */
        } else {
            this.date = new Date();
        }
        /* 初始化为一个空的 hashmap */
        this.blobs = new HashMap<>();
    }

    /* 将这个提交对象写入 COMMIT_DIR ，并且重置 HEAD 指针 */
    public void makeCommit() {
        /* 读取父提交的 blobs ，即继承父提交的快照 */
        if (this.parent != null) {
            this.blobs = this.getParentAsCommit().blobs;
        }
        /* 读取暂存区 index */
        Index idx = Methods.readStagingArea();
        /* 将暂存区已添加的文件添加到 blobs */
        boolean flag = getStage(idx);
        /* 从 blobs 移除文件路径，并且删除工作区文件 */
        flag = unStage(flag, idx);
        /*
          即 flag 判断是否增加或者删除
          若为非初始提交且没有任何变化，则退出
         */
        if (this.parent != null && !flag) {
            Methods.exit("No changes added to the commit.");
        }
        /* 计算本次提交的 uid */
        setUid();
        /* 按对象 uid 建目录并得到对象文件路径 */
        File out = Repository.makeObjectDir(this.uid);
        /* 清空暂存区 index */
        idx.cleanStagingArea();
        /* 将提交对象序列化写入 */
        writeObject(out, this);
        /* 移动 HEAD 指针指向这个提交，更新分支 branch 引用 */
        Methods.setHEAD(this, Methods.readHEADAsBranch());
        /* 将其 40 位 uid 写入 COMMITS */
        String cs = readContentsAsString(Repository.COMMITS);
        cs += this.uid;
        writeContents(Repository.COMMITS, cs);
    }

    /* 把暂存区里“待添加”的文件合并进本次提交的快照 blobs */
    private boolean getStage(Index i) {
        boolean flag = false;
        Map<String, String> added = i.getAdded();
        if (!added.isEmpty()) {
            flag = true;
            this.blobs.putAll(i.getAdded());
        }
        return flag;
    }

    /* 把暂存区里“待移除”的文件从快照 blobs 中删除，并删除工作区对应文件 */
    private boolean unStage(boolean flag, Index i) {
        Set<String> rm = i.getRemoved();
        if (!rm.isEmpty()) {
            flag = true;
            rm.forEach(f -> {
                blobs.remove(f);
                restrictedDelete(f);
            });
        }
        return flag;
    }

    /**
     * @return 当前提交的 uid
     */
    public String getUid() {
        return this.uid;
    }

    /* 计算并设置当前提交的 uid */
    public void setUid() {
        this.uid = sha1(this.parent + this.date + this.log);
    }

    public String getParentAsString() {
        return parent;
    }

    /**
     * @return 父提交的 uid
     */
    public Commit getParentAsCommit() {
        return Methods.toCommit(this.parent);
    }

    /**
     * @return 提交的时间戳
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return 提交的信息
     */
    public String getLog() {
        return log;
    }

    /**
     * @return 提交的快照映射 blobs
     */
    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    /**
     * 给定文件对象 f ，按决定路径查找本提交中的 blobs
     * @param f
     * @return blob id
     */
    public String getBlob(File f) {
        return blobs.get(f.getAbsolutePath());
    }

    public Commit getSecondParentAsCommit() {
        return Methods.toCommit(this.secondParent);
    }

}
