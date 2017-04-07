/**
 * Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>
 */

package stave.synctask.visitor;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.w3c.dom.Element;
import stave.cpntools.SyncTaskCPN;
import stave.synctask.ast.AssignStmt;
import stave.synctask.ast.BlockStmt;
import stave.synctask.ast.BoolDecl;
import stave.synctask.ast.BopExpr;
import stave.synctask.ast.CondDecl;
import stave.synctask.ast.ConstBoolExpr;
import stave.synctask.ast.ConstIntExpr;
import stave.synctask.ast.IfElseStmt;
import stave.synctask.ast.IntDecl;
import stave.synctask.ast.LockDecl;
import stave.synctask.ast.Main;
import stave.synctask.ast.MaxExpr;
import stave.synctask.ast.MinExpr;
import stave.synctask.ast.NotifyAllStmt;
import stave.synctask.ast.NotifyStmt;
import stave.synctask.ast.Program;
import stave.synctask.ast.SkipStmt;
import stave.synctask.ast.StartThread;
import stave.synctask.ast.StartThreadStar;
import stave.synctask.ast.Stmt;
import stave.synctask.ast.StmtStar;
import stave.synctask.ast.SyncBlockStar;
import stave.synctask.ast.SyncBlockStmt;
import stave.synctask.ast.ThreadType;
import stave.synctask.ast.ThreadTypeStar;
import stave.synctask.ast.UopExpr;
import stave.synctask.ast.VarDecl;
import stave.synctask.ast.VarDeclStar;
import stave.synctask.ast.VarNameExpr;
import stave.synctask.ast.WaitStmt;
import stave.synctask.ast.WhileStmt;

public class SyncTaskToCpnVisitor extends GenericBasicVisitor<SyncTaskCPN.Node> {

    // Default separation between elements - Distance is relative from their centers
    private final int mspacingx = 168;
    private final int mspacingy = 168;
    // Main object - Stores the DOM
    protected SyncTaskCPN mcpnet;
    // Stores all locks held at a given control point.
    // Must be released when calling "wait()", all required after notified.
    // Preserving lock count for reentrant locks.
    private Hashtable<String, Integer> mlockset;
    // Store the variable declaration, for further reference.
    private VarDeclStar mvars;

    // Initialize with default //indentation
    public SyncTaskToCpnVisitor() throws javax.xml.parsers.ParserConfigurationException {

        mcpnet = new SyncTaskCPN();

        // Initialize the lockcounter
        mlockset = new Hashtable<String, Integer>();

        // Initialize variable table, to guarantee that nasty Null Pointers won't show up.
        mvars = new VarDeclStar();
    }

    /**
     * Retrieve the variable declaration object
     *
     * @return The variable declaration for the given name
     */
    protected VarDecl locateVarDecl(String pvarname) {
        return mvars.getVarDecl(pvarname);
    }


    // TODO - Implement a better approach to read the variable type.
    protected String getVarType(String pvarname) {
        VarDecl lvar = locateVarDecl(pvarname);
        if (lvar instanceof LockDecl) {
            return "LOCK";
        } else if (lvar instanceof CondDecl) {
            return "COND";
        } else if (lvar instanceof BoolDecl) {
            return "BOOL";
        } else if (lvar instanceof IntDecl) {
            return new String("INT" + ((IntDecl) lvar).getLowerBound() + "_" + ((IntDecl) lvar).getUpperBound());
        }

        return "";
    }

    /**
     * Increment the lock counter
     *
     * @param plock Name of lock being released
     * @return True if already owned this lock. False, otherwise.
     */
    private boolean testAndSetLockCount(String plock) {
        if (mlockset.containsKey(plock)) {
            mlockset.put(plock, mlockset.get(plock) + 1);
            return true;
        }

        mlockset.put(plock, 1);
        return false;
    }

    /**
     * Reduce the lock counter
     *
     * @param plock Name of lock being released
     */
    private void releaseLock(String plock) {
        if (!(mlockset.containsKey(plock))) {
            // TODO - throw exception?
        } else if (mlockset.get(plock).equals(1)) {
            mlockset.remove(plock);
        } else {
            mlockset.put(plock, mlockset.get(plock) + 1);
        }
    }

    // Return the object containing the DOM
    public SyncTaskCPN getCPNToolsNet() {
        return mcpnet;
    }

   /* Process the AST Elements  */

    public SyncTaskCPN.Node visit(Program n, SyncTaskCPN.Node lparent) {

        // Proceess Main first, to get the global variables first.
        n.getMain().accept(this, mcpnet.topPage());

        n.getThreadTypes().accept(this, mcpnet.topPage());

        // Set the layout and initial place marking
        mcpnet.topPage().concludeTopPage();

        // Finally add the types, since thread should now have been processed.
        // The exception are the integer sets, that should have been added along
        // Their variable sets.
        // TODO - This is ugly, should at least have a single method for adding all.
        mcpnet.addLockColset();
        mcpnet.addBoolColset();
        mcpnet.addThreadColset();
        mcpnet.addConditionColset();
        mcpnet.addVarDeclarations();

        return lparent;
    }

