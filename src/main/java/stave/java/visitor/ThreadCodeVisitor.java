package stave.java.visitor;

//import com.sun.tools.javac.code.TypeTags;

import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import java.util.Enumeration;
import stave.java.annotation.MetaNode;
import stave.java.annotation.MetaResource;
import stave.java.annotation.MetaSyncBlock;
import stave.java.ast.AJCSynchronized;
import stave.synctask.ast.BlockStmt;
import stave.synctask.ast.Node;
import stave.synctask.ast.NotifyAllStmt;
import stave.synctask.ast.NotifyStmt;
import stave.synctask.ast.SyncBlockStmt;
import stave.synctask.ast.ThreadType;
import stave.synctask.ast.ThreadTypeStar;
import stave.synctask.ast.WaitStmt;

/**
 * Second pass in the Java program.
 * It extracts information about the synchronization control flow.
 */
public class ThreadCodeVisitor extends AnnotationParsingVisitor {

    // Structure holding the synctask program
    protected ThreadTypeStar mthreadstar;

    // Reference to the thread type currently being extracted
    protected ThreadType mthreadtype = null;

    // Information about all resource types found in previous stage
    protected java.util.Hashtable<String, MetaResource> mmetareslist;

    // Reference to metadata about thes SyncBlock being generated,
    // extracted from the Annotations
    protected MetaSyncBlock mmetasblock = null;

    // Information about the resource being used currently
    protected MetaResource mmetaresource = null;

    // Member used to pass parameters;
    protected Node mparam = null;

    // Member used to collect return values;
    protected Node mresult = null;

    // Member that preserves the current class being processed.
    // TODO - replace this for a query in the symbol table
    protected String mcurrentclass;

    public ThreadCodeVisitor() {
        mthreadstar = new ThreadTypeStar();
        mmetareslist = new java.util.Hashtable<String, MetaResource>();
    }

    public ThreadCodeVisitor(ThreadTypeStar llist) {
        mthreadstar = llist;
    }

    public ThreadCodeVisitor(java.util.Hashtable<String, MetaResource> lmetareslist) {

        mthreadstar = new ThreadTypeStar();
        mmetareslist = lmetareslist;
    }

    public ThreadTypeStar getThreadTypes() {
        return mthreadstar;
    }

    /* return ST's type */
    protected MetaResource getMetaResource(String lname) throws UnknownTypeException {
        if (!(mmetareslist.containsKey(lname))) {
            throw new UnknownTypeException(lname);
        }

        return mmetareslist.get(lname);
    }

    // Creates a visitor that contains information for a given context.
    // The context should have information about available resource types and the SyncBlock information found.
    protected Java2SyncTaskVisitor contextAwareVisitor(MetaSyncBlock lstartcontext) throws UnknownTypeException, Exception {
        Java2SyncTaskVisitor myvisitor = new Java2SyncTaskVisitor();

        // Create mapping between condvars and its SyncTask representation.
        for (Enumeration<String> e = lstartcontext.getMonitoredCondvars(); e.hasMoreElements(); ) {
            String myjavacond = e.nextElement();
            String mystaskcond = lstartcontext.getSTCondVar(myjavacond);

            myvisitor.addOperToContext(myjavacond + ".wait", new WaitStmt(mystaskcond));
            myvisitor.addOperToContext(myjavacond + ".notify", new NotifyStmt(mystaskcond));
            myvisitor.addOperToContext(myjavacond + ".notifyAll", new NotifyAllStmt(mystaskcond));

            // TODO - Add operations for Condition objects and checks
        }

        // Now associate the resource operations to its object
        for (Enumeration<String> e = lstartcontext.getMonitoredResources(); e.hasMoreElements(); ) {
            String myjavares = e.nextElement();
            MetaResource mysttype = getMetaResource(lstartcontext.getSTResource(myjavares));

            // Add the associated predicates
            for (Enumeration<String> f = mysttype.availablePredicates(); f.hasMoreElements(); ) {
                String mypredicate = f.nextElement();

                myvisitor.addPredToContext(myjavares + "." + mypredicate, mysttype.getPredicate(mypredicate));
            }

            // Add the associated operations
            for (Enumeration<String> f = mysttype.availableOperations(); f.hasMoreElements(); ) {
                String myoperation = f.nextElement();

                myvisitor.addOperToContext(myjavares + "." + myoperation, mysttype.getOperation(myoperation));
            }
        }

        return myvisitor;
    }

    /*
    * TODO - Currently just set the class name, to be used if no annotation was found for threadtype.
    * This should be replaced to a query to the symbol table
    */
    public void visitClassDef(JCClassDecl tree) {
        mcurrentclass = tree.sym.getQualifiedName().toString();
        debug("ThreadCodeVisitor.visitClassDef: Parsing class: " + mcurrentclass);

        super.visitClassDef(tree);
    }

