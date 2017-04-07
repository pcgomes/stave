package stave.synctask.ast;

import java.util.Hashtable;
import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a full SyncTask specification program.
 */

public final class ThreadTypeStar extends Node {

    private Hashtable<String, ThreadType> mthreads;

    public ThreadTypeStar() {
        mthreads = new Hashtable<String, ThreadType>();
    }

    public ThreadTypeStar(Hashtable<String, ThreadType> lthreads) {
        setThreadTypes(lthreads);
    }

    /**
     * Get the list of declared ThreadTypes
     *
     * @return List with the declared threads
     */
    public Hashtable<String, ThreadType> getThreadTypes() {
        return mthreads;
    }

    /**
     * Get the list of declared ThreadTypes
     *
     * @return List with the declared threads
     */
    public void setThreadTypes(Hashtable<String, ThreadType> lthreads) {
        mthreads = lthreads;
    }

    /**
     * Add a new thread type to the SyncTask program
     *
     * @param lttype AST node with thread code
     */
    public void addThreadType(ThreadType lttype) {
        if (mthreads.put(lttype.getTypeName(), lttype) != null) {
            System.err.println("Warning: Thread type " + lttype.getTypeName() + "was redefined");
        }
    }

    /**
     * If the ThreadType was already present, return it.
     * Otherwise create a new ThreadType object.
     *
     * @return Reference to thread type, or null if not present
     */
    public ThreadType getOrMakeNewThreadType(String lkey) {
        // Type was already there. Nothing to do.
        if (mthreads.containsKey(lkey)) {
            return mthreads.get(lkey);
        }

        // Create it and add to hash with threadtypes
        ThreadType lnewtype = new ThreadType(lkey);
        mthreads.put(lkey, lnewtype);

        return lnewtype;
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

