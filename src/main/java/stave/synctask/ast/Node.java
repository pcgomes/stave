package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * Generic node in the abstract syntax tree of a SyncTask program
 */

public abstract class Node {

    private static boolean debug = false;

    private Node mparent = null;

    /**
     * Set debug flag on. Used for priting warnings.
     */
    public static void debugOn() {
        debug = true;
    }

    /**
     * Set debug flag off. Used for priting warnings.
     */
    public static void debugOff() {
        debug = false;
    }

    /**
     * Return if debug flag is set or not
     *
     * @return True if debus flag is set. False otherwise
     */
    public static boolean debug() {
        return debug;
    }

    /**
     * Accept method for visitor support.
     *
     * @param v   the visitor implementation
     * @param arg visitor's argument
     */
    public abstract <A> void accept(VoidVisitor<A> v, A arg);

    /**
     * Accept method for visitor support.
     *
     * @param v   the visitor implementation
     * @param arg visitor's argument
     * @return parametrized type
     */
    public abstract <R, A> R accept(GenericVisitor<R, A> v, A arg);

    /**
     * Return the parent node in the abstract syntax tree
     *
     * @return reference to parent element in the AST
     */
    public Node getParent() {
        return mparent;
    }

    /**
     * Return the parent node in the abstract syntax tree
     *
     * @return reference to parent element in the AST
     */
    public void setParent(Node lparent) {
        mparent = lparent;
    }
}
