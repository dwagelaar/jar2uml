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
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import be.ac.vub.jar2uml.JarToUML;


public class JarToUMLImportDependenciesWizardPage extends WizardNewFileCreationPage {

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);
	
	protected FilesFieldEditor editor;
	protected JarToUML jarToUML = new JarToUML();
	
	// cache of newly-created file
	private IFile newFile;

	public JarToUMLImportDependenciesWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
		setTitle(pageName); //NON-NLS-1
		setDescription("Import dependencies of a Jar file from the local file system into a UML model in the workspace"); //NON-NLS-1
		logger.setLevel(Level.INFO);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */	
	protected void createAdvancedControls(Composite parent) {
		Composite fileSelectionArea = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		fileSelectionArea.setLayoutData(fileSelectionData);

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		fileSelectionArea.setLayout(fileSelectionLayout);
		
		editor = new FilesFieldEditor("fileSelect","Select File: ",fileSelectionArea); //NON-NLS-1 //NON-NLS-2
		editor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				IPath path = new Path(JarToUMLImportDependenciesWizardPage.this.editor.getStringValue());
				setFileName(path.removeFileExtension().lastSegment() + "Deps.uml");
			}
		});
		String[] extensions = new String[] { "*.zip;*.jar" }; //NON-NLS-1
		editor.setFileExtensions(extensions);
		
		fileSelectionArea.moveAbove(null);
	}
	
	 /* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createLinkTarget()
	 */
	protected void createLinkTarget() {
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

    /**
     * Creates a file resource given the file handle and contents.
     *
     * @param fileHandle the file handle to create a file resource with
     * @param contents the initial contents of the new file resource, or
     *   <code>null</code> if none (equivalent to an empty stream)
     * @param monitor the progress monitor to show visual progress with
     * @exception CoreException if the operation fails
     * @exception OperationCanceledException if the operation is canceled
     */
    protected void createFile(IFile fileHandle, InputStream contents,
            IProgressMonitor monitor) throws CoreException {
    	IPath path = fileHandle.getFullPath();
		jarToUML.setOutputFile(path.toString());
		jarToUML.setOutputModelName(path.removeFileExtension().lastSegment());
		jarToUML.setMonitor(monitor);
		jarToUML.run();

        if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
    }
    
    /**
     * Creates a file resource and returns it. Overriding necessary for compatibility
     * with Eclipse 3.3.
     */
    public IFile createNewFile() {
		if (newFile != null) {
			return newFile;
		}

		// create the new file and cache it if successful
		final IPath containerPath = getContainerFullPath();
		final IPath newFilePath = containerPath.append(getFileName());
		final IFile newFileHandle = createFileHandle(newFilePath);
		final InputStream initialContents = getInitialContents();

        createLinkTarget();
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
                    monitor.beginTask("Creating", 2000);
                    ContainerGenerator generator = new ContainerGenerator(
                            containerPath);
                    generator.generateContainer(new SubProgressMonitor(monitor,
                            1000));
                    createFile(newFileHandle, initialContents,
                            new SubProgressMonitor(monitor, 1000));
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof CoreException) {
                ErrorDialog
                        .openError(
                                getContainer().getShell(), // Was Utilities.getFocusShell()
                                "Creation Problems",
                                null, // no special message
                                ((CoreException) e.getTargetException())
                                        .getStatus());
            } else {
                // CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
            	logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                MessageDialog
                        .openError(
                                getContainer().getShell(),
                                "Creation problems", "Internal error: " + e.getTargetException().getMessage());
            }
            return null;
        }

		newFile = newFileHandle;

		return newFile;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getNewFileLabel()
	 */
	protected String getNewFileLabel() {
		return "New File Name:"; //NON-NLS-1
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validateLinkedResource()
	 */
	protected IStatus validateLinkedResource() {
		return new Status(IStatus.OK, "be.ac.vub.jar2uml", IStatus.OK, "", null); //NON-NLS-1 //NON-NLS-2
	}
}
