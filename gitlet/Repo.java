package gitlet;

import edu.princeton.cs.algs4.BST;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**Class constructor for our working repository.
 *  @author David Zhu
 *  **/
public class Repo implements Serializable {

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

    /**Initializes our .gitlet metadata folder. **/
    public void init() throws IOException {
        if (GITLET.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        } else {
            GITLET.mkdir();
            Commit initial = new Commit("initial commit", null);
            if (!COMMITS.exists()) {
                COMMITS.mkdir();
            }
            if (!STAGE.exists()) {
                STAGE.createNewFile();
            }
            if (!BLOBS.exists()) {
                BLOBS.mkdir();
            }
            if (!BRANCHES.exists()) {
                BRANCHES.mkdir();
            }
            if (!MASTER.exists()) {
                MASTER.createNewFile();
            }
            if (!HEAD.exists()) {
                HEAD.createNewFile();
            }
            if (!CURRBRANCH.exists()) {
                CURRBRANCH.createNewFile();
            }
            byte[] a = Utils.serialize(initial);
            String shaID = Utils.sha1(a);
            head = shaID;
            Utils.writeContents(MASTER, head);
            File firstCommit = Utils.join(COMMITS, head);
            Utils.writeObject(firstCommit, initial);
            Utils.writeContents(HEAD, head);
            HashMapWrapper b = stage.getWrapper();
            Utils.writeObject(STAGE, b);
            Utils.writeContents(CURRBRANCH, "master");
        }
    }