    public SyncTaskCPN.Node visit(ThreadTypeStar n, SyncTaskCPN.Node lparent) {
        // Create one page per thread type, and link to the start end end palce.

        // Add thread type to the enumeration of all available types.
        // The enumeration consists of elements found here, and at the main block.
        // NOTE: This must be done before navigating on the AST, so that all threads
        // are aware of all thread type by the moment it is analyzed.
        for (Enumeration<ThreadType> e = n.getThreadTypes().elements(); e.hasMoreElements(); ) {
            mcpnet.addEnumThread(e.nextElement().getTypeName());
        }
        // This create the fusion set for the "awaken" threads, for each thread type.
        // It must be added here, so the other threads know where to add their references
        mcpnet.createAwakenFusionPlaces();

        for (Enumeration<ThreadType> e = n.getThreadTypes().elements(); e.hasMoreElements(); ) {
            ThreadType lthread = e.nextElement();
            Element lstransition = mcpnet.topPage().addThreadTypeTopPage(lthread.getTypeName());
            // construct instace object and store for usage of subpages
            mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lstransition));

            SyncTaskCPN.Stmt lsubpage = (SyncTaskCPN.Stmt) lthread.accept(this, lparent);

            // Link the substitution transition to the used page.
            // Note: the method in/outPort() return a socket in this case.
            // They are names as such because in theory they would be the ports, if there was a super page.
            // TODO - Fix the linking to new model
            mcpnet.connectSubstitutionPage(lstransition, mcpnet.topPage().inPort(), mcpnet.topPage().outPort(),
                    lsubpage.element(), lsubpage.inPort(), lsubpage.outPort());

