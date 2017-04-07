package stave.java.annotation;

import java.util.Enumeration;
import java.util.Hashtable;

/*
Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>

Annotations that are part of a global context of other annotation.
E.g., a predicate annotation inside a resource annotation.
It stores the effect of a method invocation over the resource.

*/
public class MetaResMethod extends MetaNode {

    protected boolean minline;

    protected String mcode;

    protected Hashtable<String, String> midmap;

    MetaResMethod() {
        // DEfault it to have empty code
        mcode = new String();
        setInline(false);
        midmap = new Hashtable<String, String>();
    }

    public boolean isInline() {
        return minline;
    }

    public void setInline(boolean lin) {
        minline = lin;
    }

    public boolean isCode() {
        return !minline;
    }

    public void addIdMap(String lfrom, String lto) {
        midmap.put(lfrom, lto);
    }

    public String getIdMap(String lfrom) {
        return midmap.get(lfrom);
    }

    public boolean hasIdMap(String lfrom) {
        return midmap.containsKey(lfrom);
    }

    public String getSTCode() {
        return mcode;
    }

    public void setSTCode(String lsnippet) {
        mcode = lsnippet;
    }

    public Enumeration<String> getMappedIds() {
        return midmap.keys();
    }
}
