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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;

/**
 * Import wizard to import dependencies of a Jar file from the local file system into a UML model in the workspace
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLImportDependenciesWizardPage extends AbstractImportWizardPage {

	protected FilesFieldEditor editor;

	public JarToUMLImportDependenciesWizardPage(String pageName, String description,
			IStructuredSelection selection) {
		super(pageName, description, selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */	
	protected void createAdvancedControls(Composite parent) {
		editor = createFilesFieldEditor(parent, ".deps.uml");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	protected InputStream getInitialContents() {
		try {
        	jarToUML.clearJars();
        	jarToUML.setIncludeFeatures(true);
        	jarToUML.setIncludeInstructionReferences(true);
       		jarToUML.setFilter(null);
       		jarToUML.setDependenciesOnly(true);
	    	StringTokenizer files = new StringTokenizer(editor.getStringValue(), ";");
	    	while (files.hasMoreTokens()) {
				jarToUML.addJar(new JarFile(files.nextToken()));
	    	}
			return new FileInputStream(new File(editor.getStringValue()));
		} catch (IOException e) {
			return null;
		}
	}

}
