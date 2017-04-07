package stave.java.annotation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.Pretty;
import java.io.StringWriter;
import stave.synctask.ast.Expr;
import stave.synctask.ast.Node;
import stave.synctask.visitor.PrettyPrintVisitor;

/**
 * @author: Pedro de Carvalho Gomes <pedrodcg@kth.se>
 *
 * Generic class that implements functionalities to compare JCTree AST nodes.
 */

public abstract class MetaNode {

    // Object's uniqude id in the SyncTask.
    protected String mid = null;

    MetaNode() {
    }

    public String getId() {
        return mid;
    }

    public void setId(String lid) {
        mid = lid;
    }

    public boolean isIdSet() {
        return mid != null;
    }

    public String getSimpleName(JCExpression tree) {
        // Generate String with full qualified name and take right-most part
        return getSimpleName(tree.toString());
    }

    public String getSimpleName(String lname) {
        int lperiod = lname.lastIndexOf('.');

        // Name had no hierarchy info - returning "as-is"
        if (lperiod == -1) {
            return lname;
        }

        // Return substring after last period
        return (lname.substring(lperiod + 1, lname.length()));
    }

    public String getHierarchy(JCExpression tree) {
        // Generate String with full qualified name and take right-most part
        return (getHierarchy(tree.toString()));
    }

    public String getHierarchy(String lname) {
        int lperiod = lname.lastIndexOf('.');

        // Name had no hierarchy info - Adding this
        if (lperiod == -1) {
            return new String("this");
        }

        //  Return everything before the last comma
        return (lname.substring(0, lperiod));
    }

    // Take a parenthesis expression such as ((expr))
    // and unfold it until it becomes expr
    public JCExpression trimParens(JCExpression tree) {
        while (tree.getTag() == JCTree.Tag.PARENS) {
            tree = ((JCParens) tree).expr;
        }
        return tree;
    }

    public Expr.Op convertOperator(JCTree.Tag tag) {
        switch (tag) {
            case NOT:
                return Expr.Op.NEGA;
            case POSTINC:
                return Expr.Op.INCR;
            case POSTDEC:
                return Expr.Op.DECR;
            case OR:
                return Expr.Op.LOOR;
            case AND:
                return Expr.Op.LAND;
            case EQ:
                return Expr.Op.EQUA;
            case NE:
                return Expr.Op.NTEQ;
            case LT:
                return Expr.Op.LOWT;
            case GT:
                return Expr.Op.BIGT;
            case PLUS:
                return Expr.Op.ADDI;
            case MINUS:
                return Expr.Op.MINU;
         /*
      case JCTree.Tag.PREINC:  return Expr.Op.;
	 case JCTree.Tag.PREDEC:  return Expr.Op.;
	 case JCTree.Tag.LE:      return Expr.Op.;
	 case JCTree.Tag.GE:      return Expr.Op.;
	 case JCTree.Tag.BITOR:   return Expr.Op.;
	 case JCTree.Tag.BITXOR:  return Expr.Op.;
	 case JCTree.Tag.BITAND:  return Expr.Op.;
	 case JCTree.Tag.MUL:     return Expr.Op.ERRO;
	 case JCTree.Tag.DIV:     return Expr.Op.ERRO;
	 case JCTree.Tag.MOD:     return Expr.Op.ERRO;
         */
            default:
                throw new Error("Unsupported operator: " + (new Pretty(new java.io.StringWriter(), false)).operatorName(tag));
        }


    }

    // Prints a SyncTask code snippet with root node lentry
    public StringWriter printSyncTaskCode(Node lentry) {
        PrettyPrintVisitor myvisitor = new PrettyPrintVisitor(new StringWriter());
        myvisitor.visit(lentry, new Integer(0));
        return ((StringWriter) myvisitor.getWriter());
    }
}
