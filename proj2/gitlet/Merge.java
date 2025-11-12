package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Checkout.checkoutBranch;
import static gitlet.Checkout.checkoutFile;
import static gitlet.Index.isModified;
import static gitlet.Utils.*;

/**
 * 表示 Gitlet 的合并，即 gitlet-merge
 *
 * @author Kai Decker
 */

public class Merge {

    /**
     * 从给定的分支 branch 和目前的分支 branch 合并文件
     */
    public static void merge(Branch current, Branch given) {
        /* 计算分裂点的提交的 uid */
        String split = getSplitPoint(current, given);
        /*
         * 如果给定分支 branch 的 HEAD 头指针指向的就是分裂点
         * 说明 given 是 current 的祖先，无需合并
         */
        if (given.getHEADAsString().equals(split)) {
            Methods.exit("Given branch is an ancestor of the current branch.");
        }
        /*
         * 如果当前分支 branch 的 HEAD 头指针指向的是分裂点
         * 说明当前分支落后，则快进或切换到给定分支 branch
         */
        if (current.getHEADAsString().equals(split)) {
            checkoutBranch(given.toString());
            Methods.exit("Current branch fast-forwarded.");
        }
        /* 将分裂点 uid 解析成提交的实例 */
        Commit sp = Methods.toCommit(split);
        /* 分别得到当前和给定分支 branch 的 HEAD 头指针提交对象 */
        Commit cur = current.getHEADAsCommit();
        Commit tar = given.getHEADAsCommit();
        /* 取出当前提交跟踪的所有文件路径 */
        Set<String> files = new HashSet<>(cur.getBlobs().keySet());
        /* 再把给定提交的所有文件路径合并进去 */
        files.addAll(tar.getBlobs().keySet());
        List<String> cwd = plainFilenamesIn(Repository.CWD);
        /* 如果工作区非空，把每个文件名转为绝对路径字符串加入集合 */
        if (cwd != null) {
            cwd.forEach(n -> files.add(join(Repository.CWD, n).getAbsolutePath()));
        }
        String msg = "Merged " + given.getName() + " into " + current.getName() + ".";
        doMerge(files, sp, cur, tar, msg);
    }

    /**
     * @return 两个分支之间第一个分裂点的提交的 uid
     */
    private static String getSplitPoint(Branch current, Branch given) {
        List<Commit> splits = new ArrayList<>();
        Set<String> commits = new HashSet<>();
        /* 从当前分支的头指针 HEAD 开始，把它及其所有祖先 uid 加入 commits */
        dfs(current.getHEADAsCommit(), commits, splits);
        /* 从给定分支 HEAD 开始遍历，当遇到已在 commits 中的提交时，把该提交加入 splits */
        dfs(given.getHEADAsCommit(), commits, splits);
        return splits.stream()
                .max(Comparator.comparing(Commit::getDate))
                .get()
                .getUid();
    }

    /**
     * 将当前提交的所有祖先提交添加到一个集合中
     *
     * @param commits 一个存储所有祖先提交（包括自身）的集合
     */
    public static void findAllAncestors(Commit b, Set<String> commits) {
        dfs(b, commits, null);
    }

    /**
     * 深度优先搜索
     */
    private static void dfs(Commit b, Set<String> commits, List<Commit> splits) {
        if (b == null) {
            return;
        }
        if (commits.contains(b.getUid())) {
            /* 判断是否是第二次 dfs */
            if (splits != null) {
                splits.add(b);
            }
            return;
        }
        commits.add(b.getUid());
        dfs(b.getParentAsCommit(), commits, splits);
        dfs(b.getSecondParentAsCommit(), commits, splits);
    }

    private static void doMerge(Set<String> files, Commit split,
                                Commit current, Commit given, String msg) {
        Index idx = Methods.readStagingArea();
        /* 文件合并 */
        files.forEach(f -> merge(split, current, given, idx, join(f)));
        new Commit(msg, current.getUid(), given.getUid()).makeCommit();
    }

