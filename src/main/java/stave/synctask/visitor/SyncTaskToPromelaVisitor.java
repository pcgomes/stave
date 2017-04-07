package stave.synctask.visitor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import stave.promela.Lib;
import stave.promela.StaticLib;
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

public class SyncTaskToPromelaVisitor implements VoidVisitor<Integer> {

    static Lib lib = StaticLib.getInstance();

    private StringBuilder sb;
    private String linesep;
    private List<String> warnings;
    private List<IntDecl> intdecls;
    private List<LockDecl> lockdecls;
    private List<CondDecl> conddecls;

    public SyncTaskToPromelaVisitor() {
        sb = new StringBuilder();
        linesep = "\n"; // TODO Change to fetching from system
        warnings = new LinkedList<String>();
        intdecls = new ArrayList<IntDecl>();
        lockdecls = new ArrayList<LockDecl>();
        conddecls = new ArrayList<CondDecl>();
    }

    public void write(OutputStreamWriter out) throws IOException {
        out.write(lib.getLibrary());
        out.write(sb.toString());
    }

    private void insertLineBreak() {
        sb.append(linesep);
    }

    private String variablePadding(String varname) {
        return "x_" + varname;
    }

    private void insertIndentation(Integer ind) {
        for (int i = 0; i < ind; i++) {
            sb.append("\t");
        }
    }

    private void warn(String warning) {
        warnings.add(warning);
    }

    @Override
    public void visit(AssignStmt n, Integer ind) {
        n.getTargetVar().accept(this, ind);
        sb.append(" = ");
        n.getExpr().accept(this, ind);
        sb.append(";");
    }

    @Override
    public void visit(BlockStmt n, Integer ind) {
        sb.append("{");
        insertLineBreak();
        ;
        n.getStmts().accept(this, ind + 1);
        insertIndentation(ind);
        sb.append("}");
        insertLineBreak();
    }

    @Override
    public void visit(BoolDecl n, Integer ind) {
        insertIndentation(ind);
        sb.append("bool ");
        sb.append(variablePadding(n.getVarName()));
        sb.append(" = ");
        sb.append(n.getValue());
        sb.append(";");
    }

    @Override
    public void visit(BopExpr n, Integer ind) {
        n.getLeftOperand().accept(this, ind);
        switch (n.getOperator()) {
            default:
                warn("Found non-binary operator " + n.getOperator().name() + " in unary expression.");
            case LOOR: // ||
                sb.append(" || ");
                break;
            case LAND: // &&
                sb.append(" && ");
                break;
            case EQUA: // ==
                sb.append(" == ");
                break;
            case NTEQ: // !=
                sb.append(" != ");
                break;
            case LOWT: // <
                sb.append(" < ");
                break;
            case BIGT: // >
                sb.append(" > ");
                break;
            case ADDI: // +
                sb.append(" + ");
                break;
            case MINU: // -
                sb.append(" - ");
                break;
        }
        n.getRightOperand().accept(this, ind);
    }

    @Override
    public void visit(CondDecl n, Integer ind) {
        insertIndentation(ind);
        sb.append("Lib_Cond ");
        sb.append(variablePadding(n.getVarName()));
        sb.append(";");
        insertLineBreak();
        insertIndentation(ind);
        conddecls.add(n);
    }

    @Override
    public void visit(ConstBoolExpr n, Integer ind) {
        sb.append(n.getValue());
    }

    @Override
    public void visit(ConstIntExpr n, Integer ind) {
        sb.append(n.getValue());
    }

    @Override
    public void visit(IfElseStmt n, Integer ind) {
        sb.append("if");
        insertLineBreak();
        insertIndentation(ind);
        sb.append(":: ");
        n.getExpr().accept(this, ind);
        sb.append(" -> ");
        //Block bracers should be inserted by the Stmt implementation if it is a block
        n.getStmtIf().accept(this, ind + 1);
        insertLineBreak();
        insertIndentation(ind);
        sb.append(":: else -> ");
        insertLineBreak();
        n.getStmtElse().accept(this, ind + 1);
        insertLineBreak();
        insertIndentation(ind);
        sb.append("fi");
    }

