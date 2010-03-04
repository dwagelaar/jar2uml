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
package be.ac.vub.jar2uml.test;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.ecore.resource.Resource;

import junit.framework.TestCase;

/**
 * Test case functionality for EMF-based code.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class EMFTestCase extends TestCase {

	/**
	 * Creates a new {@link EMFTestCase}.
	 */
	public EMFTestCase() {
		super();
	}

	/**
	 * Creates a new {@link EMFTestCase}.
	 * @param name
	 */
	public EMFTestCase(String name) {
		super(name);
	}

	/**
	 * Asserts that leftResource and rightResource are equal. Uses EMF Compare.
	 * @param leftObject
	 * @param rightObject
	 * @throws InterruptedException 
	 */
	public static void assertEquals(Resource leftResource, Resource rightResource)
	throws InterruptedException {
		final Map<String, Object> options = new HashMap<String, Object>();
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, Boolean.TRUE);
		MatchModel match = MatchService.doResourceMatch(leftResource, rightResource, options);
		assertTrue(match.getUnmatchedElements().isEmpty());
	}

}