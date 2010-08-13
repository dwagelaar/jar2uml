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

/**
 * Per-item history utility class for ordered items.
 * Uses a lookup table to implement the history.
 * Meant to be fast, not to be sub-classed.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public final class LocalHistoryTable implements Serializable {

	private static final long serialVersionUID = 7170069224023100850L;

	/**
	 * Utility class for accessing the per-item history.
	 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
	 */
	public final class LocalHistorySet {
		
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
			final int i = item.getIndex();
			if (table[index].get(i)) {
				return false;
			}
			table[index].set(i);
			return true;
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
	private final transient LocalHistorySet[] localHistorySetCache;

	private boolean unmergeable;

	/**
	 * Creates a new {@link LocalHistoryTable}.
	 * @param capacity the maximum amount of items in the history
	 */
	public LocalHistoryTable(int capacity) {
		super();
		this.capacity = capacity;
		this.table = new BitSet[capacity];
		this.localHistorySetCache = new LocalHistorySet[capacity];
	}

	/**
	 * @param item
	 * @return the local history of item
	 */
	public LocalHistorySet get(final OrderedItem item) {
		final int i = item.getIndex();
		if (localHistorySetCache[i] == null) {
			localHistorySetCache[i] = new LocalHistorySet(i);
		}
		return localHistorySetCache[i];
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @return a deep copy of this
	 */
	public LocalHistoryTable getCopy() {
		LocalHistoryTable copy = new LocalHistoryTable(capacity);
		for (int i = 0; i < capacity; i++) {
			if (table[i] != null) {
				copy.table[i] = (BitSet) table[i].clone();
			}
		}
		return copy;
	}

	/**
	 * Adds elements of history to this history table, resulting in a history union.
	 * @param history history table to merge
	 * @throws IllegalArgumentException if history does not have the same capacity, or history is unmergeable
	 */
	public void merge(LocalHistoryTable history) {
		if (this == history) {
			return;
		}
		if (capacity != history.capacity) {
			throw new IllegalArgumentException();
		}
		if (history.isUnmergeable()) {
			throw new IllegalArgumentException();
		}
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
	 * Sets the unmergeable flag
	 * @see #merge(LocalHistoryTable)
	 */
	public void setUnmergeable() {
		this.unmergeable = true;
	}

	/**
	 * @return the unmergeable flag
	 * @see #merge(LocalHistoryTable)
	 */
	public boolean isUnmergeable() {
		return unmergeable;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + (isUnmergeable() ? "(u)" : "") + Arrays.toString(table); //$NON-NLS-1$
	}
}
