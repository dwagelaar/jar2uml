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
package be.ac.vub.jar2uml;


/**
 * {@link JarToUML} exception class.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLException extends RuntimeException {

	private static final long serialVersionUID = 2537156925879228701L;

	/**
	 * Creates a new {@link JarToUMLException}.
	 */
	public JarToUMLException() {
		super();
	}

	/**
	 * Creates a new {@link JarToUMLException}.
	 * @param message
	 * @param cause
	 */
	public JarToUMLException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link JarToUMLException}.
	 * @param message
	 */
	public JarToUMLException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link JarToUMLException}.
	 * @param cause
	 */
	public JarToUMLException(Throwable cause) {
		super(cause);
	}

}
