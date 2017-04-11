package stave;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.DumpVisitor;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;
import java_cup.runtime.Symbol;
import javax.tools.JavaFileManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import stave.java.ast.AJCCompilationUnit;
import stave.java.visitor.APretty;
import stave.java.visitor.AnnotationParsingVisitor;
import stave.java.visitor.MainInitVisitor;
import stave.java.visitor.PrintAstVisitor;
import stave.java.visitor.ResourceTypeVisitor;
import stave.java.visitor.ThreadCodeVisitor;
import stave.javaparser.visitor.ComplyToJCVisitor;
import stave.javaparser.visitor.JavaParser2JCTree;
import stave.synctask.ast.Program;
import stave.synctask.ast.SyncTaskScanner;
import stave.synctask.ast.parser;
import stave.synctask.visitor.PrettyPrintVisitor;
import stave.synctask.visitor.SyncTaskToCpnVisitor;
import stave.synctask.visitor.SyncTaskToPromelaVisitor;

/**
 * @author Pedro de Carvalho Gomes <pedrodcg@csc.kth.se>
 *
 *         STaVE entry point and sequence of analyses
 */
public class Main {

    static private CommandLine cmd;

    static private boolean setOptions(String[] lcommand) {

        Options loptions = new Options()
                .addOption(new Option("d", "debug", false,
                        "print debug information (highly verbose)"))
                .addOption(new Option("is", "inst", false,
                        "set input as SyncTask program"))
                .addOption(new Option("ij", "injava", false,
                        "set input as annotated Java program"))
                .addOption(new Option("oc", "outcpn", true,
                        "output Coloured Petri Net in CPN Tools format"))
                .addOption(new Option("os", "outst", true,
                        "output SynTask program to file, and leave (requires -ij or -is)"))
                .addOption(new Option("oj", "outjava", true,
                        "output annotated Java program to file, and leave. (requires -ij)"))
                .addOption(new Option("w", "warning", false,
                        "print warning messages"))
                .addOption(new Option("op", "outprom", true,
                        "output Promela program to file, and leave. (requires -ij or -is)"));

        try {
            cmd = (new DefaultParser()).parse(loptions, lcommand);
        } catch (org.apache.commons.cli.ParseException e) {
            // TODO - Check how to get this more precisely
            String mytoolcmd = "java [-cp classpaths] stave.Main";
            String myoptlist = " [options] [input_files]";
            String myheader = "Input annotated Java or SynTask source, and output Coloured Petri Nets";
            String myfooter = "\nSTaVe - More information at http://www.csc.kth.se/~pedrodcg/stave";

            HelpFormatter formatter = new HelpFormatter();
            //formatter.printUsage( new java.io.PrintWriter(System.out,true), 100, "java -jar synctask.jar", loptions );
            formatter.printHelp(mytoolcmd + myoptlist, null, loptions, myfooter, false);
            //formatter.printHelp(mytoolcmd + myoptlist, loptions, false);
            return (false);
        }
        return (true);
    }

