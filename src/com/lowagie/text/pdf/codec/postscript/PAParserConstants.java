/* Generated By:JavaCC: Do not edit this line. PAParserConstants.java */
package com.lowagie.text.pdf.codec.postscript;

public interface PAParserConstants {

  int EOF = 0;
  int WHITESPACE = 1;
  int INTEGER_LITERAL = 4;
  int DECIMAL_LITERAL = 5;
  int HEX_LITERAL = 6;
  int FLOATING_POINT_LITERAL = 7;
  int EXPONENT = 8;
  int STRING_LITERAL = 9;
  int IDENTIFIER = 10;
  int KEY_IDENTIFIER = 11;
  int IMMEDIATE_IDENTIFIER = 12;
  int LETTER = 13;
  int DIGIT = 14;
  int LBRACE = 15;
  int RBRACE = 16;
  int LBRACKET = 17;
  int RBRACKET = 18;
  int LDICT = 19;
  int RDICT = 20;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "<WHITESPACE>",
    "<token of kind 2>",
    "<token of kind 3>",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<STRING_LITERAL>",
    "<IDENTIFIER>",
    "<KEY_IDENTIFIER>",
    "<IMMEDIATE_IDENTIFIER>",
    "<LETTER>",
    "<DIGIT>",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\"<<\"",
    "\">>\"",
  };

}
