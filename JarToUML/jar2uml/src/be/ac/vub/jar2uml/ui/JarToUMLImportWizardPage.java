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

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import be.ac.vub.jar2uml.JavaAPIFilter;
import be.ac.vub.jar2uml.PublicAPIFilter;

/**
 * Import wizard to import a Jar file from the local file system into a UML model in the workspace 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLImportWizardPage extends AbstractImportWizardPage {

	protected FilesFieldEditor editor;
	protected Button onlyJavaApiBtn;
	protected Button allElementsBtn;
	protected Button includeInstrRefsBtn;
	protected Button includeFeaturesBtn;

	/**
	 * Creates a new JarToUMLImportWizardPage
	 * @param pageName
	 * @param description
	 * @param selection
	 */
	public JarToUMLImportWizardPage(String pageName, String description, 
			IStructuredSelection selection) {
		super(pageName, description, selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */	
	@Override
	protected void createAdvancedControls(Composite parent) {
		editor = createFilesFieldEditor(parent, ".uml");
		includeFeaturesBtn = 
			createCheckbox(parent, "Include operations and attributes", true); 
		onlyJavaApiBtn = 
			createCheckbox(parent, "Only Java API packages", false);
		allElementsBtn = 
			createCheckbox(parent, "Include anonymous and private elements", false);
		includeInstrRefsBtn = 
			createCheckbox(parent, "Include elements referenced by bytecode instructions", false);

		includeFeaturesBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!includeFeaturesBtn.getSelection()) {
					allElementsBtn.setSelection(false);
					allElementsBtn.setEnabled(false);
					includeInstrRefsBtn.setSelection(false);
					includeInstrRefsBtn.setEnabled(false);
				} else {
					allElementsBtn.setEnabled(true);
					includeInstrRefsBtn.setEnabled(true);
				}
			}
		});

		onlyJavaApiBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (onlyJavaApiBtn.getSelection()) {
					allElementsBtn.setSelection(false);
					allElementsBtn.setEnabled(false);
				} else {
					allElementsBtn.setEnabled(true);
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	@Override
	protected InputStream getInitialContents() {
		InputStream in = super.getInitialContents();
		try {
        	jarToUML.setIncludeFeatures(includeFeaturesBtn.getSelection());
        	if (onlyJavaApiBtn.getSelection()) {
    			jarToUML.setFilter(new JavaAPIFilter());
        	} else if (allElementsBtn.getSelection()) {
        		jarToUML.setFilter(null);
        	} else {
    			jarToUML.setFilter(new PublicAPIFilter());
        	}
        	jarToUML.setIncludeInstructionReferences(includeInstrRefsBtn.getSelection());
	    	StringTokenizer files = new StringTokenizer(editor.getStringValue(), ";");
	    	while (files.hasMoreTokens()) {
				jarToUML.addJar(new JarFile(files.nextToken()));
	    	}
			return in;
		} catch (IOException e) {
			return null;
		}
	}
}