    private static void merge(Commit split, Commit current, Commit given, Index idx, File f) {
        boolean flag = onlyPresentInCurrentBranch(f, current, given, split);
        flag = onlyAbsentInCurrentBranch(f, current, given, split, flag);
        flag = onlyPresentInGivenBranch(f, current, given, split, idx, flag);
        flag = onlyAbsentInGivenBranch(f, current, given, split, idx, flag);
        flag = onlyModifiedInGivenBranch(f, current, given, split, idx, flag);
        flag = onlyModifiedInCurrentBranch(f, current, given, split, flag);
        flag = modifiedInSame(f, current, given, split, flag);
        if (!flag && conflict(f, current, given, idx)) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * 对于自分裂点以来在给定分支中被修改
     * 但在当前分支中自分割点以来未被修改的文件
     * 应将其版本更改为给定分支中的版本
     */
    private static boolean onlyModifiedInGivenBranch(File file, Commit current, Commit given,
                                                     Commit split, Index index, boolean flag) {
        if (flag) {
            return true;
        }
        /* 当前分支相对分裂点未修改该文件 */
        if (!isModified(file, current, split)) {
            /* 给定分支里该文件被删除 */
            if (given.getBlob(file) == null) {
                index.remove(file);
                return true;
            /* 给定分支相对分裂点有修改 */
            } else if (isModified(file, given, split)) {
                checkoutFile(given, file);
                index.add(file);
                return true;
            }
        }
        return false;
    }

    /* 只在当前分支相对分裂点发生了修改，而给定分支中没变 */
    private static boolean onlyModifiedInCurrentBranch(File file, Commit current,
                                                       Commit given, Commit split, boolean flag) {
        if (flag) {
            return true;
        }
        return isModified(file, current, split) && !isModified(file, given, split);
    }

    /**
     * 对于在当前分支和给定分支中以相同方式被修改的文件
     * 即两个文件现在具有相同的内容，或者都已被删除
     * 合并操作将保持这些文件不变
     * 如果一个文件在当前分支和给定分支中均被删除
     * 但工作目录中存在同名文件
     * 则该文件将保持不变，并在合并结果中继续保持未跟踪和未暂存状态
     */
    private static boolean modifiedInSame(File file, Commit current,
                                          Commit given, Commit split, boolean flag) {
        if (flag) {
            return true;
        }
        return isModified(file, current, split)
                && Objects.equals(current.getBlob(file), given.getBlob(file));
    }

    /**
     * 对于在分割点不存在，
     * 且仅出现在当前分支中的文件，
     * 应保持原状不变。
     */
    private static boolean onlyPresentInCurrentBranch(File file, Commit current,
                                                      Commit given, Commit split) {
        return split.getBlob(file) == null
                && current.getBlob(file) != null && given.getBlob(file) == null;
    }

    /**
     * 对于在分割点不存在
     * 且仅出现在给定分支中的文件
     * 应被检出并暂存
     */
    private static boolean onlyPresentInGivenBranch(File file, Commit current, Commit given,
                                                    Commit split, Index index, boolean flag) {
        if (flag) {
            return true;
        }
        if (split.getBlob(file) == null && current.getBlob(file) == null
                && given.getBlob(file) != null) {
            checkoutFile(given, file);
            index.add(file);
            return true;
        }
        return false;
    }

        /**
         * 对于在分割点存在
         * 在当前分支中未修改
         * 但在给定分支中不存在的文件
         * 应被删除（并取消跟踪）
         * 即该文件在分裂点存在，当前分支未改动，而给定分支删除了
         */
    private static boolean onlyAbsentInGivenBranch(File file, Commit current, Commit given,
                                                   Commit split, Index index, boolean flag) {
        if (flag) {
            return true;
        }
        if (split.getBlob(file) != null && !isModified(file, current, split)
                && given.getBlob(file) == null) {
            index.remove(file);
            return true;
        }
        return false;
    }

    /**
     * 对于在分割点存在
     * 在给定分支中未修改
     * 但在当前分支中不存在的文件
     * 应继续保持不存在状态
     * 即该文件在分裂点存在，给定分支未改动，而当前分支删除了
     */
    private static boolean onlyAbsentInCurrentBranch(File file, Commit current,
                                                     Commit given, Commit split, boolean flag) {
        if (flag) {
            return true;
        }
        return split.getBlob(file) != null && !isModified(file, given, split)
                && current.getBlob(file) == null;
    }

    /**
     * 在当前分支和给定分支中以不同方式修改的文件将处于冲突状态
     * "以不同方式修改"可以指：
     * 两个文件的内容都发生了更改且彼此不同
     * 或者一个文件的内容被更改而另一个文件被删除
     * 或者文件在分割点不存在
     * 且在给定分支和当前分支中具有不同的内容
     * 在这种情况下，将冲突文件的内容替换为：
     * <br>
     * <br><<<<<<< HEAD
     * <br>contents of file in current branch
     * <br>=======
     * <br>contents of file in given branch
     * <br>>>>>>>><br>
     * <br>
     * 将被分支中删除的文件视为空文件
     * 在此处使用直接连接
     * 对于末尾没有换行符的文件，
     * 您可能会得到如下内容：
     * <br>
     * <br><<<<<<< HEAD
     * <br>contents of file in current branch=======
     * <br>contents of file in given branch>>>>>>><br>
     * <br>
     */
    private static boolean conflict(File file, Commit current, Commit given, Index index) {
        String cur = current.getBlob(file);
        String tar = given.getBlob(file);
        /* 只要两边的快照 blob 不同，则判断为冲突 */
        if (!Objects.equals(cur, tar)) {
            String curContent = "";
            String tarContent = "";
            if (cur != null) {
                curContent = Methods.toBlob(cur).getContent();
            }
            if (tar != null) {
                tarContent = Methods.toBlob(tar).getContent();
            }
            String content = "<<<<<<< HEAD\n" + curContent + "=======\n" + tarContent + ">>>>>>>\n";
            writeContents(file, content);
            index.add(file);
            return true;
        }
        return false;
    }
}
