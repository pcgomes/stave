package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents one Thread type, i.e.,
 * one type control flow in the SyncTask program.
 */

public final class ThreadType extends Node {

    private String mtypename = null;

    private SyncBlockStar mblocks = null;

    public ThreadType(String ltypename) {
        setTypeName(ltypename);
        mblocks = new SyncBlockStar();
    }

    public ThreadType(String ltypename, SyncBlockStar lblocks) {
        setTypeName(ltypename);
        setSyncBlocks(lblocks);
    }

    /**
     * Get the name of the declated thread type
     *
     * @return List of SyncBlocks
     */
    public String getTypeName() {
        return mtypename;
    }

    /**
     * Set the name of the declated thread type
     *
     * @param String with nm
     */
    public void setTypeName(String ltypename) {
        mtypename = ltypename;
    }

    /**
     * Get the list with the adjacent synchronized blocks
     *
     * @return List of SyncBlocks
     */
    public SyncBlockStar getSyncBlocks() {
        return mblocks;
    }

    /**
     * Set the list with the adjacent synchronized blocks
     *
     * @param lblocks List of SyncBlocks
     */
    public void setSyncBlocks(SyncBlockStar lblocks) {
        mblocks = lblocks;
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