    private static void openAnnotatedJava() {
        // Create list with source code names
        @SuppressWarnings("unchecked")
        Vector<String> myfilelist = new Vector<String>(cmd.getArgList());

        // Table that stores parsed compilation units
        Hashtable<String, CompilationUnit> myjpunitlist = new Hashtable<String, CompilationUnit>();

        // Declare outside, so filename can be used in case of exception.

        // Generate AST with JavaParser
        for (Enumeration<String> e = myfilelist.elements(); e.hasMoreElements(); ) {
            String mysource = "";
            CompilationUnit mycunit = null;

            try {
                // read filename
                mysource = e.nextElement();

                // Compile it with java parser.
                mycunit = JavaParser.parse(new FileInputStream(mysource));

                // Then fix the AST following the JC requirements
                ComplyToJCVisitor myfixervisitor = new ComplyToJCVisitor();
                mycunit = (CompilationUnit) myfixervisitor.visit(mycunit,
                        new Integer(0));

                // creates an input stream and parse it using Java Parser
                myjpunitlist.put(mysource, mycunit);

                if (cmd.hasOption("d")) {
                    // ASTDumpVisitor myjpvisitor = new ASTDumpVisitor();
                    DumpVisitor myjpvisitor = new DumpVisitor();
                    myjpvisitor.visit(myjpunitlist.get(mysource), null);
                    System.out.print(myjpvisitor.getSource());
                }
            } catch (com.github.javaparser.ParseException ex) {
                System.out
                        .println("Error: Parsing of Annotated Java file failed: "
                                + mysource + ". Exiting...");
                System.exit(1);
            } catch (FileNotFoundException ex) {
                System.out.println("Error: File not found: " + mysource
                        + ". Exiting...");
                System.exit(1);
            }
        }

        // Building internals from Java Compiler
        Context mycontext = new Context();
        JavaCompiler myjcompiler = new JavaCompiler(mycontext);
        JavaFileManager myfilemanager = mycontext.get(JavaFileManager.class);
        // Phase that Javac may go to: Setting code generation
        myjcompiler.shouldStopPolicyIfNoError = CompileState.GENERATE;

        // Table that stores the Java Compiler's ASTs
        List<JCCompilationUnit> ljctreelist = List.<JCCompilationUnit>nil();

        // Convert to Java Parser AST to JCTree AST's
        for (Enumeration<String> e = myjpunitlist.keys(); e.hasMoreElements(); ) {

            // read filename
            String mysource = e.nextElement();

            CompilationUnit myjpunit = myjpunitlist.get(mysource);

            JavaParser2JCTree translator = new JavaParser2JCTree(mycontext);
            AJCCompilationUnit myjctreeunit = (AJCCompilationUnit) translator
                    .visit(myjpunit, myjpunit);

            // Setting additional information for Javac:
            // - Source file. Otherwise it throws a NullPointerException
            myjctreeunit.sourcefile = ((JavacFileManager) myfilemanager)
                    .getFileForInput(mysource);

            // Storing in the list
            ljctreelist = ljctreelist.append(myjctreeunit);

            // Debug: Shows how the JCTree AST was generated. Output node types.
            if (cmd.hasOption("d")) {
                try {
                    Writer mystdout = new OutputStreamWriter(System.out);
                    (new PrintAstVisitor(mystdout, true))
                            .visitTopLevel(myjctreeunit);
                    mystdout.flush();
                } catch (Exception z) {
                }
            }

            // TODO - Implement the visitor to compare the ASTs generated
            // separately by JavaParser and Javac
            // JCTree.JCCompilationUnit mycunit = myjcompiler.parse(argv[0]);
        }

        // Enter (phase I): starting to build symtable
        Enter myenter = Enter.instance(mycontext);
        myenter.main(ljctreelist);
        // Enter (phase II): Finishing to build symtable
        /*
         * MemberEnter mymemberenter = MemberEnter.instance(mycontext);
		 * mymemberenter.visitTopLevel(myjctreeunit);
		 */

        // Get the todo list generated by Enter phase
        // From now on, the output of a phase is the input of the other.
        Todo mytodo = Todo.instance(mycontext);

        // atrribute: type-checking, name resolution, constant folding
        // flow: deadcode elimination
        // desugar: removes synctactic sugar: inner classes, class literals,
        // assertions, foreachs
        myjcompiler.desugar(myjcompiler.flow(myjcompiler.attribute(mytodo)));

        // generate: produce bytecode or source code. Erases the whole AST
        // myjcompiler.generate(myjcompiler.desugar(myjcompiler.flow(myjcompiler.attribute(
        // mytodo))));

        // Prints the Java program to output files and leave
        if (cmd.hasOption("oj")) {
            for (ListIterator<JCCompilationUnit> i = ljctreelist.listIterator(); i
                    .hasNext(); ) {
                JCCompilationUnit myjctreeunit = i.next();
                try {
                    Writer myoutputfile;

                    if (cmd.getOptionValue("oj") == null) {
                        myoutputfile = new FileWriter(FileDescriptor.out);
                    } else {
                        myoutputfile = new FileWriter(cmd.getOptionValue("oj"));
                    }

                    (new APretty(myoutputfile, true))
                            .visitTopLevel(myjctreeunit);
                    myoutputfile.flush();

                } catch (Exception e) {
                    // TODO - Check what to report in case of error.
                }
            }
            return;
        }

        // Setting warnings for the annotation parsing.
        if (cmd.hasOption("w")) {
            AnnotationParsingVisitor.setWarning(true);
        }

        // Setting debug for the annotation parsing. This may be highly verbose.
        if (cmd.hasOption("d")) {
            AnnotationParsingVisitor.setDebug(true);
        }

		/*
         * Resouce annotations must be processed beforehand, so that both the
		 * thread type reader (ThreadCodeVisitor) and the initialization reader
		 * (MainInitVisitor) know how to process a resouce type when they found
		 * it.
		 */
        ResourceTypeVisitor mytypereader = new ResourceTypeVisitor();
        for (ListIterator<JCCompilationUnit> i = ljctreelist.listIterator(); i
                .hasNext(); ) {
            JCCompilationUnit myjctreeunit = i.next();

            mytypereader.visitTopLevel(myjctreeunit);
        }

        // Initialize the Thread visitor and Main visitor with the resouce
        // found.
        ThreadCodeVisitor mytranslator = new ThreadCodeVisitor(
                mytypereader.getTypeList());
        MainInitVisitor mymainreader = new MainInitVisitor(
                mytypereader.getTypeList());

        // Process all compilations units for one more more SyncTask programs.
        for (ListIterator<JCCompilationUnit> i = ljctreelist.listIterator(); i
                .hasNext(); ) {
            JCCompilationUnit myjctreeunit = i.next();

            // Extract the threads codes
            mytranslator.visitTopLevel(myjctreeunit);
            // Extract the declarations relatives to initialization.
            mymainreader.visitTopLevel(myjctreeunit);
        }

        // Merge the ThreadTypes found with the initializion Main objects found.
        Hashtable<String, Program> lsynctasks = AnnotationParsingVisitor
                .mergeMainAndTypes(mytranslator.getThreadTypes(),
                        mymainreader.getSTMainList());

        // Printing resulting SyncTask to file
        if (cmd.hasOption("os")) {

            PrettyPrintVisitor mypprint;

            if (cmd.getOptionValue("os") == null) {
                mypprint = new PrettyPrintVisitor();
            } else {
                mypprint = new PrettyPrintVisitor(cmd.getOptionValue("os"));
            }

            // Process for each of the SyncTask
            for (Enumeration<Program> e = lsynctasks.elements(); e
                    .hasMoreElements(); ) {
                mypprint.visit(e.nextElement(), new Integer(0));
            }
        }

        if (cmd.hasOption("oc")) {

            try {
                SyncTaskToCpnVisitor mycpnmaker = new SyncTaskToCpnVisitor();

                // Process for each of the SyncTask
                for (Enumeration<Program> e = lsynctasks.elements(); e
                        .hasMoreElements(); ) {

                    // TODO - Currently overriding the file. Should generate a
                    // new one
                    mycpnmaker.visit(e.nextElement(), mycpnmaker
                            .getCPNToolsNet().topPage());
                    mycpnmaker.getCPNToolsNet().writeDOMtoCpnFile(
                            cmd.getOptionValue("oc"));
                }

            } catch (ParserConfigurationException
                    | TransformerConfigurationException ex) {
                System.out
                        .println("Error: Could not construct the CPN Tools representation. Exiting.");
                System.exit(1);
            } catch (TransformerException ex) {
                System.out
                        .println("Error: Could not construct the CPN Tools representation. Exiting.");
                System.exit(1);
            } catch (FileNotFoundException ex) {
                System.out.println("Error: File not found: "
                        + cmd.getOptionValue("is") + ". Exiting.");
                System.exit(1);
            }
        }

        if (cmd.hasOption("op")) {
            SyncTaskToPromelaVisitor promvisitor = new SyncTaskToPromelaVisitor();

            for (Enumeration<Program> e = lsynctasks.elements(); e
                    .hasMoreElements(); ) {
                promvisitor.visit(e.nextElement(), null); // TODO figure out
                // last parameter
                try {
                    FileWriter file = new FileWriter(cmd.getOptionValue("op"),
                            false);
                    promvisitor.write(file);
                    file.flush();
                    file.close();
                } catch (FileNotFoundException ex) {
                    System.out.println("Error: File not found: "
                            + cmd.getOptionValue("is") + ". Exiting.");
                    System.exit(1);
                } catch (IOException ex) {
                    System.out.println("Error: Generic IO Error: "
                            + cmd.getOptionValue("is") + ". Exiting.");
                    System.exit(1);
                }
            }

        }
    }

