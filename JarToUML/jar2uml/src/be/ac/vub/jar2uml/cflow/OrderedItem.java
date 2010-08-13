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


/**
 * Represents an object with an index. 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public interface OrderedItem {

	/**
	 * @return the (unique) index of the item between 0 and {@link LocalHistoryTable#getCapacity()}.
	 */
	public int getIndex();

}