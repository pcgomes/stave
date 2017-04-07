/**
 * Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>
 */

package stave.synctask.visitor;

import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Enumeration;
import stave.synctask.ast.AssignStmt;
import stave.synctask.ast.BlockStmt;
import stave.synctask.ast.BoolDecl;
import stave.synctask.ast.BopExpr;
import stave.synctask.ast.CondDecl;
import stave.synctask.ast.ConstBoolExpr;
import stave.synctask.ast.ConstIntExpr;
import stave.synctask.ast.Expr;
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

public class PrettyPrintVisitor implements VoidVisitor<Integer> {

    private static String ind;
    private static String lsep;

    // Set-up line separator for the host OS
    // and type of indentation
    static {
        lsep = System.getProperty("line.separator");
        ind = "   ";
    }

    private Writer out_code;
    private String fname = null;

    // Initialize with default indentation
    public PrettyPrintVisitor() {
    }

    // Initialize with default indentation
    public PrettyPrintVisitor(Writer lout) {
        out_code = lout;
    }

    // Initialize object for symbol table
    public PrettyPrintVisitor(String filename) {
        fname = filename;
    }

    // Initialize object for symbol table
    public PrettyPrintVisitor(String filename, String lindent) {
        fname = filename;
        ind = lindent;
    }

    // Get the object that was writen
    public Writer getWriter() {
        return out_code;
    }

    private void openBuffer() {
        if (out_code == null) {
            // Opening output file
            if (fname != null) {
                try {
                    out_code = new FileWriter(fname);
                } catch (java.io.IOException e) {
                    System.err.println("Error: Could not open file '" + fname + "'");
                    System.err.println("Writing to standard output.");
                    // No outputfile provided. Writing in stdout
                    out_code = new FileWriter(FileDescriptor.out);
                }
            } else {
                out_code = new FileWriter(FileDescriptor.out);
            }
        }
    }

    //TODO - Should not close if File is stdout
    private void flushBuffer() {
        // Flush and close the file
        try {
            out_code.flush();
        } catch (java.io.IOException e) {
            System.err.println("Error: Raised exception when flusing SyncTask code from buffer");
        }
    }

    // Write in the file
    private void writeCode(String code) {
        try {
            out_code.write(code);
        } catch (java.io.IOException e) {
            // TODO: what should do if can not write even to std::out?
        }
    }

    // Write in the file
    private void writeCodeNewLine(String code) {
        writeCode(code + lsep);
    }

    // Write in the file
    private void writeNewLine() {
        writeCode(lsep);
    }

    // Keep nest level, to
    // indent correctly
    //private int lnest = 0;

    private void indent(int lnest) {
        for (int i = 0; i < lnest; i++) {
            writeCode(ind);
        }
    }

    // Write in the file
    private void indentedLine(int lnest, String code) {
        indent(lnest);
        writeCode(code + lsep);
    }

   /* Entering method for partial code printing */

    public void visit(Expr n, Integer lnest) {
        openBuffer();
        n.accept(this, lnest);
        flushBuffer();
    }

    public void visit(Stmt n, Integer lnest) {
        openBuffer();
        n.accept(this, lnest);
        flushBuffer();
    }

   /* AST Elements  */

    public void visit(Program n, Integer lnest) {
        openBuffer();
        n.getThreadTypes().accept(this, lnest);
        n.getMain().accept(this, lnest);
        flushBuffer();
    }

