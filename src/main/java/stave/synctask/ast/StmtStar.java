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

public final class StmtStar extends Node {

    Vector<Stmt> mstmts;

    public StmtStar() {
        mstmts = new Vector<Stmt>();
    }

    public StmtStar(Vector<Stmt> lstmts) {
        setStmts(lstmts);
    }

    /**
     * Get the list statements
     *
     * @return List of statements
     */
    public Vector<Stmt> getStmts() {
        return mstmts;
    }

    /**
     * Set the list with statements
     *
     * @param lstmts vector of statements
     */
    public void setStmts(Vector<Stmt> lstmts) {
        mstmts = lstmts;
    }

    /**
     * Add a new statement to the code block
     *
     * @param lstmt statement to be appended at code block
     */
    public void addStmt(Stmt lstmt) {
        mstmts.add(lstmt);
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

