package stave.synctask.ast;

import stave.synctask.visitor.GenericVisitor;
import stave.synctask.visitor.VoidVisitor;

/**
 * AST Node for a SyncTask program
 *
 * Generic declaration of binary operation
 */

public class BopExpr extends Expr {
    private Expr mloper;
    private Expr mroper;
    private Expr.Op moperator;

    public BopExpr(Expr lloper, Expr lroper, Expr.Op loperator) {
        setLeftOperand(lloper);
        setRightOperand(lroper);
        setOperator(loperator);
    }

    /**
     * Get the left operand
     *
     * @return Expression of the left operand
     */
    public Expr getLeftOperand() {
        return mloper;
    }

    /**
     * Set the left-hand operand
     *
     * @param lloper Operand expression
     */
    public void setLeftOperand(Expr lloper) {
        mloper = lloper;
    }

    /**
     * Get the right operand
     *
     * @return Expression of the right operand
     */
    public Expr getRightOperand() {
        return mroper;
    }

    /**
     * Set the right operand
     *
     * @param lroper Operand expression
     */
    public void setRightOperand(Expr lroper) {
        mroper = lroper;
    }

    /**
     * Get the operator of the binary operation
     *
     * @return Operator of the binary expression
     */
    public Expr.Op getOperator() {
        return moperator;
    }

    /**
     * Set the operator of the binary expression
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

