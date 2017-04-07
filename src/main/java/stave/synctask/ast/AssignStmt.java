package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * This class represents the assignment to a variable
 */

public final class AssignStmt extends Stmt {

    VarNameExpr mtargetvar;

    Expr mexpr;

    public AssignStmt(VarNameExpr ltarget, Expr lexpr) {
        setTargetVar(ltarget);
        setExpr(lexpr);
    }

    /**
     * Get the name of variable to be assigned
     *
     * @return object with variable name
     */
    public VarNameExpr getTargetVar() {
        return mtargetvar;
    }

    /**
     * Set the variable to be assigned
     *
     * @param nvar variable to be assigned
     */
    public void setTargetVar(VarNameExpr lvar) {
        mtargetvar = lvar;
    }

    /**
     * Get the right-hand side of the assignment
     *
     * @return Expression assigned to variable
     */
    public Expr getExpr() {
        return mexpr;
    }

    /**
     * Set the right-hand side of the assignment
     *
     * @param nval value to assign to expression
     */
    public void setExpr(Expr lexpr) {
        mexpr = lexpr;
    }

    @Override
    public <A> void accept(final VoidVisitor<A> v, final A arg) {
        v.visit(this, arg);
    }

    @Override
    public <R, A> R accept(final GenericVisitor<R, A> v, final A arg) {
        return v.visit(this, arg);
    }
}

