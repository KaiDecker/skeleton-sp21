package gitlet;

import java.lang.reflect.Method;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *
 *  @author Kai Decker
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Methods.exit("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                GitletUtils.init(args);
                break;
            case "add":
                GitletUtils.add(args);
                break;
            case "commit":
                GitletUtils.commit(args);
                break;
            case "rm":
                GitletUtils.remove(args);
                break;
            case "log":
                GitletUtils.log(args);
                break;
            case "global-log":
                GitletUtils.globalLog(args);
                break;
            case "find":
                GitletUtils.find(args);
                break;
            case "checkout":
                GitletUtils.checkout(args);
                break;
            case "status":
                GitletUtils.status(args);
                break;
            case "branch":
                GitletUtils.branch(args);
                break;
            case "rm-branch":
                GitletUtils.removeBranch(args);
                break;
            case "reset":
                GitletUtils.reset(args);
                break;
            case "merge":
                GitletUtils.merge(args);
                break;
            case "add-remote":
                GitletUtils.addRemote(args);
                break;
            case "rm-remote":
                GitletUtils.rmRemote(args);
                break;
            case "fetch":
                GitletUtils.fetch(args);
                break;
            case "push":
                GitletUtils.push(args);
                break;
            case "pull":
                GitletUtils.pull(args);
                break;
            default:
                Methods.exit("No command with that name exists.");
        }
    }
}
