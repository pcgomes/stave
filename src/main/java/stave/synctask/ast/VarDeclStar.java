package stave.synctask.ast;

import java.util.Enumeration;
import java.util.Hashtable;
import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a list of variable declarations
 */

public final class VarDeclStar extends Node {

    private Hashtable<String, VarDecl> mvars;

    public VarDeclStar() {
        mvars = new Hashtable<String, VarDecl>();
    }

    public VarDeclStar(Hashtable<String, VarDecl> ldecls) {
        setVarDecls(ldecls);
    }

    /**
     * Merge two lists of variable declarations from the same 'Main' method of a SyncTask.
     * This is the case when a synctask is declared in different places.
     * If the same variable name is declared twice, the one in this list is preserved.
     *
     * @param lvars List of new variable declarations
     */
    public void mergeVarDecls(VarDeclStar lvars) {
        Hashtable<String, VarDecl> lvarlist = lvars.getVarDecls();

        for (Enumeration<VarDecl> e = lvarlist.elements(); e.hasMoreElements(); ) {
            VarDecl lnewdecl = e.nextElement();

            if (lvarlist.containsKey(lnewdecl.getVarName())) {
            /* There is already a variable declared with this name. */
                if (debug()) {
                    System.err.println("Warning - Variable '" + lnewdecl.getVarName() + "' was redeclared. Ignoring redeclarationi");
                }
            } else {
                // New type, just add it to the hashmap.
                addVarDecl(lnewdecl);
            }
        }
    }


    /**
     * Get the list of variables declaration and initialization
     *
     * @return List with the declared statements
     */
    public Hashtable<String, VarDecl> getVarDecls() {
        return mvars;
    }

    /**
     * Set the list of variables declaration and initialization
     *
     * @param ldecl List with the declared statements
     */
    public void setVarDecls(Hashtable<String, VarDecl> ldecls) {
        mvars = ldecls;
    }

    /**
     * Get a variable declaration from its name
     *
     * @return Object representing a variable declaration
     */
    public VarDecl getVarDecl(String pvarname) {
        return mvars.get(pvarname);
    }

    /**
     * Add a new variable declaration and initialization to the list
     *
     * @param ldecl node representing the triple with variable name, type and initial value
     */
    public void addVarDecl(VarDecl ldecl) {
        if (mvars.put(ldecl.getVarName(), ldecl) != null) {
            System.err.println("Warning: variable " + ldecl.getVarName() + " was redefined.");
        }
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

