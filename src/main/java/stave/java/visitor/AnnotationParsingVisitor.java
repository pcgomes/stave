package stave.java.visitor;

import java.util.Enumeration;
import java.util.Hashtable;
import stave.java.annotation.JavaAnnotationScanner;
import stave.java.annotation.MetaNode;
import stave.synctask.ast.Main;
import stave.synctask.ast.Node;
import stave.synctask.ast.Program;
import stave.synctask.ast.ThreadTypeStar;

/**
 * Base class for parsing annotations. Contains the exceptions for common
 */
public abstract class AnnotationParsingVisitor extends com.sun.tools.javac.tree.TreeScanner {

    protected static boolean mwarnings = false;
    protected static boolean mdebug = true;

    AnnotationParsingVisitor() {
    }

    public static void setWarning(boolean lwarn) {
        mwarnings = lwarn;
    }

    public static boolean onWarning() {
        return mwarnings;
    }

    public static void setDebug(boolean ldebug) {
        mdebug = ldebug;
    }

    public static boolean onDebug() {
        return mdebug;
    }

    protected static void warning(String larg) {
        if (mwarnings) {
            System.err.println("Warning - " + larg);
        }
    }

    protected static void warning(String larg, Exception e) {
        warning(larg + "Message: " + e.getMessage());
    }

    protected static void warning(Exception e) {
        warning(e.getMessage());
    }

    protected static void debug(String larg) {
        if (mdebug) {
            System.err.println("Debug: - " + larg);
        }
    }

    protected static void debug(String larg, Exception e) {
        debug(larg + "Message(Exception): " + e.getMessage());
    }
 
   /* The following exceptions are to be used only while parsing annotations */

    public static Hashtable<String, Program> mergeMainAndTypes(ThreadTypeStar lttypes, Hashtable<String, Main> lmains) {
        // FIXME - Currently not checking if thread types have been used.
        // Should just add to program the Threads Types actually used
        Hashtable<String, Program> mylist = new Hashtable<String, Program>();

        debug("mergeMainAndTypes: Merging " + lmains.size() + " SynckTask programs with " + lttypes.getThreadTypes().size() + " Thread types.");

        for (Enumeration<String> e = lmains.keys(); e.hasMoreElements(); ) {
            String mystname = e.nextElement();
            mylist.put(mystname, new Program(lttypes, lmains.get(mystname)));
        }

        return mylist;
    }

    // Invoke the Annotation parser and return the corresponding object
    protected MetaNode getMetaAnnotation(String lcomment) throws AnnotationParserException, Exception {
        // Creates a reader object from the annotation string
        stave.java.annotation.parser myparser;
        myparser = new stave.java.annotation.parser(new JavaAnnotationScanner(new java.io.StringReader(lcomment)));

        java_cup.runtime.Symbol parsing_result = myparser.parse();

        // Check if parsing was successul. If not, report via Exception
        if (parsing_result.value == null) {
            throw new AnnotationParserException(lcomment);
        }

        return ((MetaNode) parsing_result.value);
    }

    // Invoke the SyncTask parser and return the corresponding code snippet
    protected Node parseSyncTaskCode(String lsnippet) throws ParsingSyncTaskException {
        try {

            // Creates a reader object from the annotation string
            stave.synctask.ast.parser myparser;
            myparser = new stave.synctask.ast.parser(new stave.synctask.ast.SyncTaskScanner(new java.io.StringReader(lsnippet)));

            java_cup.runtime.Symbol parsing_result = myparser.parse();

            // Check if parsing was successul. If not, report via Exception
            if (parsing_result.value == null) {
                throw new ParsingSyncTaskException(lsnippet);
            }

            return ((Node) parsing_result.value);

        } catch (Exception e) {
            throw new ParsingSyncTaskException(lsnippet);
        }

    }

    protected class AnnotationParserException extends Exception {
        AnnotationParserException() {
            super("Parser could not parse the annotation.");
        }

        AnnotationParserException(String lannotation) {
            super("Parser failed to parse the following annotation: \"" + lannotation + "\"");
        }
    }

    protected class ParsingSyncTaskException extends Exception {
        ParsingSyncTaskException() {
            super("Parsing of SyncTask code snippet failed.");
        }

        ParsingSyncTaskException(String lannotation) {
            super("Failed to parse SyncTask code snippet: \"" + lannotation + "\"");
        }
    }

    protected class DuplicatedDeclarationException extends Exception {
        DuplicatedDeclarationException() {
            super("Found redeclaration. Ignorning it.");
        }

        DuplicatedDeclarationException(String lname) {
            super("Symbol \"" + lname + "\" redeclared. Ignoring it.");
        }
    }

    protected class UnexpectedTypeException extends Exception {
        UnexpectedTypeException() {
            super("Parsed unexpected annotation type. Ignorning it.");
        }

        UnexpectedTypeException(String lname) {
            super("Comment \"" + lname + "\" was parsed, but isn't in the expected location. Ignoring it.");
        }
    }

    protected class UnknownTypeException extends Exception {
        UnknownTypeException() {
            super("Found uknown type. Ignorning it.");
        }

        UnknownTypeException(String lname) {
            super("Type '" + lname + "' was not declared before. Ignoring it.");
        }
    }

    /* Using unchecked, so still can inherite from TreeTranslator  */
    protected class UnsupportedJavaFeatureException extends RuntimeException {
        UnsupportedJavaFeatureException() {
            super("Tried to parse an unsupported Java feature");
        }

        UnsupportedJavaFeatureException(String lannotation) {
            super("Tried to translate an unsupported Java feature: \"" + lannotation + "\"");
        }
    }
}
