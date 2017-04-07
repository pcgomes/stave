package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Generic declaration of condition variables
 */

public class CondDecl extends VarDecl {
    private String mlock;

    public CondDecl(String lvarname, String llock) {
        super(lvarname);
        setLock(llock);
    }

    /**
     * Get the corresponding lock
     *
     * @return string with variable value
     */
    public String getLock() {
        return mlock;
    }

    /**
     * Set the corresponding lock
     *
     * @param llock String withe lock name
     */
    public void setLock(String llock) {
        mlock = llock;
    }

    /**
     * Get the type of the declared variable
     *
     * @return CondDecl of this
     */
    public Type getType() {
        return Type.COND;
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

