package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Generic declaration of unary operation
 */

public class UopExpr extends Expr {
    private Expr moperand;
    private Expr.Op moperator;

    public UopExpr(Expr loperand, Expr.Op loperator) {
        setOperand(loperand);
        setOperator(loperator);
    }

    /**
     * Get the operand
     *
     * @return Expression of the left operand
     */
    public Expr getOperand() {
        return moperand;
    }

    /**
     * Set the operand
     *
     * @param loperand Operand expression
     */
    public void setOperand(Expr loperand) {
        moperand = loperand;
    }

    /**
     * Get the operator of the unary operation
     *
     * @return Operator of the unary expression
     */
    public Expr.Op getOperator() {
        return moperator;
    }

    /**
     * Set the operator of the unary expression
     *
     * @param loperator Operation
     */
    public void setOperator(Expr.Op loperator) {
        moperator = loperator;
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

