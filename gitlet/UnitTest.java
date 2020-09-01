package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author David Zhu
 */
public class UnitTest {

    /**Path to our CWD. **/
    private static final File CWD = new File(".");

    /**Path to our metadata folder called .gitlet. **/
    private static final File GITLET = new File(".gitlet");

    /**Path to our commit folder. **/
    private static final File COMMITS = Utils.join(GITLET, "Commits");

    /**Path to our stage folder. **/
    private static final File STAGE = Utils.join(GITLET, "Stage");

    /**Constructs our stage for our repo. **/
    private Stage stage = new Stage();

    /**Path to our blobs folder. **/
    private static final File BLOBS = Utils.join(GITLET, "Blobs");

    /**Path to our branches folder. **/
    private static final File BRANCHES = Utils.join(GITLET, "Branches");

    /**Path to our head folder. **/
    private static final File HEAD = Utils.join(GITLET, "Head");

    /**Path to our master branch. **/
    private static final File MASTER = Utils.join(BRANCHES, "master");

    /**The string representing head pointer. **/
    private static String head;

    /**Path to the File that contains the string representing current branch. **/
    private static final File CURRBRANCH = Utils.join(GITLET, "currBranch");

    /**The HashSet of our branch for merge. **/
    private HashSet<String> branchAncestors = new HashSet<String>();
    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
        Repo a = new Repo();
        String branch = a.getCurrBranch();
        File path = Utils.join(BRANCHES, branch);
        String commit = Utils.readContentsAsString(path);
        File newPath = Utils.join(COMMITS, commit);
        Commit res = Utils.readObject(newPath, Commit.class);

        HashMapWrapper b = stage.fromFile();
        HashMap _toAdd = b.get_toAdd();
        HashMap _toRmv = b.get_toRmv();
        String dogshit = "dogshit";
        File d = Utils.join(BRANCHES, dogshit);

        List<String> wdFiles = Utils.plainFilenamesIn(CWD);
    }

}


