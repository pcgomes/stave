/*
* Models SyncTask construct, such as expressions, statements and variables,
* as Coloured Petri ned subnets, in CPN Tools format.
* 
* Author: Pedro de Carvalho Gomes <pedrodcg@csc.kth.se>
* Last update: 2015-07-02
*/

package stave.cpntools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SyncTaskCPN extends CPNToolsNetFactory {

    // Default separation between elements - Distance is relative from their centers
    private final int mspacingx = 126;
    private final int mspacingy = 126;
    // Store all the control points where a wait was invoked.
    protected HashSet<String> mcpoints;
    // Stores the thread types found and the number of starting threads
    protected Hashtable<String, Integer> menumthread;
    // Stores the Thread Type being processed
    String mcurrentthread = "";
    // Stores the wainting control point counter for this thread.
    int mcontrolpointid = 0;
    // Stores the hierarchy of instance elements, so new substitution transitions are added accordingly.
    Stack<Element> minstance;
    private TopPage mtoppage;
    // Elements from the toppage
    private HashSet<Element> mglobalplaces;
    private HashSet<Element> mthreadsubtrans;
    // Store the list of var->vartype. TODO - Must be replaces later.
    private Hashtable<String, String> mvartype;
    private Hashtable<String, String> mvarinit;

    /**
     * Create the basic DOM structure and add the mandatory SyncTask constructs,
     * such as the main page, which contains the Start and End places for threads.
     */
    public SyncTaskCPN() throws javax.xml.parsers.ParserConfigurationException {
        super();

        // Start the element that stores the instance hierarchy.
        minstance = new Stack<Element>();
        setNextInstance(minstances);

        // Initialize the structure that will store the thread types
        menumthread = new java.util.Hashtable<String, Integer>();

        // Initialize structues used to generate the control points
        mcpoints = new java.util.HashSet<String>();

        // Creates the top page
        mtoppage = new TopPage();
    }

    /**
     * The object representing the top page in the CPN hierarchy
     *
     * @return Object of the top page
     */
    public TopPage topPage() {
        return mtoppage;
    }

    // TODO - Think of a better way to do this
    public void addVarDeclarations() {

        // Adding variable that will store control point, during notify and notifyAll.
        appendElementToGlobbox(createVarDecl("vcpoint", "CPOINT"));

        // Add the ML declaration for the global variables found, to be used in expressions evaluation.
        for (Enumeration<String> e = mvartype.keys(); e.hasMoreElements(); ) {

            String lvarname = e.nextElement();
            appendElementToGlobbox(createVarDecl(lvarname, mvartype.get(lvarname)));

          /* This was removed because at first there's no need for a primed version for variables
          String lvarname = e.nextElement();
          // create the variable instances
          HashSet<String> lvars = new HashSet<String>();
          lvars.add( lvarname );
          lvars.add( lvarname + "_" );
          appendElementToGlobbox( createVarDeclList( lvars, mvartype.get( lvarname ) ) );
          */
        }
    }

    /**
     * Add the LOCK color set declaration to the DOM
     */
    public void addLockColset() {
        appendElementToGlobbox(createUnitColset("LOCK"));
    }

    /**
     * Add the BOOL color set declaration to the DOM
     */
    public void addBoolColset() {
        appendElementToGlobbox(createBoolColset("BOOL"));
    }

    /**
     * Add the declaration for the color set for the condition variables.
     */
    public void addConditionColset() {
        try {
            //appendElementToGlobbox( createEnumColset( "CONDITION" , mcpoints) );
            appendElementToGlobbox(createEnumColset("CPOINT", mcpoints));
            appendElementToGlobbox(createPairColset("CONDITION", "THREAD", "CPOINT"));
        } catch (BadCPNDefinitionException e) {
            warning(e);
            warning("No waiting point found - Creating a dummy CONDITION color set.");
            appendElementToGlobbox(createUnitColset("CONDITION"));
        }
    }

    /**
     * Add the declaration for the color set for the thread types.
     */
    public void addThreadColset() {
        try {
            appendElementToGlobbox(createEnumColset("THREAD", menumthread.keySet()));
        } catch (BadCPNDefinitionException e) {
            warning(e);
        }
    }

    /**
     * Add the declaration for a color set of a bounded integer.
     */
    public void addIntColset(String pname, String plower, String puper) {
        appendElementToGlobbox(createIntColset(pname, plower, puper));
    }

    /**
     * Generates a new bounded integer type for the given interval, if such doesn't exist yet.
     *
     * @param plbound The (inclusive) lower bound
     * @param pubound The (inclusive) upper bound
     * @return name to reference the generated type.
     */
    public String makeOrGetBoundedIntType(int plbound, int pubound) {
        String lname = new String("INT" + plbound + "_" + pubound);

        NodeList ltypeids = mglobbox.getElementsByTagName("id");
        for (int i = 0; i < ltypeids.getLength(); i++) {

            if (lname.equals(ltypeids.item(i).getNodeValue())) {
                // Some other variable with the exact same bound has already been declared.
                // There's no need to add another declaration. Returning it.
                return lname;
            }
        }

        // Create type and return string identifier,
        addIntColset(lname, Integer.toString(plbound), Integer.toString(pubound));
        return lname;
    }

    /**
     * Adds a thread type to this program and how many threads of its kind are spawned
     *
     * @param pthreadtype The name idenfying the thread type
     * @param pamount     Number of spawned threads to add.
     */
    public void addEnumThread(String ptype, int pamount) {
        // Negative values should never be set. But if they are, they're ignored.
        int lamount = (pamount > 0) ? pamount : 0;

        if (menumthread.containsKey(ptype)) {
            menumthread.put(ptype, lamount + menumthread.get(ptype));
        } else {
            menumthread.put(ptype, lamount);
        }
    }

    /**
     * Adds a thread type to the thread description, but does not increase the number of spawned thread.
     *
     * @param pthreadtype The name idenfying the thread type
     */
    public void addEnumThread(String ptype) {
        addEnumThread(ptype, 0);
    }

    /**
     * Creates a new unique idenfiers of a control point cointaing a wait() call.
     */
    public String addUniqueControlPoint() {
        String lcp = new String(mcurrentthread + "_" + mcontrolpointid);
        mcpoints.add(lcp);
        mcontrolpointid++;

        return lcp;
    }

    public String currentThread() {
        return mcurrentthread;
    }

    /**
     * Set the instance element to be the working one, i.e., the one accepting new sub-instances.
     */
    public void setNextInstance(Element pnext) {
        minstance.push(pnext);
    }

    /**
     * Restore the previous instance of the working instance element
     */
    public void restorePrevInstance() {
        if (!(minstance.empty())) {
            minstance.pop();
        }
    }

    /**
     * Add instance element to the working instance element.
     *
     * @param lnewinstance Instance element to be added to the DOM.
     */
    public void addInstance(Element lnewinstance) {
        minstance.peek().appendChild(lnewinstance);
    }

    /**
     * Add instance element to the working instance element,
     * and make it the current working instance.
     *
     * @param lnewinstance Instance element to be added to the DOM.
     */
    public void addAndSetNextInstance(Element lnewinstance) {
        addInstance(lnewinstance);
        setNextInstance(lnewinstance);
    }

    // NOTE: This must be invoked before starting to parse the code.
    // It guarantee that all threads will have their "awaken" place
    public void createAwakenFusionPlaces() {
        for (Enumeration<String> e = menumthread.keys(); e.hasMoreElements(); ) {
            appendElementToCpnet(createFusion("awaken_" + e.nextElement()));
        }
    }

    // Base class for all SyncTask nodes
    public static class Node {
        // Must generate uniqids, to create unique names for pages
        // This is a requirement for CPN Tools state-space exploration
        private static long muniqueid = 0;

        protected static String getUniqueId() {
            return Long.toString(muniqueid++);
        }
    }

    /**
     * Creates different subpages for the SyncTask elements
     */
    public abstract class Stmt extends Node {

        protected Element mpage;
        // (inport)i
        protected Element mpinport;
        // (inport)o
        protected Element mpoutport;

        /**
         * Set the page name to be displayed in the binder and left menu
         *
         * @param ppagename Page name
         */
        protected Stmt(String ppagename) {
            mpage = createPage(ppagename + getUniqueId());
            appendElementToCpnet(mpage);
        }

        public Element element() {
            return mpage;
        }

        public Element inPort() {
            return mpinport;
        }

        public Element outPort() {
            return mpoutport;
        }

        protected Element addInPortPlace(String pname, String ptype, String pinit) {
            return (Element) mpage.appendChild(createInPortPlace(pname, ptype, pinit));
        }

        protected Element addOutPortPlace(String pname, String ptype, String pinit) {
            return (Element) mpage.appendChild(createOutPortPlace(pname, ptype, pinit));
        }

        protected Element addSubstitutionTransition(String pname) {
            return (Element) mpage.appendChild(createSubstitutionTransition(pname));
        }

        public void linkSubstitutionTransition(Element pstransition, Element pinsocket, Element poutsocket, Stmt psubpage) {
            connectSubstitutionPage(pstransition, pinsocket, poutsocket, psubpage.element(), psubpage.inPort(), psubpage.outPort());
        }

        protected Element addInhibitorArc(Element pplace, Element ptrans) {
            return (Element) mpage.appendChild(createInhibitorArc(pplace, ptrans));
        }

        protected Element addArcPtoT(Element pplace, Element ptrans, String pexpression) {
            return (Element) mpage.appendChild(createArcPtoT(pplace, ptrans, pexpression));
        }

        protected Element addArcTtoP(Element ptrans, Element pplace, String pexpression) {
            return (Element) mpage.appendChild(createArcTtoP(ptrans, pplace, pexpression));
        }

        protected Element addPlace(String pname, String ptype, String pinit) {
            return (Element) mpage.appendChild(createBasicPlace(pname, ptype, pinit));
        }

        // TODO - Check to discontiue
        protected Element addFusionPlace(String pfusionname, String pname, String ptype, String pinit) {
            return (Element) mpage.appendChild(createFusionPlace(pfusionname, pname, ptype, pinit));
        }

        protected Element addFusionPlace(String pname) {
            return (Element) mpage.appendChild(createFusionPlace(pname, pname, mvartype.get(pname), mvarinit.get(pname)));
        }

        protected Collection<Element> addAllFusionPlaces(Hashtable<String, String> pvars) {
            HashSet<Element> lplaces = new HashSet<Element>();
            for (Enumeration<String> e = pvars.keys(); e.hasMoreElements(); ) {
                lplaces.add(addFusionPlace(e.nextElement()));
            }
            return lplaces;
        }

        // Connect all places to a given transition with reflexive arcs (bidirectional, with same expression).
        // Connect a places and a transition with a reflexive arc (place)<-exp->[transition]
        // Notice: in CPN Tools it is represented by two distinct arcs, with opposite orientation.
        protected Collection<Element> addReflexiveArcs(Element ptrans, Collection<Element> pvars) {
            HashSet<Element> larcs = new HashSet<Element>();
            for (Iterator<Element> e = pvars.iterator(); e.hasNext(); ) {
                Element lplace = e.next();
                // TODO - Check a better way to get the name
                String lvarname = findFirstTag(lplace, "text").getTextContent();
                larcs.add(addArcTtoP(ptrans, lplace, lvarname));
                larcs.add(addArcPtoT(lplace, ptrans, lvarname));
            }
            return larcs;
        }

        protected Element addTransition(String pname) {
            return (Element) mpage.appendChild(createBasicTransition(pname));
        }

        protected Element addConditionTransition(String pname, String pcondition) {
            return (Element) mpage.appendChild(createConditionTransition(pname, pcondition));
        }

        protected Element addFusionAndFusionPlace(String pfusionname, String pname, String ptype, String pinit) {

            // Step1: Create the fusion set that all instances of this variable must participate.
            Element lfusion = createFusion(pname);
            appendElementToCpnet(lfusion);

            // Step2: Create the fusion place, which will also add itself to the fusion set
            return addFusionPlace(pname, pname, ptype, pinit);
        }

    }

    public class TopPage extends Stmt {

        /**
         * Create the top page, which contains declaration of inital markings
         * and global variables, represented by places with fusion sets.
         */
        private TopPage() {
            //super( "GlobalDeclarations" );
            super("SyncTask");

            // Append the page and instantiate it
            //appendElementToCpnet( mtoppage );
            addAndSetNextInstance(createInstanceForPage(mpage));

            // Create start place
            mpinport = addPlace("startPlace", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mpinport, 0, 1 * mspacingy);

            // Create end place
            mpoutport = addPlace("endPlace", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mpoutport, 0, 3 * mspacingy);

            // Initialize structues that will store the objects in this page
            mglobalplaces = new HashSet<Element>();
            mthreadsubtrans = new HashSet<Element>();
            ;

            // Structures that will keep track of all vars in the program.
            mvartype = new Hashtable<String, String>();
            mvarinit = new Hashtable<String, String>();
        }

        /**
         * Create the place representing a global variable, and set the information about its fusion set.
         *
         * @param pname varible name, as in the SyncTask program
         * @param ptype variable type
         * @param pinit initial marking, in CPN tools notation
         * @return place containing information about the fusion set.
         */
        public Element createGlobalVariable(String pname, String ptype, String pinit) {

            // Stores it in the "mini-symbol table". TODO - Replace this structure.
            mvartype.put(pname, ptype);
            mvarinit.put(pname, pinit);

            // Step1: Create the fusion set that all instances of this variable must participate.
            Element lplace = addFusionAndFusionPlace(pname, pname, ptype, pinit);
            LayoutFactory.FusionPlace.setDefaultLayout(lplace);

            // Store reference for further positioning/layout
            // TODO - Check if this is really necessary.
            mglobalplaces.add(lplace);

            return lplace;
        }

        /**
         * Retrieve the reference to the top page
         *
         * @return Reference to top page object
         */
        public Element getTopPage() {
            return mtoppage.element();
        }

        /**
         * Creaet a substitution transition for a thread type in the top page.
         *
         * @param pttype Name of the thread type being processed
         * @return Substitution transition element. Must be used later for
         */
        public Element addThreadTypeTopPage(String pttype) {

            mcurrentthread = pttype;
            mcontrolpointid = 0;

            // Create substitution transition and add it to top page.
            Element ltthread = addSubstitutionTransition(pttype);

            // Create default layout and st to list which will be positioned in the page later.
            LayoutFactory.SubstitutionTransition.setDefaultLayout(ltthread);
            // TODO - Replace this structure by a method that collects all STs
            mthreadsubtrans.add(ltthread);

            // (start) -> [[thread]]
            Element larc = addArcPtoT(mpinport, ltthread, "1`" + pttype);
            // [[thread]] -> (end)
            larc = addArcTtoP(ltthread, mpoutport, "1`" + pttype);

            return ltthread;
        }

        /**
         * Create the initial place in the top page, containing the executing tokens.
         */
        public void setStartPlaceMarking() {
            Element linitmark = (Element) findFirstTag(mpinport, "initmark");
            Element ltext = (Element) findFirstTag(linitmark, "text");
            ltext.setTextContent(createMarkingText(menumthread));
        }

        /**
         * Set the layout of elements in the main page
         */
        public void concludeTopPage() {
            // Set initial marking.
            setStartPlaceMarking();

            // Set the layout information
            // TODO - Replace mglobalplaces and mthreadsubtrans by searches
            LayoutFactory.FusionPlace.distributeHorizontally(mglobalplaces, 0, mspacingx);
            LayoutFactory.SubstitutionTransition.distributeHorizontally(mthreadsubtrans, 2 * mspacingx, mspacingx);

            LayoutFactory.Arc.positionAllArcs(this.element());

            restorePrevInstance();
        }
    }

    /**
     * Declare pages that contain a single substitution transition
     */
    public abstract class StmtS1 extends Stmt {
        // [[s1]]
        protected Element mts1;

        public StmtS1(String pname) {
            super(pname);
            mts1 = addSubstitutionTransition("s1");
        }

        public Element transS1() {
            return mts1;
        }

        public abstract Element insocketS1();

        public abstract Element outsocketS1();

        public void connectS1(Stmt psubpage) {
            linkSubstitutionTransition(transS1(), insocketS1(), outsocketS1(), psubpage);
        }
    }

    /**
     * Declare pages that contain two substitution transition
     */
    public abstract class StmtS1S2 extends StmtS1 {
        // [[s2]]
        protected Element mts2;

        public StmtS1S2(String pname) {
            super(pname);
            mts2 = addSubstitutionTransition("s2");
        }

        public Element transS2() {
            return mts2;
        }

        public abstract Element insocketS2();

        public abstract Element outsocketS2();

        public void connectS2(Stmt psubpage) {
            linkSubstitutionTransition(transS2(), insocketS2(), outsocketS2(), psubpage);
        }
    }

    /**
     * Creates the page for sequential composition
     */
    public class Composition extends StmtS1S2 {

        // (s1s2)
        protected Element mps1s2;

        public Composition(String pttype) {
            super("Composition_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 0, 0);

            // [[s1]]
            LayoutFactory.SubstitutionTransition.setLayoutAndPosition(mts1, mspacingx, 0);

            // (inport)i -> [[s1]]
            larc = addArcPtoT(mpinport, mts1, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (s1s2)
            mps1s2 = addPlace("s1s2", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mps1s2, 2 * mspacingx, 0);

            // [[s1]] -> (s1s2)
            larc = addArcTtoP(mts1, mps1s2, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [[s2]]
            LayoutFactory.SubstitutionTransition.setLayoutAndPosition(mts2, 3 * mspacingx, 0);

            // (s1s2) -> [[s2]]
            larc = addArcPtoT(mps1s2, mts2, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 4 * mspacingx, 0);

            // [[s2]] -> (outport)o
            larc = addArcTtoP(mts2, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);
        }

        public Element insocketS1() {
            return mpinport;
        }

        public Element outsocketS1() {
            return mps1s2;
        }

        public Element insocketS2() {
            return mps1s2;
        }

        public Element outsocketS2() {
            return mpoutport;
        }
    }

    /* Creates the page for thread declaration */
    public class Thread extends StmtS1 {

        protected Element mpawaken;

        public Thread(String pttype) {
            super("Thread_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 0, 0);

            // [[s1]]
            LayoutFactory.SubstitutionTransition.setLayoutAndPosition(mts1, mspacingx, 0);

            // (inport)i -> [[s1]]
            larc = addArcPtoT(mpinport, mts1, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 2 * mspacingx, 0 * mspacingy);

            // [[s1]] -> (outport)o
            larc = addArcTtoP(mts1, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // f(awaken)f
            //mpawaken = addFusionAndFusionPlace( "awaken_" + pttype, "awaken_" + pttype, "CONDITION", "" );
            //LayoutFactory.FusionPlace.setLayoutAndPosition( mpawaken, 1*mspacingx, 1*mspacingx);
        }

        public Element insocketS1() {
            return mpinport;
        }

        public Element outsocketS1() {
            return mpoutport;
        }
    }

    /* Creates the page for thread declaration */
    // TODO - Add lock couting for reentrant lock case
    public class SyncBlock extends StmtS1 {

        protected Element mplock;
        protected Element mtacquire;
        protected Element mpentering;
        protected Element mpleaving;
        protected Element mtrelease;

        public SyncBlock(String pttype, String plock) {
            super("SyncBlock_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 0, 0);

            // [acquireLock]
            mtacquire = addTransition("acquireLock");
            LayoutFactory.Transition.setLayoutAndPosition(mtacquire, mspacingx, 0);

            // (inport) -> [acquireLock]
            larc = addArcPtoT(mpinport, mtacquire, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (entering)
            mpentering = addPlace("entering", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mpentering, 2 * mspacingx, 0);

            // [acquireLock] -> (entering)
            larc = addArcTtoP(mtacquire, mpentering, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [[s1]]
            // mts1 =  addSubstitutionTransition( "s1" );
            LayoutFactory.SubstitutionTransition.setLayoutAndPosition(mts1, 2 * mspacingx, mspacingy);

            // (entering) -> [[s1]]
            larc = addArcPtoT(mpentering, mts1, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (leaving)
            mpleaving = addPlace("leaving", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mpleaving, 2 * mspacingx, 2 * mspacingy);

            // [[s1]] -> (leaving)
            larc = addArcTtoP(mts1, mpleaving, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [releaseLock]
            mtrelease = addTransition("releaseLock");
            LayoutFactory.Transition.setLayoutAndPosition(mtrelease, mspacingx, 2 * mspacingy);

            // (leaving) -> [releaseLock]
            larc = addArcPtoT(mpleaving, mtrelease, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 0, 2 * mspacingy);

            // [releaseLock] -> (outport)
            larc = addArcTtoP(mtrelease, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // ((lock))
            Element mplock = addFusionPlace(plock, plock, "LOCK", "1`()");
            LayoutFactory.FusionPlace.setLayoutAndPosition(mplock, mspacingx, mspacingy);

            // ((lock)) -> [acquireLock]
            larc = addArcPtoT(mplock, mtacquire, "1`()");
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [releaseLock] -> ((lock))
            larc = addArcTtoP(mtrelease, mplock, "1`()");
            LayoutFactory.Arc.setLayoutAndPosition(larc);

        }

        public Element insocketS1() {
            return mpentering;
        }

        public Element outsocketS1() {
            return mpleaving;
        }
    }

    /* Creates the page for thread declaration */
    public class IfElse extends StmtS1S2 {

        protected Element mtif;
        protected Element mpenterings1;
        protected Element mtelse;
        protected Element mpenterings2;

        public IfElse(String pttype, Expr pcond) {

            super("IfElse_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 0 * mspacingx, 1 * mspacingy);

            // [If]
            mtif = addConditionTransition("If", pcond.text());
            LayoutFactory.ConditionTransition.setLayoutAndPosition(mtif, -1 * mspacingx, 1 * mspacingy);

            // (inport)i -> [If]
            larc = addArcPtoT(mpinport, mtif, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (enteringS1)
            mpenterings1 = addPlace("enteringS1", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mpenterings1, -1 * mspacingx, 2 * mspacingy);

            // [If] -> (enteringS1)
            larc = addArcTtoP(mtif, mpenterings1, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [[s1]]
            LayoutFactory.SubstitutionTransition.setLayoutAndPosition(mts1, -1 * mspacingx, 3 * mspacingy);

            // (entering) -> [[s1]]
            larc = addArcPtoT(mpenterings1, mts1, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 0 * mspacingx, 2 * mspacingy);

            // [[s1]] -> (outport)o
            larc = addArcTtoP(mts1, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [Else]
            mtelse = addConditionTransition("Else", "not (" + pcond.text() + ")");
            LayoutFactory.ConditionTransition.setLayoutAndPosition(mtelse, 1 * mspacingx, 1 * mspacingy);

            // (inport)i -> [Else]
            larc = addArcPtoT(mpinport, mtelse, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (enteringS2)
            mpenterings2 = addPlace("enteringS2", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mpenterings2, 1 * mspacingx, 2 * mspacingy);

            // [Else] -> (enteringS2)
            larc = addArcTtoP(mtelse, mpenterings2, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [[s2]]
            LayoutFactory.SubstitutionTransition.setLayoutAndPosition(mts2, 1 * mspacingx, 3 * mspacingy);

            // (enteringS2) -> [[s2]]
            larc = addArcPtoT(mpenterings2, mts2, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [[s2]] -> (outport)o
            larc = addArcTtoP(mts2, mpoutport, "2`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // Global vars referenced in the transition
            Collection<Element> lglobals = addAllFusionPlaces(pcond.getVars());
            LayoutFactory.FusionPlace.setDefaultLayout(lglobals);
            LayoutFactory.FusionPlace.distributeHorizontally(lglobals, 0 * mspacingy, mspacingx);

            // Link global vars to the transitions where they are referenced
            Collection<Element> larcs = addReflexiveArcs(mtif, lglobals);
            LayoutFactory.Arc.setLayoutAndPosition(larcs);

            // Link global vars to the transitions where they are referenced
            larcs = addReflexiveArcs(mtelse, lglobals);
            LayoutFactory.Arc.setLayoutAndPosition(larcs);
        }

        public Element insocketS1() {
            return mpenterings1;
        }

        public Element outsocketS1() {
            return mpoutport;
        }

        public Element insocketS2() {
            return mpenterings2;
        }

        public Element outsocketS2() {
            return mpoutport;
        }
    }

    /* Creates the page for thread declaration */
    public class Wait extends Stmt {

        protected Element mplock;
        protected Element mtwait;
        protected Element mpwaitset;
        protected Element mpawaken;
        protected Element mtreacquire;

        public Wait(String pttype, String pcond, String plock) {
            super("Wait_" + pttype);

            String lcpoint = addUniqueControlPoint();

            Element larc;

            // Part I - Put thread to sleep and release lock

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 0, 0);

            // [wait]
            mtwait = addTransition("wait_" + pcond);
            LayoutFactory.Transition.setLayoutAndPosition(mtwait, 1 * mspacingx, 0 * mspacingy);

            // (inport)i -> [wait]
            larc = addArcPtoT(mpinport, mtwait, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // ((waitset))
            mpwaitset = addFusionPlace(pcond, pcond, "CONDITION", "");
            LayoutFactory.FusionPlace.setLayoutAndPosition(mpwaitset, 2 * mspacingx, 0 * mspacingy);

            // [wait] -> ((waitset))
            larc = addArcTtoP(mtwait, mpwaitset, "(" + pttype + "," + lcpoint + ")");
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // ((lock))
            Element mplock = addFusionPlace(plock, plock, "LOCK", "1`()");
            LayoutFactory.FusionPlace.setLayoutAndPosition(mplock, 1 * mspacingx, 1 * mspacingy);

            // [wait] -> ((lock))
            larc = addArcTtoP(mtwait, mplock, "1`()");
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // Part II - Woken threads and lock reacquisition

            // (awaken)
            mpawaken = addFusionPlace("awaken_" + pttype, "awaken_" + pttype, "CONDITION", "");
            LayoutFactory.FusionPlace.setLayoutAndPosition(mpawaken, 2 * mspacingx, 2 * mspacingy);

            // [reacquireLock]
            mtreacquire = addTransition("reacquireLock");
            LayoutFactory.Transition.setLayoutAndPosition(mtreacquire, 1 * mspacingx, 2 * mspacingy);

            // (awaken) -> [reacquireLock]
            larc = addArcPtoT(mpawaken, mtreacquire, "(" + pttype + "," + lcpoint + ")");
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // ((lock)) -> [reacquireLock]
            larc = addArcPtoT(mplock, mtreacquire, "1`()");
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 0 * mspacingx, 2 * mspacingy);

            // [reacquireLock] -> (outport)
            larc = addArcTtoP(mtreacquire, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

        }
    }

    public class Notify extends Stmt {

        protected Element mtflagend;
        protected Element mpwaitset;

        public Notify(String pttype, String pcond) {

            super("Notify_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, -1 * mspacingx, 1 * mspacingy);

            // [flagEmpty]
            mtflagend = addTransition("flagEmpty_" + pcond);
            LayoutFactory.Transition.setLayoutAndPosition(mtflagend, -1 * mspacingx, 0 * mspacingy);

            // (inport)i -> [flagEmpty]
            larc = addArcPtoT(mpinport, mtflagend, "1`" + pttype);
            LayoutFactory.Arc.setDefaultLayout(larc);

            // ((waitset))
            mpwaitset = addFusionPlace(pcond, pcond, "CONDITION", "");
            LayoutFactory.FusionPlace.setLayoutAndPosition(mpwaitset, 1 * mspacingx, 1 * mspacingy);

            // ((waitset)) -o [flagEmpty]
            larc = addInhibitorArc(mpwaitset, mtflagend);
            LayoutFactory.InhibitorArc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 1 * mspacingx, 0 * mspacingy);

            // [flagEmpty] -> (outport)o
            larc = addArcTtoP(mtflagend, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setDefaultLayout(larc);

            // Add all [wakeT] for thread types
            // TODO - Currently a type T is being added even if some type never waits on the condition. Fix this.
            ArrayList<Element> ltwakes = new ArrayList<Element>();
            ArrayList<Element> lpawakens = new ArrayList<Element>();
            for (Enumeration<String> e = menumthread.keys(); e.hasMoreElements(); ) {
                String ltwaiting = e.nextElement();

                // [wake_T]
                Element ltwake = addTransition("wake_" + ltwaiting);
                LayoutFactory.Transition.setDefaultLayout(ltwake);
                ltwakes.add(ltwake);

                // (inport)i -> [wake_T]
                // NOTE: This unidirectional arc is different then the NotifyAll, which is bidirectional.
                LayoutFactory.Arc.setDefaultLayout(addArcPtoT(mpinport, ltwake, "1`" + pttype));
                //[wake_T] -> (outport)o
                LayoutFactory.Arc.setDefaultLayout(addArcTtoP(ltwake, mpoutport, "1`" + pttype));

                // ((waitset)) -> [wake_T]
                LayoutFactory.Arc.setDefaultLayout(addArcPtoT(mpwaitset, ltwake, "1`(" + ltwaiting + ",vcpoint)"));

                // (awaken_T)
                // Note: awaken are not "global" vars, therefore can't invoke addFusionPlace with single argument
                // TODO - Awake are not conditon, so it should have another type. Check best scheme.
                Element lpawaken = addFusionPlace("awaken_" + ltwaiting, "awaken_" + ltwaiting, "CONDITION", "");
                LayoutFactory.FusionPlace.setDefaultLayout(lpawaken);
                lpawakens.add(lpawaken);

                // [wake_T] -> (awaken_T)
                LayoutFactory.Arc.setDefaultLayout(addArcTtoP(ltwake, lpawaken, "1`(" + ltwaiting + ",vcpoint)"));
            }

            LayoutFactory.Transition.distributeHorizontally(ltwakes, 2 * mspacingy, mspacingx);
            LayoutFactory.FusionPlace.distributeHorizontally(lpawakens, 3 * mspacingy, mspacingx);
            LayoutFactory.Arc.positionAllArcs(mpage);
        }
    }


    public class NotifyAll extends Stmt {

        protected Element mtflagend;
        protected Element mpwaitset;

        public NotifyAll(String pttype, String pcond) {

            super("NotifyAll_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, -1 * mspacingx, 1 * mspacingy);

            // [flagEnd]
            mtflagend = addTransition("flagEnd_" + pcond);
            LayoutFactory.Transition.setLayoutAndPosition(mtflagend, -1 * mspacingx, 0 * mspacingy);

            // (inport)i -> [flagEnd]
            larc = addArcPtoT(mpinport, mtflagend, "1`" + pttype);
            LayoutFactory.Arc.setDefaultLayout(larc);

            // ((waitset))
            mpwaitset = addFusionPlace(pcond, pcond, "CONDITION", "");
            LayoutFactory.FusionPlace.setLayoutAndPosition(mpwaitset, 1 * mspacingx, 1 * mspacingy);

            // ((waitset)) -o [flagEnd]
            larc = addInhibitorArc(mpwaitset, mtflagend);
            LayoutFactory.InhibitorArc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 1 * mspacingx, 0 * mspacingy);

            // [flagEnd] -> (outport)o
            larc = addArcTtoP(mtflagend, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setDefaultLayout(larc);

            // Add all [wakeT] for thread types
            // TODO - Currently a type T is being added even if some type never waits on the condition. Fix this.
            ArrayList<Element> ltwakes = new ArrayList<Element>();
            ArrayList<Element> lpawakens = new ArrayList<Element>();
            for (Enumeration<String> e = menumthread.keys(); e.hasMoreElements(); ) {
                String ltwaiting = e.nextElement();

                // [wake_T]
                Element ltwake = addTransition("wake_" + ltwaiting);
                LayoutFactory.Transition.setDefaultLayout(ltwake);
                ltwakes.add(ltwake);

                // (inport) <-> [wake_T]
                LayoutFactory.Arc.setDefaultLayout(addArcTtoP(ltwake, mpinport, "1`" + pttype));
                LayoutFactory.Arc.setDefaultLayout(addArcPtoT(mpinport, ltwake, "1`" + pttype));

                // ((waitset)) -> [wake_T]
                LayoutFactory.Arc.setDefaultLayout(addArcPtoT(mpwaitset, ltwake, "1`(" + ltwaiting + ",vcpoint)"));

                // (awaken_T)
                // Note: awaken are not "global" vars, therefore can't invoke addFusionPlace with single argument
                // TODO - Awake are not conditon, so it should have another type. Check best scheme.
                Element lpawaken = addFusionPlace("awaken_" + ltwaiting, "awaken_" + ltwaiting, "CONDITION", "");
                LayoutFactory.FusionPlace.setDefaultLayout(lpawaken);
                lpawakens.add(lpawaken);

                // [wake_T] -> (awaken_T)
                LayoutFactory.Arc.setDefaultLayout(addArcTtoP(ltwake, lpawaken, "1`(" + ltwaiting + ",vcpoint)"));
            }

            LayoutFactory.Transition.distributeHorizontally(ltwakes, 2 * mspacingy, mspacingx);
            LayoutFactory.FusionPlace.distributeHorizontally(lpawakens, 3 * mspacingy, mspacingx);
            LayoutFactory.Arc.positionAllArcs(mpage);
        }
    }

    /* Creates the page for thread declaration */
    public class Skip extends Stmt {

        protected Element mtskip;

        public Skip(String pttype) {

            super("Skip_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 0, 0);

            // [Skip]
            mtskip = addTransition("Skip");
            LayoutFactory.Transition.setLayoutAndPosition(mtskip, 1 * mspacingx, 0 * mspacingy);

            // (inport)i -> [Skip]
            larc = addArcPtoT(mpinport, mtskip, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 2 * mspacingx, 0 * mspacingy);

            // [Skip] -> (outport)o
            larc = addArcTtoP(mtskip, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

        }
    }

    /* Creates the page for thread declaration */
    public class Assign extends Stmt {

        protected Element mtassign;
        protected Element mpvariable;

        public Assign(String pttype, String pvarname, Expr pexpr) {

            super("Assign_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 1 * mspacingx, 0 * mspacingy);

            // [Assign]
            mtassign = addTransition("Assign");
            LayoutFactory.Transition.setLayoutAndPosition(mtassign, 1 * mspacingx, 1 * mspacingy);

            // (inport)i -> [Assign]
            larc = addArcPtoT(mpinport, mtassign, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 1 * mspacingx, 2 * mspacingy);

            // [Assign] -> (outport)o
            larc = addArcTtoP(mtassign, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // ((variable))
            mpvariable = addFusionPlace(pvarname);
            LayoutFactory.FusionPlace.setLayoutAndPosition(mpvariable, 2 * mspacingx, 1 * mspacingy);

            // [Assign] -> ((variable))
            larc = addArcTtoP(mtassign, mpvariable, pexpr.text());
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // ((variable)) -> [Assign]
            larc = addArcPtoT(mpvariable, mtassign, pvarname);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // Global vars referenced in the transition - Must filter the target var first
            Hashtable<String, String> lfreevars = pexpr.getVars();
            lfreevars.remove(pvarname);

            Collection<Element> lglobals = addAllFusionPlaces(lfreevars);
            LayoutFactory.FusionPlace.setDefaultLayout(lglobals);
            LayoutFactory.FusionPlace.distributeHorizontally(lglobals, 3 * mspacingy, mspacingx);

            // Link global vars to the transitions where they are referenced
            Collection<Element> larcs = addReflexiveArcs(mtassign, lglobals);
            LayoutFactory.Arc.setLayoutAndPosition(larcs);
        }
    }

    /* Creates the page for thread declaration */
    public class While extends StmtS1 {

        protected Element mttrue;
        protected Element mpentering;
        protected Element mtfalse;

        public While(String pttype, Expr pcond) {

            super("While_" + pttype);

            Element larc;

            // (inport)i
            mpinport = addInPortPlace("inport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpinport, 0 * mspacingx, 1 * mspacingy);

            // [whileTrue]
            mttrue = addConditionTransition("whileTrue", pcond.text());
            LayoutFactory.ConditionTransition.setLayoutAndPosition(mttrue, -1 * mspacingx, 1 * mspacingy);

            // (inport)i -> [whileTrue]
            larc = addArcPtoT(mpinport, mttrue, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (entering)
            mpentering = addPlace("entering", "THREAD", "");
            LayoutFactory.Place.setLayoutAndPosition(mpentering, -1 * mspacingx, 2 * mspacingy);

            // [whileTrue] -> (entering)
            larc = addArcTtoP(mttrue, mpentering, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [[s1]]
            LayoutFactory.SubstitutionTransition.setLayoutAndPosition(mts1, 0 * mspacingx, 2 * mspacingy);

            // (entering) -> [[s1]]
            larc = addArcPtoT(mpentering, mts1, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [[s1]] -> (inport)i
            larc = addArcTtoP(mts1, mpinport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // [whileFalse]
            mtfalse = addConditionTransition("whileFalse", "not (" + pcond.text() + ")");
            LayoutFactory.ConditionTransition.setLayoutAndPosition(mtfalse, 1 * mspacingx, 1 * mspacingy);

            // (inport)i -> [whileTrue]
            larc = addArcPtoT(mpinport, mtfalse, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // (outport)o
            mpoutport = addOutPortPlace("outport", "THREAD", "");
            LayoutFactory.PortPlace.setLayoutAndPosition(mpoutport, 1 * mspacingx, 2 * mspacingy);

            // [whileFalse] -> (outport)o
            larc = addArcTtoP(mtfalse, mpoutport, "1`" + pttype);
            LayoutFactory.Arc.setLayoutAndPosition(larc);

            // Global vars referenced in the transition
            Collection<Element> lglobals = addAllFusionPlaces(pcond.getVars());
            LayoutFactory.FusionPlace.setDefaultLayout(lglobals);
            LayoutFactory.FusionPlace.distributeHorizontally(lglobals, 0 * mspacingy, mspacingx);

            // Link global vars to the transitions where they are referenced
            Collection<Element> larcs = addReflexiveArcs(mtfalse, lglobals);
            LayoutFactory.Arc.setLayoutAndPosition(larcs);

            // Link global vars to the transitions where they are referenced
            larcs = addReflexiveArcs(mttrue, lglobals);
            LayoutFactory.Arc.setLayoutAndPosition(larcs);
        }

        public Element insocketS1() {
            return mpentering;
        }

        public Element outsocketS1() {
            return mpinport;
        }
    }

    public abstract class Expr extends Node {

        protected Hashtable<String, String> mvars;
        protected String mmlexpr;

        public Expr(String pvalue) {
            mvars = new Hashtable<String, String>();
            mmlexpr = new String(pvalue);
        }

        public Hashtable<String, String> getVars() {
            return mvars;
        }

        protected void addVars(Expr pexpr) {
            mvars.putAll(pexpr.getVars());
        }

        public String text() {
            return mmlexpr;
        }
    }

    public class Bop extends Expr {
        public Bop(Expr pop1, Expr pop2, String poperator) {
            super("(" + pop1.text() + ")" + poperator + "(" + pop2.text() + ")");
            addVars(pop1);
            addVars(pop2);
        }
    }

    public class Uop extends Expr {
        public Uop(Expr pop1, String poperator) {
            super(poperator + " (" + pop1 + ")");
            addVars(pop1);
        }
    }

    public class VarName extends Expr {
        public VarName(String pvarname, String ptype) {
            super(pvarname);
            mvars.put(pvarname, ptype);
        }
    }

    public class ConstBool extends Expr {
        public ConstBool(boolean pconst) {
            super(Boolean.toString(pconst));
        }
    }

    public class ConstInt extends Expr {
        public ConstInt(int pconst) {
            super(Integer.toString(pconst));
        }
    }

    public class Max extends Expr {
        public Max(String pvarname) {
            super(pvarname);
            //mvars.add( pvarname , ptype );
        }
    }

    public class Min extends Expr {
        public Min(String pvarname) {
            super(pvarname);
            //mvars.add( pvarname );
        }
    }
}
