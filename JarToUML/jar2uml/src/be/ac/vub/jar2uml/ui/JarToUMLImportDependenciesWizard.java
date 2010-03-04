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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

import be.ac.vub.jar2uml.JarToUMLResources;

/**
 * Import wizard to import dependencies of a Jar file from the local file system into a UML model in the workspace 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLImportDependenciesWizard extends AbstractJarToUMLImportWizard {

	/**
	 * Creates a new {@link JarToUMLImportDependenciesWizard}.
	 */
	public JarToUMLImportDependenciesWizard() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.ui.AbstractJarToUMLImportWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		mainPage = new JarToUMLImportDependenciesWizardPage(
				JarToUMLResources.getString("JarToUMLImportDependenciesWizard.pageName"),
				JarToUMLResources.getString("JarToUMLImportDependenciesWizard.description"),
				selection); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
