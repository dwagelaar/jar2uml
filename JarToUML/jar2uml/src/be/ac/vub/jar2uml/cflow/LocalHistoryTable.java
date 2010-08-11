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

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import junit.framework.Assert;

/**
 * Per-item history utility class for ordered items.
 * Uses a lookup table to implement the history.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class LocalHistoryTable implements Serializable, Cloneable {

	private static final long serialVersionUID = 7170069224023100850L;

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

	/**
	 * Utility class for accessing the per-item history.
	 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
	 */
	public class LocalHistorySet {
		
		private final int index;

		/**
		 * Private constructor - no instances should be created manually.
		 * @param index the index of the ordered item
		 */
		private LocalHistorySet(final int index) {
			super();
			this.index = index;
			if (table[index] == null) {
				table[index] = new BitSet(capacity);
			}
		}

		/**
		 * @param item
		 * @return <code>true</code> iff item is contained in this history set
		 */
		public boolean contains(final OrderedItem item) {
			return table[index].get(item.getIndex());
		}

		/**
		 * Adds item to this history set.
		 * @param item
		 * @return <code>true</code> iff this set changed as a result
		 */
		public boolean add(final OrderedItem item) {
			final boolean changed = !table[index].get(item.getIndex());
			table[index].set(item.getIndex());
			return changed;
		}

		/**
		 * Adds all contents of history to this history set.
		 * @param history
		 * @return <code>true</code> iff this set changed as a result
		 */
		public boolean addAll(final LocalHistorySet history) {
			final BitSet copy = (BitSet) table[index].clone();
			table[index].or(table[history.index]);
			return !copy.equals(table[index]);
		}

		/**
		 * @return the index
		 */
		public int getIndex() {
			return index;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getClass().getSimpleName() + table[index];
		}
	}

	private final int capacity;
	private final BitSet[] table;

	private boolean uncoveredCode;

	/**
	 * Creates a new {@link LocalHistoryTable}.
	 * @param capacity the maximum amount of items in the history
	 */
	public LocalHistoryTable(int capacity) {
		super();
		this.capacity = capacity;
		this.table = new BitSet[capacity];
	}

	/**
	 * @param item
	 * @return the local history of item
	 */
	public LocalHistorySet get(OrderedItem item) {
		return new LocalHistorySet(item.getIndex());
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		final LocalHistoryTable copy = new LocalHistoryTable(capacity);
		for (int i = 0; i < capacity; i++) {
			if (table[i] != null) {
				copy.table[i] = (BitSet) table[i].clone();
			}
		}
		if (hasUncoveredCode()) {
			copy.setUncoveredCode();
		}
		return copy;
	}

	/**
	 * Adds elements of history to this history table, resulting in a history union.
	 * @param history
	 */
	public void merge(LocalHistoryTable history) {
		if (this == history) {
			return;
		}
		Assert.assertEquals(capacity, history.capacity);
		Assert.assertFalse(history.hasUncoveredCode()); //history tables with this flag set should not be merged
		for (int i = 0; i < capacity; i++) {
			if (history.table[i] != null) {
				if (table[i] != null) {
					table[i].or(history.table[i]);
				} else {
					table[i] = (BitSet) history.table[i].clone();
				}
			}
		}
	}

	/**
	 * Sets the uncovered code flag
	 */
	public void setUncoveredCode() {
		this.uncoveredCode = true;
	}

	/**
	 * @return the uncovered code flag
	 */
	public boolean hasUncoveredCode() {
		return uncoveredCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + (hasUncoveredCode() ? "(uc)" : "") + Arrays.toString(table); //$NON-NLS-1$
	}
}
