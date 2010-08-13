package be.ac.vub.jar2uml.test.data.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 *
 * $Id: //depot/code/org.antlr/release/antlr-2.7.6/antlr/BaseAST.java#1 $
 */

/**
 * A Child-Sibling Tree.
 *
 * A tree with PLUS at the root and with two children 3 and 4 is
 * structured as:
 *
 *		PLUS
 *		  |
 *		  3 -- 4
 *
 * and can be specified easily in LISP notation as
 *
 * (PLUS 3 4)
 *
 * where every '(' starts a new subtree.
 *
 * These trees are particular useful for translators because of
 * the flexibility of the children lists.  They are also very easy
 * to walk automatically, whereas trees with specific children
 * reference fields can't easily be walked automatically.
 *
 * This class contains the basic support for an AST.
 * Most people will create ASTs that are subclasses of
 * BaseAST or of CommonAST.
 */
public abstract class BaseAST {

    public static String decode(String text) {
        char c, c1, c2, c3, c4, c5;
        StringBuffer n = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            if (c == '&') {
                c1 = text.charAt(i + 1);
                c2 = text.charAt(i + 2);
                c3 = text.charAt(i + 3);
                c4 = text.charAt(i + 4);
                c5 = text.charAt(i + 5);

                if (c1 == 'a' && c2 == 'm' && c3 == 'p' && c4 == ';') {
                    n.append("&");
                    i += 5;
                }
                else if (c1 == 'l' && c2 == 't' && c3 == ';') {
                    n.append("<");
                    i += 4;
                }
                else if (c1 == 'g' && c2 == 't' && c3 == ';') {
                    n.append(">");
                    i += 4;
                }
                else if (c1 == 'q' && c2 == 'u' && c3 == 'o' &&
                    c4 == 't' && c5 == ';') {
                    n.append("\"");
                    i += 6;
                }
                else if (c1 == 'a' && c2 == 'p' && c3 == 'o' &&
                    c4 == 's' && c5 == ';') {
                    n.append("'");
                    i += 6;
                }
                else
                    n.append("&");
            }
            else
                n.append(c);
        }
        return new String(n);
    }

}
