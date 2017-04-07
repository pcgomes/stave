package stave.promela;

import java.io.IOException;

public interface Lib {

    public String getLibrary() throws IOException;

    public String libPadding(String varname);

    public String generateIntBoundsDeclaration(String varName);

    public String generateIntBoundsInitilization(String varName, int min, int max);

    public String generateCondInitilization(String cond, String lockname);

    public String generateLockListDeclaration(int size);

    public String generateMax(String varName);

    public String generateMin(String varName);
}