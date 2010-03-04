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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Contains the Jar2UML resource bundle.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLResources {

	private static final ResourceBundle resourceBundle =
		ResourceBundle.getBundle("be.ac.vub.jar2uml.messages"); //$NON-NLS-1$

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

}
