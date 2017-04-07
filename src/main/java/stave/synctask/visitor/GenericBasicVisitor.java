/**
 * Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>
 */

package stave.synctask.visitor;

import java.util.Enumeration;
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
import stave.synctask.ast.Node;
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

public class GenericBasicVisitor<A> implements GenericVisitor<A, A> {

    public A visit(Program n, A pparent) {
        n.getThreadTypes().accept(this, pparent);
        n.getMain().accept(this, pparent);

        return pparent;
    }

    public A visit(ThreadTypeStar n, A pparent) {
        for (Enumeration<ThreadType> e = n.getThreadTypes().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, pparent);
        }

        return pparent;
    }

    public A visit(ThreadType n, A pparent) {
        n.getSyncBlocks().accept(this, pparent);

        return pparent;
    }

    public A visit(SyncBlockStar n, A pparent) {
        for (Enumeration<SyncBlockStmt> e = n.getSyncBlocks().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, pparent);
        }

        return pparent;
    }

    public A visit(SyncBlockStmt n, A pparent) {
        n.getBlock().accept(this, pparent);

        return pparent;
    }

    public A visit(BlockStmt n, A pparent) {
        n.getStmts().accept(this, pparent);

        return pparent;
    }

    public A visit(StmtStar n, A pparent) {
        for (Enumeration<Stmt> e = n.getStmts().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, pparent);
        }

        return pparent;
    }

    public A visit(AssignStmt n, A pparent) {
        n.getTargetVar().accept(this, pparent);
        n.getExpr().accept(this, pparent);

        return pparent;
    }

    public A visit(IfElseStmt n, A pparent) {
        n.getExpr().accept(this, pparent);
        n.getStmtIf().accept(this, pparent);
        n.getStmtElse().accept(this, pparent);

        return pparent;
    }

    public A visit(WhileStmt n, A pparent) {
        n.getExpr().accept(this, pparent);
        n.getStmt().accept(this, pparent);

        return pparent;
    }

    public A visit(SkipStmt n, A pparent) {

        return pparent;
    }

    public A visit(NotifyStmt n, A pparent) {

        return pparent;
    }

    public A visit(NotifyAllStmt n, A pparent) {

        return pparent;
    }

    public A visit(WaitStmt n, A pparent) {

        return pparent;
    }

    public A visit(Main n, A pparent) {
        n.getVarDecl().accept(this, pparent);
        n.getStartThreads().accept(this, pparent);

        return pparent;
    }

    public A visit(VarDeclStar n, A pparent) {
        for (Enumeration<VarDecl> e = n.getVarDecls().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, pparent);
        }

        return pparent;
    }

    public A visit(BoolDecl n, A pparent) {

        return pparent;
    }

    public A visit(IntDecl n, A pparent) {

        return pparent;
    }

    public A visit(LockDecl n, A pparent) {

        return pparent;
    }

    public A visit(CondDecl n, A pparent) {

        return pparent;
    }

    public A visit(StartThreadStar n, A pparent) {
        for (Enumeration<StartThread> e = n.getStartThreads().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, pparent);
        }

        return pparent;
    }

    public A visit(StartThread n, A pparent) {

        return pparent;
    }

    public A visit(BopExpr n, A pparent) {
        n.getLeftOperand().accept(this, pparent);
        n.getRightOperand().accept(this, pparent);

        return pparent;
    }

    public A visit(UopExpr n, A pparent) {
        n.getOperand().accept(this, pparent);

        return pparent;
    }

    public A visit(ConstBoolExpr n, A pparent) {

        return pparent;
    }

    public A visit(ConstIntExpr n, A pparent) {

        return pparent;
    }

    public A visit(VarNameExpr n, A pparent) {

        return pparent;
    }

    public A visit(MinExpr n, A pparent) {

        return pparent;
    }

    public A visit(MaxExpr n, A pparent) {

        return pparent;
    }

    public A visit(Node n, A pparent) {
        n.accept(this, pparent);

        return pparent;
    }
}