    public void visitSynchronized(JCSynchronized tree) {

        if (((AJCSynchronized) tree).hasComment()) {

            debug("ThreadCodeVisitor.visitSynchronized: Parsing syncronized with lock: " + tree.lock.toString());

            String lcomment = ((AJCSynchronized) tree).getComment();

            try {

                // Parse comments and see if got something useful
                MetaNode lresult = getMetaAnnotation(lcomment);
                if (!(lresult instanceof MetaSyncBlock)) {
                    return;
                }

                mmetasblock = (MetaSyncBlock) lresult;

                // Now translate Java to SyncTask using the context
                Java2SyncTaskVisitor lcodevisitor = contextAwareVisitor(mmetasblock);
                lcodevisitor.visitBlock(tree.body);

                // Make the lock object for this synchronized block
                String mylock = mmetasblock.getSTLock(tree.lock);

                // Check if ThreadType name was set; otherwise set the as the name of the enclosing class
                if (!mmetasblock.isThreadTypeSet()) {
                    mmetasblock.setThreadType(mcurrentclass);
                }

                // Get the ThreadType that owns this code block.
                mthreadtype = mthreadstar.getOrMakeNewThreadType(mmetasblock.getThreadType());

                // Get new block and add the node
                mthreadtype.getSyncBlocks().addSyncBlock(new SyncBlockStmt(mylock, (BlockStmt) lcodevisitor.getResultNode()));

            } catch (AnnotationParserException e) {
                warning(e);
            } catch (DuplicatedDeclarationException e) {
                warning(e);
            } catch (UnknownTypeException e) {
                warning(e);
            } catch (Exception | Error e) {
                warning("Could not parse the following comment: \"" + lcomment + "\". Message: " + e.getMessage());
            } finally {

                // Processing of this SyncBlock is over - Reseting its value
                mresult = null;
                mmetasblock = null;
            }

            // No annotation, move on
        } else {
            super.visitSynchronized(tree);
        }
    }

