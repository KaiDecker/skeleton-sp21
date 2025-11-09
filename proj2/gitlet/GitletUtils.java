package gitlet;

import java.io.File;
import java.util.List;

import static gitlet.Methods.*;
import static gitlet.Repository.*;
import static gitlet.Utils.join;

/**
 * 可以理解为 Gitlet 的实现层或者分发层
 * 详细的实现方法交给各自的类
 * @author Kai Decker
 */

public class GitletUtils {
    /**
     * 使用命令 'init'
     * 初始化 '.gitlet' 仓库并且创建第一次提交
     */
    public static void init(String[] args) {
        /* 判断命令行参数 */
        judgeOperands(args, 0);
        File repo = join(CWD, ".gitlet");
        if (repo.exists()) {
            exit("A Gitlet version-control system already exists in the current directory.");
        }
        /* 使用 Repository 类中的方法来创建 */
        initializeRepo();
        /* 创建初始的提交 */
        Commit commit = new Commit("initial commit", null);
        commit.makeCommit();
    }

    /**
     * 使用命令 'add + fileName'
     * 来把工作区文件放入 index 暂存区
     */
    public static void add(String[] args) {
        /* 要求仓库存在并且恰好 1 个操作数即文件名 */
        judgeCommand(args, 1);
        File inFile = join(CWD, args[1]);
        if (!inFile.exists()) {
            exit("File does not exist.");
        }
        /* 把文件放入 index */
        readStagingArea().add(inFile);
    }

    /**
     * 使用命令 'rm + fileName'
     * 将文件从 index 暂存区移除
     */
    public static void remove(String[] args) {
        /* 要求仓库存在并且恰好 1 个操作数即文件名 */
        judgeCommand(args, 1);
        File inFile = join(CWD, args[1]);
        if (!readStagingArea().remove(inFile)) {
            exit("No reason to remove the file.");
        }
    }

    /**
     * 使用命令 'commit + message'
     * 以 index 暂存区为快照创建提交
     */
    public static void commit(String[] args) {
        exitUnlessRepoExists();
        if (args.length < 2 || args[1].equals("")) {
            exit("Please enter a commit message.");
        }
        judgeOperands(args, 1);
        String message = args[1];
        String h = readHEADContent();
        /* 以 HEAD 为父提交创建新提交，清空 index 暂存区 */
        new Commit(message, h).makeCommit();
    }

    /**
     * 三种用法的 checkout 检出
     * 使用命令 'checkout -- [file name]'
     * 把当前提交里的该文件覆盖到工作区
     * 或者  'checkout [commit id] -- [file name]'
     * 从指定 id 的提交获取文件到工作区
     * 或者  'checkout [branch name]'
     * 切换分支并更新工作区
     */
    public static void checkout(String[] args) {
        exitUnlessRepoExists();
        judgeOperands(1, 3, args);
        if (args.length == 3 && args[1].equals("--")) {
            File file = join(CWD, args[2]);
            Checkout.checkoutFile(file);
        } else if (args.length == 4 && args[2].equals("--")) {
            /* 需要根据 id 寻找 */
            Commit commit = Commit.findWithUid(args[1]);
            if (commit == null) {
                exit("No commit with that id exists.");
            }
            File file = join(CWD, args[3]);
            Checkout.checkoutFile(commit, file);
        } else if (args.length == 2) {
            Checkout.checkoutBranch(args[1]);
        } else {
            exit("Incorrect operands.");
        }
    }

    /**
     * 使用命令 'log'
     * 打印当前分支的向后日志
     */
    public static void log(String[] args) {
        judgeCommand(args, 0);
        Log.log(readHEADAsCommit());
    }

    /**
     * 使用命令 'global-log'
     * 打印全局所有提交日志
     */
    public static void globalLog(String[] args) {
        judgeCommand(args, 0);
        Log.globalLog();
    }

    /**
     * 使用命令 'status'
     * 打印当前工作目录的分支/暂存/未跟踪/修改等状态
     */
    public static void status(String[] args) {
        judgeCommand(args, 0);
        Status.printStatus();
    }