    @Override
    public void visit(IntDecl n, Integer ind) {
        insertIndentation(ind);
        sb.append("int ");
        sb.append(variablePadding(n.getVarName()));
        sb.append(" = ");
        sb.append(n.getValue());
        sb.append(";");

        insertLineBreak();
        ;
        sb.append(lib.generateIntBoundsDeclaration(n.getVarName()));
        intdecls.add(n); //Adds the declaration to list of vars to have ltl formulaes generated for them.
    }

    @Override
    public void visit(LockDecl n, Integer ind) {
        insertIndentation(ind);
        sb.append("byte ");
        sb.append(variablePadding(n.getVarName()));
        sb.append(" = ");
        sb.append(lockdecls.size());
        sb.append(";");
        lockdecls.add(n);
    }

    @Override
    public void visit(Main n, Integer ind) {
        n.getVarDecl().accept(this, ind + 1);
        insertIndentation(ind);
        sb.append(lib.generateLockListDeclaration(lockdecls.size()));
        insertLineBreak();
        sb.append("init {");
        insertLineBreak();
        for (IntDecl i : intdecls) {
            sb.append(lib.generateIntBoundsInitilization(i.getVarName(), i.getLowerBound(), i.getUpperBound()));
        }

        for (CondDecl c : conddecls) {
            sb.append(lib.generateCondInitilization(variablePadding(c.getVarName()), variablePadding(c.getLock())));
            insertLineBreak();
        }

        n.getStartThreads().accept(this, ind + 1);
        insertLineBreak();
        sb.append("}");
        insertLineBreak();
        insertLineBreak();
    }

    @Override
    public void visit(MaxExpr n, Integer ind) {
        sb.append(lib.generateMax(n.getVarName()));
    }

    @Override
    public void visit(MinExpr n, Integer ind) {
        sb.append(lib.generateMin(n.getVarName()));
    }

    @Override
    public void visit(NotifyAllStmt n, Integer ind) {
        sb.append("Lib_notifyAll( ");
        sb.append(variablePadding(n.getCondition()));
        sb.append(");");
    }

    @Override
    public void visit(NotifyStmt n, Integer ind) {
        sb.append("Lib_notify( ");
        sb.append(variablePadding(n.getCondition()));
        sb.append(");");
    }

    @Override
    public void visit(Program n, Integer ind) {
        n.getMain().accept(this, ind);
        n.getThreadTypes().accept(this, ind);

        for (IntDecl id : intdecls) {
            sb.append("ltl limits_" + variablePadding(id.getVarName()) + " {[] (");
            sb.append(variablePadding(id.getVarName()) + ">=" + id.getLowerBound() + " && ");
            sb.append(variablePadding(id.getVarName()) + "<=" + id.getUpperBound());
            sb.append(")}");
            insertLineBreak();
        }
    }

    @Override
    public void visit(SkipStmt n, Integer ind) {
        sb.append("skip;");
    }

    @Override
    public void visit(StartThread n, Integer ind) {
        insertIndentation(ind);
        switch (n.getNumber()) {
            case 3:
                sb.append("run " + n.getType() + "();");
                insertLineBreak();
                insertIndentation(ind);
            case 2:
                sb.append("run " + n.getType() + "();");
                insertLineBreak();
                insertIndentation(ind);
            case 1:
                sb.append("run " + n.getType() + "();");
                insertLineBreak();
                break;
            case 0:
                sb.append("/* No processes of type " + n.getType() + " to start.*/");
            default:
                generateStartThreadLoop(n, ind + 1);
                break;
        }
    }

    private void generateStartThreadLoop(StartThread n, Integer ind) {

        sb.append("proc = 1;");
        insertLineBreak();
        insertIndentation(ind); //reset counter
        sb.append("do /* Loop until break*/");
        insertLineBreak();
        insertIndentation(ind);
        sb.append(":: proc <= " + n.getNumber());
        insertLineBreak();
        insertIndentation(ind + 1);
        sb.append("run " + n.getType() + "();");
        insertLineBreak();
        insertIndentation(ind + 1);
        sb.append("proc++;");
        insertLineBreak();
        insertIndentation(ind);
        sb.append(":: proc > " + n.getNumber());
        insertLineBreak();
        insertIndentation(ind + 1);
        sb.append("break;");
        insertLineBreak();
        insertIndentation(ind);
        sb.append("od;");
    }

