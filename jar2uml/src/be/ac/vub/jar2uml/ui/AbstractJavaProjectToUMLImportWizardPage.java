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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import be.ac.vub.jar2uml.JarToUMLResources;

/**
 * Shared functionality for Jar2UML import from Java project wizard pages
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class AbstractJavaProjectToUMLImportWizardPage extends AbstractImportWizardPage {

	protected Button includeReferencedProjectsBtn;
	public AbstractJavaProjectToUMLImportWizardPage(String pageName,
			String description, IStructuredSelection selection) {
		super(pageName, description, selection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.Selection) {
			handleSelectionEvent(event);
		}
		super.handleEvent(event);
	}

	/**
	 * Handles the selection of a new resource container
	 * @param event
	 */
	protected abstract void handleSelectionEvent(Event event);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		boolean valid = super.validatePage();
		if (valid) {
			IPath path = getContainerFullPath();
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			IProject project = resource.getProject();
			if (!project.isAccessible()) {
				setErrorMessage(String.format(
						JarToUMLResources.getString("AbstractJavaProjectToUMLImportWizardPage.projectNotAccessible"), 
						project.getName())); //$NON-NLS-1$
				valid = false;
			} else {
				try {
					if (project.getNature(JavaCore.NATURE_ID) == null) {
						setErrorMessage(JarToUMLResources.getString("AbstractJavaProjectToUMLImportWizardPage.onlyJavaProjects")); //$NON-NLS-1$
						valid = false;
					}
				} catch (CoreException e) {
					JarToUMLPlugin.getPlugin().report(e);
				}
			}
		}
		return valid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createAdvancedControls(Composite parent) {
		super.createAdvancedControls(parent);
		includeReferencedProjectsBtn = 
			createCheckbox(parent, JarToUMLResources.getString("AbstractJavaProjectToUMLImportWizardPage.includeReferencedProjects"), true); //$NON-NLS-1$
	}

}