    /**Creates a new commit with MESSAGE. **/
    public void commit(String message) {
        getHead();
        Commit a = new Commit(message, head);
        Commit parent = a.deserializeParent();
        a.copyBlobs(parent);
        HashMapWrapper wrapper = stage.fromFile();
        HashMap<String, String> _toAdd = wrapper.get_toAdd();
        HashMap<String, String> _toRmv = wrapper.get_toRmv();
        if (_toAdd.size() == 0 && _toRmv.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        a.setBlobs(_toAdd, _toRmv);
        a.saveCommit();
        String newHead = Utils.sha1(Utils.serialize(a));
        head = newHead;
        updateMetadata();
        _toAdd.clear();
        _toRmv.clear();
        stage.saveWrapper(wrapper);
    }

    /**Updates head, branches, and master. **/
    public void updateMetadata() {
        String current = getCurrBranch();
        File branchPath = Utils.join(BRANCHES, current);
        Utils.writeContents(branchPath, head);
        Utils.writeContents(HEAD, head);
    }

    /**Calls add with argument FILENAME. **/
    public void add(String filename) {
        stage.add(filename);
    }

    /**Calls remove with argument FILENAME. **/
    public void remove(String filename) {
        stage.remove(filename);
    }

    /**Retrieves head from our metadata folder and updates instance variable head. **/
    public void getHead() {
        String branch = getCurrBranch();
        File path = Utils.join(BRANCHES, branch);
        String id = Utils.readContentsAsString(path);
        head = id;
    }

    /**Retrieves current branch from our metadata folder and updates instance variable head. **/
    public String getCurrBranch() {
        String id = Utils.readContentsAsString(CURRBRANCH);
        return id;
    }

    /**Prints out the log of our commits. **/
    public void log() {
        getHead();
        File headCommitPath = Utils.join(COMMITS, head);
        Commit headCommit = Utils.readObject(headCommitPath, Commit.class);
        Commit pointer = headCommit;
        logHelper(pointer, head);
        String commitParent;
        while (pointer.getParent() != null) {
            commitParent = pointer.getParent();
            pointer = pointer.deserializeParent();
            logHelper(pointer, commitParent);
        }
    }

    /**Helper function to log that actually prints COMMIT info. **/
    public void logHelper(Commit commit, String commitID) {
        String message = commit.getMsg();
        Date commitDate = commit.getDate();

        TimeZone time = SimpleTimeZone.getTimeZone("America/Los_Angeles");
        SimpleDateFormat a = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
        a.setTimeZone(time);
        String date = a.format(commitDate) + " -0800";
        System.out.print("===");
        System.out.println();
        System.out.println("commit " + commitID);
        if (commit.getParent2() != null) {
            String parent1 = commit.getParent().substring(0, 7);
            String parent2 = commit.getParent2().substring(0, 7);
            System.out.println("Merge: " + parent1 + " " + parent2);
        }
        System.out.println("Date: " + date);
        System.out.println(message);
        System.out.println();

    }

    /**Finds a commit with MESSAGE, else prints error message. **/
    public void find(String message) {
        boolean found = false;
        for (File each : COMMITS.listFiles()) {
            Commit a = Utils.readObject(each, Commit.class);
            String id = each.getName();
            if (message.equals(a.getMsg())) {
                System.out.println(id);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**Prints out the global-log. **/
    public void global() {
        File[] list = COMMITS.listFiles();
        assert list != null;
        for (File each : list) {
            String id = each.getName();
            Commit a = Utils.readObject(each, Commit.class);
            logHelper(a, id);
        }
    }

    /**Prints out the status for the repo. **/
    public void status() {
        //Branches handled
        List<String> names = Utils.plainFilenamesIn(BRANCHES);
        String currentBranch = getCurrBranch();
        System.out.println("=== Branches ===");
        assert names != null;
        for (String each : names) {
            if (each.equals(currentBranch)){
                System.out.print("*");
            }
            System.out.println(each);
        }
        System.out.println();

        //Stage handled
        HashMapWrapper wrapper = stage.fromFile();
        HashMap<String, String> toAddHash = wrapper.get_toAdd();
        HashMap<String, String> toRmvHash = wrapper.get_toRmv();
        List<String> toAdd = hashKeyToArray(toAddHash);
        List<String> toRmv = hashKeyToArray(toRmvHash);
        System.out.println("=== Staged Files ===");
        for (String each : toAdd) {
            System.out.println(each);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String each : toRmv) {
            System.out.println(each);
        }
        System.out.println();

        //Not Staged Handled
        // EXTRA CREDIT PORTION
        System.out.println("=== Modifications Not Staged For Commit ===");
        HashMap<String, String> currBlubs = getBranchHead(getCurrBranch()).getBlobs();
        List<String> wdFiles = Utils.plainFilenamesIn(CWD); //Files in WD
        wdFiles.remove(".gitignore");
        wdFiles.remove("proj2.iml");
        for (String filename : currBlubs.keySet()) {
            if (wdFiles.contains(filename)) {
                File path = new File(filename);
                String wdContent = Utils.readContentsAsString(path); //wdContent
                String shaID = currBlubs.get(filename);
                File blobPath = Utils.join(BLOBS, shaID);
                Blob blob = Utils.readObject(blobPath, Blob.class);
                String blobContent = blob.getContents();
                if (!wdContent.equals(blobContent)) {
                    if (!toAddHash.containsKey(filename) && !toRmvHash.containsKey(filename)) {
                        System.out.println(filename + " (modified)");
                    }
                }
            } else if (!wdFiles.contains(filename)) {
                if (!toRmvHash.containsKey(filename)) {
                    System.out.println(filename + " (deleted)");
                }
            }
        }
        for (String filename : toAddHash.keySet()) {
            if (!wdFiles.contains(filename)) {
                System.out.println(filename + " (deleted)");
            } else {
                File path = new File(filename);
                String wdContent = Utils.readContentsAsString(path); //wdContent
                String shaID = toAddHash.get(filename);
                File blobPath = Utils.join(BLOBS, shaID);
                Blob blob = Utils.readObject(blobPath, Blob.class);
                String blobContent = blob.getContents();
                if (!blobContent.equals(wdContent)) {
                    System.out.println(filename + " (modified)");
                }
            }
        }

        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String filename : wdFiles) {
            if (!currBlubs.containsKey(filename) && !toAddHash.containsKey(filename)
            && !toRmvHash.containsKey(filename)) {
                System.out.println(filename);
            }
        }
    }

    /**Updates the branches. **/
    public void updateBranch(String branch, String head) {
        File path = Utils.join(BRANCHES, branch);
        Utils.writeContents(path, head);
    }

    /**Returns HASHMAP values as an array. **/
    public List<String> hashKeyToArray(HashMap<String, String> map) {
        Set<String> keys = map.keySet();
        String[] files = map.keySet().toArray(new String[0]);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /**Checkout 1: Takes head commit and branch and puts them in working directory, overwriting if necessary.
     * New version is not staged, with args FILENAME. **/
    public void check1(String filename) throws IOException {
        String currBranch = getCurrBranch();
        File path = Utils.join(BRANCHES, currBranch);
        String commitID = Utils.readContentsAsString(path);
        File pathCommit = Utils.join(COMMITS, commitID);
        Commit com = Utils.readObject(pathCommit, Commit.class);
        checkHelper(com, filename);
    }

    /**Checkout 2: Takes given COMMITID and FILENAME and puts them in working directory, overwriting if necessary.
     * New version is not staged. **/
    public void check2(String commitID, String filename) throws IOException {
        File path = Utils.join(COMMITS, commitID);
        if (path.exists()) {
            Commit com = Utils.readObject(path, Commit.class);
            checkHelper(com, filename);
        } else if (!path.exists()) {
            String fullCommitID = abbreviate(commitID);
            if (fullCommitID != null) {
                File actualPath = Utils.join(COMMITS, fullCommitID);
                Commit actualCom = Utils.readObject(actualPath, Commit.class);
                checkHelper(actualCom, filename);
            } else {
                System.out.println("No commit with that id exists.");
            }
        }
    }

    /**Checkout 3: Takes all files in BRANCH and puts them in working directory, overwriting if necessary.
     * BRANCH will now be the current branch (HEAD). Any files tracked in current branch but not present in
     * BRANCH will be deleted. Clears the staging area, unless BRANCH is the current branch. **/
    public void check3(String branch) throws IOException {
        if (getCurrBranch().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            File path = Utils.join(BRANCHES, branch);
            if (path.exists()) {
                String branchString = Utils.readContentsAsString(path);
                checkHelper2(branchString);
                setCurrBranch(branch);
            } else {
                System.out.println("No such branch exists.");
            }
        }
    }

    /**Helper function for checkout commit. **/
    public void checkHelper(Commit com, String filename) throws IOException {
        HashMap<String, String> blub = com.getBlobs();
        if (blub.containsKey(filename)) {
            String blobCommit = blub.get(filename);
            File blobPath = Utils.join(BLOBS, blobCommit);
            Blob blob = Utils.readObject(blobPath, Blob.class);
            File wdBlob = new File(filename);
            //File wdBlob = new File(".\\testing\\src\\" + filename);
            if (!wdBlob.exists()) {
                wdBlob.createNewFile();
            }
            Utils.writeContents(wdBlob, blob.getContents());
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /**Creates a new branch called BRANCH that points to current head node.**/
    public void branch(String branch) throws IOException {
        getHead();
        File path = Utils.join(BRANCHES, branch);
        if (path.exists()) {
            System.out.print("A branch with that name already exists.");
            return;
        }
        path.createNewFile();
        Utils.writeContents(path, head);

    }

    /**Removes branch called BRANCH. **/
    public void rmBranch(String branch) {
        File path = Utils.join(BRANCHES, branch);
        if (branch.equals(getCurrBranch())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        if (!path.exists()) {
            System.out.print("A branch with that name does not exist.");
            return;
        }
        path.delete();
    }

    /**Checks out all the files tracked by ID and removes files that aren't tracked by ID.
     * Additionally, moves currBranch's head to be that ID. **/
    public void reset(String id) throws IOException {
        File path = Utils.join(COMMITS, id);
        if (!path.exists()) {
            String fullCommitID = abbreviate(id);
            if (fullCommitID != null) {
                checkHelper2(fullCommitID);
                String headCurrBranch = getCurrBranch();
                File currBranchPath = Utils.join(BRANCHES, headCurrBranch);
                Utils.writeContents(currBranchPath, fullCommitID);
            } else {
                System.out.println("No commit with that id exists.");
            }
        } else {
            checkHelper2(id);
            String headCurrBranch = getCurrBranch();
            File currBranchPath = Utils.join(BRANCHES, headCurrBranch);
            Utils.writeContents(currBranchPath, id);
        }
    }

    /**Checks that ARGS of ARGARRAY are valid length that gitlet has been initialized. **/
    public void commandChecker(String[] argArray, int args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        } else if (argArray.length != args) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /**Sets current branch with BRANCH. **/
    public void setCurrBranch(String branch) {
        Utils.writeContents(CURRBRANCH, branch);
    }

    /**Clears the staging area. **/
    public void clearStage() {
        HashMapWrapper wrapper = stage.fromFile();
        HashMap<String, String> _toAdd = wrapper.get_toAdd();
        HashMap<String, String> _toRmv = wrapper.get_toRmv();
        _toAdd.clear();
        _toRmv.clear();
        stage.saveWrapper(wrapper);
    }

    /**Checks for abbreviated SHA-IDS with argument PREFIX. **/
    public String abbreviate(String prefix) {
        String[] commitIDs = COMMITS.list();
        assert commitIDs != null;
        for (String each : commitIDs) {
            if (each.startsWith(prefix)) {
                return each;
            }
        }
        return null;
    }

    /**Helper function for reset and check3 that takes in string COMMITID. **/
    public void checkHelper2(String commitID) throws IOException {
        File commitPath = Utils.join(COMMITS, commitID);
        Commit com = Utils.readObject(commitPath, Commit.class);
        HashMap<String, String> blub = com.getBlobs(); //Blobs for commit argument

        String currBranch = getCurrBranch();
        File currPath = Utils.join(BRANCHES, currBranch);
        String currCommitID = Utils.readContentsAsString(currPath);
        File pathCurrCommit = Utils.join(COMMITS, currCommitID);
        Commit currCom = Utils.readObject(pathCurrCommit, Commit.class);
        HashMap<String, String> currBlub = currCom.getBlobs(); //Blobs for current commit

        List<String> wdFiles = Utils.plainFilenamesIn(CWD); //Files in WD
        List<String> commitFiles = hashKeyToArray(blub); //Files tracked by commit
        List<String> currFiles = hashKeyToArray(currBlub); //Files tracked by current commit
        for (String each : wdFiles) {
            if (!currFiles.contains(each)) {
                if (commitFiles.contains(each)) {
                    System.out.println("There is an untracked file in the way;" +
                            " delete it, or add and commit it first.");
                    return;
                }
            }
        }
        for (String filename : commitFiles) {
            String blobCommit = blub.get(filename);
            File blobPath = Utils.join(BLOBS, blobCommit);
            Blob blob = Utils.readObject(blobPath, Blob.class);
            File wdBlob = new File(filename);
            if (!wdBlob.exists()) {
                wdBlob.createNewFile();
            }
            Utils.writeContents(wdBlob, blob.getContents());
        }
        for (String filename : currFiles) {
            if (!commitFiles.contains(filename)) {
                File path = new File(filename);
                path.delete();
            }
        }
        clearStage();
        Utils.writeObject(pathCurrCommit, currCom);
    }

    /**Merges files from BRANCH to currBranch. **/
    public void merge(String branch) throws IOException {
        //Checks failure cases
        boolean conflict = false;
        HashMapWrapper wrapper = stage.fromFile();
        HashMap<String, String> _toAdd = wrapper.get_toAdd();
        HashMap<String, String> _toRmv = wrapper.get_toRmv();
        if (_toAdd.size() != 0 || _toRmv.size() != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        File branchPath = Utils.join(BRANCHES, branch);
        File currPath = Utils.join(BRANCHES, getCurrBranch());
        if (!branchPath.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (getCurrBranch().equals(branch)) {
            System.out.println("Cannot merge a branch with itself");
            return;
        }
        Commit branchHead = getBranchHead(branch);
        Commit currHead = getBranchHead(getCurrBranch());
        String branchID = Utils.readContentsAsString(branchPath);
        String currID = Utils.readContentsAsString(currPath);

        HashMap<String, String> currBlub = currHead.getBlobs();
        HashMap<String, String> blub = branchHead.getBlobs();

        if (untrackedFileError(blub, currBlub)) {
            System.out.println("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
            return;
        }
        //Main body
        String splitPointStr = closestSplitPoint(branch);
        File splitPath = Utils.join(COMMITS, splitPointStr);
        Commit splitPoint = Utils.readObject(splitPath, Commit.class);
        HashMap<String, String> splitBlub = splitPoint.getBlobs();

        Commit mergeCommit = new Commit(("Merged " + branch + " into " + getCurrBranch() + "."),
                currID, branchID);

        if (splitPointStr.equals(branchID)) { //Fix?
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (splitPointStr.equals(currID)) {
            check3(branch);
            System.out.println("Current branch fast-forwarded.");
            return;
        } else {
            for (String each : splitBlub.keySet()) {
                //Files exist in both branches
                if (currBlub.containsKey(each) && blub.containsKey(each)) {
                    //Files unmodified in current branch
                    if (currBlub.get(each).equals(splitBlub.get(each))) {
                        if (!splitBlub.get(each).equals(blub.get(each))) {

                            check2(branchID, each);
                            add(each);
                        }
                        //Files unmodified in branch
                    } else if (blub.get(each).equals(splitBlub.get(each))) {
                        if (!currBlub.get(each).equals(splitBlub.get(each))) {
                            add(each);
                        }
                        //Files modified the same way in both branches
                    } else if (blub.get(each).equals(currBlub.get(each))) {
                        add(each);
                        //Merge conflicts
                    } else {
                        String currValue = currBlub.get(each);
                        File currBlobPath = Utils.join(BLOBS, currValue);
                        Blob currBlob = Utils.readObject(currBlobPath, Blob.class);
                        String currContents = currBlob.getContents();

                        String branchValue = blub.get(each);
                        File branchBlobPath = Utils.join(BLOBS, branchValue);
                        Blob branchBlob = Utils.readObject(branchBlobPath, Blob.class);
                        String branchContents = branchBlob.getContents();

                        String content = "<<<<<<< HEAD\n" + currContents + "=======\n"
                                + branchContents + ">>>>>>>\n";
                        File wdBlobPath = new File(each);
                        Utils.writeContents(wdBlobPath, content);
                        add(each);
                        conflict = true;
                    }
                    //Files exists in curr but not branch
                } else if (currBlub.containsKey(each) && !blub.containsKey(each)) {
                    if (!currBlub.get(each).equals(splitBlub.get(each))) {
                        String currContents;
                        String branchContents;
                        if (currBlub.containsKey(each)) {
                            String currValue = currBlub.get(each);
                            File currBlobPath = Utils.join(BLOBS, currValue);
                            Blob currBlob = Utils.readObject(currBlobPath, Blob.class);
                            currContents = currBlob.getContents();
                        } else {
                            currContents = "";
                        }
                        if (blub.containsKey(each)) {
                            String branchValue = blub.get(each);
                            File branchBlobPath = Utils.join(BLOBS, branchValue);
                            Blob branchBlob = Utils.readObject(branchBlobPath, Blob.class);
                            branchContents = branchBlob.getContents();
                        } else {
                            branchContents = "";
                        }
                        String content = "<<<<<<< HEAD\n" + currContents + "=======\n"
                                + branchContents + ">>>>>>>\n";
                        File wdBlobPath = new File(each);
                        Utils.writeContents(wdBlobPath, content);
                        add(each);
                        conflict = true;
                    } else {
                        remove(each);
                    }
                }
            }
            for (String each : currBlub.keySet()) {
                if (!splitBlub.containsKey(each) && blub.containsKey(each)) {
                    String currContents;
                    String branchContents;
                    if (currBlub.containsKey(each)) {
                        String currValue = currBlub.get(each);
                        File currBlobPath = Utils.join(BLOBS, currValue);
                        Blob currBlob = Utils.readObject(currBlobPath, Blob.class);
                        currContents = currBlob.getContents();
                    } else {
                        currContents = "";
                    }
                    if (blub.containsKey(each)) {
                        String branchValue = blub.get(each);
                        File branchBlobPath = Utils.join(BLOBS, branchValue);
                        Blob branchBlob = Utils.readObject(branchBlobPath, Blob.class);
                        branchContents = branchBlob.getContents();
                    } else {
                        branchContents = "";
                    }
                    String content = "<<<<<<< HEAD\n" + currContents + "=======\n"
                            + branchContents + ">>>>>>>\n";
                    File wdBlobPath = new File(each);
                    Utils.writeContents(wdBlobPath, content);
                    add(each);
                    conflict = true;
                }
                if (!splitBlub.containsKey(each) && !blub.containsKey(each)) {
                    add(each);
                }
            }
            for (String each : blub.keySet()) {
                if (!splitBlub.containsKey(each) && !currBlub.containsKey(each)) {
                    check2(branchID, each);
                    add(each);
                }
            }

            wrapper = stage.fromFile();
            _toAdd = wrapper.get_toAdd();
            _toRmv = wrapper.get_toRmv();
            mergeCommit.setBlobs(_toAdd, _toRmv);
            mergeCommit.saveCommit();
            String newHead = Utils.sha1(Utils.serialize(mergeCommit));
            head = newHead;
            updateMetadata();
            _toAdd.clear();
            _toRmv.clear();
            stage.saveWrapper(wrapper);
            if (conflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }

    /**Helper function of merge for finding the closest split point of currBranch and BRANCH. **/
    public String closestSplitPoint(String branch) {
        File currPath = Utils.join(BRANCHES, getCurrBranch());
        String currID = Utils.readContentsAsString(currPath);

        File branchPath = Utils.join(BRANCHES, branch);
        String branchID = Utils.readContentsAsString(branchPath);

        setBranchAncestors(branchID);
        Queue<String> q = new LinkedList<String>();
        q.add(currID);
        while (!q.isEmpty()) {
            String id = q.remove();
            File path = Utils.join(COMMITS, id);
            Commit splitPoint = Utils.readObject(path, Commit.class);
            if (branchAncestors.contains(id)) {
                return id;
            } else {
                if (splitPoint.getParent() != null) {
                    q.add(splitPoint.getParent());
                }
                if (splitPoint.getParent2() != null) {
                    q.add(splitPoint.getParent2());
                }
            }
        }
        return null;
    }

    /**Helper function for branchAncestorsHelper that traverses tree of BRANCHID. **/
    public void setBranchAncestors(String branchID) {
        File branchComPath = Utils.join(COMMITS, branchID);
        Commit com = Utils.readObject(branchComPath, Commit.class);
        branchAncestors.add(branchID);
        if (com.getParent() != null) {
            setBranchAncestors(com.getParent());
        }
        if (com.getParent2() != null) {
            setBranchAncestors(com.getParent2());
        }
    }

    /**Retrieves head commit of given BRANCH. **/
    public Commit getBranchHead(String branch) {
        File path = Utils.join(BRANCHES, branch);
        String res = Utils.readContentsAsString(path);
        File path2 = Utils.join(COMMITS, res);
        Commit com = Utils.readObject(path2, Commit.class);
        return com;
    }

    /**Helper function for untracked files. **/
    public boolean untrackedFileError(HashMap<String, String> blub, HashMap<String, String> currBlub) {
        List<String> wdFiles = Utils.plainFilenamesIn(CWD); //Files in WD
        List<String> commitFiles = hashKeyToArray(blub); //Files tracked by commit
        List<String> currFiles = hashKeyToArray(currBlub);
        for (String each : wdFiles) {
            if (!currFiles.contains(each)) {
                if (commitFiles.contains(each)) {
                    return true;
                }
            }
        }
        return false;
    }
}