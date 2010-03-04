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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.osgi.framework.Bundle;

import be.ac.vub.jar2uml.FindContainedClassifierSwitch;
import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.JarToUMLResources;

/**
 * Test class for {@link JarToUML}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public final class JarToUMLTest extends TestCase {

	private static final Bundle bundle = Platform.getBundle("be.ac.vub.jar2uml.test");

	private static final String javatestProject = "javatest";
	private static final String javatestReferredProject = "javatestref";
	private static final String instantmessengerJar = "resources/instantmessenger.jar";
	private static final String thisClassFile = "be/ac/vub/jar2uml/test/JarToUMLTest.class";
	private static final String pkServletDepsUri = "platform:/plugin/be.ac.vub.jar2uml.test/resources/platformkitservlet.deps.uml";
	private static final String imJar = "im.jar";
	private static final String pkServletWar = "resources/platformkitservlet.war";
	private static final String pksWar = "pks.war";

	private static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	/**
	 * Creates a new {@link JarToUMLTest}.
	 * @param name
	 */
	public JarToUMLTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		//
		// Refresh workspace
		//
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUMLResources#getString(java.lang.String)}.
	 */
	public void testGetString() {
		String usage = JarToUMLResources.getString("JarToUML.usage");
		assertNotNull(usage);
		assertNotSame("JarToUML.usage", usage);
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUMLResources#getResourcebundle()}.
	 */
	public void testGetResourcebundle() {
		assertNotNull(JarToUMLResources.getResourcebundle());
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#getJavaProject(org.eclipse.core.runtime.IPath)}.
	 */
	public void testGetJavaProject() {
		try {
			//
			// Create a Java project and find it
			//
			IProject project = createJavaProject(javatestProject);
			assertNotNull(JarToUML.getJavaProject(project.getFullPath()));
		} catch (CoreException e) {
			handle(e);
		}
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#findJavaProjectReferences(org.eclipse.jdt.core.IJavaProject, java.util.Set)}.
	 */
	public void testFindJavaProjectReferences() {
		try {
			//
			// Retrieve Java project
			//
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
			IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
			//
			// Create another Java project and add it to the classpath of the first project
			//
			IProject projectref = createJavaProject(javatestReferredProject);
			IClasspathEntry[] cp = jproject.getResolvedClasspath(true);
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			for (IClasspathEntry entry : cp) {
				entries.add(entry);
			}
			entries.add(JavaCore.newProjectEntry(projectref.getFullPath()));
			jproject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			logger.info("Java project classpath entries: " + entries);
			IJavaProject jprojectref = JarToUML.getJavaProject(projectref.getFullPath());
			//
			// Find references of the first project
			//
			Set<IJavaProject> refs = new HashSet<IJavaProject>();
			JarToUML.findJavaProjectReferences(jproject, refs);
			logger.info("Java project references: " + refs);
			assertFalse(refs.isEmpty());
			assertTrue(refs.contains(jprojectref));
		} catch (CoreException e) {
			handle(e);
		}
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#createResourceSet()}.
	 */
	public void testCreateResourceSet() {
		ResourceSet resourceSet = JarToUML.createResourceSet();
		assertNotNull(resourceSet);
		Object factory = resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().get(UMLResource.FILE_EXTENSION);
		assertSame(UMLResource.Factory.INSTANCE, factory);
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#findClassFilesIn(org.eclipse.core.resources.IContainer, java.util.List)}.
	 */
	public void testFindClassFilesIn() {
		try {
			//
			// Retrieve Java project
			//
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
			IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
			//
			// Copy "JarToUMLTest.class" into Java project
			//
			IPath outPath = jproject.getOutputLocation();
			logger.info("class file path: " + outPath);
			IPath classFilePath = outPath.append(thisClassFile);
			IFile classFile = ResourcesPlugin.getWorkspace().getRoot().getFile(classFilePath);
			createPath((IFolder) classFile.getParent());
			InputStream input = JarToUMLTest.class.getResourceAsStream("JarToUMLTest.class");
			classFile.create(input, true, null);
			logger.info("created file: " + classFile);
			//
			// Find the copied class file
			//
			List<IFile> cfs = new ArrayList<IFile>();
			JarToUML.findClassFilesIn(project, cfs);
			logger.info("Class files in project: " + cfs);
			assertFalse(cfs.isEmpty());
			assertTrue(cfs.contains(classFile));
		} catch (JavaModelException e) {
			handle(e);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#findDerivedClassifiers(org.eclipse.uml2.uml.Classifier)}.
	 */
	public void testFindDerivedClassifiers() {
		//
		// Load a UML model with derived classifiers, and find Model object
		//
		logger.info("Loading UML model from: " + pkServletDepsUri);
		Resource res = JarToUML.createResourceSet().getResource(URI.createURI(pkServletDepsUri), true);
		Model root = findModel(res);
		//
		// Find java.lang.String
		//
		FindContainedClassifierSwitch find = new FindContainedClassifierSwitch();
		Classifier javaLangString = find.findClassifier(root, "java.lang.String", null);
		assertNotNull(javaLangString);
		//
		// Find derived classifiers
		//
		Collection<Classifier> derived = JarToUML.findDerivedClassifiers(javaLangString);
		logger.info("Found derived classifiers: " + derived);
		assertFalse(derived.isEmpty());
		assertFalse(derived.contains(javaLangString));
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#isNamedClass(org.apache.bcel.classfile.JavaClass)}.
	 */
	public void testIsNamedClass() {
		try {
			//
			// Test this class
			//
			URL thisClassUrl = bundle.getResource(thisClassFile);
			ClassParser parser = new ClassParser(thisClassUrl.openStream(), thisClassFile);
			JavaClass javaClass = parser.parse();
			assertTrue(JarToUML.isNamedClass(javaClass));
			//
			// Test an anonymous nested class
			//
			String anoClassFile = thisClassFile.replace(".class", "$1.class");
			URL anoClassUrl = bundle.getResource(anoClassFile);
			parser = new ClassParser(anoClassUrl.openStream(), anoClassFile);
			javaClass = parser.parse();
			assertFalse(JarToUML.isNamedClass(javaClass));
		} catch (IOException e) {
			handle(e);
		}
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#isPreverified(org.apache.bcel.classfile.Code)}.
	 */
	public void testIsPreverifiedCode() {
		try {
			//
			// Create "im.jar" in Java test project
			//
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
			URL imJarUrl = bundle.getResource(instantmessengerJar);
			IFile file = project.getFile(imJar);
			logger.info("Creating jar file: " + file);
			file.create(imJarUrl.openStream(), true, null);
			//
			// run with preverified
			//
			JarToUML jar2uml = new JarToUML();
			JarFile imJarFile = new JarFile(file.getLocation().toFile());
			assertNotNull(imJarFile);
			jar2uml.addJar(imJarFile);
			jar2uml.run();
			assertTrue(jar2uml.isPreverified());
			//
			// run without preverified
			//
			jar2uml = new JarToUML();
			jar2uml.addPath(project);
			jar2uml.run();
			assertFalse(jar2uml.isPreverified());
		} catch (IOException e) {
			handle(e);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#toUMLVisibility(org.apache.bcel.classfile.AccessFlags)}.
	 */
	@SuppressWarnings("serial")
	public void testToUMLVisibility() {
		assertEquals(VisibilityKind.PUBLIC_LITERAL, JarToUML.toUMLVisibility(new AccessFlags(Constants.ACC_PUBLIC) { } ));
		assertEquals(VisibilityKind.PROTECTED_LITERAL, JarToUML.toUMLVisibility(new AccessFlags(Constants.ACC_PROTECTED) { } ));
		assertEquals(VisibilityKind.PRIVATE_LITERAL, JarToUML.toUMLVisibility(new AccessFlags(Constants.ACC_PRIVATE) { } ));
		assertEquals(VisibilityKind.PRIVATE_LITERAL, JarToUML.toUMLVisibility(new AccessFlags() { } ));
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#qualifiedName(org.eclipse.uml2.uml.NamedElement)}.
	 */
	public void testQualifiedName() {
		//
		// Load a UML model with derived classifiers, and find Model object
		//
		logger.info("Loading UML model from: " + pkServletDepsUri);
		Resource res = JarToUML.createResourceSet().getResource(URI.createURI(pkServletDepsUri), true);
		Model root = findModel(res);
		//
		// Find java.lang.String
		//
		FindContainedClassifierSwitch find = new FindContainedClassifierSwitch();
		Classifier javaLangString = find.findClassifier(root, "java.lang.String", null);
		assertNotNull(javaLangString);
		//
		// Test qualified names
		//
		assertEquals("platformkitservlet.deps", JarToUML.qualifiedName(root));
		assertEquals("java::lang::String", JarToUML.qualifiedName(javaLangString));
		assertEquals("java::lang", JarToUML.qualifiedName(javaLangString.getPackage()));
		assertEquals("java", JarToUML.qualifiedName(javaLangString.getPackage().getNestingPackage()));
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#getNameList(java.util.Collection)}.
	 */
	public void testGetNameList() {
		//
		// Load a UML model with derived classifiers, and find Model object
		//
		logger.info("Loading UML model from: " + pkServletDepsUri);
		Resource res = JarToUML.createResourceSet().getResource(URI.createURI(pkServletDepsUri), true);
		Model root = findModel(res);
		//
		// Find java.lang.String, java.lang.Object
		//
		FindContainedClassifierSwitch find = new FindContainedClassifierSwitch();
		Classifier javaLangString = find.findClassifier(root, "java.lang.String", null);
		assertNotNull(javaLangString);
		Classifier javaLangObject = find.findClassifier(root, "java.lang.Object", null);
		assertNotNull(javaLangObject);
		//
		// Test name list
		//
		List<NamedElement> elements = new ArrayList<NamedElement>();
		elements.add(root);
		elements.add(javaLangString);
		elements.add(javaLangObject);
		List<String> names = JarToUML.getNameList(elements);
		logger.info("Name list: " + names);
		assertFalse(names.isEmpty());
		assertTrue(names.size() == 3);
		assertEquals("platformkitservlet.deps", names.get(0));
		assertEquals("java::lang::String", names.get(1));
		assertEquals("java::lang::Object", names.get(2));
	}

	/**
	 * Test method for {@link JarToUML#annotate(org.eclipse.uml2.uml.Element, String, String)}.
	 */
	public void testAnnotate() {
		//
		// Load a UML model, and find Model object
		//
		logger.info("Loading UML model from: " + pkServletDepsUri);
		Resource res = JarToUML.createResourceSet().getResource(URI.createURI(pkServletDepsUri), true);
		Model root = findModel(res);
		//
		// Find java.lang.String
		//
		FindContainedClassifierSwitch find = new FindContainedClassifierSwitch();
		Classifier javaLangString = find.findClassifier(root, "java.lang.String", null);
		assertNotNull(javaLangString);
		//
		// Annotate
		//
		JarToUML.annotate(javaLangString, "test", "test");
		logger.info("Found annotations: " + javaLangString.getEAnnotations());
		EAnnotation ann = javaLangString.getEAnnotation(JarToUML.EANNOTATION);
		assertNotNull(ann);
		assertEquals("test", ann.getDetails().get("test"));
	}

	/**
	 * Test method for {@link JarToUML#deannotate(org.eclipse.uml2.uml.Element, String)}.
	 */
	public void testDeannotate() {
		//
		// Load a UML model, and find Model object
		//
		logger.info("Loading UML model from: " + pkServletDepsUri);
		Resource res = JarToUML.createResourceSet().getResource(URI.createURI(pkServletDepsUri), true);
		Model root = findModel(res);
		//
		// Find java.lang.String
		//
		FindContainedClassifierSwitch find = new FindContainedClassifierSwitch();
		Classifier javaLangString = find.findClassifier(root, "java.lang.String", null);
		assertNotNull(javaLangString);
		//
		// Annotate
		//
		JarToUML.annotate(javaLangString, "test", "test");
		JarToUML.annotate(javaLangString, "test2", "test2");
		//
		// Deannotate test
		//
		JarToUML.deannotate(javaLangString, "test");
		logger.info("Found annotations: " + javaLangString.getEAnnotations());
		EAnnotation ann = javaLangString.getEAnnotation(JarToUML.EANNOTATION);
		assertNotNull(ann);
		assertFalse(ann.getDetails().containsKey("test"));
		//
		// Deannotate test2
		//
		JarToUML.deannotate(javaLangString, "test2");
		ann = javaLangString.getEAnnotation(JarToUML.EANNOTATION);
		assertNull(ann);
	}

	/**
	 * Test method for {@link JarToUML#addPaths(IJavaProject, boolean)}.
	 */
	public void testAddPaths() {
		//
		// Retrieve Java projects
		//
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
		IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
		IProject projectref = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestReferredProject);
		IJavaProject jprojectref = JarToUML.getJavaProject(projectref.getFullPath());
		try {
			//
			// Retrieve output locations for Java projects
			//
			IResource projectOutput = ResourcesPlugin.getWorkspace().getRoot().findMember(
					jproject.getOutputLocation());
			IResource projectrefOutput = ResourcesPlugin.getWorkspace().getRoot().findMember(
					jprojectref.getOutputLocation());
			//
			// Test without workspace references
			//
			JarToUML jar2uml = new JarToUML();
			jar2uml.addPaths(jproject, false);
			List<IContainer> paths = jar2uml.getPaths();
			assertFalse(paths.isEmpty());
			assertTrue(paths.contains(projectOutput));
			assertFalse(paths.contains(projectrefOutput));
			assertTrue(jar2uml.getJars().isEmpty());
			assertTrue(jar2uml.getCpJars().isEmpty());
			assertTrue(jar2uml.getCpPaths().isEmpty());
			//
			// Test with workspace references
			//
			jar2uml = new JarToUML();
			jar2uml.addPaths(jproject, true);
			paths = jar2uml.getPaths();
			assertFalse(paths.isEmpty());
			assertTrue(paths.contains(projectOutput));
			assertTrue(paths.contains(projectrefOutput));
			assertTrue(jar2uml.getJars().isEmpty());
			assertTrue(jar2uml.getCpJars().isEmpty());
			assertTrue(jar2uml.getCpPaths().isEmpty());
		} catch (JavaModelException e) {
			handle(e);
		} catch (IOException e) {
			handle(e);
		}
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#run()}.
	 */
	public void testRun() {
		try {
			//
			// Create "pks.war" in Java test project
			//
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
			URL pksJarUrl = bundle.getResource(pkServletWar);
			IFile file = project.getFile(pksWar);
			logger.info("Creating jar file: " + file);
			file.create(pksJarUrl.openStream(), true, null);
			//
			// test runs
			//
			testRunProject(false);
			testRunProject(true);
			testRunJar(false);
			testRunJar(true);
		} catch (CoreException e) {
			handle(e);
		} catch (IOException e) {
			handle(e);
		}
	}

	private void testRunProject(boolean depsOnly) throws JavaModelException, IOException {
		//
		// test run on Java test project
		//
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
		IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
		JarToUML jar2uml = new JarToUML();
		jar2uml.addPaths(jproject, true);
		jar2uml.setIncludeFeatures(true);
		jar2uml.setIncludeInstructionReferences(true);
		jar2uml.setDependenciesOnly(depsOnly);
		jar2uml.setOutputFile("platform:/resource/" + javatestProject + "/" + javatestProject + ".uml");
		jar2uml.setOutputModelName(javatestProject);
		assertFalse(jar2uml.isRunComplete());
		jar2uml.run();
		assertTrue(jar2uml.isRunComplete());
		Model model = jar2uml.getModel();
		validateModel(model);
		model.eResource().save(Collections.EMPTY_MAP);
	}

	private void testRunJar(boolean depsOnly) throws IOException {
		//
		// fetch jar file
		//
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(javatestProject);
		IFile file = project.getFile(pksWar);
		//
		// test run on "pks.war"
		//
		JarToUML jar2uml = new JarToUML();
		JarFile pksJarFile = new JarFile(file.getLocation().toFile());
		assertNotNull(pksJarFile);
		jar2uml.addJar(pksJarFile);
		jar2uml.setIncludeFeatures(true);
		jar2uml.setIncludeInstructionReferences(true);
		jar2uml.setDependenciesOnly(depsOnly);
		jar2uml.setOutputFile("platform:/resource/" + javatestProject + "/" + pksWar + ".uml");
		jar2uml.setOutputModelName(pksWar);
		assertFalse(jar2uml.isRunComplete());
		jar2uml.run();
		assertTrue(jar2uml.isRunComplete());
		Model model = jar2uml.getModel();
		validateModel(model);
		model.eResource().save(Collections.EMPTY_MAP);
	}

	/**
	 * @param name
	 * @return A new project with the Java project nature.
	 * @throws CoreException
	 */
	private IProject createJavaProject(String name) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		project.create(null);
		project.open(null);
		IProjectDescription description = project.getDescription();
		String natures[] = new String[] { "org.eclipse.jdt.core.javanature" };
		description.setNatureIds(natures);
		project.setDescription(description, null);
		return project;
	}

	/**
	 * Creates path and its parents.
	 * @param path
	 * @throws CoreException
	 */
	private void createPath(IFolder path) throws CoreException {
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
	private static void handle(Exception e) {
		logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		fail(e.getLocalizedMessage());
	}

	/**
	 * @param res
	 * @return The (first) root Model in res, if any, <code>null</code> otherwise.
	 */
	private static Model findModel(Resource res) {
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
	private void validateModel(Model model) {
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
		logger.info("Model diagnostics: " + diagnostics.getMessage());
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
	}

}
