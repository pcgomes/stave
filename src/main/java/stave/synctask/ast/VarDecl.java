package stave.synctask.ast;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a generic variable
 */

public abstract class VarDecl extends Node {

    private String mvarname;

    public VarDecl(String lvarname) {
        setVarName(lvarname);
    }

    /**
     * Get the name of the declared variable
     *
     * @return string with variable name
     */
    public String getVarName() {
        return mvarname;
    }

    /**
     * Set the type of the declared variable
     *
     * @param lvarname string with variable name
     */
    public void setVarName(String lvarname) {
        mvarname = lvarname;
    }

    /**
     * Get the type of the declared variable
     *
     * @return variable type
     */
    public abstract Type getType();

}

