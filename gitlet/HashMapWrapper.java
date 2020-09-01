package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**Wrapper class for our HashMap objects. **/
public class HashMapWrapper implements Serializable {

    /**HashMap of our Stage for Addition area, with String1 file name and String2 SHA-ID. **/
    private HashMap<String, String> _toAdd = new HashMap<String, String>();

    /**HashMap of our Stage for Remove area, with String1 file name and String2 SHA-ID. **/
    private HashMap<String, String> _toRmv = new HashMap<String, String>();

    /**Path to our metadata folder called .gitlet. **/
    private static final File GITLET = new File(".gitlet");

    /**Path to our head folder. **/
    private static final File HEAD = Utils.join(GITLET, "Head");

    /**Path to our commit folder. **/
    private static final File COMMITS = Utils.join(GITLET, "Commits");

    /**Path to our stage folder. **/
    private static final File STAGE = Utils.join(GITLET, "Stage");

    /** Sets the HASHMAP to our stored variable. **/
    public void setHashMap(HashMap<String, String> HashMap) {
        _toAdd = HashMap;
    }

    /**Returns our toAdd HashMap. **/
    public HashMap<String, String> get_toAdd() {
        return _toAdd;
    }

    /**Returns our toRmv HashMap. **/
    public HashMap<String, String> get_toRmv() {
        return _toRmv;
    }

}
