package stave.java.visitor;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.util.List;
import java.util.HashSet;
import java.util.Hashtable;
import stave.synctask.ast.AssignStmt;
import stave.synctask.ast.BlockStmt;
import stave.synctask.ast.BopExpr;
import stave.synctask.ast.ConstBoolExpr;
import stave.synctask.ast.ConstIntExpr;
import stave.synctask.ast.Expr;
import stave.synctask.ast.IfElseStmt;
import stave.synctask.ast.Node;
import stave.synctask.ast.SkipStmt;
import stave.synctask.ast.Stmt;
import stave.synctask.ast.UopExpr;
import stave.synctask.ast.VarNameExpr;
import stave.synctask.ast.WhileStmt;


public class Java2SyncTaskVisitor extends AnnotationParsingVisitor {

    protected Hashtable<String, Expr> contextpred;
    protected Hashtable<String, Stmt> contextoper;
    protected HashSet<String> contextobjects;
    protected Node mnode;

    Java2SyncTaskVisitor() {
        contextpred = new Hashtable<String, Expr>();
        contextoper = new Hashtable<String, Stmt>();
        contextobjects = new HashSet<String>();
    }

    Java2SyncTaskVisitor(Hashtable<String, Expr> lexprs, Hashtable<String, Stmt> lstmts) {
        this();

        contextpred.putAll(lexprs);
        contextoper.putAll(lstmts);
    }

    public static Expr.Op convertOp(JCTree.Tag lop) {
        switch (lop) {
            // !
            case NOT:
                return Expr.Op.NEGA;
            // ++
            case POSTINC:
            case PREINC:
                return Expr.Op.INCR;
            // --
            case POSTDEC:
            case PREDEC:
                return Expr.Op.DECR;
            // ||
            case OR:
                return Expr.Op.LOOR;
            // &&
            case AND:
                return Expr.Op.LAND;
            // ==
            case EQ:
                return Expr.Op.EQUA;
            // !=
            case NE:
                return Expr.Op.NTEQ;
            // <
            case LT:
                return Expr.Op.LOWT;
            // >
            case GT:
                return Expr.Op.BIGT;
            // += is being grouped  here also for conviniency
            case PLUS_ASG:
                // +
            case PLUS:
                return Expr.Op.ADDI;
            // -
            case MINUS_ASG:
                // -= is being grouped  here also for conviniency
            case MINUS:
                return Expr.Op.MINU;

            // Unsupported operators are marked with error
            default:
                return Expr.Op.ERRO;
        }
    }

    // Appending list of strings that are mapped to expressions
    public void addContextPredicates(Hashtable<String, Expr> lexprs) {
        contextpred.putAll(lexprs);
    }

    // Adding a single predicate to the list
    public void addPredToContext(String lcode, Expr lstcode) {
        contextpred.put(lcode, lstcode);
    }

    // Appending list of string taht are mapped to operations
    public void addContextOperations(Hashtable<String, Stmt> lstmts) {
        contextoper.putAll(lstmts);
    }

    // Adding a single operation to the list
    public void addOperToContext(String lcode, Stmt lstcode) {
        contextoper.put(lcode, lstcode);
    }

    protected boolean hasPredicate(String lname) {
        return contextpred.containsKey(lname);
    }

    protected boolean hasOperation(String lname) {
        return contextoper.containsKey(lname);
    }

    protected Expr getPred(String lname) {
        return contextpred.get(lname);
    }

    protected Stmt getOper(String lname) {
        return contextoper.get(lname);
    }

    protected Node getASTNode(String lname) {
        Node rnode = getPred(lname);
        if (rnode != null) {
            return rnode;
        }

        return getOper(lname);
    }

    public void addMappedObject(String lname) {
        contextobjects.add(lname);
    }

    public boolean isMappedObject(String lname) {
        return contextobjects.contains(lname);
    }

   /* Visitor Methods
   * Visitor starts from method level since only code is analyzed
   */

    public Node getResultNode() {
        return mnode;
    }