    @Override
    public void visit(StartThreadStar n, Integer ind) {
        insertIndentation(ind);
        sb.append("byte proc;"); // Reusable variable for all StartThreads.
        insertLineBreak();
        insertIndentation(ind);
        sb.append("atomic {");
        insertLineBreak();
        for (Enumeration<StartThread> e = n.getStartThreads().elements(); e.hasMoreElements(); ) {
            //insertIndentation(sb, ind+1);
            StartThread st = e.nextElement();
            st.accept(this, ind + 1);
            insertLineBreak();
        }
        insertIndentation(ind);
        sb.append("}");
    }

    @Override
    public void visit(StmtStar n, Integer ind) {
        for (Enumeration<Stmt> e = n.getStmts().elements(); e.hasMoreElements(); ) {
            insertIndentation(ind);
            e.nextElement().accept(this, ind);
            insertLineBreak();
            ;
        }
    }

    @Override
    public void visit(SyncBlockStar n, Integer ind) {
        for (Enumeration<SyncBlockStmt> e = n.getSyncBlocks().elements(); e
                .hasMoreElements(); ) {
            e.nextElement().accept(this, ind + 1);
        }
    }

    @Override
    public void visit(SyncBlockStmt n, Integer ind) {
        insertIndentation(ind);
        sb.append("Lib_lock(");
        sb.append(variablePadding(n.getLock()));
        sb.append(");");
        insertLineBreak();

        n.getBlock().accept(this, ind);

        insertIndentation(ind);
        sb.append("Lib_unlock(");
        sb.append(variablePadding(n.getLock()));
        sb.append(");");
        insertLineBreak();
    }

    @Override
    public void visit(ThreadType n, Integer ind) {
        sb.append("proctype " + n.getTypeName() + "() {");
        insertLineBreak();
        n.getSyncBlocks().accept(this, ind + 1);
        sb.append("}");
        insertLineBreak();
        insertLineBreak();
    }

    @Override
    public void visit(ThreadTypeStar n, Integer ind) {
        for (Enumeration<ThreadType> e = n.getThreadTypes().elements(); e
                .hasMoreElements(); ) {
            e.nextElement().accept(this, ind);
        }
    }

    @Override
    public void visit(UopExpr n, Integer ind) {
        switch (n.getOperator()) {
            default:
                warn("Found non-unary operator " + n.getOperator().name() + " in unary expression.");
            case NEGA:
                sb.append("!");
                n.getOperand().accept(this, ind);
                break;
            case MINU:
                sb.append("-");
                n.getOperand().accept(this, ind);
                break;
            case INCR:
                n.getOperand().accept(this, ind);
                sb.append("++");
                break;
            case DECR:
                n.getOperand().accept(this, ind);
                sb.append("--");
                break;
        }

    }

    @Override
    public void visit(VarDeclStar n, Integer ind) {
        for (Enumeration<VarDecl> e = n.getVarDecls().elements(); e
                .hasMoreElements(); ) {
            e.nextElement().accept(this, ind);
            insertLineBreak();
            ;
        }
    }

    @Override
    public void visit(VarNameExpr n, Integer ind) {
        sb.append(variablePadding(n.getVarName()));
    }

    @Override
    public void visit(WaitStmt n, Integer ind) {
        sb.append("Lib_wait( ");
        sb.append(variablePadding(n.getCondition()));
        sb.append(");");
    }

    @Override
    public void visit(WhileStmt n, Integer ind) {
        sb.append("do");
        insertLineBreak();
        insertIndentation(ind);
        sb.append(":: ");
        n.getExpr().accept(this, ind);
        sb.append(" -> ");
        n.getStmt().accept(this, ind + 1);
        insertIndentation(ind);
        sb.append(":: else -> break");
        insertLineBreak();
        insertIndentation(ind);
        sb.append("od");
    }

}
