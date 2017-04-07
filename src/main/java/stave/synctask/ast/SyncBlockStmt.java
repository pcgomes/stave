package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a mutual exclusion block.
 */

public final class SyncBlockStmt extends Stmt {

    private String mlockname;

    private BlockStmt mblock;

    public SyncBlockStmt(String llock) {
        setLock(llock);
    }

    public SyncBlockStmt(String llock, BlockStmt lblock) {
        setLock(llock);
        setBlock(lblock);
    }

    /**
     * Get the name of the lock variable guaranteeing the mutual exclusion.
     *
     * @return List with the declared threads
     */
    public String getLock() {
        return mlockname;
    }

    /**
     * Set the name of variable that guarantees the mutual exclusion.
     */
    public void setLock(String llock) {
        mlockname = llock;
    }

    /**
     * Get the block of code protected by the lock
     *
     * @return List with the declared statements
     */
    public BlockStmt getBlock() {
        return mblock;
    }

    /**
     * Set the ast node with code block
     *
     * @param lblock Set node with code block
     */
    public void setBlock(BlockStmt lblock) {
        mblock = lblock;
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