    public void visitMethodDef(JCMethodDecl tree) {
        debug("Java2SyncTaskVisitor.visitMethodDef: " + tree.name.toString());
        //scan(tree.mods);
        //scan(tree.typarams);
        //scan(tree.restype);
        //scan(tree.params);

        scan(tree.body);

        //Removing BlockStmt layer, if there's such
        //if (mnode instanceof BlockStmt) mnode = ((BlockStmt)mnode).getStmts();

    }

    // Simply translate to a SyncTask skip
    public void visitSkip(JCSkip tree) {
        mnode = new SkipStmt();
    }

    // Visit declarations inside a block, an process them one by one.
    public void visitBlock(JCBlock tree) {
        debug("Java2SyncTaskVisitor.visitBlock");

        BlockStmt myblock = new BlockStmt();

        for (List<? extends JCTree> l = tree.stats; l.nonEmpty(); l = l.tail) {
            l.head.accept(this);
            if ((mnode != null) && (mnode instanceof Stmt)) {
                myblock.addStmt((Stmt) mnode);
            }
        }

        // Only assign mnode if a statement.
        // Otherwise, it's an Expr and its last value should be kept and returned.
        if (mnode instanceof Stmt) {
            mnode = myblock;
        }
    }

    public void visitWhileLoop(JCWhileLoop tree) {
        debug("Java2SyncTaskVisitor.visitWhileLoop: while (" + tree.cond.toString() + ")");
        Expr mycond;
        Stmt mybody;

        if (hasPredicate(tree.cond.toString())) {
            //debug("Java2SyncTaskVisitor.visitWhileLoop: valid expression");
            mycond = getPred(tree.cond.toString());
        } else {
            tree.cond.accept(this);
            // Check if got a valid object
            if ((mnode != null) && (mnode instanceof Expr)) {
                //debug("Java2SyncTaskVisitor.visitWhileLoop: found valid expression");
                mycond = (Expr) mnode;
            } else {
                //debug("Java2SyncTaskVisitor.visitWhileLoop: Bad expression. Leaving");
                mnode = null;
                return;
            }
        }

        if (hasOperation(tree.body.toString())) {
            mybody = getOper(tree.body.toString());
        } else {
            tree.body.accept(this);
            if ((mnode != null) && (mnode instanceof Stmt)) {
                //debug("Java2SyncTaskVisitor.visitWhileLoop: found valid loop body");
                mybody = (Stmt) mnode;
            } else {
                //debug("Java2SyncTaskVisitor.visitWhileLoop: No valid loop body");
                mnode = null;
                return;
            }
        }

        mnode = new WhileStmt(mycond, mybody);
    }

    // SyncTask has just if-else. In case of pure if, append skip.
    public void visitIf(JCIf tree) {
        debug("Java2SyncTaskVisitor.visitIF: " + tree.toString());

        Expr mycond;
        Stmt myif;
        Stmt myelse;

        if (hasPredicate(tree.cond.toString())) {
            mycond = getPred(tree.cond.toString());
        } else {
            tree.cond.accept(this);
            mycond = (Expr) mnode;
        }

        if (hasOperation(tree.thenpart.toString())) {
            myif = getOper(tree.thenpart.toString());
        } else {
            tree.thenpart.accept(this);
            myif = (Stmt) mnode;
        }

        // Pure if (without else) is marked as null.
        if (tree.elsepart == null) {
            myelse = new SkipStmt();
        } else {
            if (hasOperation(tree.elsepart.toString())) {
                myelse = getOper(tree.elsepart.toString());
            } else {
                tree.elsepart.accept(this);
                myelse = (Stmt) mnode;
            }
        }

        mnode = new IfElseStmt(mycond, myif, myelse);
    }

