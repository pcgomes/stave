package stave.synctask.ast;

import java_cup.runtime.*;

/**
* SynckTask lexer
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
%class SyncTaskScanner 
%cup
%line
%column
	

// Helper definitions
END_OF_LINE = \n|\r\n|\f
//NOT_EOL = [^\n|\r\n]

// White space - separators
WHITE_SPACE = [ \t\b] | {END_OF_LINE}

// SyncTask Identifiers
ID = [a-zA-Z_][a-zA-Z0-9_]*

// Integers must be within Java limit of [-2147483648,2147483647]
// TODO: Being sloppy here with lower bound 
INTEGER = 0
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

// Overflow int
OFLOWINT = [1-9][0-9]*

// SyncJava Comments - Not supporting nested comments
COMMENT_SINGLE =  "//"~{END_OF_LINE} | "//"
COMMENT_MULTIPLE = "/*"~"*/"
SYNCTASK_COMMENT = {COMMENT_SINGLE} | {COMMENT_MULTIPLE}

%%
// Regular Expression matching

<YYINITIAL> {

// Reserved words

"Thread" { if (DebugLexer == true ) System.out.println("THREAD"); return symbol(sym.THREAD, yytext()); }

"synchronized" { if (DebugLexer == true ) System.out.println("SYNCHRONIZED"); return symbol(sym.SYNCHRONIZED, yytext()); }

"main" { if (DebugLexer == true ) System.out.println("MAIN"); return symbol(sym.MAIN, yytext()); }

"start" { if (DebugLexer == true ) System.out.println("SKIP"); return symbol(sym.START, yytext());  }

"skip" { if (DebugLexer == true ) System.out.println("SKIP");return symbol(sym.SKIP, yytext());  }

"notify" { if (DebugLexer == true ) System.out.println("NOTIFY"); return symbol(sym.NOTIFY, yytext()); } 

"notifyAll" { if (DebugLexer == true ) System.out.println("NOTIFYALL"); return symbol(sym.NOTIFYALL, yytext()); }

"wait" { if (DebugLexer == true ) System.out.println("WAIT");return symbol(sym.WAIT, yytext());  }

"if" { if (DebugLexer == true ) System.out.println("IF");return symbol(sym.IF, yytext());  }

"else" { if (DebugLexer == true ) System.out.println("ELSE"); return symbol(sym.ELSE, yytext()); }

"while" { if (DebugLexer == true ) System.out.println("WHILE");return symbol(sym.WHILE, yytext());  }

"Int" { if (DebugLexer == true ) System.out.println("INT");return symbol(sym.INT, yytext());  }

"Bool" { if (DebugLexer == true ) System.out.println("BOOL");return symbol(sym.BOOL, yytext());  }

"Lock" { if (DebugLexer == true ) System.out.println("LOCK");return symbol(sym.LOCK, yytext());  }

"Cond" { if (DebugLexer == true ) System.out.println("COND");return symbol(sym.COND, yytext());  }

"true" { if (DebugLexer == true ) System.out.println("TRUE"); return symbol(sym.TRUE, yytext()); }

"false" { if (DebugLexer == true ) System.out.println("FALSE"); return symbol(sym.FALSE, yytext()); }

"min" { if (DebugLexer == true ) System.out.println("MIN"); return symbol(sym.MIN, yytext()); }

"max" { if (DebugLexer == true ) System.out.println("MAX"); return symbol(sym.MAX, yytext()); }

// Delimiters

"(" { if (DebugLexer == true ) System.out.println("OPAR"); return symbol(sym.OPAR, yytext()); }
")" { if (DebugLexer == true ) System.out.println("CPAR"); return symbol(sym.CPAR, yytext()); }

"{" { if (DebugLexer == true ) System.out.println("OCBR"); return symbol(sym.OCBR, yytext()); }
"}" { if (DebugLexer == true ) System.out.println("CCBR"); return symbol(sym.CCBR, yytext()); }

//"[" { if (DebugLexer == true ) System.out.println("OSBR"); return symbol(sym.OSBR, yytext()); }
//"]" { if (DebugLexer == true ) System.out.println("CSBR"); return symbol(sym.CSBR, yytext()); }

"," { if (DebugLexer == true ) System.out.println("COMM"); return symbol(sym.COMM, yytext()); }
";" { if (DebugLexer == true ) System.out.println("SCOL"); return symbol(sym.SCOL, yytext()); }
//"." { if (DebugLexer == true ) System.out.println("FSTP"); return symbol(sym.FSTP, yytext()); }

// Operators
"!"  { if (DebugLexer == true ) System.out.println("NEGA"); return symbol(sym.NEGA, yytext()); }
"="  { if (DebugLexer == true ) System.out.println("ASSG"); return symbol(sym.ASSG, yytext()); }
"&&" { if (DebugLexer == true ) System.out.println("LAND"); return symbol(sym.LAND, yytext()); }
"<"  { if (DebugLexer == true ) System.out.println("LOWT"); return symbol(sym.LOWT, yytext()); }
">"  { if (DebugLexer == true ) System.out.println("BIGT"); return symbol(sym.BIGT, yytext()); }
"==" { if (DebugLexer == true ) System.out.println("EQUA"); return symbol(sym.EQUA, yytext()); }
"!=" { if (DebugLexer == true ) System.out.println("NTEQ"); return symbol(sym.NTEQ, yytext()); }
"||" { if (DebugLexer == true ) System.out.println("LOOR"); return symbol(sym.LOOR, yytext()); }
"+"  { if (DebugLexer == true ) System.out.println("ADDI"); return symbol(sym.ADDI, yytext()); }
"-"  { if (DebugLexer == true ) System.out.println("MINU"); return symbol(sym.MINU, yytext()); }
"++" { if (DebugLexer == true ) System.out.println("INCR"); return symbol(sym.INCR, yytext()); }
"--" { if (DebugLexer == true ) System.out.println("DECR"); return symbol(sym.DECR, yytext()); }
//"<=" { if (DebugLexer == true ) System.out.println("LTEQ"); return symbol(sym.LTEQ, yytext()); }
//">=" { if (DebugLexer == true ) System.out.println("BTEQ"); return symbol(sym.BTEQ, yytext()); }
//"*"  { if (DebugLexer == true ) System.out.println("MULT"); return symbol(sym.MULT, yytext()); }

{ID} { if (DebugLexer == true ) System.out.println("ID(" + yytext() + ")"); return symbol(sym.ID, yytext()); }

{INTEGER} { if (DebugLexer == true )  System.out.println("NUM(" + yytext() + ")"); return symbol(sym.INTEGER, yytext()); }

{SYNCTASK_COMMENT} { /* Comment - nothing to do */ }

{WHITE_SPACE} { /* WHITE_SPACE - nothing to do */ }

// Errror messages

{OFLOWINT}   {  System.err.println("Integer (" + yytext() + ") way too big to fit in 32 bits");
                   throw new ArithmeticException(); }

}
