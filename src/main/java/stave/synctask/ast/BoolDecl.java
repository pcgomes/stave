package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Generic declaration of boolean variables
 */

public class BoolDecl extends VarDecl {

    boolean mvalue;

    public BoolDecl(String lvarname, boolean lvalue) {
        super(lvarname);
        setValue(lvalue);
    }

    /**
     * Get the value of the boolean variable
     *
     * @return string with variable value
     */
    public boolean getValue() {
        return mvalue;
    }

    /**
     * Set the value of the boolean variable
     *
     * @param lvalue boolean variable
     */
    public void setValue(boolean lvalue) {
        mvalue = lvalue;
    }

    /**
     * Set the boolean variable to true
     */
    public void setFalse() {
        mvalue = false;
    }

    /**
     * Set the boolean variable to true
     */
    public void setTrue() {
        mvalue = true;
    }

    /**
     * Get the type of the declared variable
     *
     * @return Type of this
     */
    public Type getType() {
        return Type.BOOL;
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

