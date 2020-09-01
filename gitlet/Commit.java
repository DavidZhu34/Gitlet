package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**Class constructor for Commit.
 *  @author David Zhu
 *  **/
public class Commit implements Serializable {

    /**Message of our commit. **/
    private String _message;

    /**Path to our metadata folder called .gitlet. **/
    private static final File GITLET = new File(".gitlet");

    /**Path to our commit folder. **/
    private static final File COMMITS = Utils.join(GITLET, "Commits");

    /**Path to our head folder. **/
    private static final File HEAD = Utils.join(GITLET, "Head");

    /**Datetime of our commit. **/
    private Date _date;

    /**Parent of the commit. **/
    private String _parent;

    /**Parent 2 of the commit .**/
    private String _parent2;

    /**Branch of the commit. **/
    private Commit _branch;

    /** Sha-ID of the commit. **/
    private String _id;

    /** Set of blobs, represented as strings. **/
    private HashMap<String, String> _blobs = new HashMap<String, String>();

    /**Constructor for our commit object. **/
    public Commit(String message, String parent) {
        this._message = message;
        this._parent = parent;
        if ((this._parent == null) && (this._message == "initial commit")) {
            _date = new Date(0);
        } else {
            _date = new Date();
        }
    }

    /**Constructor for our commit object that has 2nd parent. **/
    public Commit(String message, String parent, String parent2) {
        this._message = message;
        this._parent = parent;
        this._parent2 = parent2;
        if ((this._parent == null) && (this._message == "initial commit")) {
            _date = new Date(0);
        } else {
            _date = new Date();
        }
    }

    /**Sets the blobs for new commit with TOADD and TORMV. **/
    public void setBlobs(HashMap<String, String> toAdd, HashMap<String, String> toRmv) {
        _blobs.putAll(toAdd);
        for (String each : toRmv.keySet()) {
            _blobs.remove(each);
        }
    }

    /**Deserializes PARENT commit. **/
    public Commit deserializeParent() {
        File file = Utils.join(COMMITS, this._parent);
        Commit res = Utils.readObject(file, Commit.class);
        return res;
    }

    /**Getter method for _blobs that returns _blobs variable. **/
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }

    /**Getter method for _parent of Commit. **/
    public String getParent() {
        return _parent;
    }

    /**Getter method for _parent2 of Commit. **/
    public String getParent2() {
        return _parent2;
    }

    /**Copies _blobs from parent to new commit. **/
    public void copyBlobs(Commit parent) {
        this._blobs = parent._blobs;
    }

    /**Writes commit to .gitlet. **/
    public void saveCommit() {
        byte[] content = Utils.serialize(this);
        String id = Utils.sha1(content);
        File toSave = Utils.join(COMMITS, id);
        Utils.writeObject(toSave, this);
    }

    /**Gets and returns date for commit. **/
    public Date getDate() {
        return _date;
    }

    /**Gets and returns message for commit. **/
    public String getMsg() {
        return _message;
    }

}
