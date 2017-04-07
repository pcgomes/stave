package stave.synctask.visitor;

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
import stave.synctask.ast.StmtStar;
import stave.synctask.ast.SyncBlockStar;
import stave.synctask.ast.SyncBlockStmt;
import stave.synctask.ast.ThreadType;
import stave.synctask.ast.ThreadTypeStar;
import stave.synctask.ast.UopExpr;
import stave.synctask.ast.VarDeclStar;
import stave.synctask.ast.VarNameExpr;
import stave.synctask.ast.WaitStmt;
import stave.synctask.ast.WhileStmt;

public interface VoidVisitor<A> {
    public void visit(AssignStmt n, A arg);

    public void visit(BlockStmt n, A arg);

    public void visit(BoolDecl n, A arg);

    public void visit(BopExpr n, A arg);

    public void visit(CondDecl n, A arg);

    public void visit(ConstBoolExpr n, A arg);

    public void visit(ConstIntExpr n, A arg);

    public void visit(IfElseStmt n, A arg);

    public void visit(IntDecl n, A arg);

    public void visit(LockDecl n, A arg);

    public void visit(Main n, A arg);

    public void visit(MaxExpr n, A arg);

    public void visit(MinExpr n, A arg);

    public void visit(NotifyAllStmt n, A arg);

    public void visit(NotifyStmt n, A arg);

    public void visit(Program n, A arg);

    public void visit(SkipStmt n, A arg);

    public void visit(StartThread n, A arg);

    public void visit(StartThreadStar n, A arg);

    public void visit(StmtStar n, A arg);

    public void visit(SyncBlockStar n, A arg);

    public void visit(SyncBlockStmt n, A arg);

    public void visit(ThreadType n, A arg);

    public void visit(ThreadTypeStar n, A arg);

    public void visit(UopExpr n, A arg);

    public void visit(VarDeclStar n, A arg);

    public void visit(VarNameExpr n, A arg);

    public void visit(WaitStmt n, A arg);

    public void visit(WhileStmt n, A arg);
}

