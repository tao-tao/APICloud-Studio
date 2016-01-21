/* The following code was generated by JFlex 1.4.3 on 11/1/12 2:07 PM */

// $codepro.audit.disable
/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.css.parsing;

import java.io.Reader;
import java.io.StringReader;

import beaver.Scanner;

import com.aptana.core.util.StringUtil;
import com.aptana.css.core.parsing.CSSTokenType;
import com.aptana.css.core.parsing.CSSTokenTypeSymbol;


@SuppressWarnings({"unused", "nls"})

/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 11/1/12 2:07 PM from the specification file
 * <tt>/Users/cwilliams/repos/studio3/plugins/com.aptana.editor.css/parsing/CSSColoring.flex</tt>
 */
public class CSSColoringFlexScanner extends Scanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\23\1\25\1\0\1\3\1\24\22\0\1\23\1\46\1\5"+
    "\1\42\1\53\1\41\1\0\1\6\1\57\1\60\1\10\1\22\1\63"+
    "\1\11\1\21\1\7\12\1\1\20\1\54\1\0\1\50\1\64\1\0"+
    "\1\43\1\34\1\4\1\33\1\17\1\30\1\44\1\15\1\36\1\16"+
    "\1\4\1\40\1\4\1\31\1\26\1\14\1\12\1\4\1\13\1\35"+
    "\1\27\1\45\2\4\1\32\1\4\1\37\1\61\1\2\1\62\1\52"+
    "\1\4\1\0\1\34\1\4\1\33\1\17\1\30\1\44\1\15\1\36"+
    "\1\16\1\4\1\40\1\4\1\31\1\26\1\14\1\12\1\4\1\13"+
    "\1\35\1\27\1\45\2\4\1\32\1\4\1\37\1\55\1\51\1\56"+
    "\1\47\uff81\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\1\2\1\1\1\3\1\4\1\5\1\6"+
    "\1\7\1\10\1\11\1\4\1\12\1\1\1\13\1\4"+
    "\1\14\4\1\1\15\3\1\1\16\1\17\1\20\1\21"+
    "\1\22\1\23\1\24\1\25\1\26\1\0\5\4\1\0"+
    "\3\4\1\27\2\4\1\30\2\5\2\6\1\0\1\4"+
    "\1\2\1\0\1\31\1\2\1\4\1\32\1\0\1\33"+
    "\1\0\7\33\3\0\1\34\1\35\1\36\1\37\1\40"+
    "\3\4\1\2\1\41\1\42\1\43\1\44\1\4\1\5"+
    "\1\6\1\0\1\4\10\31\1\27\2\31\10\0\1\27"+
    "\2\0\1\45\7\33\2\0\1\46\1\4\1\47\1\3"+
    "\1\4\1\40\3\31\1\41\1\42\1\43\1\44\1\31"+
    "\1\40\3\0\1\41\1\42\1\43\1\44\1\0\7\33"+
    "\2\0\1\50\1\4\1\46\1\31\1\47\1\46\1\0"+
    "\1\47\1\33\1\51\5\33\1\0\1\4\2\50\3\33"+
    "\1\52\2\33\2\0\1\33\1\53\3\33\1\0\1\4"+
    "\1\0\2\33\1\54\1\33\1\0\3\33\1\0\1\33"+
    "\1\55\1\56\1\57\3\33\1\60";

  private static int [] zzUnpackAction() {
    int [] result = new int[207];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\65\0\152\0\237\0\324\0\u0109\0\u013e\0\u0173"+
    "\0\u01a8\0\65\0\u01dd\0\u0212\0\65\0\u0247\0\u027c\0\u02b1"+
    "\0\65\0\u02e6\0\u031b\0\u0350\0\u0385\0\65\0\u03ba\0\u03ef"+
    "\0\u0424\0\65\0\65\0\65\0\65\0\65\0\65\0\65"+
    "\0\65\0\65\0\237\0\u0459\0\u048e\0\u04c3\0\u04f8\0\u052d"+
    "\0\u0562\0\u0597\0\u05cc\0\u0601\0\u0109\0\u0636\0\u066b\0\65"+
    "\0\u06a0\0\65\0\u06d5\0\65\0\u070a\0\u073f\0\u0774\0\u07a9"+
    "\0\u07de\0\u0813\0\u0848\0\u02e6\0\u087d\0\u08b2\0\u08e7\0\u091c"+
    "\0\u0951\0\u0986\0\u09bb\0\u09f0\0\u0a25\0\u0a5a\0\u0350\0\u0a8f"+
    "\0\u0ac4\0\65\0\65\0\65\0\65\0\u0109\0\u0af9\0\u0b2e"+
    "\0\u0b63\0\u0b98\0\u0109\0\u0109\0\u0109\0\u0109\0\u0bcd\0\u0c02"+
    "\0\u0c37\0\u0c6c\0\u0ca1\0\u0cd6\0\u0d0b\0\u0d40\0\u0d75\0\u0daa"+
    "\0\u0ddf\0\u0e14\0\u0e49\0\u07de\0\u0e7e\0\u0eb3\0\u0ee8\0\u0f1d"+
    "\0\u0f52\0\u0f87\0\u0fbc\0\u0ff1\0\u1026\0\u105b\0\65\0\u1090"+
    "\0\u10c5\0\u0109\0\u10fa\0\u112f\0\u1164\0\u1199\0\u11ce\0\u1203"+
    "\0\u1238\0\u126d\0\u12a2\0\u0109\0\u12d7\0\u0109\0\65\0\u130c"+
    "\0\u07de\0\u1341\0\u1376\0\u13ab\0\u07de\0\u07de\0\u07de\0\u07de"+
    "\0\u13e0\0\65\0\u1415\0\u144a\0\u147f\0\65\0\65\0\65"+
    "\0\65\0\u14b4\0\u14e9\0\u151e\0\u1553\0\u1588\0\u15bd\0\u15f2"+
    "\0\u1627\0\u165c\0\u1691\0\u0109\0\u16c6\0\u07de\0\u16fb\0\u07de"+
    "\0\65\0\u1730\0\65\0\u1765\0\u08b2\0\u179a\0\u17cf\0\u1804"+
    "\0\u1839\0\u186e\0\u18a3\0\u18d8\0\u07de\0\65\0\u190d\0\u1942"+
    "\0\u1977\0\u08b2\0\u19ac\0\u19e1\0\u1a16\0\u1a4b\0\u1a80\0\u08b2"+
    "\0\u1ab5\0\u1aea\0\u1b1f\0\u1b54\0\u1b89\0\u1bbe\0\u1bf3\0\u1c28"+
    "\0\u08b2\0\u1c5d\0\u1c92\0\u1cc7\0\u1cfc\0\u1d31\0\u1d66\0\u1d9b"+
    "\0\u08b2\0\u08b2\0\65\0\u1dd0\0\u1e05\0\u1e3a\0\u08b2";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[207];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11"+
    "\1\12\1\13\1\14\5\6\1\15\1\16\1\17\3\5"+
    "\1\20\12\6\1\21\1\22\1\23\2\6\1\24\1\25"+
    "\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35"+
    "\1\36\1\37\1\40\1\41\1\42\66\0\1\3\1\43"+
    "\1\0\1\6\4\0\1\6\1\44\1\45\1\6\1\46"+
    "\1\47\1\50\1\0\1\51\4\0\2\6\1\52\1\53"+
    "\1\6\1\54\1\6\1\55\1\56\1\6\1\57\1\60"+
    "\2\0\2\6\17\0\3\6\1\0\20\6\2\0\37\6"+
    "\3\0\1\5\17\0\3\5\40\0\1\6\1\43\1\0"+
    "\1\6\4\0\7\6\6\0\13\6\3\0\2\6\17\0"+
    "\2\7\1\61\1\0\1\7\1\62\16\7\2\0\37\7"+
    "\2\10\1\63\1\0\2\10\1\64\15\10\2\0\37\10"+
    "\10\0\1\65\55\0\1\3\1\43\1\0\1\6\4\0"+
    "\7\6\1\0\1\51\4\0\13\6\3\0\2\6\20\0"+
    "\1\6\1\43\1\0\1\6\4\0\2\6\1\66\4\6"+
    "\6\0\13\6\3\0\2\6\20\0\1\67\1\70\1\0"+
    "\1\71\4\0\7\71\6\0\13\71\3\0\2\71\20\0"+
    "\1\72\17\0\1\51\44\0\1\6\1\43\1\0\1\6"+
    "\4\0\3\6\1\73\3\6\6\0\13\6\3\0\2\6"+
    "\20\0\1\74\1\75\1\0\1\74\4\0\7\74\6\0"+
    "\13\74\3\0\2\74\20\0\1\76\1\77\1\0\1\76"+
    "\4\0\1\100\1\101\3\76\1\102\1\76\6\0\1\103"+
    "\2\76\1\104\1\76\1\105\5\76\3\0\1\106\1\76"+
    "\22\0\1\107\3\0\1\110\6\0\1\111\4\0\3\107"+
    "\107\0\1\112\64\0\1\113\64\0\1\114\64\0\1\115"+
    "\15\0\1\6\1\43\1\0\1\6\4\0\7\6\6\0"+
    "\1\6\1\116\2\6\2\116\5\6\3\0\2\6\20\0"+
    "\1\6\1\43\1\0\1\6\4\0\7\6\6\0\6\6"+
    "\1\117\4\6\3\0\2\6\20\0\1\6\1\43\1\0"+
    "\1\6\4\0\2\6\1\120\4\6\6\0\13\6\3\0"+
    "\2\6\20\0\1\6\1\43\1\0\1\6\4\0\7\6"+
    "\6\0\1\116\12\6\3\0\2\6\20\0\1\6\1\43"+
    "\1\0\1\6\4\0\7\6\6\0\2\6\1\121\10\6"+
    "\3\0\2\6\20\0\1\122\64\0\1\6\1\43\1\0"+
    "\1\6\4\0\7\6\6\0\3\6\1\123\1\124\6\6"+
    "\3\0\2\6\20\0\1\6\1\43\1\0\1\6\4\0"+
    "\7\6\6\0\3\6\1\116\3\6\1\125\3\6\3\0"+
    "\2\6\20\0\1\6\1\43\1\0\1\6\4\0\7\6"+
    "\6\0\3\6\1\116\7\6\3\0\2\6\20\0\1\6"+
    "\1\43\1\0\1\6\4\0\7\6\6\0\11\6\1\126"+
    "\1\6\3\0\2\6\20\0\1\6\1\43\1\0\1\6"+
    "\4\0\7\6\6\0\10\6\1\127\2\6\3\0\2\6"+
    "\17\0\2\7\1\61\21\7\1\130\40\7\2\10\1\63"+
    "\21\10\1\131\40\10\10\65\1\132\54\65\1\0\1\6"+
    "\1\43\1\0\1\6\4\0\3\6\1\133\3\6\6\0"+
    "\13\6\3\0\2\6\20\0\1\67\1\70\1\0\1\71"+
    "\4\0\1\71\1\134\1\135\1\71\1\136\1\137\1\140"+
    "\6\0\2\71\1\141\1\142\1\71\1\143\1\71\1\144"+
    "\1\145\1\71\1\146\1\60\2\0\2\71\17\0\3\71"+
    "\1\0\20\71\2\0\37\71\1\0\1\71\1\70\1\0"+
    "\1\71\4\0\7\71\6\0\13\71\3\0\2\71\20\0"+
    "\1\72\10\0\1\147\1\150\1\0\1\151\1\152\1\153"+
    "\1\0\1\51\6\0\1\154\1\155\1\0\1\156\1\0"+
    "\1\157\1\160\1\0\1\161\1\60\24\0\1\6\1\43"+
    "\1\0\1\6\4\0\7\6\6\0\1\6\1\162\11\6"+
    "\3\0\2\6\17\0\3\74\1\0\20\74\2\0\37\74"+
    "\1\0\1\76\1\77\1\0\1\76\4\0\7\76\6\0"+
    "\13\76\3\0\2\76\17\0\3\76\1\0\20\76\2\0"+
    "\37\76\1\0\1\76\1\77\1\0\1\76\4\0\7\76"+
    "\6\0\3\76\1\163\7\76\3\0\2\76\20\0\1\76"+
    "\1\77\1\0\1\76\4\0\7\76\6\0\6\76\1\164"+
    "\4\76\3\0\2\76\20\0\1\76\1\77\1\0\1\76"+
    "\4\0\7\76\6\0\3\76\1\165\7\76\3\0\2\76"+
    "\20\0\1\76\1\77\1\0\1\76\4\0\7\76\6\0"+
    "\6\76\1\166\4\76\3\0\2\76\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\7\76\6\0\2\76\1\167\10\76"+
    "\3\0\2\76\20\0\1\76\1\77\1\0\1\76\4\0"+
    "\7\76\6\0\10\76\1\170\2\76\3\0\2\76\20\0"+
    "\1\76\1\77\1\0\1\76\4\0\3\76\1\171\3\76"+
    "\6\0\13\76\3\0\2\76\27\0\1\172\105\0\1\173"+
    "\34\0\1\6\1\43\1\0\1\6\4\0\6\6\1\174"+
    "\6\0\13\6\3\0\2\6\20\0\1\6\1\43\1\0"+
    "\1\6\4\0\7\6\6\0\6\6\1\175\4\6\3\0"+
    "\2\6\20\0\1\6\1\43\1\0\1\6\4\0\4\6"+
    "\1\174\2\6\6\0\13\6\3\0\2\6\20\0\1\122"+
    "\10\0\1\147\1\150\1\0\1\151\1\152\1\153\10\0"+
    "\1\154\1\155\1\0\1\156\1\0\1\157\1\160\1\0"+
    "\1\161\1\60\24\0\1\6\1\43\1\0\1\6\4\0"+
    "\7\6\6\0\11\6\1\176\1\6\3\0\2\6\17\0"+
    "\2\7\1\61\1\0\1\7\1\62\16\7\1\0\40\7"+
    "\2\10\1\63\1\0\2\10\1\64\15\10\1\0\40\10"+
    "\7\65\1\177\1\132\54\65\1\0\1\6\1\43\1\0"+
    "\1\6\4\0\4\6\1\200\2\6\6\0\13\6\3\0"+
    "\2\6\20\0\1\71\1\70\1\0\1\71\4\0\7\71"+
    "\6\0\1\71\1\201\2\71\2\201\5\71\3\0\2\71"+
    "\20\0\1\71\1\70\1\0\1\71\4\0\7\71\6\0"+
    "\6\71\1\202\4\71\3\0\2\71\20\0\1\71\1\70"+
    "\1\0\1\71\4\0\2\71\1\203\4\71\6\0\13\71"+
    "\3\0\2\71\20\0\1\71\1\70\1\0\1\71\4\0"+
    "\7\71\6\0\1\201\12\71\3\0\2\71\20\0\1\71"+
    "\1\70\1\0\1\71\4\0\7\71\6\0\2\71\1\204"+
    "\10\71\3\0\2\71\20\0\1\71\1\70\1\0\1\71"+
    "\4\0\7\71\6\0\3\71\1\205\1\206\6\71\3\0"+
    "\2\71\20\0\1\71\1\70\1\0\1\71\4\0\7\71"+
    "\6\0\3\71\1\201\3\71\1\207\3\71\3\0\2\71"+
    "\20\0\1\71\1\70\1\0\1\71\4\0\7\71\6\0"+
    "\3\71\1\201\7\71\3\0\2\71\20\0\1\71\1\70"+
    "\1\0\1\71\4\0\7\71\6\0\11\71\1\210\1\71"+
    "\3\0\2\71\20\0\1\71\1\70\1\0\1\71\4\0"+
    "\7\71\6\0\10\71\1\211\2\71\3\0\2\71\46\0"+
    "\1\212\2\0\2\212\65\0\1\213\43\0\1\214\77\0"+
    "\1\212\66\0\1\215\65\0\1\216\1\217\63\0\1\212"+
    "\3\0\1\220\60\0\1\212\72\0\1\221\63\0\1\222"+
    "\27\0\1\76\1\77\1\0\1\76\4\0\3\76\1\223"+
    "\3\76\6\0\13\76\3\0\2\76\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\4\76\1\224\2\76\6\0\13\76"+
    "\3\0\2\76\20\0\1\76\1\77\1\0\1\76\4\0"+
    "\1\76\1\225\5\76\6\0\13\76\3\0\2\76\20\0"+
    "\1\76\1\77\1\0\1\76\4\0\7\76\6\0\3\76"+
    "\1\226\7\76\3\0\2\76\20\0\1\76\1\77\1\0"+
    "\1\76\4\0\6\76\1\227\6\0\13\76\3\0\2\76"+
    "\20\0\1\76\1\77\1\0\1\76\4\0\7\76\6\0"+
    "\6\76\1\230\4\76\3\0\2\76\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\7\76\6\0\1\231\12\76\3\0"+
    "\2\76\17\0\10\172\1\232\54\172\12\0\1\233\53\0"+
    "\1\6\1\43\1\0\1\6\4\0\6\6\1\234\6\0"+
    "\13\6\3\0\2\6\20\0\1\6\1\43\1\0\1\6"+
    "\4\0\5\6\1\235\1\6\6\0\13\6\3\0\2\6"+
    "\20\0\1\71\1\70\1\0\1\71\4\0\6\71\1\236"+
    "\6\0\13\71\3\0\2\71\20\0\1\71\1\70\1\0"+
    "\1\71\4\0\7\71\6\0\6\71\1\237\4\71\3\0"+
    "\2\71\20\0\1\71\1\70\1\0\1\71\4\0\4\71"+
    "\1\236\2\71\6\0\13\71\3\0\2\71\20\0\1\71"+
    "\1\70\1\0\1\71\4\0\7\71\6\0\11\71\1\240"+
    "\1\71\3\0\2\71\36\0\1\241\101\0\1\242\45\0"+
    "\1\241\106\0\1\243\26\0\1\76\1\77\1\0\1\76"+
    "\4\0\7\76\6\0\11\76\1\244\1\76\3\0\2\76"+
    "\20\0\1\76\1\77\1\0\1\76\4\0\7\76\6\0"+
    "\2\76\1\245\10\76\3\0\2\76\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\3\76\1\246\3\76\6\0\13\76"+
    "\3\0\2\76\20\0\1\76\1\77\1\0\1\76\4\0"+
    "\7\76\6\0\2\76\1\247\10\76\3\0\2\76\20\0"+
    "\1\76\1\77\1\0\1\76\4\0\5\76\1\250\1\76"+
    "\6\0\13\76\3\0\2\76\20\0\1\76\1\77\1\0"+
    "\1\76\4\0\2\76\1\251\4\76\6\0\13\76\3\0"+
    "\2\76\20\0\1\76\1\77\1\0\1\76\4\0\7\76"+
    "\6\0\1\76\1\252\11\76\3\0\2\76\17\0\7\172"+
    "\1\107\1\232\54\172\14\0\1\253\51\0\1\6\1\43"+
    "\1\0\1\6\4\0\6\6\1\254\6\0\13\6\3\0"+
    "\2\6\20\0\1\71\1\70\1\0\1\71\4\0\6\71"+
    "\1\255\6\0\13\71\3\0\2\71\36\0\1\256\46\0"+
    "\1\76\1\77\1\0\1\76\4\0\1\257\6\76\6\0"+
    "\13\76\3\0\2\76\20\0\1\76\1\77\1\0\1\76"+
    "\4\0\2\76\1\260\4\76\6\0\13\76\3\0\2\76"+
    "\20\0\1\76\1\77\1\0\1\76\4\0\7\76\6\0"+
    "\7\76\1\261\3\76\3\0\2\76\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\7\76\6\0\6\76\1\262\4\76"+
    "\3\0\2\76\20\0\1\76\1\77\1\0\1\76\4\0"+
    "\7\76\6\0\7\76\1\263\3\76\3\0\2\76\20\0"+
    "\1\76\1\77\1\0\1\76\4\0\1\264\6\76\6\0"+
    "\13\76\3\0\2\76\32\0\1\265\52\0\1\6\1\43"+
    "\1\0\1\6\4\0\7\6\1\266\5\0\13\6\3\0"+
    "\2\6\20\0\1\76\1\77\1\0\1\76\4\0\6\76"+
    "\1\267\6\0\13\76\3\0\2\76\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\7\76\6\0\1\76\1\270\11\76"+
    "\3\0\2\76\20\0\1\76\1\77\1\0\1\76\4\0"+
    "\1\76\1\271\5\76\6\0\13\76\3\0\2\76\20\0"+
    "\1\76\1\77\1\0\1\76\4\0\7\76\6\0\2\76"+
    "\1\272\10\76\3\0\2\76\20\0\1\76\1\77\1\0"+
    "\1\76\4\0\7\76\6\0\13\76\3\0\1\273\1\76"+
    "\46\0\1\274\36\0\1\275\1\276\1\0\1\275\4\0"+
    "\7\275\6\0\13\275\3\0\2\275\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\3\76\1\277\3\76\6\0\13\76"+
    "\3\0\2\76\20\0\1\76\1\77\1\0\1\76\4\0"+
    "\7\76\6\0\6\76\1\300\4\76\3\0\2\76\20\0"+
    "\1\76\1\77\1\0\1\76\4\0\7\76\6\0\1\76"+
    "\1\301\11\76\3\0\2\76\20\0\1\76\1\77\1\0"+
    "\1\76\4\0\7\76\6\0\6\76\1\302\4\76\3\0"+
    "\2\76\53\0\1\303\31\0\1\275\1\276\1\0\1\275"+
    "\4\0\7\275\1\0\1\266\4\0\13\275\3\0\2\275"+
    "\17\0\3\275\1\0\20\275\2\0\37\275\1\0\1\76"+
    "\1\77\1\0\1\76\4\0\7\76\6\0\5\76\1\304"+
    "\5\76\3\0\2\76\20\0\1\76\1\77\1\0\1\76"+
    "\4\0\7\76\6\0\5\76\1\305\5\76\3\0\2\76"+
    "\20\0\1\76\1\77\1\0\1\76\4\0\7\76\6\0"+
    "\5\76\1\306\5\76\3\0\2\76\45\0\1\307\37\0"+
    "\1\76\1\77\1\0\1\76\4\0\7\76\6\0\13\76"+
    "\3\0\1\76\1\310\20\0\1\76\1\77\1\0\1\76"+
    "\4\0\7\76\6\0\2\76\1\311\10\76\3\0\2\76"+
    "\20\0\1\76\1\77\1\0\1\76\4\0\7\76\6\0"+
    "\2\76\1\312\10\76\3\0\2\76\46\0\1\313\36\0"+
    "\1\76\1\77\1\0\1\76\4\0\7\76\6\0\3\76"+
    "\1\314\7\76\3\0\2\76\20\0\1\76\1\77\1\0"+
    "\1\76\4\0\7\76\6\0\2\76\1\315\10\76\3\0"+
    "\2\76\20\0\1\76\1\77\1\0\1\76\4\0\7\76"+
    "\6\0\1\316\12\76\3\0\2\76\20\0\1\76\1\77"+
    "\1\0\1\76\4\0\7\76\6\0\1\76\1\317\11\76"+
    "\3\0\2\76\17\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[7791];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\7\1\1\11\2\1\1\11\3\1\1\11"+
    "\4\1\1\11\3\1\11\11\1\0\5\1\1\0\6\1"+
    "\1\11\1\1\1\11\1\1\1\11\1\0\2\1\1\0"+
    "\4\1\1\0\1\1\1\0\7\1\3\0\4\11\14\1"+
    "\1\0\14\1\10\0\1\11\2\0\10\1\2\0\3\1"+
    "\1\11\12\1\1\11\3\0\4\11\1\0\7\1\2\0"+
    "\5\1\1\11\1\0\1\11\7\1\1\0\2\1\1\11"+
    "\6\1\2\0\5\1\1\0\1\1\1\0\4\1\1\0"+
    "\3\1\1\0\3\1\1\11\4\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[207];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
	// last token used for look behind. Also needed when implementing the ITokenScanner interface
	private CSSTokenTypeSymbol _lastToken;
	private CSSTokenTypeSymbol lookAhead;

	// curly brace nesting level
	private int _nestingLevel;

	// a flag indicating we're inside of a @media block
	private boolean _inMedia;

	public CSSColoringFlexScanner()
	{
		this((Reader) null);
	}

	public CSSTokenTypeSymbol getLastToken()
	{
		return _lastToken;
	}


    private CSSTokenTypeSymbol newTokenAndLookAhead(CSSTokenType current, CSSTokenType next, int lookAheadLen)
    {
        int right = yychar + yylength() - 1 - lookAheadLen;
        CSSTokenTypeSymbol currentSymbol = new CSSTokenTypeSymbol(
            current, yychar, right, "");
        
        CSSTokenTypeSymbol nextSymbol = new CSSTokenTypeSymbol(
            next, right+1, right + lookAheadLen, "");
            
        lookAhead = nextSymbol;
        return currentSymbol;
    }
    
	private CSSTokenTypeSymbol newToken(CSSTokenType id, Object value)
	{
		return new CSSTokenTypeSymbol(id, yychar, yychar + yylength() - 1, value);
	}

	public CSSTokenTypeSymbol nextToken() throws java.io.IOException, Scanner.Exception
	{
	    if(lookAhead != null){
	       _lastToken = lookAhead;
	       lookAhead = null;
        }
        else{
    		try
    		{
    			// get next token
    			_lastToken = yylex();
    		} 
    		catch (Scanner.Exception e)
    		{
    			// create default token type
    			int end = yychar + yylength() - 1;
    
    			_lastToken = new CSSTokenTypeSymbol(CSSTokenType.EOF, yychar, end, "");
    		}
        }
		return _lastToken;
	}


	public void setSource(String source)
	{
		yyreset(new StringReader(source));

		// clear last token
		_lastToken = null;

		// reset nesting level
		_nestingLevel = 0;
	}


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public CSSColoringFlexScanner(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public CSSColoringFlexScanner(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 184) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead > 0) {
      zzEndRead+= numRead;
      return false;
    }
    // unlikely but not impossible: read 0 characters, but not at end of stream    
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      } else {
        zzBuffer[zzEndRead++] = (char) c;
        return false;
      }     
    }

	// numRead < 0
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public CSSTokenTypeSymbol yylex() throws java.io.IOException, Scanner.Exception {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      yychar+= zzMarkedPosL-zzStartRead;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 3: 
          { /* ignore */
          }
        case 49: break;
        case 4: 
          { return newToken(CSSTokenType.IDENTIFIER, yytext());
          }
        case 50: break;
        case 32: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.LENGTH, 2);
          }
        case 51: break;
        case 43: 
          { return newToken(CSSTokenType.IMPORT, StringUtil.EMPTY);
          }
        case 52: break;
        case 28: 
          { return newToken(CSSTokenType.INCLUDES, StringUtil.EMPTY);
          }
        case 53: break;
        case 29: 
          { return newToken(CSSTokenType.DASHMATCH, StringUtil.EMPTY);
          }
        case 54: break;
        case 45: 
          { return newToken(CSSTokenType.NAMESPACE, StringUtil.EMPTY);
          }
        case 55: break;
        case 9: 
          { return newToken(CSSTokenType.MINUS, StringUtil.EMPTY);
          }
        case 56: break;
        case 40: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.ANGLE, 4);
          }
        case 57: break;
        case 5: 
          { return newToken(CSSTokenType.DOUBLE_QUOTED_STRING, StringUtil.EMPTY);
          }
        case 58: break;
        case 13: 
          { return newToken(CSSTokenType.EQUAL, StringUtil.EMPTY);
          }
        case 59: break;
        case 20: 
          { return newToken(CSSTokenType.RBRACKET, StringUtil.EMPTY);
          }
        case 60: break;
        case 41: 
          { return newToken(CSSTokenType.PAGE, StringUtil.EMPTY);
          }
        case 61: break;
        case 18: 
          { return newToken(CSSTokenType.RPAREN, StringUtil.EMPTY);
          }
        case 62: break;
        case 30: 
          { return newToken(CSSTokenType.BEGINS_WITH, StringUtil.EMPTY);
          }
        case 63: break;
        case 38: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.ANGLE, 3);
          }
        case 64: break;
        case 39: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.FREQUENCY, 3);
          }
        case 65: break;
        case 19: 
          { return newToken(CSSTokenType.LBRACKET, StringUtil.EMPTY);
          }
        case 66: break;
        case 15: 
          { _nestingLevel++;

									return newToken(CSSTokenType.LCURLY, StringUtil.EMPTY);
          }
        case 67: break;
        case 27: 
          { return newToken(CSSTokenType.AT_RULE, StringUtil.EMPTY);
          }
        case 68: break;
        case 42: 
          { _inMedia = true; return newToken(CSSTokenType.MEDIA_KEYWORD, StringUtil.EMPTY);
          }
        case 69: break;
        case 48: 
          { return newToken(CSSTokenType.MOZ_DOCUMENT, StringUtil.EMPTY);
          }
        case 70: break;
        case 22: 
          { return newToken(CSSTokenType.GREATER, StringUtil.EMPTY);
          }
        case 71: break;
        case 21: 
          { return newToken(CSSTokenType.COMMA, StringUtil.EMPTY);
          }
        case 72: break;
        case 36: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.FREQUENCY, 2);
          }
        case 73: break;
        case 25: 
          { CSSTokenType type;

									if ((_inMedia && _nestingLevel == 1) || _nestingLevel <= 0)
									{
										type = CSSTokenType.CLASS;
									}
									else
									{
										boolean numbers = true;
										String text = yytext();
										int len = text.length();

										for (int i = 1; i < len; i++)
										{
											char c = text.charAt(i);

											if (c < '0' || '9' < c)
											{
												numbers = false;
												break;
											}
										}

										type = (numbers) ? CSSTokenType.NUMBER : CSSTokenType.CLASS;
									}

									return newToken(type, StringUtil.EMPTY);
          }
        case 74: break;
        case 6: 
          { return newToken(CSSTokenType.SINGLE_QUOTED_STRING, StringUtil.EMPTY);
          }
        case 75: break;
        case 7: 
          { return newToken(CSSTokenType.SLASH, StringUtil.EMPTY);
          }
        case 76: break;
        case 33: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.EMS, 2);
          }
        case 77: break;
        case 46: 
          { return newToken(CSSTokenType.FONTFACE, StringUtil.EMPTY);
          }
        case 78: break;
        case 34: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.EXS, 2);
          }
        case 79: break;
        case 31: 
          { return newToken(CSSTokenType.ENDS_WITH, StringUtil.EMPTY);
          }
        case 80: break;
        case 24: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.PERCENTAGE, 1);
          }
        case 81: break;
        case 26: 
          { CSSTokenType type;

									if ((_inMedia && _nestingLevel == 1) || _nestingLevel <= 0)
									{
										type = CSSTokenType.ID;
									}
									else
									{
										boolean numbers = true;
										String text = yytext();
										int len = text.length();

										for (int i = 1; i < len; i++)
										{
											char c = text.charAt(i);

											if (!('0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F'))
											{
												numbers = false;
												break;
											}
										}

										type = (numbers) ? CSSTokenType.RGB : CSSTokenType.ID;
									}

									return newToken(type, StringUtil.EMPTY);
          }
        case 82: break;
        case 35: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.TIME, 2);
          }
        case 83: break;
        case 14: 
          { return newToken(CSSTokenType.SEMICOLON, StringUtil.EMPTY);
          }
        case 84: break;
        case 1: 
          { return newToken(CSSTokenType.ERROR, StringUtil.EMPTY);
          }
        case 85: break;
        case 23: 
          { return newTokenAndLookAhead(CSSTokenType.NUMBER, CSSTokenType.TIME, 1);
          }
        case 86: break;
        case 2: 
          { return newToken(CSSTokenType.NUMBER, StringUtil.EMPTY);
          }
        case 87: break;
        case 10: 
          { return newToken(CSSTokenType.COLON, StringUtil.EMPTY);
          }
        case 88: break;
        case 47: 
          { return newToken(CSSTokenType.IMPORTANT, StringUtil.EMPTY);
          }
        case 89: break;
        case 44: 
          { return newToken(CSSTokenType.CHARSET, StringUtil.EMPTY);
          }
        case 90: break;
        case 16: 
          { _nestingLevel--;

									if (_nestingLevel == 0)
									{
										// reset (possibly set) media flag
										_inMedia = false;
									}

									return newToken(CSSTokenType.RCURLY, StringUtil.EMPTY);
          }
        case 91: break;
        case 12: 
          { return newToken(CSSTokenType.PERCENTAGE, StringUtil.EMPTY);
          }
        case 92: break;
        case 8: 
          { return newToken(CSSTokenType.STAR, StringUtil.EMPTY);
          }
        case 93: break;
        case 11: 
          { return newToken(CSSTokenType.PLUS, StringUtil.EMPTY);
          }
        case 94: break;
        case 17: 
          { return newToken(CSSTokenType.LPAREN, StringUtil.EMPTY);
          }
        case 95: break;
        case 37: 
          { return newToken(CSSTokenType.NOT, StringUtil.EMPTY);
          }
        case 96: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
              { 	return newToken(CSSTokenType.EOF, "end-of-file");
 }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