    /**
     * 使用命令 'find + message'
     * 按提交消息查找所有匹配的提交 id，每行一个
     */
    public static void find(String[] args) {
        judgeCommand(args, 1);
        List<String> uid = Commit.findWithMessage(args[1]);
        if (uid.isEmpty()) {
            exit("Found no commit with that message.");
        }
        uid.forEach(System.out::println);
    }

    /**
     * 使用命令 'branch [branch name]'
     * 使用给定的名字来创建 branch
     */
    public static void branch(String[] args) {
        judgeCommand(args, 1);
        Branch b = new Branch(args[1], readHEADContent());
        b.updateBranch();
    }

    /**
     * 使用命令 'rm-branch [branch name]'
     * 删除给定名字的 branch
     */
    public static void removeBranch(String[] args) {
        judgeCommand(args, 1);
        String name = args[1];
        Branch cur = readHEADAsBranch();
        if (name.equals(cur.toString())) {
            exit("Cannot remove the current branch.");
        } else if (!cur.remove(name)) {
            exit("A branch with that name does not exist.");
        }
    }

    /**
     * 使用命令 'reset [commit id]'
     * 把当前分支重置到某个提交的 status
     */
    public static void reset(String[] args) {
        judgeCommand(args, 1);
        Commit commit = toCommit(args[1]);
        if (commit == null) {
            exit("No commit with that id exists.");
        }
        untrackedExist();
        /* 结合 Checkout */
        Checkout.reset(commit);
    }

    /**
     * 使用命令 'merge [branch name]'
     * 把目标分支合并进当前分支
     */
    public static void merge(String[] args) {
        judgeCommand(args, 1);
        Branch cur = readHEADAsBranch();
        Branch b = Branch.readBranch(args[1]);
        if (b == null) {
            exit("A branch with that name does not exist.");
        }
        if (b.toString().equals(cur.toString())) {
            exit("Cannot merge a branch with itself.");
        }
        if (!readStagingArea().isCommitted()) {
            exit("You have uncommitted changes.");
        }
        untrackedExist();
        /*使用 Merge 类*/
        Merge.merge(cur, b);
    }

    /**
     * 使用命令 'add-remote [remote name] [name of remote directory]/.gitlet'
     * 添加一个新的远程仓库，并将给定的登录信息保存在指定的名称下
     */
    public static void addRemote(String[] args) {
        judgeCommand(args, 2);
        if (!readRemotes().addRemote(args[1], correctPath(args[2]))) {
            exit("A remote with that name already exists.");
        }
    }

    /**
     * 使用命令 'rm-remote [remote name]'
     * 删除与指定名称关联的相关信息
     */
    public static void rmRemote(String[] args) {
        judgeCommand(args, 1);
        if (!readRemotes().removeRemote(args[1])) {
            exit("A remote with that name does not exist.");
        }
    }

    /**
     * 使用命令 'push [remote name] [remote branch name]'
     * 将本地分支推送到远程仓库
     */
    public static void push(String[] args) {
        judgeCommand(args, 2);
        Remote r = readRemotes();
        String remoteName = args[1];
        String branchName = args[2];
        if (!r.isExists(remoteName) || !r.getRemote(remoteName).exists()) {
            Methods.exit("Remote directory not found.");
        }
        r.push(remoteName, Branch.readBranch(branchName, getRemoteBranchDir(remoteName)));
    }

    /**
     * 使用命令 'fetch [remote name] [remote branch name]'
     * 获取远程分支到本地
     */
    public static void fetch(String[] args) {
        judgeCommand(args, 2);
        Remote r = readRemotes();
        String remoteName = args[1];
        String branchName = args[2];
        if (!r.isExists(remoteName) || !r.getRemote(remoteName).exists()) {
            Methods.exit("Remote directory not found.");
        }
        r.fetch(remoteName, Branch.readBranch(branchName, getRemoteBranchDir(remoteName)));
    }

    /**
     * 使用命令 'pull [remote name] [remote branch name]'
     * 首先拉取远程仓库的指定分支 branch [remote name]/[remote branch name]
     * 之后合并
     */
    public static void pull(String[] args) {
        fetch(args);
        String[] command = new String[2];
        command[0] = "merge";
        command[1] = Branch.correctName(args[1] + "/" + args[2]);
        merge(command);
    }
}
