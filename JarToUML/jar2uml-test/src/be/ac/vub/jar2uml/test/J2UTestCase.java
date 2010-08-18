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
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import junit.framework.Assert;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
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
	public static final Bundle dataBundle = Platform.getBundle(PLUGIN_ID + ".data");

	public static final String pkServletDepsUri = PLUGIN_URI + "/resources/platformkitservlet.deps.uml";
	public static final String pkServletWar = "resources/platformkitservlet.war";

	public static final String atJar = "resources/at2-build080507/ambienttalk2.jar";
	public static final String antlrJar = "resources/at2-build080507/lib/antlr.jar";
	public static final String getoptJar = "resources/at2-build080507/lib/java-getopt-1.0.13.jar";
	public static final String atModelUri = PLUGIN_URI + "/resources/at2-build080507/ambienttalk2.uml";
	public static final String atDepsModelUri = PLUGIN_URI + "/resources/at2-build080507/ambienttalk2.deps.uml";

	public static final String instantmessengerJar = "resources/instantmessenger.jar";

	public static final String jaxbOsgiJar = "resources/jaxb-osgi.jar";
	public static final String jaxbOsgiDepsUri = PLUGIN_URI + "/resources/jaxb-osgi.deps.uml";

	public static final String j2eeJar = "resources/j2ee.jar";
	public static final String j2eeDepsUri = PLUGIN_URI + "/resources/j2ee.deps.uml";

	/**
	 * Copies the file at the given path to the root of the given project.
	 * @param path
	 * @param project
	 * @return The target file.
	 * @throws CoreException
	 * @throws IOException
	 */
	public static IFile copyFileToProject(String path, IProject project)
	throws CoreException, IOException {
		String targetPath = fileName(path);
		IFile file = project.getFile(targetPath);
		if (!file.exists()) {
			JarToUML.logger.info("Creating jar file: " + file);
			URL url = bundle.getResource(path);
			file.create(url.openStream(), true, null);
		}
		return file;
	}

	/**
	 * Copies the class file to the right place in the given project.
	 * @param clazz
	 * @param project Must be a Java project!
	 * @return The target file.
	 * @throws CoreException
	 * @throws IOException
	 */
	public static IFile copyClassToJavaProject(final Class<?> clazz, final IProject project)
	throws CoreException, IOException {
		String path = classFilePath(clazz);
		JarToUML.logger.info("copying class file: " + path);
		IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
		IPath outPath = jproject.getOutputLocation();
		JarToUML.logger.info("class file path: " + outPath);
		IPath classFilePath = outPath.append(path);
		final IFile classFile = ResourcesPlugin.getWorkspace().getRoot().getFile(classFilePath);
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (!classFile.exists()) {
					createPath((IFolder) classFile.getParent());
					try {
						classFile.create(getClassContents(clazz), true, null);
						JarToUML.logger.info("created file: " + classFile);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}, null);
		return classFile;
	}

	/**
	 * @param clazz
	 * @return the contents of clazz as an {@link InputStream}
	 * @throws IOException
	 */
	public static InputStream getClassContents(Class<?> clazz) throws IOException {
		String path = classFilePath(clazz);
		URL classURL = bundle.getResource(path);
		if (classURL == null) {
			dataBundle.getResource(path);
		}
		Assert.assertNotNull(classURL);
		return classURL.openStream();
	}

	/**
	 * @param clazz
	 * @return The file path of clazz relative to the classpath root.
	 * Works only for named classes.
	 */
	public static String classFilePath(Class<?> clazz) {
		return clazz.getName().replace('.', '/').concat(".class");
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
	 * @param file
	 * @return The {@link JarInputStream} corresponding to file.
	 * @throws IOException
	 * @throws CoreException 
	 */
	public static JarInputStream jarInputStream(IFile file) throws IOException, CoreException {
		return new JarInputStream(file.getContents());
	}

	/**
	 * @param name
	 * @return A new project with the Java project nature.
	 * @throws CoreException
	 */
	public static IProject createJavaProject(final String name) throws CoreException {
		final IProject project = getProject(name);
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (!project.exists()) {
					project.create(null);
					project.open(null);
					IProjectDescription description = project.getDescription();
					String natures[] = new String[] { "org.eclipse.jdt.core.javanature" };
					description.setNatureIds(natures);
					project.setDescription(description, null);
				}
			}
		}, null);
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
		if (!path.exists()) {
			path.create(true, true, null);
		}
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
	 * @param element
	 * @return A human-readable {@link String} that identifies element.
	 */
	public static final String toString(Element element) {
		if (element instanceof NamedElement) {
			final String qName = ((NamedElement) element).getQualifiedName();
			if (qName != null) {
				return qName;
			} else {
				return element.toString();
			}
		} else {
			return element.toString();
		}
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
	 * Validates the use of "inferred" tags in element.
	 * @param element
	 * @return <code>true</code> iff element is marked as inferred
	 */
	public static boolean validateInferredTags(Element element) {
		if ("true".equals(JarToUML.getAnnotationValue(element, "inferred"))) {
			// if this element is inferred, it cannot have any children marked as inferred
			boolean someChildrenInferred = false;
			for (Element child : element.getOwnedElements()) {
				someChildrenInferred |= validateInferredTags(child);
			}
			Assert.assertFalse(
					String.format("Some children of %s marked as inferred, while self is already marked as inferred", toString(element)),
					someChildrenInferred);
			return true;
		} else {
			// if this element is not inferred, it cannot have all children marked as inferred
			boolean allChildrenInferred = true;
			boolean hasChildren = false;
			for (Element child : element.getOwnedElements()) {
				hasChildren = true;
				allChildrenInferred &= validateInferredTags(child);
			}
			allChildrenInferred = allChildrenInferred && hasChildren;
			Assert.assertFalse(
					String.format("All children of %s marked as inferred, while self not inferred", toString(element)),
					allChildrenInferred);
			return false;
		}
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

	/**
	 * @param clazz
	 * @return the parsed test class
	 * @throws ClassFormatException
	 * @throws IOException
	 */
	protected JavaClass getTestClass(Class<?> clazz) throws ClassFormatException, IOException {
		final ClassParser parser = new ClassParser(
				getClassContents(clazz), classFilePath(clazz));
		return parser.parse();
	}

}