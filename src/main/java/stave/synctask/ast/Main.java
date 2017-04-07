package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a the initialization method main.
 */

public final class Main extends Node {

    VarDeclStar mvars;
    StartThreadStar mthreads;

    public Main() {
        mvars = new VarDeclStar();
        mthreads = new StartThreadStar();
    }

    public Main(VarDeclStar ldecls, StartThreadStar lthreads) {
        setVarDecl(ldecls);
        setStartThreads(lthreads);
    }

    /**
     * Allows the merging of two main declarations into a single one
     * In case of conflict, invoking method has precedence.
     *
     * @param lmain a second declaration of main
     */
    public void mergeMain(Main lmain) {
        mvars.mergeVarDecls(lmain.getVarDecl());
        mthreads.mergeStartThreads(lmain.getStartThreads());
    }

    /**
     * Get the list of variables declaration and initialization
     *
     * @return List with the declared statements
     */
    public VarDeclStar getVarDecl() {
        return mvars;
    }

    /**
     * Set the list of variables declaration and initialization
     *
     * @param lvars List with the declared statements
     */
    public void setVarDecl(VarDeclStar lvars) {
        mvars = lvars;
    }

    /**
     * Get the list representing the global thread composition
     *
     * @return List of spawning threads
     */
    public StartThreadStar getStartThreads() {
        return mthreads;
    }

    /**
     * Set the list of all threads that are started
     *
     * @param lthreads List with the starting threads
     */
    public void setStartThreads(StartThreadStar lthreads) {
        mthreads = lthreads;
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

