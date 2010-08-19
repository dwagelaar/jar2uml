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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.JarToUMLResources;

/**
 * Shared functionality for Jar2UML import wizard pages
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class AbstractImportWizardPage extends WizardNewFileCreationPage {

	public static final String WIZ_IMAGE = "icons/full/wizban/Jar2UMLWizard.png"; //$NON-NLS-1$

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);
	protected JarToUML jarToUML;
	protected Button updateExistingModelBtn;

	private boolean fileExists;

	// cache of newly-created file
	private IFile newFile;

	/**
	 * Creates a new {@link AbstractImportWizardPage}.
	 * @param pageName
	 * @param description
	 * @param selection
	 * @see WizardNewFileCreationPage#WizardNewFileCreationPage(String, IStructuredSelection)
	 */
	public AbstractImportWizardPage(String pageName, String description,
			IStructuredSelection selection) {
		super(pageName, selection);
		setTitle(pageName);
		setDescription(description);
		setAllowExistingResources(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createLinkTarget()
	 */
	@Override
	protected void createLinkTarget() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	@Override
	protected InputStream getInitialContents() {
		jarToUML = new JarToUML();
		jarToUML.setUpdateExistingFile(updateExistingModelBtn.getSelection() && updateExistingModelBtn.getEnabled());
		return new ByteArrayInputStream(new byte[0]); // dummy input stream
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createAdvancedControls(Composite parent) {
		updateExistingModelBtn = 
			createCheckbox(parent, JarToUMLResources.getString("AbstractImportWizardPage.updateExisting"), false); //$NON-NLS-1$
		updateExistingModelBtn.setEnabled(isFileExists());
	}

	/**
	 * @return the {@link IFile} handle to the file to be created
	 */
	protected IFile getFileHandle() {
		final IPath containerPath = getContainerFullPath();
		final IPath newFilePath = containerPath.append(getFileName());
		return createFileHandle(newFilePath);
	}

	/**
	 * Creates a file resource and returns it. Overriding necessary for compatibility
	 * with Eclipse 3.3.
	 */
	@Override
	public IFile createNewFile() {
		if (newFile != null) {
			return newFile;
		}

		// create the new file and cache it if successful
		final IPath containerPath = getContainerFullPath();
		final IFile newFileHandle = getFileHandle();
		final InputStream initialContents = getInitialContents();

		createLinkTarget();
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor)
			throws CoreException {
				try {
					monitor.beginTask(JarToUMLResources.getString("AbstractImportWizardPage.taskName"), 100); //$NON-NLS-1$
					ContainerGenerator generator = new ContainerGenerator(
							containerPath);
					generator.generateContainer(new SubProgressMonitor(monitor,
							100));
					//postpone file creation to a background job
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
						JarToUMLResources.getString("AbstractImportWizardPage.creationProblems"), //$NON-NLS-1$
						null, // no special message
						((CoreException) e.getTargetException())
						.getStatus());
			} else {
				// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
				logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
				StringBuffer body = new StringBuffer();
				body.append(JarToUMLResources.getString("AbstractImportWizardPage.internalError")); //$NON-NLS-1$
				body.append(' ');
				body.append(e.getTargetException().getMessage());
				MessageDialog
				.openError(
						getContainer().getShell(),
						JarToUMLResources.getString("AbstractImportWizardPage.creationProblems"),
						body.toString()); //$NON-NLS-1$
				JarToUMLPlugin.getPlugin().log(body.toString(), IStatus.ERROR, e);
			}
			return null;
		}

		final WorkspaceJob job = new WorkspaceJob(JarToUMLResources.getString("AbstractImportWizardPage.taskName")) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				IStatus st;
				try {
					createFile(newFileHandle, initialContents, monitor);
					st = new Status(
							IStatus.OK, 
							JarToUMLPlugin.getPlugin().getBundle().getSymbolicName(), 
							String.format(
									JarToUMLResources.getString("AbstractImportWizardPage.taskCompleted"), 
									getName())); //$NON-NLS-1$
				} catch (OperationCanceledException e) {
					st = Status.CANCEL_STATUS;
				} catch (Exception e) {
					JarToUMLPlugin.getPlugin().report(e);
					st = new Status(
							IStatus.ERROR, 
							JarToUMLPlugin.getPlugin().getBundle().getSymbolicName(), 
							e.getLocalizedMessage(),
							e);
				} finally {
					monitor.done();
				}
				return st;
			}
		}; //$NON-NLS-1$
		job.setRule(getFileHandle().getProject()); //lock project
		job.setUser(true);
		job.schedule();

		newFile = newFileHandle;

		return newFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getNewFileLabel()
	 */
	@Override
	protected String getNewFileLabel() {
		return JarToUMLResources.getString("AbstractImportWizardPage.newFileLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validateLinkedResource()
	 */
	@Override
	protected IStatus validateLinkedResource() {
		return new Status(
				IStatus.OK, 
				JarToUMLPlugin.getPlugin().getBundle().getSymbolicName(), 
				IStatus.OK, 
				"", 
				null); //$NON-NLS-1$
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
	@Override
	protected void createFile(IFile fileHandle, InputStream contents, IProgressMonitor monitor)
	throws CoreException {
		final IPath path = fileHandle.getFullPath();
		jarToUML.setOutputFile(path.toString());
		jarToUML.setOutputModelName(path.removeFileExtension().lastSegment());
		jarToUML.setMonitor(monitor);
		jarToUML.run();
		if (jarToUML.isRunComplete()) {
			try {
				jarToUML.getModel().eResource().save(Collections.EMPTY_MAP);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		jarToUML = null;

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * @param parent
	 * @param fileExtension Extension to add to the created file
	 * @return A {@link FilesFieldEditor} widget
	 */
	protected FilesFieldEditor createFilesFieldEditor(Composite parent, final String fileExtension) {
		final FilesFieldEditor editor = createFilesFieldEditor(parent);
		editor.getTextControl(parent).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				IPath path = new Path(editor.getStringValue());
				setFileName(path.removeFileExtension().lastSegment() + fileExtension);
			}
		});
		return editor;
	}

	/**
	 * @param parent
	 * @return A {@link FilesFieldEditor} widget
	 */
	protected FilesFieldEditor createFilesFieldEditor(Composite parent) {
		final FilesFieldEditor editor = new FilesFieldEditor(
				"fileSelect",
				JarToUMLResources.getString("AbstractImportWizardPage.selectFile"),
				parent); //$NON-NLS-1$ //$NON-NLS-2$
		String[] extensions = new String[] { "*.zip;*.jar;*.war;*.ear;*.sar;*.rar" }; //$NON-NLS-1$
		editor.setFileExtensions(extensions);
		return editor;
	}

	/**
	 * @param parent
	 * @return A {@link Composite} widget that can contain a {@link FilesFieldEditor}.
	 */
	protected Composite createFileSelectionArea(Composite parent) {
		final Composite fileSelectionArea = new Composite(parent, SWT.NONE);
		final GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		fileSelectionArea.setLayoutData(fileSelectionData);

		final GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		fileSelectionArea.setLayout(fileSelectionLayout);

		fileSelectionArea.moveAbove(null);
		return fileSelectionArea;
	}

	/**
	 * @param parent
	 * @param text
	 * @param enabled
	 * @return A checkbox
	 */
	protected Button createCheckbox(Composite parent, String text, boolean enabled) {
		Button btn = new Button(parent, SWT.CHECK | SWT.LEFT);
		btn.setText(text);
		btn.setSelection(enabled);
		return btn;
	}

	/**
	 * Adds all relevant Java projects based on {@link #getContainerFullPath()}
	 * @param includeWorkspaceReferences Include referenced projects and jar files in workspace
	 * @throws IOException 
	 * @throws JavaModelException 
	 */
	protected void addAllJavaProjects(boolean includeWorkspaceReferences) throws JavaModelException, IOException {
		IPath path = getContainerFullPath();
		IJavaProject javaProject = JarToUML.getJavaProject(path);
		jarToUML.addPaths(javaProject, includeWorkspaceReferences);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#setFileName(java.lang.String)
	 */
	@Override
	public void setFileName(String value) {
		super.setFileName(value);
	}

	/**
	 * @return the fileExists
	 */
	public boolean isFileExists() {
		return fileExists;
	}

	/**
	 * @param fileExists the fileExists to set
	 */
	public void setFileExists(boolean fileExists) {
		this.fileExists = fileExists;
		if (updateExistingModelBtn != null) {
			updateExistingModelBtn.setEnabled(fileExists);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		try {
			setFileExists(getFileHandle().exists());
		} catch (Exception e) {
			setFileExists(false);
		}
		return super.validatePage();
	}

}