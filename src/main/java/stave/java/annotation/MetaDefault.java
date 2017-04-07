package stave.java.annotation;

/*
Author: Pedro de Carvalho Gomes <pedrodcg@kth.se>

Collect the annotations for default capacity (@defaultcap) or default value (@defaultval)

*/
public class MetaDefault extends MetaNode {

    protected DefType mtype;

    // The actual default value for either 'value', or 'capacity'
    protected Integer mvalue;

    MetaDefault() {
        mtype = DefType.VAL;
        mvalue = null;
    }

    MetaDefault(DefType ltype) {
        mtype = ltype;
        mvalue = null;
    }

    MetaDefault(DefType ltype, int lvalue) {
        this(ltype);
        mvalue = lvalue;
    }

    public boolean isCapacity() {
        return mtype == DefType.CAP;
    }

    public boolean isValue() {
        return mtype == DefType.VAL;
    }

    public String getTag() {
        if (isCapacity()) {
            return new String("@defaultcap");
        } else if (isValue()) {
            return new String("@defaultval");
        } else {
            return new String("Unknown");
        }
    }

    public void setTypeToValue() {
        mtype = DefType.VAL;
    }

    public void setTypeToCapacity() {
        mtype = DefType.CAP;
    }

    public boolean isSet() {
        return mvalue != null;
    }

    public void set(int lval) {
        mvalue = new Integer(lval);
    }

    public int get() {
        return mvalue;
    }

    public static enum DefType {
        CAP,
        VAL,
    }
}
