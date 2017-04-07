package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Declare a constant integer
 */

public class ConstBoolExpr extends Expr {

    private final boolean mconst;

    public ConstBoolExpr(boolean lconst) {
        //setConst(lconst);
        mconst = lconst;
    }

    /**
     * Get the constant value
     *
     * @return constant value
     */
    public boolean getValue() {
        return mconst;
    }

//   /**
//   * Set the constant value
//   * @param lconst constant value
//   */
//   public void setValue(boolean lconst) {
//      mconst = lconst;
//   }

    @Override
    public <R, A> R accept(final GenericVisitor<R, A> v, final A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(final VoidVisitor<A> v, final A arg) {
        v.visit(this, arg);
    }
}