    protected static void openSynctask() {
        // Create list with source code names
        @SuppressWarnings("unchecked")
        Vector<String> myfilelist = new Vector<String>(cmd.getArgList());

        for (Enumeration<String> e = myfilelist.elements(); e.hasMoreElements(); ) {

            String mysource = e.nextElement();

            try {
                FileReader myinputfile = new FileReader(mysource);

                // Create scanner and passes to parser
                SyncTaskScanner myscanner = new SyncTaskScanner(myinputfile);
                parser myparser = new parser(myscanner);
                Symbol parse_result = myparser.parse();

                if (parse_result.value != null) {
                    PrettyPrintVisitor mypprint;
                    if (cmd.hasOption("os")) {
                        mypprint = new PrettyPrintVisitor(
                                cmd.getOptionValue("os"));
                        mypprint.visit((Program) parse_result.value,
                                new Integer(0));
                    }

                } else {
                    System.out.println("Error: Synctask Parser failed");
                    System.exit(1);
                }

                if (cmd.hasOption("oc")) {
                    SyncTaskToCpnVisitor mycpnmaker = new SyncTaskToCpnVisitor();

                    mycpnmaker.visit((Program) parse_result.value, mycpnmaker
                            .getCPNToolsNet().topPage());

                    mycpnmaker.getCPNToolsNet().writeDOMtoCpnFile(
                            cmd.getOptionValue("oc"));
                }

                if (cmd.hasOption("op")) {
                    SyncTaskToPromelaVisitor promvisitor = new SyncTaskToPromelaVisitor();
                    promvisitor.visit((Program) parse_result.value, 0); // TODO
                    // figure
                    // out
                    // last
                    // parameter
                    System.out.println("Outputting to promela: " + cmd.getOptionValue("op"));
                    FileWriter file = new FileWriter(cmd.getOptionValue("op"),
                            false);
                    promvisitor.write(file);
                    file.flush();
                    file.close();
                }

                // Do the action
            } catch (ParserConfigurationException ex) {
                System.out
                        .println("Error: Could not construct the CPN Tools representation. Exiting.");
                System.exit(1);
            } catch (FileNotFoundException ex) {
                System.out.println("Error: File not found: "
                        + cmd.getOptionValue("is") + ". Exiting.");
                System.exit(1);
            } catch (IOException ex) {
                System.err.println("Error: IO error scanning file "
                        + cmd.getOptionValue("s") + ". Exiting.");
                System.exit(1);
            } catch (Exception ex) {
                System.err.println("Error: Exception found. Exiting.");
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void main(String argv[]) {
        if ((setOptions(argv))) {

            // Input is a SyncTask file
            if (cmd.hasOption("is")) {
                openSynctask();
            } else if (cmd.hasOption("ij")) {
                openAnnotatedJava();
            }
        }
    }
}
