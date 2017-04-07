package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Expression minimum value of an integer variable
 */

public class MinExpr extends Expr {

    private String mvarname;

    public MinExpr(String lvarname) {
        setVarName(lvarname);
    }

    /**
     * Get the variable name
     *
     * @return String representing the variable name
     */
    public String getVarName() {
        return mvarname;
    }

    /**
     * Set the variable name
     *
     * @param lvarname Variable name
     */
    public void setVarName(String lvarname) {
        mvarname = lvarname;
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

