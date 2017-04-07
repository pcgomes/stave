package stave.java.visitor;

import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import stave.java.annotation.MetaDefault;
import stave.java.annotation.MetaNode;
import stave.java.annotation.MetaResMethod;
import stave.java.annotation.MetaResource;
import stave.java.ast.AJCClassDecl;
import stave.java.ast.AJCMethodDecl;
import stave.java.ast.AJCVariableDecl;
import stave.synctask.ast.Expr;
import stave.synctask.ast.Node;
import stave.synctask.ast.Stmt;

/**
 * First pass in the Java program.
 * It and extracts information about SyncTask types and initialization.
 */
public class ResourceTypeVisitor extends AnnotationParsingVisitor {

    // Structure holding the (possibly many) resouce types found along the code
    protected java.util.Hashtable<String, MetaResource> mtypelist;

    // Member used to pass parameters
    protected Node mparam = null;

    // Member used to collect return values
    protected Node mresult = null;

    // Data structure representing the resource annotation being processed
    protected MetaResource mmeta = null;

    public ResourceTypeVisitor() {
        mtypelist = new java.util.Hashtable<String, MetaResource>();
    }

    protected void addNewType(String lname, MetaResource lmeta) {
        mtypelist.put(lname, lmeta);
    }

    public java.util.Hashtable<String, MetaResource> getTypeList() {
        return mtypelist;
    }

    public boolean isTypeSet(String ltype) {
        return mtypelist.containsKey(ltype);
    }

    public MetaResource getType(String lprog) {
        return mtypelist.get(lprog);
    }

    /**
     * All declarations of resource should be made on top of a class declaration.
     */
    public void visitClassDef(JCClassDecl tree) {

        // Search for annotations.
        if (((AJCClassDecl) tree).hasComment()) {

            String lcomment = ((AJCClassDecl) tree).getComment();
            String lcname = tree.sym.getQualifiedName().toString();

            debug("ResourceTypeVisitor.visitClassDef: Parsing class " + lcname);

            try {

                mmeta = (MetaResource) getMetaAnnotation(lcomment);

                // Store class symbol for later test of inheritance
                mmeta.setSymbol(tree.sym);

                // If the resource name wasn't set, get classe's flat name
                if (!mmeta.isIdSet()) {
                    // Default value must be extracted from the variable initialization.
                    mmeta.setId(tree.name.toString());
                }

                // Only annotations of default value should be allowed
                if (!(mmeta instanceof MetaResource)) {
                    throw new UnexpectedTypeException(lcomment);
                }

                // Check if redeclaring a resource with same name.
                if (isTypeSet(mmeta.getId())) {
                    throw new DuplicatedDeclarationException(mmeta.getId());
                }

                // Proceed with visiting
                super.visitClassDef(tree);

                if (onDebug()) {
                    mmeta.dumpMetaResource();
                }

                // Finished parsing the resource. Store it.
                addNewType(mmeta.getId(), mmeta);

            } catch (AnnotationParserException e) {
                warning(e);
            } catch (DuplicatedDeclarationException e) {
                warning(e);
            } catch (Exception | Error e) {
                warning("Class " + lcname + ": Could not parse the following comment: \"" + lcomment + "\". Message: " + e.getMessage());
            } finally {

                // Null marks that a Resource is no longer being processed.
                mmeta = null;
            }

        } else {
            // No annotation, move on.
            // Not descending tree because it should not contain declaration.
            //super.visitMethodDef(tree);
        }
    }

