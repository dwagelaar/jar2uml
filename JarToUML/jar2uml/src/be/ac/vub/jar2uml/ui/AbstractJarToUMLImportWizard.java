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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import be.ac.vub.jar2uml.JarToUML;

/**
 * Abstract import wizard class for JarToUML wizards.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class AbstractJarToUMLImportWizard extends Wizard implements IImportWizard {

	public static final String WIZ_IMAGE = "icons/full/wizban/Jar2UMLWizard.png";

	protected AbstractImportWizardPage mainPage;

	/**
	 * Creates a new {@link AbstractJarToUMLImportWizard}.
	 */
	public AbstractJarToUMLImportWizard() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if (file == null)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages(); 
		addPage(mainPage);        
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(JarToUML.getString("AbstractJarToUMLImportWizard.windowTitle")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(JarToUMLPlugin.getPlugin().getImageDescriptor(WIZ_IMAGE));
		setNeedsProgressMonitor(true);
	}

}