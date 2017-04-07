package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a full SyncTask specification program.
 */

public final class Program extends Node {

    private ThreadTypeStar mthreads;
    private Main mmain;

    /**
     * Creates an empty program
     */
    public Program() {
        mthreads = new ThreadTypeStar();
        mmain = new Main();
    }

    public Program(ThreadTypeStar lthreads, Main lmain) {
        setThreadTypes(lthreads);
        setMain(lmain);
    }

    /**
     * Get the list of declared ThreadTypes
     *
     * @return List with the declared threads
     */
    public ThreadTypeStar getThreadTypes() {
        return mthreads;
    }

    /**
     * Get the list of declared ThreadTypes
     *
     * @return List with the declared threads
     */
    public void setThreadTypes(ThreadTypeStar lthreads) {
        mthreads = lthreads;
    }

    /**
     * Get the node representing the declaration of main block
     *
     * @return Node with the declaration of main block
     */
    public Main getMain() {
        return mmain;
    }

    /**
     * Set the node
     */
    public void setMain(Main lmain) {
        mmain = lmain;
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

