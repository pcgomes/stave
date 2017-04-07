package stave.java.annotation;

import java_cup.runtime.*;

/**
* Java Annotation lexer
* Author: Pedro de Carvalho Gomes <pedrodcg@csc.kth.se>
* Date: 2015-04-21 
* Last change: 2015-04-21 
*/


%%

%{

   protected static boolean DebugLexer = false;

   //public static void init() {};

   private Symbol symbol(int type) {
      //System.out.println("[" + (yyline+1) + "," + (yycolumn+1) + "]");
      return new Symbol(type, yyline, yycolumn);
   }

   private Symbol symbol(int type, Object value) {
      //System.out.println("Symbol(" + (yyline + 1) + "," + (yycolumn + 1) + "): " + value.toString());
      return new Symbol(type, yyline, yycolumn, value);
   }

%}

// JFlex Directives
//%standalone
%public
%class JavaAnnotationScanner 
%cup
%line
%column
	

// Helper definitions
END_OF_LINE = \n|\r\n|\f
//NOT_EOL = [^\n|\r\n]

// White space - separators
WHITE_SPACE = [ \t\b] | {END_OF_LINE} | \*

// SyncTask Identifiers
ID = [a-zA-Z_][a-zA-Z0-9_.()]*
//ID = [a-zA-Z_][a-zA-Z0-9_]*

// Integers must be within Java limit of [-2147483648,2147483647]
// TODO: Being sloppy here with lower bound 
INT = 0
    // Up to nine, anything is accepted
    |[1-9][0-9]{0,8}
    |1[0-9]{9}
    |20[0-9]{8}
    |21[0-3][0-9]{7}
    |214[0-6][0-9]{6}
    |2147[0-3][0-9]{5}
    |21474[0-7][0-9]{4}
    |214748[0-2][0-9]{3}
    |2147483[0-5][0-9]{2}
    |21474836[0-4][0-7]

// Code snippet is marked between @ + brackets because pure curly brackets are already used in SyncTask.
CODE = "@{"~"}@"

// Overflow int
//OFLOWINT = [1-9][0-9]*

%%
// Regular Expression matching

<YYINITIAL> {

// Reserved words
"@capacity"   { if (DebugLexer == true ) System.out.println("@CAPACITY");  return symbol(sym.ATCAPACITY,   yytext()); }
"@code"       { if (DebugLexer == true ) System.out.println("@CODE");      return symbol(sym.ATCODE,       yytext()); }
"@condvar"    { if (DebugLexer == true ) System.out.println("@CONDVAR");   return symbol(sym.ATCONDVAR,    yytext()); }
"@defaultcap" { if (DebugLexer == true ) System.out.println("@DEFAULTCAP");return symbol(sym.ATDEFAULTCAP, yytext()); }
"@defaultval" { if (DebugLexer == true ) System.out.println("@DEFAULTVAL");return symbol(sym.ATDEFAULTVAL, yytext()); }
"@inline"     { if (DebugLexer == true ) System.out.println("@INLINE");    return symbol(sym.ATINLINE,     yytext()); }
"@lock"       { if (DebugLexer == true ) System.out.println("@LOCK");      return symbol(sym.ATLOCK,       yytext()); }
"@map"        { if (DebugLexer == true ) System.out.println("@MAP");       return symbol(sym.ATMAP,        yytext()); }
"@monitor"    { if (DebugLexer == true ) System.out.println("@MONINOR");   return symbol(sym.ATMONITOR,    yytext()); }
"@object"     { if (DebugLexer == true ) System.out.println("@OBJECT");    return symbol(sym.ATOBJECT,     yytext()); }
"@operation"  { if (DebugLexer == true ) System.out.println("@OPERATION"); return symbol(sym.ATOPERATION,  yytext()); }
"@predicate"  { if (DebugLexer == true ) System.out.println("@PREDICATE"); return symbol(sym.ATPREDICATE,  yytext()); }
"@resource"   { if (DebugLexer == true ) System.out.println("@RESOURCE");  return symbol(sym.ATRESOURCE,   yytext()); }
"@synctask"   { if (DebugLexer == true ) System.out.println("@SYNCTASK");  return symbol(sym.ATSYNCTASK,   yytext()); }
"@syncblock"  { if (DebugLexer == true ) System.out.println("@SYNCBLOCK"); return symbol(sym.ATSYNCBLOCK,  yytext()); }
"@threadtype" { if (DebugLexer == true ) System.out.println("@THREADTYPE");return symbol(sym.ATTHREADTYPE, yytext()); }
"@thread"     { if (DebugLexer == true ) System.out.println("@THREAD");    return symbol(sym.ATTHREAD,     yytext()); }
"@value"      { if (DebugLexer == true ) System.out.println("@VALUE");     return symbol(sym.ATVALUE,      yytext()); }

// Symbols
"->"          { if (DebugLexer == true ) System.out.println("MAPS");       return symbol(sym.MAPS, yytext()); }
":"           { if (DebugLexer == true ) System.out.println("TYPEOF");     return symbol(sym.TYPEOF, yytext()); }

{ID}          { if (DebugLexer == true ) System.out.println("ID(" + yytext() + ")");  return symbol(sym.ID, yytext()); }
{INT}         { if (DebugLexer == true ) System.out.println("INT(" + yytext() + ")"); return symbol(sym.INT, yytext()); }
{CODE}        { String stcode = yytext().substring(2,yytext().length()-2);
                if (DebugLexer == true ) System.out.println("CODE(" + stcode + ")");  return symbol(sym.CODE, stcode); }

{WHITE_SPACE} { /* WHITE_SPACE - nothing to do */ }

}
