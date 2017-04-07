package stave.java.annotation;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/*
Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>

Class that contains that meta-information extracted from by the annotation parser.
Used to extract the SyncBlock task from the JCTree AST.
*/

public class MetaSyncBlock extends MetaNode {

    protected String mthreadtype;
    protected Hashtable<String, String> mmaplock;
    protected Hashtable<String, String> mmapcondvar;

    // Map resource object to its SyncTask type
    protected Hashtable<String, String> mmapresource;

    MetaSyncBlock() {
        mthreadtype = null;
        mmaplock = new Hashtable<String, String>();
        mmapcondvar = new Hashtable<String, String>();
        mmapresource = new Hashtable<String, String>();
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

    public Enumeration<String> getMonitoredLocks() {
        return mmaplock.keys();
    }

    public Enumeration<String> getMonitoredCondvars() {
        return mmapcondvar.keys();
    }

    public Enumeration<String> getMonitoredResources() {
        return mmapresource.keys();
    }

    // A monitor adds automatically a lock and a condvar.
    public void addMonitorMap(String ljavavar, String lstvar) {
        addCondVarMap(ljavavar, lstvar + "_cond");
        addLockMap(ljavavar, lstvar + "_lock");
    }

    public void addLockMap(String ljavavar, String lstvar) {
        mmaplock.put(ljavavar, lstvar);
    }

    public boolean isLockMapped(JCExpression tree) throws Exception {
        return (mmaplock.get(tree.toString()) == null) ? false : true;
    }

    public String getSTLock(JCExpression tree) throws Exception {
        return getSTLock(tree.toString());
    }

    public String getSTLock(String lexpr) throws Exception {
        String mystcondlock = mmaplock.get(lexpr);

        if (mystcondlock == null) {
            throw new Exception("There is no SyncTask condlock mapping for " + lexpr);
        }

        return mystcondlock;
    }

    public void addCondVarMap(String ljavavar, String lstvar) {
        mmapcondvar.put(ljavavar, lstvar);
    }

    public boolean isCondVarMapped(JCExpression tree) throws Exception {
        return (mmapcondvar.get(tree.toString()) == null) ? false : true;
    }

    public String getSTCondVar(JCExpression tree) throws Exception {
        return getSTCondVar(tree.toString());
    }

    public String getSTCondVar(String lexpr) throws Exception {
        String mystcondlock = mmapcondvar.get(lexpr);

        if (mystcondlock == null) {
            throw new Exception("There is no SyncTask condlock mapping for " + lexpr);
        }

        return mystcondlock;
    }

    public void addResourceMap(String ljavavar, String lstvar) {
        mmapresource.put(ljavavar, lstvar);
    }

    public boolean isResourceMapped(JCExpression tree) throws IOException {

        return (mmapresource.get(tree.toString()) == null) ? false : true;
    }

    public String getSTResource(String lexpr) throws Exception {
        String lresource = mmapresource.get(lexpr);

        if (lresource == null) {
            throw new Exception("There is no SyncTask resource mapping for " + lexpr);
        }

        return lresource;
    }

    public String getSTResource(JCExpression tree) throws Exception {
        // Return the resource, or propagate the exception in case of failure
        return getSTResource(tree.toString());
    }

}
