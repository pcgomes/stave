package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents the instiation of threads of a given type
 */

public final class StartThread extends Node {

    // Number of threads to start
    private int mnumber;

    // Name of the thread type
    private String mtype;

    public StartThread(int lnumber, String ltype) {
        setNumber(lnumber);
        setType(ltype);
    }

    /**
     * Get the name of the Thread type
     *
     * @return String with name of the thread type
     */
    public String getType() {
        return mtype;
    }

    /**
     * Set the name of the thread type being spawned
     *
     * @param ltype String with thread type's name
     */
    public void setType(String ltype) {
        mtype = ltype;
    }

    /**
     * Get how many threads of this type will be spawned
     *
     * @return int with number of threads from this type.
     */
    public int getNumber() {
        return mnumber;
    }

    /**
     * Set the number of threads of this type that will be spawned
     *
     * @param lnumber int with how many threads to start
     */
    public void setNumber(int lnumber) {
        mnumber = lnumber;
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