            mcpnet.restorePrevInstance();
        }

        return mcpnet.topPage();
    }

    public SyncTaskCPN.Node visit(ThreadType n, SyncTaskCPN.Node lparent) {

        // Create the thread page
        SyncTaskCPN.Thread lpage = mcpnet.new Thread(n.getTypeName());

        // Instantiate this page
        mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lpage.transS1()));

        SyncTaskCPN.Stmt lsubpage = (SyncTaskCPN.Stmt) n.getSyncBlocks().accept(this, lparent);

        // Link the substitution transition to the used page.
        lpage.connectS1(lsubpage);

        // Restore instance
        mcpnet.restorePrevInstance();

        return lpage;
    }

    public SyncTaskCPN.Node visit(SyncBlockStar n, SyncTaskCPN.Node lparent) {

        //if ( n.getSyncBlocks().size() < 0 ) return null;

      /*
      SyncTaskCPN.Stmt lpage = null;

      for (Enumeration<SyncBlockStmt> e = n.getSyncBlocks().elements(); e.hasMoreElements(); ) {
	 lpage = (Stmt) e.nextElement().accept(this, lparent);
      }
      */

        return this.<SyncBlockStmt>createComposition(n.getSyncBlocks().subList(0, n.getSyncBlocks().size()), lparent);
    }

    public SyncTaskCPN.Node visit(SyncBlockStmt n, SyncTaskCPN.Node lparent) {

        // Retrieve the mutex object guarding this synchronized block
        String llock = n.getLock();

        // If already had the lock, just forward the page received. No need to re-lock.
        if (testAndSetLockCount(llock)) {
            SyncTaskCPN.Node lsubpage = n.getBlock().accept(this, lparent);
            // After the descending subpages have been processed, lock may be released.
            releaseLock(llock);
            return lsubpage;
        }

        // Create the thread page
        SyncTaskCPN.SyncBlock lpage = mcpnet.new SyncBlock(mcpnet.currentThread(), llock);

        // Instantiate this page
        mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lpage.transS1()));

        SyncTaskCPN.Stmt lsubpage = (SyncTaskCPN.Stmt) n.getBlock().accept(this, lparent);

        // Link the substitution transition to the used page.
        lpage.connectS1(lsubpage);

        // Restore instance
        mcpnet.restorePrevInstance();

        // After the descending subpages have been processed, lock may be released.
        releaseLock(llock);

        return lpage;
    }

    public SyncTaskCPN.Node visit(BlockStmt n, SyncTaskCPN.Node lparent) {

        return n.getStmts().accept(this, lparent);
    }

    public SyncTaskCPN.Node visit(StmtStar n, SyncTaskCPN.Node lparent) {

        // TODO - Check if must really add a Skip for an empty block.
        if (n.getStmts().size() < 1) {
            // Create the thread page
            return mcpnet.new Skip(mcpnet.currentThread());

        } else {
            // TODO - Recursing over sublists. Replace this for iterator.
            return this.<Stmt>createComposition(n.getStmts().subList(0, n.getStmts().size()), lparent);
        }
    }

    // Created to recurse over elements inside the upper Vector<Stmt>
    private <T extends stave.synctask.ast.Stmt> SyncTaskCPN.Stmt createComposition(List<T> plist, SyncTaskCPN.Node lparent) {

        if (plist.size() == 1) {
            // There's no need for composition.
            Stmt lnode = plist.get(0);
            return (SyncTaskCPN.Stmt) lnode.accept(this, lparent);
        }

        SyncTaskCPN.Composition lpage = mcpnet.new Composition(mcpnet.currentThread());

        // S1 - Previous page.
        mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lpage.transS1()));
        SyncTaskCPN.Stmt lsubpage1 = (SyncTaskCPN.Stmt) (plist.get(0)).accept(this, lparent);
        lpage.connectS1(lsubpage1);
        mcpnet.restorePrevInstance();

        // Setting S2
        mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lpage.transS2()));
        SyncTaskCPN.Stmt lsubpage2 = this.<T>createComposition(plist.subList(1, plist.size()), lparent);
        lpage.connectS2(lsubpage2);
        mcpnet.restorePrevInstance();

        return lpage;
    }

    public SyncTaskCPN.Node visit(AssignStmt n, SyncTaskCPN.Node lparent) {

        SyncTaskCPN.Expr lvar = (SyncTaskCPN.Expr) n.getTargetVar().accept(this, lparent);
        SyncTaskCPN.Expr lexpr = (SyncTaskCPN.Expr) n.getExpr().accept(this, lparent);

        // Create the thread page
        SyncTaskCPN.Assign lpage = mcpnet.new Assign(mcpnet.currentThread(), lvar.text(), lexpr);

        return lpage;
    }

    public SyncTaskCPN.Node visit(IfElseStmt n, SyncTaskCPN.Node lparent) {
        SyncTaskCPN.Expr lexpr = (SyncTaskCPN.Expr) n.getExpr().accept(this, lparent);

        SyncTaskCPN.IfElse lpage = mcpnet.new IfElse(mcpnet.currentThread(), lexpr);

        mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lpage.transS1()));
        SyncTaskCPN.Stmt lsubpage1 = (SyncTaskCPN.Stmt) n.getStmtIf().accept(this, lparent);
        lpage.connectS1(lsubpage1);
        mcpnet.restorePrevInstance();

        mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lpage.transS2()));
        SyncTaskCPN.Stmt lsubpage2 = (SyncTaskCPN.Stmt) n.getStmtElse().accept(this, lparent);
        lpage.connectS2(lsubpage2);
        mcpnet.restorePrevInstance();

        return lpage;
    }

    public SyncTaskCPN.Node visit(WhileStmt n, SyncTaskCPN.Node lparent) {

        SyncTaskCPN.Expr lexpr = (SyncTaskCPN.Expr) n.getExpr().accept(this, lparent);

        SyncTaskCPN.While lpage = mcpnet.new While(mcpnet.currentThread(), lexpr);

        mcpnet.addAndSetNextInstance(mcpnet.createInstanceForSTransition(lpage.transS1()));

        SyncTaskCPN.Stmt lsubpage = (SyncTaskCPN.Stmt) n.getStmt().accept(this, lparent);

        lpage.connectS1(lsubpage);

        mcpnet.restorePrevInstance();

        return lpage;
    }

    public SyncTaskCPN.Node visit(SkipStmt n, SyncTaskCPN.Node lparent) {

        return mcpnet.new Skip(mcpnet.currentThread());
    }

    public SyncTaskCPN.Node visit(NotifyStmt n, SyncTaskCPN.Node lparent) {

        SyncTaskCPN.Notify lpage = mcpnet.new Notify(mcpnet.currentThread(), n.getCondition());

        return lpage;
    }

    public SyncTaskCPN.Node visit(NotifyAllStmt n, SyncTaskCPN.Node lparent) {

        SyncTaskCPN.NotifyAll lpage = mcpnet.new NotifyAll(mcpnet.currentThread(), n.getCondition());

        return lpage;
    }

    public SyncTaskCPN.Node visit(WaitStmt n, SyncTaskCPN.Node lparent) {

        String llock = ((CondDecl) locateVarDecl(n.getCondition())).getLock();

        // Create the thread page
        SyncTaskCPN.Wait lpage = mcpnet.new Wait(mcpnet.currentThread(), n.getCondition(), llock);

        return lpage;
    }

    public SyncTaskCPN.Node visit(Main n, SyncTaskCPN.Node lparent) {
        // Set the variable declaration object, for further reuse
        // TODO - Remove this. There are better ways to get this
        mvars = n.getVarDecl();

        n.getVarDecl().accept(this, lparent);
        n.getStartThreads().accept(this, lparent);

        return lparent;
    }

    public SyncTaskCPN.Node visit(VarDeclStar n, SyncTaskCPN.Node lparent) {

        for (Enumeration<VarDecl> e = n.getVarDecls().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, lparent);
        }

        return lparent;
    }

    public SyncTaskCPN.Node visit(BoolDecl n, SyncTaskCPN.Node lparent) {

        String linit = (n.getValue()) ? "true" : "false";

        Element lbool = mcpnet.topPage().createGlobalVariable(n.getVarName(), "BOOL", "1`" + linit);

        return lparent;
    }

    public SyncTaskCPN.Node visit(IntDecl n, SyncTaskCPN.Node lparent) {

        String ltype = mcpnet.makeOrGetBoundedIntType(n.getLowerBound(), n.getUpperBound());

        Element lint = mcpnet.topPage().createGlobalVariable(n.getVarName(), ltype, "1`" + n.getValue());

        return lparent;
    }

    public SyncTaskCPN.Node visit(LockDecl n, SyncTaskCPN.Node lparent) {

        Element llock = mcpnet.topPage().createGlobalVariable(n.getVarName(), "LOCK", "1`()");

        return lparent;
    }

    public SyncTaskCPN.Node visit(CondDecl n, SyncTaskCPN.Node lparent) {

        Element lcondition = mcpnet.topPage().createGlobalVariable(n.getVarName(), "CONDITION", "");

        return lparent;
    }

    public SyncTaskCPN.Node visit(StartThreadStar n, SyncTaskCPN.Node lparent) {

        for (Enumeration<StartThread> e = n.getStartThreads().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, lparent);
        }
        return lparent;
    }

    // Nothing to do here so far.
    public SyncTaskCPN.Node visit(StartThread n, SyncTaskCPN.Node lparent) {
        mcpnet.addEnumThread(n.getType(), n.getNumber());

        return lparent;
    }

    public SyncTaskCPN.Node visit(BopExpr n, SyncTaskCPN.Node lparent) {
        // andalso, orelse, not
        String loperator = "";
        switch (n.getOperator()) {
            case LAND:
                loperator = "andalso";
                break;
            case LOOR:
                loperator = "orelse";
                break;
            case EQUA:
                loperator = "=";
                break;
            case BIGT:
                loperator = ">";
                break;
            case NTEQ:
                loperator = "<>";
                break;
            case LOWT:
                loperator = "<";
                break;
            case ADDI:
                loperator = "+";
                break;
            case MINU:
                loperator = "-";
                break;
            default:
                System.err.println("Warning: Operating over invalid operator");
        }

        SyncTaskCPN.Expr lop1 = (SyncTaskCPN.Expr) n.getLeftOperand().accept(this, lparent);
        SyncTaskCPN.Expr lop2 = (SyncTaskCPN.Expr) n.getRightOperand().accept(this, lparent);

        return mcpnet.new Bop(lop1, lop2, loperator);
    }

    public SyncTaskCPN.Node visit(UopExpr n, SyncTaskCPN.Node lparent) {
        String loperator = "";

        switch (n.getOperator()) {
            case NEGA:
                loperator = new String("not");
                break;
            default:
                System.err.println("Warning: Operating over invalid operator");
        }

        return mcpnet.new Uop((SyncTaskCPN.Expr) n.getOperand().accept(this, lparent), loperator);
    }

    public SyncTaskCPN.Node visit(ConstBoolExpr n, SyncTaskCPN.Node lparent) {
        return mcpnet.new ConstBool(n.getValue());
    }

    public SyncTaskCPN.Node visit(ConstIntExpr n, SyncTaskCPN.Node lparent) {
        return mcpnet.new ConstInt(n.getValue());
    }

    public SyncTaskCPN.Node visit(VarNameExpr n, SyncTaskCPN.Node lparent) {
        return mcpnet.new VarName(n.getVarName(), getVarType(n.getVarName()));
    }

    // TODO - Figure out this operator, or replace for value
    public SyncTaskCPN.Node visit(MinExpr n, SyncTaskCPN.Node lparent) {
        return mcpnet.new Min(Integer.toString(((IntDecl) locateVarDecl(n.getVarName())).getLowerBound()));
    }

    // TODO - Figure out this operator, or replace for value
    public SyncTaskCPN.Node visit(MaxExpr n, SyncTaskCPN.Node lparent) {
        return mcpnet.new Max(Integer.toString(((IntDecl) locateVarDecl(n.getVarName())).getUpperBound()));
    }

}
