/*******************************************************************************
 * Copyright (c) 2007-2010 Dennis Wagelaar, Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package be.ac.vub.jar2uml.test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Model;
import org.osgi.framework.Bundle;

import be.ac.vub.jar2uml.JarToUML;

/**
 * Shared functionality for Jar2UML test cases.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class J2UTestCase extends EMFTestCase {

	public static final String PLUGIN_ID = "be.ac.vub.jar2uml.test";
	public static final String PLUGIN_URI = "platform:/plugin/" + PLUGIN_ID;

	public static final Bundle bundle = Platform.getBundle(PLUGIN_ID);

	public static final String javatestProject = "javatest";
	public static final String javatestReferredProject = "javatestref";

	public static final String thisClassFile = "be/ac/vub/jar2uml/test/JarToUMLTest.class";

	public static final String pkServletDepsUri = PLUGIN_URI + "/resources/platformkitservlet.deps.uml";
	public static final String pkServletWar = "resources/platformkitservlet.war";

	public static final String atJar = "resources/at2-build080507/ambienttalk2.jar";
	public static final String antlrJar = "resources/at2-build080507/lib/antlr.jar";
	public static final String getoptJar = "resources/at2-build080507/lib/java-getopt-1.0.13.jar";
	public static final String atModelUri = PLUGIN_URI + "/resources/at2-build080507/ambienttalk2.uml";
	public static final String atDepsModelUri = PLUGIN_URI + "/resources/at2-build080507/ambienttalk2.deps.uml";

	public static final String instantmessengerJar = "resources/instantmessenger.jar";

	/**
	 * Copies the file at the given path to the root of the Java test project.
	 * @param path
	 * @return The target file.
	 * @throws CoreException
	 * @throws IOException
	 */
	public static IFile copyFileToTestProject(String path)
	throws CoreException, IOException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
		URL url = bundle.getResource(path);
		String targetPath = fileName(path);
		IFile file = project.getFile(targetPath);
		JarToUML.logger.info("Creating jar file: " + file);
		file.create(url.openStream(), true, null);
		return file;
	}

	/**
	 * @param path
	 * @return The last segment of path (after last '/').
	 */
	public static String fileName(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/**
	 * @param file
	 * @return The {@link JarFile} corresponding to file.
	 * @throws IOException
	 */
	public static JarFile jarFile(IFile file) throws IOException {
		return new JarFile(file.getLocation().toFile());
	}

	/**
	 * @param name
	 * @return A new project with the Java project nature.
	 * @throws CoreException
	 */
	public static IProject createJavaProject(String name) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (!project.exists()) {
			project.create(null);
			project.open(null);
			IProjectDescription description = project.getDescription();
			String natures[] = new String[] { "org.eclipse.jdt.core.javanature" };
			description.setNatureIds(natures);
			project.setDescription(description, null);
		}
		return project;
	}

	/**
	 * @param name
	 * @return The {@link IProject} with the given name, if any, <code>null</code> otherwise.
	 */
	public static IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	/**
	 * Creates path and its parents.
	 * @param path
	 * @throws CoreException
	 */
	public static void createPath(IFolder path) throws CoreException {
		final IContainer parent = path.getParent();
		if (!parent.exists() && parent instanceof IFolder) {
			createPath((IFolder) parent);
		}
		path.create(true, true, null);
	}

	/**
	 * Handles a caught exception
	 * @param e
	 */
	public static void handle(Exception e) {
		JarToUML.logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		fail(e.getLocalizedMessage());
	}

	/**
	 * Loads a UML Model from the given EMF uri.
	 * @param uri
	 * @return The (first) root Model in the loaded resource, if any, <code>null</code> otherwise.
	 */
	public static Model loadModelFromUri(String uri) {
		JarToUML.logger.info("Loading UML model from: " + uri);
		Resource res = JarToUML.createResourceSet().getResource(URI.createURI(uri), true);
		return findModel(res);
	}

	/**
	 * @param res
	 * @return The (first) root Model in res, if any, <code>null</code> otherwise.
	 */
	public static Model findModel(Resource res) {
		Model root = null;
		for (EObject e : res.getContents()) {
			if (e instanceof Model) {
				root = (Model) e;
				break;
			}
		}
		return root;
	}

	/**
	 * Validates the model.
	 * @param model
	 */
	public static void validateModel(Model model) {
		assertNotNull(model);
		BasicDiagnostic diagnostics = new BasicDiagnostic();
		Map<Object, Object> context = new HashMap<Object, Object>();
		model.validateElementsPublicOrPrivate(diagnostics, context);
		model.validateHasNoQualifiedName(diagnostics, context);
		model.validateHasOwner(diagnostics, context);
		model.validateHasQualifiedName(diagnostics, context);
		model.validateMembersDistinguishable(diagnostics, context);
		model.validateNotOwnSelf(diagnostics, context);
		model.validateVisibilityNeedsOwnership(diagnostics, context);
		JarToUML.logger.info("Model diagnostics: " + diagnostics.getMessage());
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
	}

	/**
	 * Creates a new {@link J2UTestCase}.
	 */
	public J2UTestCase() {
		super();
	}

	/**
	 * Creates a new {@link J2UTestCase}.
	 * @param name
	 */
	public J2UTestCase(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createJavaProject(javatestProject);
		createJavaProject(javatestReferredProject);
		//
		// Refresh workspace
		//
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		JarToUML.logger.info("setup done");
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		JarToUML.logger.info("starting teardown");
		super.tearDown();
	}

}