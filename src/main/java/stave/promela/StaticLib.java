package stave.promela;

import java.io.IOException;


public class StaticLib extends AbstractLib {
    static final String lib =
            "typedef Lib_Lock { \n"
                    + "  bool locked;"
                    + "  pid p; \n"
                    + "  byte locks; \n"
                    + "} \n"
                    + "inline Lib_lock(x) { \n"
                    + "  atomic { \n"
                    + "    Lib_locks[x].locked == false || Lib_locks[x].locked == true && Lib_locks[x].p == _pid\n"
                    + "    if \n"
                    + "    :: (Lib_locks[x].locked == false) -> Lib_locks[x].locked = true; Lib_locks[x].p = _pid; Lib_locks[x].locks = 1; \n"
                    + "    :: (Lib_locks[x].locked == true && Lib_locks[x].p == _pid) -> Lib_locks[x].locks++; \n"
                    + "    fi"
                    + "  } \n"
                    + "} \n"
                    + "inline Lib_unlock(x) { \n"
                    + "  atomic {\n"
                    + "    Lib_locks[x].locks > 0\n"
                    + "    assert(Lib_locks[x].locked == true); \n"
                    + "    if \n"
                    + "    :: (Lib_locks[x].locks > 1) -> Lib_locks[x].locks--;\n"
                    + "    :: (Lib_locks[x].locks == 1) -> Lib_locks[x].locks = 0; Lib_locks[x].locked = false;\n"
                    + "    fi\n"
                    + "  }\n"
                    + "} \n"
                    + " \n"
                    + "typedef Lib_Int_Bounds { \n"
                    + "  int max; \n"
                    + "  int min; \n"
                    + "} \n"
                    + " \n"
                    + "typedef Lib_Cond { \n"
                    + "  byte lock; \n" //Index into array of locks
                    + "  byte waiting; \n"
                    + "  byte notified; \n"
                    + "} \n"
                    + "inline Lib_wait(cv) { \n"
                    + "  atomic { \n"
                    + "    cv.waiting++; \n"
                    + "	  Lib_unlock(cv.lock); \n"
                    + "  } \n"
                    + "  do \n"
                    + "	 :: 1 -> "
                    + "    (cv.notified > 0) \n"
                    + "	   atomic { \n"
                    + "	     if \n" //Still notified
                    + "      :: cv.notified > 0 ->\n"
                    + "        cv.notified--; \n"
                    + "        cv.waiting--; \n" //Accept notification
                    + "        break; \n" //escape the loop
                    + "      :: else -> skip;\n"
                    + "		 fi \n"
                    + "	  } \n"
                    + "  od \n"
                    + "  Lib_lock(cv.lock); \n" //Reclaim the lock
                    + "} \n"
                    + "inline Lib_notify(cv) { \n"
                    + "  if \n"
                    + "  :: cv.waiting > 0 -> cv.notified++; \n"
                    + "  fi;"
                    + "} \n"
                    + "inline Lib_notifyAll(cv) { \n"
                    + "  cv.notified = cv.waiting; \n"
                    + "} \n"
                    + "";

    private static StaticLib instance;

    public static Lib getInstance() {
        if (instance == null) {
            instance = new StaticLib();
        }
        return instance;
    }

    public String getLibrary() throws IOException {
        return lib;
    }
}
