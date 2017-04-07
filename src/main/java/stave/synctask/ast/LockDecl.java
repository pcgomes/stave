package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Declaration of reentrant locks
 */

public class LockDecl extends VarDecl {

    public LockDecl(String lvarname) {
        super(lvarname);
    }

    /**
     * Get the type of the declared variable
     *
     * @return Type of this
     */
    public Type getType() {
        return Type.LOCK;
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

