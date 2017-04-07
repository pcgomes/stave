package stave.promela;

public abstract class AbstractLib implements Lib {

    @Override
    public String libPadding(String call) {
        return "Lib_" + call;
    }


    protected String intBoundsPadding(String varname) {
        return "bounds_" + varname;
    }

    @Override
    public String generateIntBoundsDeclaration(String varName) {
        return String.format("Lib_Int_Bounds %s;", intBoundsPadding(varName));
    }

    @Override
    public String generateIntBoundsInitilization(String varName, int min, int max) {
        return String.format("%s.min = %d; %s.max = %d;", intBoundsPadding(varName), min, intBoundsPadding(varName), max);
    }

    @Override
    public String generateCondInitilization(String cond, String lockname) {
        return String.format("%s.lock = %s;", cond, lockname);
    }

    @Override
    public String generateLockListDeclaration(int size) {
        return String.format("Lib_Lock Lib_locks[%d];", size);
    }

    @Override
    public String generateMax(String varName) {
        return String.format(" %s.max ", intBoundsPadding(varName));
    }

    @Override
    public String generateMin(String varName) {
        return String.format(" %s.min ", intBoundsPadding(varName));
    }

}
