package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a while statement
 */

public final class WhileStmt extends Stmt {

    private Expr mexpr;

    private Stmt mstmt;

    public WhileStmt(Expr lexpr, Stmt lstmt) {
        setExpr(lexpr);
        setStmt(lstmt);
    }

    /**
     * Get the guard expression of the while loop
     *
     * @return Guard expression of the loop
     */
    public Expr getExpr() {
        return mexpr;
    }

    /**
     * Set the guard expression of the while loop
     *
     * @param lexpr Guard expression of the loop
     */
    public void setExpr(Expr lexpr) {
        mexpr = lexpr;
    }

    /**
     * Get the stmt of code protected by the lock
     *
     * @return List with the declared statements
     */
    public Stmt getStmt() {
        return mstmt;
    }

    /**
     * Set the ast node with while loop code
     *
     * @param lstmt Stmt node
     */
    public void setStmt(Stmt lstmt) {
        mstmt = lstmt;
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

