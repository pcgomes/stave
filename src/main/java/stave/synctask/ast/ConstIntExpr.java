package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Declare a constant integer
 */

public class ConstIntExpr extends Expr {

    private int mconst;

    public ConstIntExpr(int lconst) {
        setValue(lconst);
    }

    /**
     * Get the constant value
     *
     * @return constant value
     */
    public int getValue() {
        return mconst;
    }

    /**
     * Set the constant value
     *
     * @param lconst constant integer
     */
    public void setValue(int lconst) {
        mconst = lconst;
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