    // This is typically used incapsulating method invocations where the
    // return type is either void, or is ignored. E.g: doSome();
    // Must be mapped by a statement or ignored.
    public void visitExec(JCExpressionStatement tree) {
        debug("Java2SyncTaskVisitor.visitExec: " + tree.toString());
        if (hasOperation(tree.expr.toString())) {
            mnode = getOper(tree.expr.toString());
        } else {
            // visit the expression.
            tree.expr.accept(this);
        }
    }

    // So far return just "forward" the return expression. Maybe should check if method is void.
    public void visitReturn(JCReturn tree) {
        debug("Java2SyncTaskVisitor.visitReturn: " + tree.toString());
        tree.expr.accept(this);
    }

    // Only assign something if this has been mapped
    public void visitApply(JCMethodInvocation tree) {
        debug("Java2SyncTaskVisitor.visitApply: " + tree.toString());

        if (hasPredicate(tree.toString())) {
            mnode = getPred(tree.toString());
        } else if (hasOperation(tree.toString())) {
            mnode = getOper(tree.toString());
        } else {
            tree.meth.accept(this);
        }
    }

    // Not sure how to treat parenthesis now. So far, ignoring it.
    public void visitParens(JCParens tree) {
        debug("Java2SyncTaskVisitor.visitParens: " + tree.toString());
        tree.expr.accept(this);
    }

    // So far, ignoring exceptional flow.
    public void visitTry(JCTry tree) {
        debug("Java2SyncTaskVisitor.visitTry.");
        tree.body.accept(this);
    }

    public void visitAssign(JCAssign tree) {
        Expr mylhs;
        Expr myrhs;

        //if (! hasPredicate( tree.lhs.toString() ) )
        //   throw new UnsupportedJavaFeatureException("Assignment to unmapped Id(" + tree.name.toString() + ")");

        if (hasPredicate(tree.lhs.toString())) {
            mylhs = getPred(tree.lhs.toString());
        } else {
            tree.lhs.accept(this);
            mylhs = (Expr) mnode;
        }

        // Assignments in SyncTask must be to variable expression only.
        if (!(mylhs instanceof VarNameExpr)) {
            throw new UnsupportedJavaFeatureException("Assigmnent is not to a variable");
        }

        if (hasPredicate(tree.rhs.toString())) {
            myrhs = getPred(tree.rhs.toString());
        } else {
            tree.rhs.accept(this);
            myrhs = (Expr) mnode;
        }

        mnode = new AssignStmt((VarNameExpr) mylhs, myrhs);
    }

    // Must convert  x += expr into x = x + expr
    public void visitAssignop(JCAssignOp tree) {
        Expr mylhs;
        Expr myrhs;
        Expr.Op myop = convertOp(tree.getTag());

        if (myop == Expr.Op.ERRO) {
            throw new UnsupportedJavaFeatureException("operator " + tree.operator.toString());
        }

        //if (! hasPredicate( tree.lhs.toString() ) )
        //   throw new UnsupportedJavaFeatureException("Assignment to unmapped Id(" + tree.name.toString() + ")");

        if (hasPredicate(tree.lhs.toString())) {
            mylhs = getPred(tree.lhs.toString());
        } else {
            tree.lhs.accept(this);
            mylhs = (Expr) mnode;
        }

        // Assignments in SyncTask must be to variable expression only.
        if (!(mylhs instanceof VarNameExpr)) {
            throw new UnsupportedJavaFeatureException("Assigmnent is not to a variable");
        }

        if (hasPredicate(tree.rhs.toString())) {
            myrhs = getPred(tree.rhs.toString());
        } else {
            tree.rhs.accept(this);
            myrhs = (Expr) mnode;
        }

        // Unfolding x += expr to x = x + expr.

        mnode = new AssignStmt((VarNameExpr) mylhs, new BopExpr(mylhs, myrhs, myop));
    }

    // Covert only the unary operators suported in SyncTask
    public void visitUnary(JCUnary tree) {
        Expr myexpr;
        Expr.Op myop = convertOp(tree.getTag());

        if (myop == Expr.Op.ERRO) {
            throw new UnsupportedJavaFeatureException("operator " + tree.operator.toString());
        }

        if (hasPredicate(tree.arg.toString())) {
            myexpr = getPred(tree.arg.toString());
        } else {
            tree.arg.accept(this);
            myexpr = (Expr) mnode;
        }

        mnode = new UopExpr(myexpr, myop);
    }

