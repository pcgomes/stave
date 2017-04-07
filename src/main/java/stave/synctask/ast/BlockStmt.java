package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a general code block.
 */

public final class BlockStmt extends Stmt {

    StmtStar mstmts;

    public BlockStmt() {
        mstmts = new StmtStar();
    }

    public BlockStmt(StmtStar lstatement) {
        setStmts(lstatement);
    }

    /**
     * Get the block of code protected by the lock
     *
     * @return List with the declared statements
     */
    public StmtStar getStmts() {
        return mstmts;
    }

    /**
     * Set the ast node with code block
     *
     * @param lstmt List of all statements declared in the block
     */
    public void setStmts(StmtStar lstmts) {
        mstmts = lstmts;
    }

    /**
     * Method used to easily add a new statement to the StmtList
     *
     * @oaram lstmt new statement
     */
    public void addStmt(Stmt lstmt) {
        mstmts.addStmt(lstmt);
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

