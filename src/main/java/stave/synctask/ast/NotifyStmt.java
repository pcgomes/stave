package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a notify statement.
 */

public final class NotifyStmt extends Stmt {

    private String mcondition;

    public NotifyStmt(String lcondition) {
        setCondition(lcondition);
    }

    /**
     * Get the name of the condition variable to notify
     *
     * @return List with the declared threads
     */
    public String getCondition() {
        return mcondition;
    }

    /**
     * Set the name of the condition variable to notify
     *
     * @param lcondition String with the variable name
     */
    public void setCondition(String lcondition) {
        mcondition = lcondition;
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