    // Convert only binary operators supported in SyncTask
    public void visitBinary(JCBinary tree) {
        debug("Java2SyncTaskVisitor.visitBinary: " + tree.toString());

        Expr mylhs;
        Expr myrhs;
        Expr.Op myop = convertOp(tree.getTag());

        if (myop == Expr.Op.ERRO) {
            throw new UnsupportedJavaFeatureException("operator " + tree.operator.toString());
        }

        if (hasPredicate(tree.lhs.toString())) {
            mylhs = getPred(tree.lhs.toString());
        } else {
            tree.lhs.accept(this);

            // Nothing to track here. Proceed.
            if (mnode == null) {
                return;
            }

            mylhs = (Expr) mnode;
        }


        if (hasPredicate(tree.rhs.toString())) {
            myrhs = getPred(tree.rhs.toString());
        } else {
            tree.rhs.accept(this);

            // Nothing to track here. Proceed.
            if (mnode == null) {
                return;
            }

            myrhs = (Expr) mnode;
        }

        mnode = new BopExpr(mylhs, myrhs, myop);
    }

    // Naming resolution
    // If a method call, it has already been tested on the container JCMethodInvocation
    // Here is tested only the flat name, without arguments.
    public void visitSelect(JCFieldAccess tree) {
        debug("Java2SyncTaskVisitor.visitSelect: " + tree.toString());

        if (hasPredicate(tree.toString())) {
            //debug("Java2SyncTaskVisitor.visitSelect: Found predicate");
            mnode = getPred(tree.toString());
            return;
        } else if (hasOperation(tree.toString())) {
            //debug("Java2SyncTaskVisitor.visitSelect: Found operation");
            mnode = getOper(tree.toString());
            return;
        }

        mnode = null;
        return;
    }

    // convert only names that were mapped before
    public void visitIdent(JCIdent tree) {
        debug("Java2SyncTaskVisitor.visitIdent: " + tree.sym.toString());

        if (hasPredicate(tree.name.toString())) {
            mnode = getPred(tree.name.toString());
        } else
        //throw new UnsupportedJavaFeatureException("Unmapped Id(" + tree.name.toString() + ")");
        {
            mnode = null;
        }
    }

    // Convets only booleans and ints
    public void visitLiteral(JCLiteral tree) {
        switch (tree.typetag) {
            case INT:
            case LONG:
            case CHAR:
                mnode = new ConstIntExpr(((java.lang.Number) tree.value).intValue());
                break;

            case BOOLEAN:
                mnode = new ConstBoolExpr((java.lang.Boolean) tree.value);
                break;

            default:
                throw new UnsupportedJavaFeatureException("Literal neither boolean nor int");
        }

    }

   /*

   public void visitDoLoop(JCDoWhileLoop tree) {
      tree.body.accept(this);
      tree.cond.accept(this);
   }

   public void visitSynchronized(JCSynchronized tree) {
      tree.lock.accept(this);
      tree.body.accept(this);
   }

   public void visitNewClass(JCNewClass tree) {
      tree.encl.accept(this);
      tree.clazz.accept(this);
      tree.args.accept(this);
      tree.def.accept(this);
      result = tree;
   }

   public void visitVarDef(JCVariableDecl tree) {
      tree.mods.accept(this);
      tree.vartype.accept(this);
      tree.init.accept(this);
   }

   public void visitTopLevel(JCCompilationUnit tree) {
      tree.pid.accept(this);
      tree.defs.accept(this);
      result = tree;
   }

   public void visitImport(JCImport tree) {
      tree.qualid.accept(this);
      result = tree;
   }

   public void visitClassDef(JCClassDecl tree) {
      tree.mods.accept(this);
      tree.typarams = translateTypeParams(tree.typarams);
      tree.extending.accept(this);
      tree.implementing.accept(this);
      tree.defs.accept(this);
      result = tree;
   }
   */


