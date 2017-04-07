package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Declaration of bounded integer variables
 */

public class IntDecl extends VarDecl {
    private final int mlbound;
    private final int mubound;
    private int mvalue;

    public IntDecl(String lvarname, int llbound, int lubound) {
        super(lvarname);
        mlbound = llbound;
        mubound = lubound;
        if (!(llbound <= lubound)) {
            throw new NumberFormatException("Illegal bounded integer interval: [" + llbound + "," + lubound + "]");
        }
    }

    public IntDecl(String lvarname, int llbound, int lubound, int lvalue) throws NumberFormatException {
        super(lvarname);
        mlbound = llbound;
        mubound = lubound;
        setValue(lvalue);
        if (!((llbound <= lvalue) && (lvalue <= lubound))) {
            throw new NumberFormatException("Illegal bounded integer: " + lvalue + "in [" + llbound + "," + lubound + "]");
        }
    }

    /**
     * Get the smallest value that variable can have
     *
     * @return int integer's lower boundw
     */
    public int getLowerBound() {
        return mlbound;
    }

    /**
     * Get the biggest value that variable can have
     *
     * @return int integer's lower boundw
     */
    public int getUpperBound() {
        return mubound;
    }

    /**
     * Get the current
     *
     * @return integer's lower bound
     */
    public int getValue() {
        return mvalue;
    }

    /**
     * Set the integer's current value
     *
     * @param lvalue value to assign to integer
     */
    void setValue(int lvalue) {
        mvalue = lvalue;
    }

    /**
     * Get the type of the declared variable
     *
     * @return Type of this
     */
    public Type getType() {
        return Type.INT;
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

