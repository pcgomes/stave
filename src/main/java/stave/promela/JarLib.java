package stave.promela;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JarLib extends AbstractLib {

    String lib;

    public String getLibrary() throws IOException {
        if (lib == null) {
            lib = loadLibrary();
        }
        return lib;
    }

    public String loadLibrary() throws IOException {
        InputStream file = getClass().getResourceAsStream(
                "/stave/promela/Library.pml");
        InputStreamReader r = new InputStreamReader(file,
                StandardCharsets.UTF_8);

        char[] buf = new char[1024];
        StringBuilder sb = new StringBuilder();
        try {
            while (true) {
                int end = r.read(buf);
                sb.append(sb);
                if (end == -1) {
                    break;
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return sb.toString();
    }
}