    public void visit(ThreadTypeStar n, Integer lnest) {
        for (Enumeration<ThreadType> e = n.getThreadTypes().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, lnest);
        }
    }

    public void visit(ThreadType n, Integer lnest) {
        indentedLine(lnest, "Thread " + n.getTypeName() + " {");
        n.getSyncBlocks().accept(this, lnest + 1);
        indentedLine(lnest, "}");
    }

    public void visit(SyncBlockStar n, Integer lnest) {
        for (Enumeration<SyncBlockStmt> e = n.getSyncBlocks().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, lnest);
        }
    }

    public void visit(SyncBlockStmt n, Integer lnest) {
        indentedLine(lnest, "synchronized (" + n.getLock() + ")");
        n.getBlock().accept(this, lnest);
    }

    public void visit(BlockStmt n, Integer lnest) {
        indentedLine(lnest, "{");
        n.getStmts().accept(this, lnest + 1);
        indentedLine(lnest, "}");
    }

    public void visit(StmtStar n, Integer lnest) {
        for (Enumeration<Stmt> e = n.getStmts().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, lnest);
        }
    }

    public void visit(AssignStmt n, Integer lnest) {
        indent(lnest);
        n.getTargetVar().accept(this, lnest);
        writeCode("=");
        n.getExpr().accept(this, lnest);
        writeCodeNewLine("; ");
    }

    public void visit(IfElseStmt n, Integer lnest) {
        indent(lnest);
        writeCode("if (");
        n.getExpr().accept(this, lnest);
        writeCodeNewLine(") ");
        if (n.getStmtIf() instanceof BlockStmt) {
            n.getStmtIf().accept(this, lnest);
        } else {
            n.getStmtIf().accept(this, lnest + 1);
        }
        indentedLine(lnest, "else ");
        if (n.getStmtElse() instanceof BlockStmt) {
            n.getStmtElse().accept(this, lnest);
        } else {
            n.getStmtElse().accept(this, lnest + 1);
        }
    }

    public void visit(WhileStmt n, Integer lnest) {
        indent(lnest);
        writeCode("while (");
        n.getExpr().accept(this, lnest);
        writeCodeNewLine(") ");
        if (n.getStmt() instanceof BlockStmt) {
            n.getStmt().accept(this, lnest);
        } else {
            n.getStmt().accept(this, lnest + 1);
        }
    }

    public void visit(SkipStmt n, Integer lnest) {
        indentedLine(lnest, "skip;");
    }

    public void visit(NotifyStmt n, Integer lnest) {
        indentedLine(lnest, "notify(" + n.getCondition() + ");");
    }

    public void visit(NotifyAllStmt n, Integer lnest) {
        indentedLine(lnest, "notifyAll(" + n.getCondition() + ");");
    }

    public void visit(WaitStmt n, Integer lnest) {
        indentedLine(lnest, "wait(" + n.getCondition() + ");");
    }

    public void visit(Main n, Integer lnest) {
        openBuffer();
        indentedLine(lnest, "main ");
        indentedLine(lnest, "{");
        n.getVarDecl().accept(this, lnest + 1);
        n.getStartThreads().accept(this, lnest + 1);
        indentedLine(lnest, "}");
        flushBuffer();
    }

    public void visit(VarDeclStar n, Integer lnest) {
        for (Enumeration<VarDecl> e = n.getVarDecls().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, lnest);
        }
    }

    public void visit(BoolDecl n, Integer lnest) {
        indentedLine(lnest, "Bool " + n.getVarName() + "(" + Boolean.toString(n.getValue()) + ");");
    }

    public void visit(IntDecl n, Integer lnest) {
        indentedLine(lnest, "Int " + n.getVarName() + "(" +
                Integer.toString(n.getLowerBound()) + "," +
                Integer.toString(n.getUpperBound()) + "," +
                Integer.toString(n.getValue()) + ");");
    }

    public void visit(LockDecl n, Integer lnest) {
        indentedLine(lnest, "Lock " + n.getVarName() + "();");
    }

    public void visit(CondDecl n, Integer lnest) {
        indentedLine(lnest, "Cond " + n.getVarName() + "(" + n.getLock() + ");");
    }

    public void visit(StartThreadStar n, Integer lnest) {
        for (Enumeration<StartThread> e = n.getStartThreads().elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, lnest);
        }
    }

    public void visit(StartThread n, Integer lnest) {
        indentedLine(lnest, "start(" + Integer.toString(n.getNumber()) + "," + n.getType() + ");");
    }

    public void visit(BopExpr n, Integer lnest) {
        writeCode("(");
        n.getLeftOperand().accept(this, lnest);
        switch (n.getOperator()) {
            case LAND:
                writeCode("&&");
                break;
            case LOOR:
                writeCode("||");
                break;
            case EQUA:
                writeCode("==");
                break;
            case BIGT:
                writeCode(">");
                break;
            case NTEQ:
                writeCode("!=");
                break;
            case LOWT:
                writeCode("<");
                break;
            case ADDI:
                writeCode("+");
                break;
            case MINU:
                writeCode("-");
                break;
            default:
                System.err.println("Warning: Operating over invalid operator");
        }
        n.getRightOperand().accept(this, lnest);
        writeCode(")");
    }

    public void visit(UopExpr n, Integer lnest) {
        writeCode("(");
        switch (n.getOperator()) {
            case NEGA:
                writeCode("!");
                break;
            default:
                System.err.println("Warning: Operating over invalid operator");
        }
        n.getOperand().accept(this, lnest);
        writeCode(")");
    }

    public void visit(ConstBoolExpr n, Integer lnest) {
        writeCode(Boolean.toString(n.getValue()));
    }

    public void visit(ConstIntExpr n, Integer lnest) {
        writeCode(Integer.toString(n.getValue()));
    }

    public void visit(VarNameExpr n, Integer lnest) {
        writeCode(n.getVarName());
    }

    public void visit(MinExpr n, Integer lnest) {
        writeCode("min(" + n.getVarName() + ")");
    }

    public void visit(MaxExpr n, Integer lnest) {
        writeCode("max(" + n.getVarName() + ")");
    }

    // Selector visitor - Used as entry node for code snippets
    public void visit(Node n, Integer lnest) {
        n.accept(this, lnest);
    }
}
