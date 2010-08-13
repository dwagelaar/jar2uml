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
 * Call-back for cancelling a running execution.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public interface Cancellable {

	/**
	 * Checks if the running execution needs to be cancelled.
	 * @throws RuntimeException when the running execution needs to be cancelled.
	 */
	public void checkCancelled();

}