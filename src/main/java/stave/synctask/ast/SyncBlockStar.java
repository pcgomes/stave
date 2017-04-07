package stave.synctask.ast;

import java.util.Vector;
import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents one Thread type, i.e.,
 * one type control flow in the SyncTask program.
 */

public final class SyncBlockStar extends Node {

    Vector<SyncBlockStmt> mblocks;

    public SyncBlockStar() {
        mblocks = new Vector<SyncBlockStmt>();
    }

    public SyncBlockStar(Vector<SyncBlockStmt> lblocks) {
        setSyncBlocks(lblocks);
    }

    /**
     * Get the list with the adjacent synchronized blocks
     *
     * @return List of SyncBlocks
     */
    public Vector<SyncBlockStmt> getSyncBlocks() {
        return mblocks;
    }

    /**
     * Set the list with the adjacent synchronized blocks
     *
     * @param lblocks List of SyncBlocks
     */
    public void setSyncBlocks(Vector<SyncBlockStmt> lblocks) {
        mblocks = lblocks;
    }

    /**
     * Add a new mutual exclusive block to this Thread type.
     *
     * @param nblock node representing a synchronization block
     */
    public void addSyncBlock(SyncBlockStmt lblock) {
        mblocks.add(lblock);
    }

    /**
     * Add a new mutual exclusive block to this Thread type.
     *
     * @param lposition position in the array to add
     * @param nblock    node representing a synchronization block
     */
    public void addSyncBlock(int lposition, SyncBlockStmt lblock) {
        mblocks.add(lposition, lblock);
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

