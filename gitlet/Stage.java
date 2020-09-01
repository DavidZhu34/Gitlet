package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/** The class constructor for our staging area.
 *  @author David Zhu
 */
public class Stage implements Serializable {

    /**
     * Wrapper function for hashmap. **/
    private HashMapWrapper Wrapper = new HashMapWrapper();

    /**HashMap instance of our wrapper function, with String1 file name and String2 SHA-ID. **/
    private HashMap<String, String> _toAdd = Wrapper.get_toAdd();

    /**HashMap instance of our wrapper function, with String1 file name and String2 SHA-ID. **/
    private HashMap<String, String> _toRemove = Wrapper.get_toRmv();

    /**Path to our CWD. **/
    private static final File CWD = new File(".");

    /**Path to our metadata folder called .gitlet. **/
    private static final File GITLET = new File(".gitlet");

    /**Path to our branches folder. **/
    private static final File BRANCHES = Utils.join(GITLET, "Branches");

    /**Path to the File that contains the string representing current branch. **/
    private static final File CURRBRANCH = Utils.join(GITLET, "currBranch");

    /**Path to our commit folder. **/
    private static final File COMMITS = Utils.join(GITLET, "Commits");

    /**Path to our stage folder. **/
    private static final File STAGE = Utils.join(GITLET, "Stage");

    /**Retrieves head from our metadata folder. **/
    public Commit getHead() {
        String id = Utils.readContentsAsString(CURRBRANCH);
        File path = Utils.join(BRANCHES, id);
        String branchID = Utils.readContentsAsString(path);
        File commitFolder = Utils.join(COMMITS, branchID);
        Commit res = Utils.readObject(commitFolder, Commit.class);
        return res;
    }

    /**Adds blob to HashMap with FILENAME input.**/
    public void add(String filename) {
        HashMapWrapper wrapper = fromFile();
        HashMap toAdd = wrapper.get_toAdd();
        HashMap toRemove = wrapper.get_toRmv();
        File path = new File(filename);
        if (!path.exists()) {
            System.out.print("File does not exist.");
        } else {
            Blob blob = new Blob(filename);
            String blobID = Utils.sha1(Utils.serialize(blob));
            HashMap<String, String> commitBlobs = this.getHead().getBlobs(); //returns head commit's blob HashMap
            if (toAdd.containsKey(filename)) {
                toAdd.replace(filename, blobID);
            } else if (commitBlobs.containsValue(blobID)) { //checks if _blobs HashSet has redundant ShaIDs
                if (toAdd.containsValue(blobID)) {
                    toAdd.remove(filename, blobID);
                }
                if (toRemove.containsValue(blobID)) {
                    toRemove.remove(filename, blobID);
                }
                saveWrapper(wrapper);
                return;
            } else { //insert elseif above here
                toAdd.put(filename, blobID);
            }
            blob.saveBlob();
        }
        saveWrapper(wrapper);
    }

    /**Removes FILENAME if it's tracked. **/
    public void remove(String filename) {
        HashMapWrapper wrapper = fromFile();
        HashMap toAdd = wrapper.get_toAdd();
        HashMap toRemove = wrapper.get_toRmv();
        File path = new File(filename);
        Blob blob = new Blob(filename);
        String blobID = Utils.sha1(Utils.serialize(blob));
        HashMap<String, String> commitBlobs = this.getHead().getBlobs();
        String value = commitBlobs.get(filename);
        if (!path.exists()) {
            if (toAdd.containsKey(filename) || commitBlobs.containsValue(value)) {
                if (toAdd.containsKey(filename)) {
                    toAdd.remove(filename, value);
                }
                if (commitBlobs.containsValue(value)) {
                    toRemove.put(filename, value);
                    path.delete();
                }
            } else {
                System.out.print("No reason to remove the file.");
            }
        } else {
            if (toAdd.containsKey(filename) || commitBlobs.containsValue(blobID)) {
                if (toAdd.containsKey(filename)) {
                    toAdd.remove(filename, blobID);
                }
                if (commitBlobs.containsValue(blobID)) {
                    toRemove.put(filename, blobID);
                    path.delete();
                }
            } else {
                System.out.print("No reason to remove the file.");
            }
        }
        saveWrapper(wrapper);
    }

    /**Getter method for wrapper. **/
    public HashMapWrapper getWrapper() {
        return Wrapper;
    }

    /**Retrieves Wrapper from metadata which contains _toAdd and _toRemove. **/
    public HashMapWrapper fromFile() {
        HashMapWrapper res = Utils.readObject(STAGE, HashMapWrapper.class);
        return res;
    }

    /**
     * Saves HashMapWrapper WRAPPER to stage folder in .gitlet.
     **/
    public void saveWrapper(HashMapWrapper wrapper) {
        Utils.writeObject(STAGE, wrapper);
    }


}