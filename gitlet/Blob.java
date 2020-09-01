package gitlet;

import java.io.File;
import java.io.Serializable;

/** The class constructor for our blob object.
 *  @author David Zhu
 */
public class Blob implements Serializable {

    /**Represents string of the file of the blob. **/
    private String _filename;

    /**Represents content of the string. **/
    private String _contents;

    /**Path to our metadata folder called .gitlet. **/
    private static final File GITLET = new File(".gitlet");

    /**Path to our blobs folder. **/
    private static final File BLOBS = Utils.join(GITLET, "Blobs");

    /**Constructor for blob. **/
    public Blob(String filename) {
        _filename = filename;
        File path = new File(_filename);
        //File path = new File(".\\testing\\src\\" + _filename);
        if (path.exists()) {
            _contents = Utils.readContentsAsString(path);
        }
    }

    /**
     * Saves blob object to blob folder in .gitlet.
     **/
    public void saveBlob() {
        byte[] content = Utils.serialize(this);
        String id = Utils.sha1(content);
        File toSave = Utils.join(BLOBS, id);
        Utils.writeObject(toSave, this);

    }

    /**
     * Retrieves blob object called FILENAME from out metadata folder
     * and returns BLOB.
     *
     * @param filename
     */
    public Blob getFile(String filename) {
        File name = Utils.join(BLOBS, filename);
        Blob res = Utils.readObject(name, Blob.class);
        return res;
    }

    /**Retrieves contents of the blob .**/
    public String getContents() {
        return _contents;
    }

}