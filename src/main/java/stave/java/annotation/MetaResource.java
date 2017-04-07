package stave.java.annotation;

import com.sun.tools.javac.code.Symbol;
import java.util.Enumeration;
import java.util.Hashtable;
import stave.synctask.ast.Expr;
import stave.synctask.ast.Stmt;

/*
Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>

Class that contains that meta-information extracted by the annotation parser about the resouce types.
*/

public class MetaResource extends MetaNode {

    // Either int, the default, or boolean
    protected Symbol msymbol;

    // Variable identifier, as in the Java class
    // A variable that should be tracked for updates.
    protected String mjavaobject;

    // Id for the bounded variable - May be a ghost one
    protected String mvarstname;

    // Id for the variable holding the capacity. May be a ghost one.
    protected String mcapacity;

    // Default value for an int capacity. Maybe set once.
    protected Integer mdefaultcap;

    // Default initial value for the resource being tracked
    protected Integer mdefaultval;

    // Maps methods to operations over tracked variable
    protected Hashtable<String, Stmt> moperations;

    // Maps method to predicates
    protected Hashtable<String, Expr> mpredicates;

    MetaResource() {
        mjavaobject = null;
        mvarstname = new String("defaultres");
        mcapacity = null;
        mdefaultval = null;
        mdefaultcap = null;
        moperations = new Hashtable<String, Stmt>();
        mpredicates = new Hashtable<String, Expr>();
    }

    public void dumpMetaResource() {
        System.out.println("Resource: " + getId());
        System.out.println("Type: " + msymbol.toString());
        System.out.println("Java Object: " + mjavaobject);
        System.out.println("SyncTask variable: " + mvarstname);
        System.out.println("Capacity: " + mcapacity);
        System.out.println("Default variable: " + mdefaultval);
        System.out.println("Default capacity: " + mdefaultcap);

        Enumeration<String> e = mpredicates.keys();
        if (e.hasMoreElements()) {
            System.out.println("Predicate mapping:");

            while (e.hasMoreElements()) {
                stave.synctask.visitor.PrettyPrintVisitor mycodeprinter = new stave.synctask.visitor.PrettyPrintVisitor();
                String myexpr = e.nextElement();
                System.out.println("-" + myexpr + " -> ");
                mycodeprinter.visit(mpredicates.get(myexpr), new Integer(3));
                System.out.println();
            }
        }

        e = moperations.keys();
        if (e.hasMoreElements()) {
            System.out.println("Operation mapping:");

            while (e.hasMoreElements()) {
                String myop = e.nextElement();
                stave.synctask.visitor.PrettyPrintVisitor mycodeprinter = new stave.synctask.visitor.PrettyPrintVisitor();
                System.out.println("-" + myop + " -> ");
                mycodeprinter.visit(moperations.get(myop), new Integer(3));
                System.out.println();
            }
        }
    }

    /* Process the annotation before returning */
    public void concludeParse() throws Exception {

        // Several sanity checks
        if (getSTVariable() == null) {
            throw new Exception("SyncTask variable was not defined");
        }
        if (getCapacity() == null) {
            throw new Exception("Capacity was not defined");
        }

        setPredicate(getCapacity(), new stave.synctask.ast.MaxExpr(getSTVariable()));

    }

    /* Check if the annotations are valid. Otherwise report failure via exception */
    public void checkDeclValid() throws Exception {

        // Several sanity checks
        if (getSTVariable() == null) {
            throw new Exception("SyncTask variable was not defined");
        }
        if (getCapacity() == null) {
            throw new Exception("Capacity was not defined");
        }
        if (!(isDefaultCapSet())) {
            throw new Exception("Default capacity was not defined");
        }
        if (!(isDefaultValSet())) {
            throw new Exception("Default value was not defined");
        }
    }

    public String getCapacity() {
        return mcapacity;
    }

    public void setCapacity(String lvname) {
        mcapacity = lvname;
    }

    public Symbol getSymbol() {
        return msymbol;
    }

    public void setSymbol(Symbol ltype) {
        msymbol = ltype;
    }

    public String getSTVariable() {
        return mvarstname;
    }

    public void setSTVariable(String lvar) {
        mvarstname = lvar;
    }

    public String getJavaObject() {
        return mjavaobject;
    }

    public void setJavaObject(String lvar) {
        mjavaobject = lvar;
    }

    // Return if variable isn't mapped from a real Java variable.
    public boolean isGhostVar() {
        return (mjavaobject == null);
    }

    public boolean isDefaultValSet() {
        return mdefaultval != null;
    }

    public int getDefaultVal() {
        return mdefaultval;
    }

    public void setDefaultVal(int ldefault) {
        mdefaultval = ldefault;
    }

    public boolean isDefaultCapSet() {
        return mdefaultcap != null;
    }

    public int getDefaultCap() {
        return mdefaultcap;
    }

    public void setDefaultCap(int ldefault) {
        mdefaultcap = ldefault;
    }

    public boolean hasOperation(String lid) {
        return moperations.containsKey(lid);
    }

    public Stmt getOperation(String lid) {
        return moperations.get(lid);
    }

    public void setOperation(String lid, Stmt lop) {
        moperations.put(lid, lop);
    }

    public boolean hasPredicat(String lid) {
        return mpredicates.containsKey(lid);
    }

    public Expr getPredicate(String lid) {
        return mpredicates.get(lid);
    }

    public void setPredicate(String lid, Expr lpred) {
        mpredicates.put(lid, lpred);
    }

    public Hashtable<String, Expr> getPredicates() {
        return mpredicates;
    }

    public Hashtable<String, Stmt> getOperations() {
        return moperations;
    }


    public Enumeration<String> availablePredicates() {
        return mpredicates.keys();
    }

    public Enumeration<String> availableOperations() {
        return moperations.keys();
    }
}