   /*
   public void visitForLoop(JCForLoop tree) {
      tree.init.accept(this);
      tree.cond.accept(this);
      tree.step.accept(this);
      tree.body.accept(this);
      result = tree;
   }

   public void visitForeachLoop(JCEnhancedForLoop tree) {
      tree.var.accept(this);
      tree.expr.accept(this);
      tree.body.accept(this);
      result = tree;
   }

   public void visitLabelled(JCLabeledStatement tree) {
      tree.body.accept(this);
      result = tree;
   }

   public void visitSwitch(JCSwitch tree) {
      tree.selector.accept(this);
      tree.cases = translateCases(tree.cases);
      result = tree;
   }

   public void visitCase(JCCase tree) {
      tree.pat.accept(this);
      tree.stats.accept(this);
      result = tree;
   }
   */
   /*
   public void visitCatch(JCCatch tree) {
      tree.param.accept(this);
      tree.body.accept(this);
      result = tree;
   }

   public void visitConditional(JCConditional tree) {
      tree.cond.accept(this);
      tree.truepart.accept(this);
      tree.falsepart.accept(this);
      result = tree;
   }
   */
   /*
   public void visitBreak(JCBreak tree) {
      result = tree;
   }

   public void visitContinue(JCContinue tree) {
      result = tree;
   }
   */

   /*
   public void visitThrow(JCThrow tree) {
      tree.expr.accept(this);
      result = tree;
   }

   public void visitAssert(JCAssert tree) {
      tree.cond.accept(this);
      tree.detail.accept(this);
      result = tree;
   }
   */

   /* 
   public void visitNewArray(JCNewArray tree) {
      tree.elemtype.accept(this);
      tree.dims.accept(this);
      tree.elems.accept(this);
      result = tree;
   }
   */

   /*
   public void visitTypeCast(JCTypeCast tree) {
      tree.clazz.accept(this);
      tree.expr.accept(this);
      result = tree;
   }

   public void visitTypeTest(JCInstanceOf tree) {
      tree.expr.accept(this);
      tree.clazz.accept(this);
      result = tree;
   }

   public void visitIndexed(JCArrayAccess tree) {
      tree.indexed.accept(this);
      tree.index.accept(this);
      result = tree;
   }

   */

   /*
   public void visitTypeIdent(JCPrimitiveTypeTree tree) {
      result = tree;
   }

   public void visitTypeArray(JCArrayTypeTree tree) {
      tree.elemtype.accept(this);
      result = tree;
   }

   public void visitTypeApply(JCTypeApply tree) {
      tree.clazz.accept(this);
      tree.arguments.accept(this);
      result = tree;
   }

   public void visitTypeUnion(JCTypeUnion tree) {
      tree.alternatives.accept(this);
      result = tree;
   }

   public void visitTypeParameter(JCTypeParameter tree) {
      tree.bounds.accept(this);
      result = tree;
   }

   @Override
   public void visitWildcard(JCWildcard tree) {
      tree.kind.accept(this);
      tree.inner.accept(this);
      result = tree;
   }

   @Override
   public void visitTypeBoundKind(TypeBoundKind tree) {
      result = tree;
   }

   public void visitErroneous(JCErroneous tree) {
      result = tree;
   }

   public void visitLetExpr(LetExpr tree) {
      tree.defs = translateVarDefs(tree.defs);
      tree.expr.accept(this);
      result = tree;
   }

   public void visitModifiers(JCModifiers tree) {
      tree.annotations = translateAnnotations(tree.annotations);
      result = tree;
   }

   public void visitAnnotation(JCAnnotation tree) {
      tree.annotationType.accept(this);
      tree.args.accept(this);
      result = tree;
   }

   public void visitTree(JCTree tree) {
      throw new AssertionError(tree);
   }
   */
}
