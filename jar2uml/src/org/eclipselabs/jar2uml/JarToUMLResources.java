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
package org.eclipselabs.jar2uml;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Contains the Jar2UML resource bundle.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLResources {

	private static final ResourceBundle resourceBundle =
		ResourceBundle.getBundle("org.eclipselabs.jar2uml.messages"); //$NON-NLS-1$

	public static final String LOGGER = "org.eclipselabs.jar2uml"; //$NON-NLS-1$

	public static final Logger logger = Logger.getLogger(LOGGER);

	/**
	 * Not meant to be instantiated.
	 */
	private JarToUMLResources() {
		super();
	}

	/**
	 * @param key
	 * @return The (translated) string for the given key, or the key if not available.
	 */
	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * @return the resourcebundle
	 */
	public static ResourceBundle getResourcebundle() {
		return resourceBundle;
	}

	/**
	 * Reports e via the logger
	 * @param e
	 */
	public static void report(Exception e) {
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		logger.severe(e.getLocalizedMessage());
		logger.severe(stackTrace.toString());
	}

}
