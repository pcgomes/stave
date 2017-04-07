package stave.java.visitor;

import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import java.util.Enumeration;
import stave.java.annotation.MetaNode;
import stave.java.annotation.MetaResource;
import stave.java.annotation.MetaSyncTask;
import stave.java.annotation.MetaThread;
import stave.java.ast.AJCExpressionStatement;
import stave.java.ast.AJCMethodDecl;
import stave.java.ast.AJCVariableDecl;
import stave.synctask.ast.CondDecl;
import stave.synctask.ast.IntDecl;
import stave.synctask.ast.LockDecl;
import stave.synctask.ast.Main;
import stave.synctask.ast.Node;
import stave.synctask.ast.StartThread;
import stave.synctask.ast.StartThreadStar;
import stave.synctask.ast.VarDecl;
import stave.synctask.ast.VarDeclStar;

/**
 * Third pass in the Java program.
 * It extracts information about initialization.
 */
public class MainInitVisitor extends AnnotationParsingVisitor {

    // Information about all resource types found in previous stage
    protected java.util.Hashtable<String, MetaResource> mmetareslist;

    // Structure holding the (possibly many) synctask program(s)
    protected java.util.Hashtable<String, Main> mstlist;

    // Member used to pass parameters
    protected Node mparam = null;

    // Member used to collect return values
    protected Node mresult = null;

    // Meta-information about the ST program being processed
    MetaSyncTask mmetastask = null;

    public MainInitVisitor() {
        mstlist = new java.util.Hashtable<String, Main>();
        mmetareslist = new java.util.Hashtable<String, MetaResource>();
    }

    // Initializes structure
    public MainInitVisitor(String lname, Main lmaininit) {
        this();
        addNewMain(lname, lmaininit);
    }

    // Initialize with a context
    public MainInitVisitor(java.util.Hashtable<String, MetaResource> lmetareslist) {
        mstlist = new java.util.Hashtable<String, Main>();
        mmetareslist = lmetareslist;
    }

    protected void addNewMain(String lname, Main lsynctask) {
        mstlist.put(lname, lsynctask);
    }

    public java.util.Hashtable<String, Main> getSTMainList() {
        return mstlist;
    }

    public Main getSTMain(String lprog) {
        return mstlist.get(lprog);
    }

    // The procedure seaches for
    // 1 - Exact class name
    // 2 - Its subtypes (TODO)
    protected boolean isResourceTypeDefined(String lname) {
        return mmetareslist.containsKey(lname);
    }

    protected MetaResource getResourceType(String lname) {
        return mmetareslist.get(lname);
    }

    // Check if a ST program is being processed.
    protected boolean isParsingST() {
        return (mmetastask != null);
    }

    /* Gets the resuls after processing an annotated method,
    * and merges with information extracted so far */
    private void processMetaSyncTask(MetaSyncTask lmeta) throws ParsingSyncTaskException {
        debug("MainInitVisitor.processMetaSyncTask: " + lmeta.getId());

        // Construct the starting threads declaration
        StartThreadStar lthreads = new StartThreadStar();
        for (Enumeration<String> e = lmeta.availableThreadTypes(); e.hasMoreElements(); ) {
            String mytype = e.nextElement();

            lthreads.addStartThread(new StartThread(lmeta.getThreadAmount(mytype), mytype));
        }

        VarDeclStar lvars = new VarDeclStar();

        // Add the extracted variables to the final variable declaration in Main
        for (Enumeration<VarDecl> e = lmeta.getVarDecls(); e.hasMoreElements(); ) {

            VarDecl lvar = e.nextElement();

            //  Perfom check that declarations are complete
            if (lvar instanceof IntDecl) {
                //TODO - Check that bounded integer was declared with correct bounds.

            } else if (lvar instanceof CondDecl) {
                String mylock = ((CondDecl) lvar).getLock();

                // throw exception if Condition variable has no associated lock
                // or that the lock exists and was properly declared
                if ((mylock == null) || mylock.equals("") || (!(lmeta.getVarDecl(mylock) instanceof LockDecl))) {
                    throw new ParsingSyncTaskException("Cond '" + lvar.getVarName() + "' is not associated to a valid Lock.");
                }

            }

            lvars.addVarDecl(lvar);
        }

      /* Creating a new initialization (Main) */
        Main lnewinit = new Main(lvars, lthreads);

        Main lcurr = getSTMain(lmeta.getId());

        if (lcurr == null) {
            // No SyncTask with such id yet.
            addNewMain(lmeta.getId(), lnewinit);
        } else {
         /* There's already a defintion for this Main - Merge them */
            lcurr.mergeMain(lnewinit);
        }
    }

