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

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;

import be.ac.vub.jar2uml.JarToUMLResources;

/**
 * Shared functionality for Jar2UML import from Jar file wizard pages
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class AbstractJarToUMLImportWizardPage extends AbstractImportWizardPage {

	protected Composite fileSelectionArea;
	protected FilesFieldEditor editor;
	protected FilesFieldEditor cpEditor;
	protected String fileExtension;

	/**
	 * Creates a new {@link AbstractJarToUMLImportWizardPage}.
	 * @param pageName
	 * @param description
	 * @param selection
	 * @param fileExtension
	 */
	public AbstractJarToUMLImportWizardPage(String pageName,
			String description, IStructuredSelection selection, String fileExtension) {
		super(pageName, description, selection);
		this.fileExtension = fileExtension;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createAdvancedControls(Composite parent) {
		super.createAdvancedControls(parent);
		fileSelectionArea = createFileSelectionArea(parent);
		editor = createFilesFieldEditor(fileSelectionArea, fileExtension); //$NON-NLS-1$
		editor.setLabelText(JarToUMLResources.getString("AbstractJarToUMLImportWizardPage.selectJarFile")); //$NON-NLS-1$
		cpEditor = createFilesFieldEditor(fileSelectionArea);
		cpEditor.setLabelText(JarToUMLResources.getString("AbstractJarToUMLImportWizardPage.selectCpJarFile")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	@Override
	protected InputStream getInitialContents() {
		InputStream in = super.getInitialContents();
		try {
			addJarFiles();
			addCpJarFiles();
			return in;
		} catch (IOException e) {
			JarToUMLPlugin.getPlugin().report(e);
			return null;
		}
	}

	/**
	 * Adds all jar files selected in {@link #editor} to {@link AbstractImportWizardPage#jarToUML}.
	 * @throws IOException
	 */
	protected void addJarFiles() throws IOException {
		StringTokenizer files = createFileFieldTokenizer(editor);
		while (files.hasMoreTokens()) {
			jarToUML.addJar(new JarFile(files.nextToken()));
		}
	}

	/**
	 * Adds all jar files selected in {@link #cpEditor} as classpath jars to {@link AbstractImportWizardPage#jarToUML}.
	 * @throws IOException
	 */
	protected void addCpJarFiles() throws IOException {
		StringTokenizer files = createFileFieldTokenizer(cpEditor);
		while (files.hasMoreTokens()) {
			jarToUML.addCpJar(new JarFile(files.nextToken()));
		}
	}

	/**
	 * @param editor
	 * @return A {@link StringTokenizer} for each of the files selected in editor.
	 */
	protected StringTokenizer createFileFieldTokenizer(FilesFieldEditor editor) {
		return new StringTokenizer(editor.getStringValue(), ";"); //$NON-NLS-1$
	}

}