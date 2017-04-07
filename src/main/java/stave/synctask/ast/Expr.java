package stave.synctask.ast;

/**
 * AST Node for a SyncTask program
 *
 * This class represents a general code block.
 */

public abstract class Expr extends Node {

    public enum Op {
        NEGA, // !
        INCR,  // ++
        DECR,  // --
        LOOR, // ||
        LAND, // &&
        EQUA, // ==
        NTEQ, // !=
        LOWT, // <
        BIGT,// >
        ADDI, // +
        MINU, // -
        //UPOS, // +
        //UNEG, // -
        //PINC,  // ++
        //PDEC,  // --
        //LTEQ, // <=
        //BTQE, // >=
        //MULT, // *
        //DIV, // /
        //MOD, // %
        ERRO,  // Bad opcode
    }

}