    /**
     * All declarations of SyncTask should be made on top of a method declaration.
     */
    public void visitMethodDef(JCMethodDecl tree) {
        //System.out.println("Vising method " + tree.sym.toString());

        // Must check if instance, since Java compiler add a default constructor which isn't AJCMethodDecl
        if ((tree instanceof AJCMethodDecl) && (((AJCMethodDecl) tree).hasComment())) {

            debug("MainInitVisitor.visitMethodDef:" + tree.sym.toString());

            //System.out.println("Parsing method " + tree.sym.getQualifiedName().toString());
            String lcomment = ((AJCMethodDecl) tree).getComment();

            try {

                MetaNode mymetaresult = getMetaAnnotation(lcomment);

                if (!(mymetaresult instanceof MetaSyncTask)) {
                    return;
                }

                mmetastask = (MetaSyncTask) getMetaAnnotation(lcomment);

                // Proceed with visiting
                super.visitMethodDef(tree);

                processMetaSyncTask(mmetastask);

                if (onDebug()) {
                    mmetastask.dumpMetaSyncTask();
                }

            } catch (AnnotationParserException e) {
                warning(e);
            } catch (DuplicatedDeclarationException e) {
                warning(e);
            } catch (Exception | Error e) {
                warning("Could not parse the following comment: \"" + lcomment + "\". Message: " + e.getMessage());
            }

        } else {
            // No annotation, move on.
            // Not descending tree because it should not contain declaration.
            //super.visitMethodDef(tree);
        }
    }

    public void visitVarDef(JCVariableDecl tree) {

        // Check if this declaration is within a monitored block.
        if ((isParsingST())) {

            debug("MainInitVisitor.visitVarDef:" + tree.toString());

            // If there's a comment, should try to parse it.
            if (((AJCVariableDecl) tree).hasComment()) {

                String lcomment = ((AJCVariableDecl) tree).getComment();

                try {

                    MetaNode lparsed = getMetaAnnotation(lcomment);

                    if (lparsed instanceof MetaThread) {
                        MetaThread lmetaspawn = (MetaThread) lparsed;

                        // Check if Thread's type has been assigned
                        if (!(lmetaspawn.isThreadTypeSet())) {
                            lmetaspawn.setThreadType(tree.vartype.toString());
                        }

                        mmetastask.addStartThread(lmetaspawn);
                    }

                } catch (AnnotationParserException e) {
                    warning(e);
                } catch (DuplicatedDeclarationException e) {
                    warning(e);
                } catch (Exception | Error e) {
                    warning("Could not parse the following comment: \"" + lcomment + "\". Message: " + e.getMessage());
                }

            }

            // No comments - Inferring from previous annotations and context
            String myjavaname = tree.name.toString();

            if (mmetastask.isResourceMapped(myjavaname)) {
                // Get the type
                String myrestype = tree.vartype.toString();

                // Check if the SyncTask name was set
                String mystname = mmetastask.getSTResource(myjavaname);
                if (mystname == null) {
                    // Name not set. Copy it from Java variable declaration.
                    mystname = myjavaname;
                    // Update this in the resource map
                    mmetastask.addResourceMap(myjavaname, mystname);
                }

                if (myrestype.equals("boolean")) {
                    boolean mydef = false;

                    // if variable is declared as boolean x = true, can get the value.
                    if ((tree.init instanceof JCLiteral) &&
                            (((JCLiteral) tree.init).value instanceof Boolean)) {
                        mydef = ((Boolean) ((JCLiteral) tree.init).value).booleanValue();
                    }

                    mmetastask.addBoolDecl(mystname, mydef);

                } else if (isResourceTypeDefined(myrestype)) {
                    MetaResource myresdef = getResourceType(myrestype);

                    // For far, ranges are starting from zero.
                    mmetastask.addIntDecl(mystname, 0, myresdef.getDefaultCap(), myresdef.getDefaultVal());
                }

            } else if (mmetastask.isCondVarMapped(myjavaname)) {
                // TODO - Implement check for condvars
            } else if (mmetastask.isLockMapped(myjavaname)) {
                // TODO - Implement check for locks. Maybe also for threads.
            }

        } else {
            // No annotation, move on.
            // Not descending tree because it should not contain declaration.
            //super.visitVarDef(tree);
        }
    }

    public void visitAssign(JCAssign tree) {

        if (isParsingST()) {

            debug("MainInitVisitor.visitAssign:" + tree.toString());

            super.visitAssign(tree);

        } else {
            super.visitAssign(tree);
        }

    }

    public void visitExec(JCExpressionStatement tree) {

        debug("MainInitVisitor.visitExec:" + tree.toString());

        if ((((AJCExpressionStatement) tree).hasComment()) && (isParsingST())) {
            debug("MainInitVisitor.visitExec: Parsed and found comment");
        }

    }
}
