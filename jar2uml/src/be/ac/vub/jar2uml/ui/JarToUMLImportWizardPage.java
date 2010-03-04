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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import be.ac.vub.jar2uml.JarToUMLResources;
import be.ac.vub.jar2uml.JavaAPIFilter;
import be.ac.vub.jar2uml.PublicAPIFilter;

/**
 * Import wizard to import a Jar file from the local file system into a UML model in the workspace 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLImportWizardPage extends AbstractJarToUMLImportWizardPage {

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
		super.createAdvancedControls(parent);
		includeFeaturesBtn = 
			createCheckbox(parent, JarToUMLResources.getString("JarToUMLImportWizardPage.includeFeatures"), true); //$NON-NLS-1$ 
		onlyJavaApiBtn = 
			createCheckbox(parent, JarToUMLResources.getString("JarToUMLImportWizardPage.onlyJavaApi"), false); //$NON-NLS-1$
		allElementsBtn = 
			createCheckbox(parent, JarToUMLResources.getString("JarToUMLImportWizardPage.allElements"), false); //$NON-NLS-1$
		includeInstrRefsBtn = 
			createCheckbox(parent, JarToUMLResources.getString("JarToUMLImportWizardPage.includeInstrRefs"), false); //$NON-NLS-1$

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
		jarToUML.setIncludeFeatures(includeFeaturesBtn.getSelection());
		if (onlyJavaApiBtn.getSelection()) {
			jarToUML.setFilter(new JavaAPIFilter());
		} else if (allElementsBtn.getSelection()) {
			jarToUML.setFilter(null);
		} else {
			jarToUML.setFilter(new PublicAPIFilter());
		}
		jarToUML.setIncludeInstructionReferences(includeInstrRefsBtn.getSelection());
		return in;
	}
}
