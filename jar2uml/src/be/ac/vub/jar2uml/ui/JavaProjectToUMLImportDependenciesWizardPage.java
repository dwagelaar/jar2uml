/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Import wizard to import dependencies of a Java project into a UML model in the workspace
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JavaProjectToUMLImportDependenciesWizardPage extends AbstractJavaProjectToUMLImportWizardPage {

	protected Button includeReferencedProjectsBtn;

	/**
	 * Creates a new JavaProjectToUMLImportDependenciesWizardPage
	 * @param pageName
	 * @param description
	 * @param selection
	 */
	public JavaProjectToUMLImportDependenciesWizardPage(String pageName, String description,
			IStructuredSelection selection) {
		super(pageName, description, selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */	
	protected void createAdvancedControls(Composite parent) {
		includeReferencedProjectsBtn = 
			createCheckbox(parent, "Include referenced projects and jar files in workspace", true); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	protected InputStream getInitialContents() {
		jarToUML.clearPaths();
		jarToUML.setIncludeFeatures(true);
		jarToUML.setIncludeInstructionReferences(true);
		jarToUML.setFilter(null);
		jarToUML.setDependenciesOnly(true);
		addAllJavaProjects(includeReferencedProjectsBtn.getSelection());
		return super.getInitialContents();
	}

	/**
	 * Handles the selection of a new resource container
	 * @param event
	 */
	protected void handleSelectionEvent(Event event) {
		IPath path = getContainerFullPath();
		if (path != null) {
			this.setFileName(path.lastSegment() + ".deps.uml"); //NON-NLS-1
		}
	}
}
