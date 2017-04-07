package stave.synctask.ast;

import java.util.Enumeration;
import java.util.Hashtable;
import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a the global thread composition
 */

public final class StartThreadStar extends Node {

    Hashtable<String, StartThread> mthreads;

    public StartThreadStar() {
        mthreads = new Hashtable<String, StartThread>();
    }

    public StartThreadStar(Hashtable<String, StartThread> lthreads) {
        setStartThreads(lthreads);
    }

    /**
     * Merges a list of starting threads with this one.
     *
     * @param lthreads List of start threads to be merget
     */
    public void mergeStartThreads(StartThreadStar lthreads) {
        Hashtable<String, StartThread> lthreadlist = lthreads.getStartThreads();
        for (Enumeration<StartThread> e = lthreadlist.elements(); e.hasMoreElements(); ) {
            StartThread lnewtype = e.nextElement();

            if (lthreadlist.containsKey(lnewtype.getType())) {
            /* There is already a set of thread of this type being initialized.
            *  Add the amount of started threads.
            */
                StartThread ldecl = lthreadlist.get(lnewtype.getType());
                ldecl.setNumber(ldecl.getNumber() + lnewtype.getNumber());
            } else {
                // New type, just add it to the hashmap.
                addStartThread(lnewtype);
            }
        }
    }

    /**
     * Get the list representing the global thread composition
     *
     * @return List of spawning threads
     */
    public Hashtable<String, StartThread> getStartThreads() {
        return mthreads;
    }

    /**
     * Set the list of all threads that are started
     *
     * @param lthreads List with the starting threads
     */
    public void setStartThreads(Hashtable<String, StartThread> lthreads) {
        mthreads = lthreads;
    }

    /**
     * Add a new thread to the list of the ones that will be started
     *
     * @param nthread node representing a new starting thread
     */
    public void addStartThread(StartThread lthread) {
        if (mthreads.put(lthread.getType(), lthread) != null) {
            System.err.println("Warning: declaration of thread " + lthread.getType() + " was redefined.");
        }
    }

    @Override
    public <R, A> R accept(final GenericVisitor<R, A> v, final A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(final VoidVisitor<A> v, final A arg) {
        v.visit(this, arg);
    }
}