  /*
   // Create new block
   public void visitBlock(JCBlock tree) {
      if (mmetasblock == null) {
         // Not to process
	 super.visitBlock(tree);
      } else {

         BlockStmt mblock = new BlockStmt();
         
         // First, store reference to previous param
         // Passsing the list of stms as parameter
         Node lprevparam = mparam;
         mparam = mblock.getStmts();

	 super.visitBlock(tree);

         // Restore param and return object with result
         mparam = lprevparam;
         mresult = mblock;
      }
   }

   public void visitTry(JCTry tree) {
      if ( (mmetasblock == null) ) {
         // Proceed with descending
	 super.visitTry(tree);
      } else {
         System.out.println("Debug - Found try block");
         // Currently exceptions are completely ignored.
         // Here we just process the try body
         tree.body.accept(this);
      }
   }
*/

/*
   public void visitAssign(JCAssign tree) {
      try {
	 if ( (mmetasblock == null) || (! mmetasblock.isResourceMapped(tree.lhs)) ) {
	    // Not processing a SyncBlock 
	    // or target variable isn't a part of a SynkTask- Skip
	    //super.visitSelect(tree);
	 } else {
            // There's no need to descend on the tree
	    // tree.lhs.accept(this);

            // Get the resource in the SyncTask
            VarNameExpr ltarget = new VarNameExpr( mmetasblock.getSTResource(tree.lhs) );

	    // Process the right-hand side of the assignment
	    tree.rhs.accept(this);
	    Expr lexpr = (Expr) mresult;

	    // Incoming mparam is a list of Statemtns.
	    // Append new assigment to it
	    ((StmtStar)mparam).addStmt(new AssignStmt( ltarget, lexpr));
	 }
      } catch (IOException e) {
         System.err.println("Warning: IOException raised while processing isResourceMapped. Message: " + e.getMessage());
      } catch (Exception e) {
         System.err.println("Warning: Exception raised while processing getSTResource. Message: " + e.getMessage());
      }
   }

   public void visitWhileLoop(JCWhileLoop tree) {
      if ( (mmetasblock == null) ) {
         // Proceed with descending
	 super.visitWhileLoop(tree);
      } else {

         // Get condional expression for loop
         tree.cond.accept(this);
         Expr lexpr = (Expr) mresult;

         // Get the body element 
         tree.body.accept(this);
         Stmt lbody = (Stmt) mresult;

         // Incoming mparam is a list of Statemtns.
         // Append new assigment to it
         ((StmtStar)mparam).addStmt(new WhileStmt( lexpr, lbody));
      }
   }

   public void visitExec(JCExpressionStatement tree) {
      if (mmetasblock == null) {
         // Not processing a SyncBlock - Skip
      } else {
         super.visitExec(tree);
         //mresult = ((StmtStar)mparam).addStmt(new WhileStmt( lexpr, lbody));
         if ( (mresult instanceof WaitStmt)||
              (mresult instanceof NotifyStmt)  ||
              (mresult instanceof NotifyAllStmt) ) {
            ((StmtStar)mparam).addStmt( (Stmt) mresult);
         } 
      }
   }
 
   public void visitApply(JCMethodInvocation tree) {
      
      try {
	 // TODO - Cover case for java.utils.concurrency:
	 // obj.lock / obj.unlock / obj.await / obj.signal
	 if (mmetasblock == null) {
	    // This is inside a SyncBlock - Skip
	    //super.visitSelect(tree);
	 } else {
	    String lsimple = mmetasblock.getSimpleName(tree.meth);
	    String lprefix = mmetasblock.getHierarchy(tree.meth);
            
            debug("visitApply- Prefix: " + lprefix + "|Name: " + lsimple);

	    // Check if call is manipulation of condition variables.
	    if (lsimple.equals("wait")) { mresult = new WaitStmt( mmetasblock.getSTCondVar( lprefix ) ); return; }
	    else if (lsimple.equals("notify")) { mresult = new NotifyStmt( mmetasblock.getSTCondVar( lprefix ) ); return; }
	    else if (lsimple.equals("notifyAll")) { mresult = new NotifyAllStmt( mmetasblock.getSTCondVar( lprefix ) ); return; }
	 }
      } catch (Exception e) {
         int ln = Thread.currentThread().getStackTrace()[1].getLineNumber();
	 System.err.println("Warning(" + ln + "): Exception raised while processing getSimpleName. Message: " + e.getMessage());
      }
   }

   public void visitSelect(JCFieldAccess tree) {
      try {
	 if (mmetasblock == null) {
	    // Not processing a SyncBlock - Skip
	    //super.visitSelect(tree);
	 } else {
	    // First call shall generate the name without descending the tree
	    //super.visitSelect(tree);
	    // Cast below is bad.
	    mresult = new VarNameExpr( mmetasblock.getSTResource(tree) );
	 }
      } catch (Exception e) {
         int ln = Thread.currentThread().getStackTrace()[1].getLineNumber();
	 System.err.println("Warning(" + ln + "): Exception raised while processing getSTResource. Message: " + e.getMessage());
      }
   }

   public void visitIdent(JCIdent tree) {
      try {
	 if (mmetasblock == null) {
	    //super.visitIdent(tree);
	 } else {
	    // First call shall generate the name without descending the tree
	    //super.visitSelect(tree);
	    // Cast below is bad.
	    mresult = new VarNameExpr( mmetasblock.getSTResource(tree));
	 }
      } catch (Exception e) {
         int ln = Thread.currentThread().getStackTrace()[1].getLineNumber();
	 System.err.println("Warning(" + ln + "): Exception raised while processing getSTResource. Message: " + e.getMessage());
      }
   }

   // Supporting char/ints and booleans only
   public void visitLiteral(JCLiteral tree) {
      if (mmetasblock == null) {
         // Not processing a SyncBlock - Skip
         // super.visitLiteral(tree);
      } else {
         switch (tree.typetag) {
            case TypeTags.INT:
               mresult = new ConstIntExpr(((Long)tree.value).intValue());
               break;
            case TypeTags.CHAR:
               mresult = new ConstIntExpr(((Short)tree.value).intValue());
               break;
            case TypeTags.BOOLEAN:
               mresult = new ConstBoolExpr( (((Number)tree.value).intValue() == 1 ?true:false));
               break;
            default: throw new Error("Error - Unsupported literal type. ID=" + tree.typetag);
         }
      }
   }

   public void visitBinary(JCBinary tree) {
      if (mmetasblock == null) {
         // Not processing a SyncBlock - Skip
         // super.visitLiteral(tree);
      } else {
         tree.lhs.accept(this);
         Expr lexpr1 = (Expr) mresult;
 
         tree.rhs.accept(this);
         Expr lexpr2 = (Expr) mresult;

         mresult = new BopExpr( lexpr1, lexpr2, mmetasblock.convertOperator( tree.getTag() ));
      }
   }

   public void visitUnary(JCUnary tree) {
      if (mmetasblock == null) {
         // Not processing a SyncBlock - Skip
         // super.visitLiteral(tree);
      } else {
         tree.arg.accept(this);
         Expr lexpr = (Expr) mresult;
         mresult = new UopExpr( lexpr, mmetasblock.convertOperator( tree.getTag() ));
      }
   }
   */
}
