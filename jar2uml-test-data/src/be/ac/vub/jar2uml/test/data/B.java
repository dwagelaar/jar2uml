/*******************************************************************************
 * Copyright (c) 2007-2010 Dennis Wagelaar, Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package be.ac.vub.jar2uml.test.data;

import java.util.Comparator;


/**
 * Jar2UML test class. This class is imported,
 * and its references are to be inferred by Jar2UML.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class B {

	/**
	 * Test handling of code without local variable table
	 */
	static {
		new B();
	}

	public interface IPublic {
		
		/**
		 * Yeah, nesting classes inside interfaces(!)
		 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
		 *
		 */
		public class IPublicNested {
			
		}
		
	}

	public abstract interface IPublicAbstract {
		
	}

	public class BB {
		
	}
	
	private A.AA aaField;
	private String[] arrayField = new String[1];

	/**
	 * @return the aaField
	 */
	public A.AA getAaField() {
		return aaField;
	}

	/**
	 * @param aaField the aaField to set
	 */
	public void setAaField(A.AA aaField) {
		this.aaField = aaField;
	}
	
	public B() {
		super();
		A a = new A();
		a.setBbField(new BB());
		System.out.println(arrayField.length);
	}

	/**
	 * Tests switch within double conditional loop (scale test for simple algorithm)
	 * @param bogus
	 * @param outside unknown outside object
	 */
	public static void testStackSimulationTorture(char bogus, Object outside) {
		try {
			Object target = null;
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			if (target != null) {
				target.toString();
			}
		} catch (Exception e) {
			Object target = null;
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						target = new Object();
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					switch (bogus) {
					case '0':
						bogus = '!';
						break;
					case '1':
						bogus = '!';
						break;
					case '2':
						bogus = '!';
						break;
					case '3':
						bogus = '!';
						break;
					case '4':
						bogus = '!';
						break;
					case '5':
						bogus = '!';
						break;
					case '6':
						bogus = '!';
						break;
					case '7':
						bogus = '!';
						break;
					case '8':
						bogus = '!';
						break;
					case '9':
						bogus = '!';
						break;
					case 'a':
						bogus = '!';
						break;
					case 'b':
						bogus = '!';
						break;
					case 'c':
						bogus = '!';
						break;
					case 'd':
						bogus = '!';
						break;
					case 'e':
						bogus = '!';
						break;
					case 'f':
						bogus = '!';
						break;
					case 'g':
						bogus = '!';
						break;
					case 'h':
						bogus = '!';
						break;
					case 'i':
						bogus = '!';
						break;
					case 'j':
						bogus = '!';
						break;
					case 'k':
						bogus = '!';
						break;
					case 'l':
						bogus = '!';
						break;
					case 'm':
						bogus = '!';
						break;
					case 'n':
						bogus = '!';
						break;
					case 'o':
						bogus = '!';
						break;
					case 'p':
						bogus = '!';
						break;
					case 'q':
						bogus = '!';
						break;
					case 'r':
						bogus = '!';
						break;
					case 's':
						bogus = '!';
						break;
					case 't':
						bogus = '!';
						break;
					case 'u':
						bogus = '!';
						break;
					case 'v':
						bogus = '!';
						break;
					case 'w':
						bogus = '!';
						break;
					case 'x':
						bogus = '!';
						break;
					case 'y':
						bogus = '!';
						break;
					case 'z':
						bogus = '!';
						break;
					default:
						bogus = '?';
						break;
					}
				}
			}
			if (target != null) {
				target.toString();
			}
		}
	}

	protected static void testStaticProtected() {
		
	}

	protected void testProtected() {
		
	}

	/**
	 * Tests visibility rules
	 * @see http://download-llnw.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html
	 * @throws CloneNotSupportedException
	 */
	public void testLocalVariableType() throws CloneNotSupportedException {
		Object test = new String();
//		test.clone(); // this is illegal
		test = this;
//		test.clone(); // this is illegal at compile time
		this.clone();
		test.notify(); // this is a public access at compile time, but protected at runtime

		B b = new B();
		b.clone();
	}

	public boolean testLocalVariableTable(long arg0, Object arg1, Comparator<Object> arg2) {
		return arg2.compare(arg1, arg2) < 0 ? true : false;
	}

	/**
	 * Tests unreachable code detection
	 * @param bogus
	 */
	public void testUnreachableCode(String bogus) {
		StringBuffer localBuffer = null;
		String moreBogus = null;
		for (int i = 0; i < 2; i++) {
			if (moreBogus != null) {
				localBuffer = new StringBuffer(); //dead code
			}
		}
		//localBuffer can only be null!!
		localBuffer.append("test");
	}

	public void testExceptionHandler() {
		try {
			int divByZero = 34 / 0;
			System.out.println(divByZero);
			throw new RuntimeException();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//http://java.sun.com/docs/books/jvms/second_edition/html/Compiling.doc.html#13789
			//says we should have a JSR, but the modern java compiler always inlines finally blocks!
			System.out.println("now we have a JSR in our code?");
		}
	}

	/**
	 * Tests a possible null pointer access that is masked by an IFNULL check.
	 */
	public void testMaskedNullAccess(Object outside) {
		StringBuffer localBuffer = null;
		StringBuffer localBuffer2 = null;
		for (int i = 0; i < 10; i++) {
			if (outside != null) {
				//create extra long path to cover for "lucky" optimization
				localBuffer2 = localBuffer; //force at least two iterations
			}
			localBuffer = new StringBuffer();
			if (outside != null) {
				localBuffer = null; //screw up in long path - defeat simple algorithm
			}
		}
		if (localBuffer2 != null) {
			localBuffer2.append("test");
		}
	}

	/**
	 * Tests finally blocks that skip on normal execution
	 * @param outside
	 */
	public void testSemiDeadCode(Object outside) {
		Object local = new Object();
		try {
			outside.notify(); //possible NPE?
			local = null;
		} finally {
			if (local != null) {
				//this code is dead on normal execution, just not dead on exception
				//compiler inlining of this block causes search algorithm to find dead code
				local.notify();
			}
		}
	}
	
	public String normalize(String text) {
		return text;
	}
	
	public static String decode(String text) {
		return text;
	}

	/**
	 * Tests passing around local variable values and does null checks.
	 * This code triggers bugs with bad copies of the local history table,
	 * resulting in instructions that are not covered by the search algorithm.
	 * @param first
	 * @param second
	 * @return
	 */
	public String testNullObjects(String first, String second) {
		second = normalize(second);

		if (first != null && first.startsWith("bogus")) {
			first = decode(first);
		}

		if (second != null && second.startsWith("bogus")) {
			second = decode(second);
			if (first != null && !first.equals(second)) {
				second = null;
			} else {
				first = second;
				second = null;
			}
		}

		if (second != null) {
			String result = normalize(second);
			if (result != null) {
				return result;
			}
		}
		
		String result = testNullObjects(first, second); //just don't try to run this...
		if (result != null) {
			return result;
		}
		
		return testNullObjects(first, second); //just don't try to run this...
	}
}
