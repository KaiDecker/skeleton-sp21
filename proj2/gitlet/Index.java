package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.join;
import static gitlet.Utils.restrictedDelete;

/**
 * 表示一个暂存区 index 对象
 * 重要的为 add 和 remove 方法
 * 即 gitlet-add, gitlet-rm
 *
 * @author Kai Decker
 */
public class Index implements Serializable {

    /**
     * 暂存待提交的文件快照映射
     * KEY 为文件的绝对路径
     * VALUE 为 blob 的名字
     */
    private final Map<String, String> added;

    /* 暂存待删除的绝对路径的集合 */
    private final Set<String> removed;

    /* 已跟踪但尚未提交的文件绝对路径的集合 */
    private final Set<String> tracked;

    /**
     * 实例化一个 index 对象
     * 一个 index 对象存储着待提交文件，待删除文件和跟踪文件的指针
     */
    public Index() {
        added = new HashMap<>();
        removed = new HashSet<>();
        tracked = new HashSet<>();
    }

    /**
     * 判断文件在给定的提交里有没有被修改
     *
     * @return 如果文件被修改返回 true
     */
    public static boolean isModified(File inFile, Commit c) {
        /* 如果文件不存在 */
        if (!inFile.exists()) {
            return true;
        }
        /* 获取当前文件的 blob */
        String current = Blob.getBlobName(inFile);
        /* 从给定提交获取此文件的 blob */
        String oldBlobName = c.getBlob(inFile);
        /* 文件在给定提交中不存在或哈希值不相等 */
        return oldBlobName == null || !oldBlobName.equals(current);
    }

    /**
     * 同一文件在两个提交是否有差异
     * @return 如果有差异则返回 true
     */
    public static boolean isModified(File inFile, Commit current, Commit target) {
        String cur = current.getBlob(inFile);
        String tar = target.getBlob(inFile);
        return !Objects.equals(tar, cur);
    }

    /**
     * 将文件当前状态的副本添加到暂存区，即暂存文件以进行添加
     * 重新暂存一个已暂存的文件，会用新内容覆盖暂存区中的原有条目
     * 暂存区应位于 .gitlet 目录中的某个位置
     * 如果文件的当前工作版本与当前提交中的版本完全相同，
     * 则不要将其暂存以进行添加，并且如果它已在暂存区中，则将其从中移除
     * 如果该文件在执行此命令时已被标记为待删除，那么此操作将取消其待删除状态
     */
    public void add(File file) {
        String f = file.getAbsolutePath();
        /* 如果之前标记过删除，则撤销删除，恢复追踪 */
        if (isRemoved(file)) {
            removed.remove(f);
        }
        /* 只有确实有改动时才会真正写入 */
        if (isModified(file, Methods.readHEADAsCommit())) {
            added.put(f, new Blob(file).makeBlob());
            tracked.add(f);
        }
        save();
    }

    /**
     * 如果文件在暂存区中被标记为"新增"，则将其从暂存区移除
     * 如果文件已经被版本控制系统跟踪（即存在于最新提交中）
     * 会在暂存区将其标记为待删除
     * 同时从工作目录中实际删除该文件（如果用户还没手动删除）
     */
    public boolean remove(File file) {
        boolean flag = false;
        String f = file.getAbsolutePath();
        /* 如果在 added 里 */
        if (isStaged(file)) {
            added.remove(f);
            flag = true;
        }
        if (!flag && isTracked(file, Methods.readHEADAsCommit())) {
            /* 在 removed 中标为待删除 */
            removed.add(f);
            /* 删除其物理文件 */
            restrictedDelete(f);
            flag = true;
        }
        save();
        return flag;
    }

    /* 清空暂存区中的 index.added, index.removed, index.tracked */
    public void cleanStagingArea() {
        added.clear();
        removed.clear();
        tracked.clear();
        save();
    }

    /* 将 index 对象序列化写入 */
    public void save() {
        Utils.writeObject(Repository.INDEX, this);
    }

    /* 判断文件是否标记为删除，在待删中 */
    public boolean isRemoved(File inFile) {
        return removed.contains(inFile.getAbsolutePath());
    }

    /* 判断文件是否待提交 */
    public boolean isStaged(File inFile) {
        return added.containsKey(inFile.getAbsolutePath());
    }

    /* 判断文件是否已跟踪未提交 */
    private boolean isTracked(File file) {
        return tracked.contains(file.getAbsolutePath());
    }

    /**
     * 同样判断文件是否已跟踪未提交
     * 但是调用了以上，多了判断是否在提交快照 blob 里存在
     */
    public boolean isTracked(File file, Commit c) {
        return c.getBlob(file) != null || isTracked(file);
    }

    /* 判断暂存区是否有改动 */
    public boolean isCommitted() {
        return added.isEmpty() && removed.isEmpty();
    }

    public Map<String, String> getAdded() {
        return added;
    }

    public Set<String> getRemoved() {
        return removed;
    }

    /**
     * 获得在暂存区的文件名
     * 即把绝对路径集合转换为纯文件名集合
     * 利于 status 展示
     */
    public Set<String> getAddedFilenames() {
        Set<String> ret = new HashSet<>();
        added.keySet().forEach(n -> ret.add(join(n).getName()));
        return ret;
    }

    public Set<String> getRemovedFilenames() {
        Set<String> ret = new HashSet<>();
        removed.forEach(n -> ret.add(join(n).getName()));
        return ret;
    }
}