    /**
     * Analize methods that operate on the resource
     */
    public void visitMethodDef(JCMethodDecl tree) {

        // TODO - Currently method names are being identified only by its simple name.
        // Should change this to the mini-method signature (name + param types).
        // This should be integrated with symtab query.

        // Search for annotations.
        // First check is because JC adds default constructor, which is not AJCMethodDecl
        if ((tree instanceof AJCMethodDecl) && ((AJCMethodDecl) tree).hasComment() && mmeta != null) {

            String lcomment = ((AJCMethodDecl) tree).getComment();
            String lmname = tree.name.toString();

            debug("ResourceTypeVisitor.visitMethodDef: Parsing method " + lmname);

            try {

                MetaNode lresult = getMetaAnnotation(lcomment);

                // Only annotations of default value should be allowed
                //if (!(lresult instanceof MetaResMethod)) throw new UnexpectedTypeException( lcomment );
                if (!(lresult instanceof MetaResMethod)) {
                    return;
                }

                MetaResMethod lresmethod = (MetaResMethod) lresult;


                // Check if annotation was either
                // @code: meaning it should parse SyncTask code,
                // @inline: meaning that method body must be inlined.
                if (lresmethod.isCode()) {

                    Node lstcode = parseSyncTaskCode(lresmethod.getSTCode());

                    if (lstcode instanceof stave.synctask.ast.Expr) {
                        mmeta.setPredicate(lmname, (Expr) lstcode);
                    } else if (lstcode instanceof stave.synctask.ast.Stmt) {
                        mmeta.setOperation(lmname, (Stmt) lstcode);
                    } else {
                        throw new UnexpectedTypeException("Unknown SyncTask AST Type");
                    }

                } else if (lresmethod.isInline()) {

                    Java2SyncTaskVisitor lparser = new Java2SyncTaskVisitor(mmeta.getPredicates(), mmeta.getOperations());

                    // Check if some object has been set to be "monitored" as the one that abstracts to the resource.
                    if (!(mmeta.isGhostVar())) {
                        lparser.addMappedObject(mmeta.getJavaObject());
                    }

                    // Compiles the mapped strings into SyncTask code.
                    // I.e, converts the Hashmap String->String to String->Node.
                    // Then adds the mapped code to the context, which is specific to this method.
                    for (java.util.Enumeration<String> e = lresmethod.getMappedIds(); e.hasMoreElements(); ) {

                        String myid = e.nextElement();

                        //debug("ResourceTypeVisitor: Method " + lmname + ". Processing map " + myid +  "->" + lresmethod.getIdMap( myid) );

                        Node lstcode = parseSyncTaskCode(lresmethod.getIdMap(myid));

                        if (lstcode instanceof stave.synctask.ast.Expr) {
                            lparser.addPredToContext(myid, (Expr) lstcode);
                        } else if (lstcode instanceof stave.synctask.ast.Stmt) {
                            lparser.addOperToContext(myid, (Stmt) lstcode);
                        } else {
                            throw new UnexpectedTypeException("Unknown SyncTask AST Type");
                        }
                    }

                    // Parse the method, using the context constructed above.
                    lparser.visitMethodDef(tree);

                    // Add the a mapping between the method signature to its SyncTask code.
                    Node linlined = lparser.getResultNode();
                    if (linlined instanceof stave.synctask.ast.Expr) {
                        mmeta.setPredicate(lmname, (Expr) linlined);
                    } else if (linlined instanceof stave.synctask.ast.Stmt) {
                        mmeta.setOperation(lmname, (Stmt) linlined);
                    } else {
                        throw new UnexpectedTypeException("Could not inline method " + lmname + ". Unknown SyncTask AST Type");
                    }

                } else {
                    // Third option wasn't forseen. Report it.
                    throw new UnexpectedTypeException("Method is annotated, but it is neither @inline nor @code");
                }

            } catch (ParsingSyncTaskException e) {
                warning(e);
            } catch (AnnotationParserException e) {
                warning(e);
            } catch (DuplicatedDeclarationException e) {
                warning(e);
            } catch (UnexpectedTypeException e) {
                warning(e);
            } catch (Exception | Error e) {
                warning("Method " + lmname + ": Could not parse the following comment: \"" + lcomment + "\". Message: " + e.getMessage());
                if (onDebug()) {
                    e.printStackTrace();
                }
            }

        } else {
            // No annotation, move on.
            // Not descending tree because it should not contain declaration.
            //super.visitMethodDef(tree);
        }

    }

    public void visitVarDef(JCVariableDecl tree) {

        // Search for annotations.
        if (((AJCVariableDecl) tree).hasComment() && mmeta != null) {
            //System.out.println("Parsing var " + tree.sym.getQualifiedName().toString());

            String lcomment = ((AJCVariableDecl) tree).getComment();

            try {

                MetaDefault mdefault = (MetaDefault) getMetaAnnotation(lcomment);

                if (!(mdefault instanceof MetaDefault)) {
                    throw new UnexpectedTypeException(lcomment);
                }

                // If the value wasn't set in the annotation, fetch it from the variable initialization.

                if (!mdefault.isSet()) {

                    // Checking if value parsed is actually an integer
                    if (!(((tree.init) instanceof JCLiteral) && (((JCLiteral) tree.init).value instanceof java.lang.Number))) {
                        throw new AnnotationParserException(mdefault.getTag() + ": init value is not an integer.");
                    }

                    JCLiteral mynumber = (JCLiteral) tree.init;

                    // Default value must be extracted from the variable initialization.
                    mdefault.set(((java.lang.Number) mynumber.value).intValue());
                }

                // Check if annotation was @defaultvar or @defaultcap and store it.
                if (mdefault.isCapacity()) {
                    mmeta.setDefaultCap(mdefault.get());
                } else {
                    mmeta.setDefaultVal(mdefault.get());
                }

            } catch (AnnotationParserException e) {
                warning(e);
            } catch (DuplicatedDeclarationException e) {
                warning(e);
            } catch (UnexpectedTypeException e) {
                warning(e);
            } catch (Exception | Error e) {
                warning("Could not parse the following comment: \"" + lcomment + "\". Message: " + e.getMessage());
            }

        } else {
            // No annotation, move on.
            // Not descending tree because it should not contain declaration.
            //super.visitMethodDef(tree);
        }
    }
}
