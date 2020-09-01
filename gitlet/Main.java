package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author David Zhu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        Repo git = new Repo();
        String[] argArray = args;
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                } else {
                    git.init();
                }
                break;
            case "add":
                git.commandChecker(argArray, 2);
                git.add(args[1]);
                break;
            case "commit":
                git.commandChecker(argArray, 2);
                if (args[1].equals("")) {
                    System.out.print("Please enter a commit message.");
                } else {
                    git.commit(args[1]);
                }
                break;
            case "rm":
                git.commandChecker(argArray, 2);
                git.remove(args[1]);
                break;
            case "log":
                git.commandChecker(argArray, 1);
                git.log();
                break;
            case "global-log":
                git.commandChecker(argArray, 1);
                git.global();
                break;
            case "find":
                git.commandChecker(argArray, 2);
                git.find(args[1]);
                break;
            case "status":
                git.commandChecker(argArray, 1);
                git.status();
                break;
            case "checkout":
                if (args.length > 4) {
                    System.out.println("Incorrect operands.");
                } else {
                    if (args[1].equals("--")) {
                        git.check1(args[2]);
                    } else if (args.length < 3) {
                        git.check3(args[1]);
                    } else if (args[2].equals("--")) {
                        git.check2(args[1], args[3]);
                    } else if (args[2].equals("++")) {
                        System.out.println("Incorrect operands.");
                    }
                }
                break;
            case "branch":
                git.commandChecker(argArray, 2);
                git.branch(args[1]);
                break;
            case "rm-branch":
                git.commandChecker(argArray, 2);
                git.rmBranch(args[1]);
                break;
            case "reset":
                git.commandChecker(argArray, 2);
                git.reset(args[1]);
                break;
            case "merge":
                git.commandChecker(argArray, 2);
                git.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
        System.exit(0);
    }

}
