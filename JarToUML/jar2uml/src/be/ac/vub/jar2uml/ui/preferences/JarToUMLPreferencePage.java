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
package be.ac.vub.jar2uml.ui.preferences;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.JarToUMLResources;
import be.ac.vub.jar2uml.ui.JarToUMLPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class JarToUMLPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public JarToUMLPreferencePage() {
		super(GRID);
		setPreferenceStore(JarToUMLPlugin.getPlugin().getPreferenceStore());
		setDescription(JarToUMLResources.getString("JarToUMLPreferencePage.title")); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		String[][] lvls = new String[][] {
				createComboEntry(Level.OFF),
				createComboEntry(Level.SEVERE),
				createComboEntry(Level.WARNING),
				createComboEntry(Level.INFO),
				createComboEntry(Level.FINE),
				createComboEntry(Level.FINER),
				createComboEntry(Level.FINEST),
				createComboEntry(Level.ALL)
		};
		ComboFieldEditor logLevel = new ComboFieldEditor(
				PreferenceConstants.P_LOG_LEVEL,
				JarToUMLResources.getString("JarToUMLPreferencePage.logLevel"), //$NON-NLS-1$
				lvls,
				getFieldEditorParent());
		addField(logLevel);
	}

	/**
	 * @param level
	 * @return A preference combo field entry for level.
	 */
	private String[] createComboEntry(Level level) {
		return new String[] { level.getLocalizedName(), level.toString() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean ok = super.performOk();
		if (ok) {
			// Directly apply new log level
			String logLevel = getPreferenceStore().getString(PreferenceConstants.P_LOG_LEVEL);
			Logger logger = Logger.getLogger(JarToUML.LOGGER);
			logger.setLevel(Level.parse(logLevel));
			logger.info(String.format(JarToUMLResources.getString("logLevelSetTo"), logger.getLevel())); //$NON-NLS-1$
		}
		return ok;
	}

}