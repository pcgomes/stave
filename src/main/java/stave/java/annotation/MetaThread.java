package stave.java.annotation;

/*
Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>

Class that contains that meta-information extracted from by the annotation parser.
*/

public class MetaThread extends MetaNode {

    protected int mspawned = 1;
    protected String mthreadtype = null;

    // Add a single thread to be spawned.
    // Type must be inferred later.
    MetaThread() {
    }

    // Add a single thread to be spawned
    MetaThread(String lttype) {
        setThreadType(lttype);
    }

    // Add a single thread to be spawned. Store its Java name
    MetaThread(String lttype, String ltname) {
        setThreadType(lttype);
        setId(ltname);
    }

    MetaThread(String lttype, int lamount) {
        setThreadType(lttype);
        setSpawned(lamount);
    }

    MetaThread(int lamount, String ltname, String lttype) {
        this(ltname, lttype);
        setSpawned(lamount);
    }

    public boolean isThreadTypeSet() {
        return (mthreadtype != null);
    }

    public String getThreadType() {
        return mthreadtype;
    }

    public void setThreadType(String lttype) {
        mthreadtype = lttype;
    }

    public int getSpawned() {
        return mspawned;
    }

    public void setSpawned(int lamount) {
        mspawned = lamount;
    }
}
