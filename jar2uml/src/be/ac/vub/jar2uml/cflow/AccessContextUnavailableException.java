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
package be.ac.vub.jar2uml.cflow;

import org.apache.bcel.classfile.FieldOrMethod;

/**
 * Exception thrown when the access context for a {@link FieldOrMethod}
 * instruction is not available (e.g. it is <code>null</code>).
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AccessContextUnavailableException extends RuntimeException {

	private static final long serialVersionUID = -6442255567464649457L;

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 */
	public AccessContextUnavailableException() {
		super();
	}

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 * @param message
	 * @param cause
	 */
	public AccessContextUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 * @param message
	 */
	public AccessContextUnavailableException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 * @param cause
	 */
	public AccessContextUnavailableException(Throwable cause) {
		super(cause);
	}

}
