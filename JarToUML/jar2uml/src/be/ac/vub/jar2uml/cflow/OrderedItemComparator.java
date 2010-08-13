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

import java.util.Comparator;

/**
 * {@link Comparator} for {@link OrderedItem}s.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public final class OrderedItemComparator implements Comparator<OrderedItem> {

	public static final OrderedItemComparator INSTANCE = new OrderedItemComparator();

	/**
	 * @see OrderedItemComparator#INSTANCE
	 */
	private OrderedItemComparator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(OrderedItem o1, OrderedItem o2) {
		return Integer.valueOf(o1.getIndex()).compareTo(Integer.valueOf(o2.getIndex()));
	}

}