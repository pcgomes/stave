package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents an if-else statement
 */

public final class IfElseStmt extends Stmt {

    private Expr mexpr;

    private Stmt mstmtif;

    private Stmt mstmtelse;

    public IfElseStmt(Expr lexpr, Stmt lstmtif, Stmt lstmtelse) {
        setExpr(lexpr);
        setStmtIf(lstmtif);
        setStmtElse(lstmtelse);
    }

    /**
     * Get the boolean expression evaluated for branching
     *
     * @return Expr containing the branching expression
     */
    public Expr getExpr() {
        return mexpr;
    }

    /**
     * Set the boolean expression evaluated for branching
     *
     * @param lexpr boolean expression of the if-else
     */
    public void setExpr(Expr lexpr) {
        mexpr = lexpr;
    }

    /**
     * Get the statement executed if expression is true
     *
     * @return List with the declared statements
     */
    public Stmt getStmtIf() {
        return mstmtif;
    }

    /**
     * Set the statement executed if expression is true
     *
     * @param lstmt Stmt node of the if branch
     */
    public void setStmtIf(Stmt lstmt) {
        mstmtif = lstmt;
    }

    /**
     * Get the statement executed if expression is false
     *
     * @return Reference to the Else stmt
     */
    public Stmt getStmtElse() {
        return mstmtelse;
    }

    /**
     * Set the ast node with while loop code
     *
     * @param lstmt Stmt node of the else branch
     */
    public void setStmtElse(Stmt lstmt) {
        mstmtelse = lstmt;
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

