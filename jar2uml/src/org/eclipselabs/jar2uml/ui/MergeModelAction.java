/*******************************************************************************
 * Copyright (c) 2007-2011 Dennis Wagelaar, Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package org.eclipselabs.jar2uml.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.uml2.uml.Model;
import org.eclipselabs.jar2uml.JarToUMLException;
import org.eclipselabs.jar2uml.JarToUMLResources;
import org.eclipselabs.jar2uml.MergeModel;

/**
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 *
 */
public class MergeModelAction extends SelectionAction {

	/**
	 * @param res
	 * @return the root {@link Model} element in res, if any, <code>null</code> otherwise
	 */
	public static final Model findRootModel(Resource res) {
		for (EObject rootEl : res.getContents()) {
			if (rootEl instanceof Model) {
				return (Model) rootEl;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		final IFile baseModelFile = (IFile) ((IStructuredSelection) selection).getFirstElement();
		final String baseModelName = baseModelFile.getName();
		final CheckedTreeSelectionDialog dlg = new CheckedTreeSelectionDialog(
				JarToUMLPlugin.getPlugin().getShell(), 
				new WorkbenchLabelProvider(), 
				new WorkbenchContentProvider());
		dlg.setInput(ResourcesPlugin.getWorkspace().getRoot());
		dlg.setContainerMode(true);
		dlg.setHelpAvailable(false);
		dlg.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IResource) {
					IResource resource = (IResource) element;
					if (resource.getType() == IResource.FILE) {
						return resource.getFileExtension().toLowerCase().equals("uml"); //$NON-NLS-1$
					}
					return true;
				}
				return false;
			}
		});
		dlg.setTitle(JarToUMLResources.getString("MergeModelAction.dlgTitle")); //$NON-NLS-1$
		dlg.setMessage(String.format(
				JarToUMLResources.getString("MergeModelAction.dlgMessage"), 
				baseModelName)); //$NON-NLS-1$
		JarToUMLPlugin.getPlugin().getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				dlg.open();
			}
		});
		if (dlg.getReturnCode() != Window.OK) {
			return;
		}
		final Object[] selectedResources = dlg.getResult();
		
		final WorkspaceJob job = new WorkspaceJob(String.format(
				JarToUMLResources.getString("MergeModelAction.jobTitle"), 
				baseModelName)) { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				IStatus st;
				try {
					mergeModels(baseModelFile, selectedResources, monitor);
					st = new Status(
							IStatus.OK, 
							JarToUMLPlugin.getPlugin().getBundle().getSymbolicName(), 
							String.format(
									JarToUMLResources.getString("MergeModelAction.jobResult"), 
									baseModelName)); //$NON-NLS-1$
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
		};
		job.setRule(baseModelFile.getProject()); //lock project
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Merges resources into the base model.
	 * @param baseFile the file containing the base model
	 * @param resources the resources to merge
	 * @param monitor the progress monitor
	 * @throws IOException 
	 */
	protected void mergeModels(IFile baseFile, Object[] resources, IProgressMonitor monitor) throws IOException {
		assert monitor != null;
		if (resources.length == 0) {
			throw new JarToUMLException(JarToUMLResources.getString("MergeModelAction.noModelsSelected")); //$NON-NLS-1$
		}
		final List<IFile> files = new ArrayList<IFile>(resources.length);
		for (Object resource : resources) {
			if (resource instanceof IFile) {
				files.add((IFile) resource);
			}
		}

		monitor.beginTask(
				String.format(
						JarToUMLResources.getString("MergeModelAction.mergingStart"), 
						baseFile.getName()), 
				2 + files.size() * 10); //$NON-NLS-1$
		monitor.subTask(JarToUMLResources.getString("MergeModelAction.loadingBase")); //$NON-NLS-1$
		final ResourceSet rs = new ResourceSetImpl();
		final URI baseURI = URI.createPlatformResourceURI(
				baseFile.getProject().getName() + '/' +
				baseFile.getProjectRelativePath().toString(), 
				true);
		final Resource baseRes = rs.getResource(baseURI, true);
		final Model base = findRootModel(baseRes);
		assert base != null;
		final MergeModel mergeModel = new MergeModel();
		mergeModel.setBaseModel(base);
		monitor.worked(1);

		for (IFile mergeFile : files) {
			monitor.subTask(String.format(
					JarToUMLResources.getString("MergeModelAction.merging"), 
					mergeFile.getName())); //$NON-NLS-1$
			URI mergeURI = URI.createPlatformResourceURI(
					mergeFile.getProject().getName() + '/' +
					mergeFile.getProjectRelativePath().toString(), 
					true);
			Resource mergeRes = rs.getResource(mergeURI, true);
			Model merge = findRootModel(mergeRes);
			assert merge != null;
			mergeModel.setMergeModel(merge);
			mergeModel.setMonitor(new SubProgressMonitor(monitor, 10));
			mergeModel.run();
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		monitor.subTask(JarToUMLResources.getString("MergeModelAction.saving")); //$NON-NLS-1$
		baseRes.save(Collections.emptyMap());
		monitor.worked(1);
	}

}
