package stave.java.annotation;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import java.util.Enumeration;
import java.util.Hashtable;
import stave.synctask.ast.BoolDecl;
import stave.synctask.ast.CondDecl;
import stave.synctask.ast.IntDecl;
import stave.synctask.ast.LockDecl;
import stave.synctask.ast.VarDecl;

/*
Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>

Class that contains that meta-information extracted from by the annotation parser.
Used to extract the SyncTask task from the JCTree AST.
*/

public class MetaSyncTask extends MetaNode {

    // The following structures map Java names to SyncTask names.
    // They are used for tracking occurances of SyncTask objects in the Java code.
    protected Hashtable<String, String> mmapcondvar;
    protected Hashtable<String, String> mmaplock;
    protected Hashtable<String, String> mmapresource;

    // Store the items defined for the Main block
    protected Hashtable<String, Integer> mmapthread;
    protected Hashtable<String, VarDecl> mvardecl;

    MetaSyncTask() {
        // Initialize the stringmaps
        mmaplock = new Hashtable<String, String>();
        mmapcondvar = new Hashtable<String, String>();
        mmapresource = new Hashtable<String, String>();

        mvardecl = new Hashtable<String, VarDecl>();
        mmapthread = new Hashtable<String, Integer>();
    }

    public void addLockMap(String ljavavar, String lstvar) {
        mmaplock.put(ljavavar, lstvar);

        addLockDecl(lstvar);
    }

    public boolean isLockMapped(String tree) {
        return (mmaplock.get(tree) == null) ? false : true;
    }

    public boolean isLockMapped(JCExpression tree) {
        return isLockMapped(tree.toString());
    }

    public String getSTLock(JCExpression tree) throws Exception {
        return getSTLock(tree.toString());
    }

    public String getSTLock(String lexpr) throws Exception {
        String lstcondlock = mmaplock.get(lexpr);

        if (lstcondlock == null) {
            throw new Exception("There is no SyncTask condlock mapping for " + lexpr);
        }

        return lstcondlock;
    }

    public void addCondVarMap(String ljavavar, String lstvar) {
        mmapcondvar.put(ljavavar, lstvar);

        addCondDecl(lstvar, "");
    }

    public boolean isCondVarMapped(String tree) {
        return (mmapcondvar.get(tree) == null) ? false : true;
    }

    public boolean isCondVarMapped(JCExpression tree) {
        return isCondVarMapped(tree.toString());
    }

    public String getSTCondition(JCExpression tree) throws Exception {
        return getSTCondition(tree.toString());
    }

    public String getSTCondition(String lexpr) throws Exception {
        String lstcondlock = mmapcondvar.get(lexpr);

        if (lstcondlock == null) {
            throw new Exception("There is no SyncTask condlock mapping for " + lexpr);
        }

        return lstcondlock;
    }

    // Add lock and condvar at once, adding the necessary suffixes
    public void addMonitorMap(String ljavavar, String lstvar) {
        String mylock = new String(lstvar + "_lock");
        String mycond = new String(lstvar + "_cond");

        mmaplock.put(ljavavar, mylock);
        mmapcondvar.put(ljavavar, mycond);

        // Set the
        addLockDecl(mylock);
        addCondDecl(mycond, mylock);
    }

    public void addResourceMap(String ljavavar, String lstvar) {
        mmapresource.put(ljavavar, lstvar);
    }

    public boolean isResourceMapped(String tree) {
        return mmapresource.containsKey(tree);
    }

    public boolean isResourceMapped(JCExpression tree) {
        return isResourceMapped(tree.toString());
    }

    public String getSTResource(String lexpr) {

        return mmapresource.get(lexpr);
    }

    public String getSTResource(JCExpression tree) {
        return getSTResource(tree.toString());
    }

    // Adds a new thread to be spawned in SyncTask
    public void addStartThread(String lstart, int lamount) throws Exception {
        if (lamount < 0) {
            throw new Exception("Tried to set a negative amount of threads to " + lstart);
        }

        if (mmapthread.containsKey(lstart)) {
            // Thread Type was already there. Adding spawned threads to its description.
            Integer lcurrent = mmapthread.get(lstart);
            mmapthread.put(lstart, lcurrent + new Integer(lamount));
        } else {
            // New thread type.
            mmapthread.put(lstart, new Integer(lamount));
        }
    }

    // Increment the counter for a given thread type.
    public void addStartThread(String lstart) throws Exception {
        addStartThread(lstart, 1);
    }

    public void addStartThread(MetaThread lstart) throws Exception {
        addStartThread(lstart.getThreadType(), lstart.getSpawned());
    }

    // Get the amount of spawned thread for this given thread type.
    public int getThreadAmount(String ltype) {
        if (mmapthread.containsKey(ltype)) {
            return mmapthread.get(ltype);
        }

        return 0;
    }

    public Enumeration<String> availableThreadTypes() {
        return mmapthread.keys();
    }

    public Enumeration<VarDecl> getVarDecls() {
        return mvardecl.elements();
    }

    public VarDecl getVarDecl(String lvar) {
        return mvardecl.get(lvar);
    }


   /* Methods to add a variable declaration to the SyncTask program */

    public void addLockDecl(String lname) {
        mvardecl.put(lname, new LockDecl(lname));
    }

    public void addCondDecl(String lname, String llock) {
        mvardecl.put(lname, new CondDecl(lname, llock));
    }

    public void addBoolDecl(String lname, boolean ldefault) {
        mvardecl.put(lname, new BoolDecl(lname, ldefault));
    }

    public void addIntDecl(String lname, int llbound, int llupper, int ldefault) {
        mvardecl.put(lname, new IntDecl(lname, llbound, llupper, ldefault));
    }

   /* Visualization methods */

    public void dumpMetaSyncTask() {
        System.out.println("--- SyncTask " + getId() + " ---");

      /* Print list of condition variables */
        Enumeration<String> e = mmapcondvar.keys();
        if (e.hasMoreElements()) {
            System.out.println("Condition variables: ");
            while (e.hasMoreElements()) {
                String mycond = e.nextElement();
                System.out.println("- " + mycond + " -> " + mmapcondvar.get(mycond));
            }
        }

      /* Print list of resources */
        e = mmapresource.keys();
        if (e.hasMoreElements()) {
            System.out.println("Resources: ");
            while (e.hasMoreElements()) {
                String myres = e.nextElement();
                System.out.println("- " + myres + " -> " + mmapresource.get(myres));
            }
        }

      /* Map threads */
        e = mmapthread.keys();
        if (e.hasMoreElements()) {
            System.out.println("Threads: ");
            while (e.hasMoreElements()) {
                String mythread = e.nextElement();
                System.out.println("- " + mythread + "[" + mmapthread.get(mythread) + "]");
            }
        }
    }

}
