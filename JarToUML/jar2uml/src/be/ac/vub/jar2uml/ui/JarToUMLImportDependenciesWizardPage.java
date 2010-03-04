/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * Copyright (c) 2007-2010 Dennis Wagelaar, Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package be.ac.vub.jar2uml.ui;

import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Import wizard to import dependencies of a Jar file from the local file system into a UML model in the workspace
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLImportDependenciesWizardPage extends AbstractJarToUMLImportWizardPage {

	/**
	 * Creates a new {@link JarToUMLImportDependenciesWizardPage}.
	 * @param pageName
	 * @param description
	 * @param selection
	 */
	public JarToUMLImportDependenciesWizardPage(String pageName, String description,
			IStructuredSelection selection) {
		super(pageName, description, selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	@Override
	protected InputStream getInitialContents() {
		InputStream in = super.getInitialContents();
		jarToUML.setIncludeFeatures(true);
		jarToUML.setIncludeInstructionReferences(true);
		jarToUML.setFilter(null);
		jarToUML.setDependenciesOnly(true);
		return in;
	}

}
