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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
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

import be.ac.vub.jar2uml.FindContainedClassifierSwitch;
import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.JarToUMLResources;
import be.ac.vub.jar2uml.MarkInferredClassifiers;
import be.ac.vub.jar2uml.ParseClasses;

/**
 * Test class for {@link JarToUML}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public final class JarToUMLTest extends J2UTestCase {

	/**
	 * Creates a new {@link JarToUMLTest}.
	 * @param name
	 */
	public JarToUMLTest(String name) {
		super(name);
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
		IProject project = getProject(javatestProject);
		assertNotNull(JarToUML.getJavaProject(project.getFullPath()));
	}

	/**
	 * Test method for {@link be.ac.vub.jar2uml.JarToUML#findJavaProjectReferences(org.eclipse.jdt.core.IJavaProject, java.util.Set)}.
	 */
	public void testFindJavaProjectReferences() {
		try {
			//
			// Retrieve Java project
			//
			IProject project = getProject(javatestProject);
			IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
			//
			// Retrieve another Java project and add it to the classpath of the first project
			//
			IProject projectref = getProject(javatestReferredProject);
			IClasspathEntry[] cp = jproject.getResolvedClasspath(true);
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			for (IClasspathEntry entry : cp) {
				entries.add(entry);
			}
			entries.add(JavaCore.newProjectEntry(projectref.getFullPath()));
			jproject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			JarToUML.logger.info("Java project classpath entries: " + entries);
			IJavaProject jprojectref = JarToUML.getJavaProject(projectref.getFullPath());
			//
			// Find references of the first project
			//
			Set<IJavaProject> refs = new HashSet<IJavaProject>();
			JarToUML.findJavaProjectReferences(jproject, refs);
			JarToUML.logger.info("Java project references: " + refs);
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
	 * Test method for {@link be.ac.vub.jar2uml.ParseClasses#findClassFilesIn(org.eclipse.core.resources.IContainer, java.util.List)}.
	 */
	public void testFindClassFilesIn() {
		try {
			//
			// Retrieve Java project
			//
			IProject project = getProject(javatestProject);
			IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
			//
			// Copy "JarToUMLTest.class" into Java project
			//
			IPath outPath = jproject.getOutputLocation();
			JarToUML.logger.info("class file path: " + outPath);
			IPath classFilePath = outPath.append(thisClassFile);
			IFile classFile = ResourcesPlugin.getWorkspace().getRoot().getFile(classFilePath);
			createPath((IFolder) classFile.getParent());
			InputStream input = JarToUMLTest.class.getResourceAsStream("JarToUMLTest.class");
			classFile.create(input, true, null);
			JarToUML.logger.info("created file: " + classFile);
			//
			// Find the copied class file
			//
			List<IFile> cfs = new ArrayList<IFile>();
			ParseClasses.findClassFilesIn(project, cfs);
			JarToUML.logger.info("Class files in project: " + cfs);
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
		JarToUML.logger.info("Loading UML model from: " + pkServletDepsUri);
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
		Collection<Classifier> derived = MarkInferredClassifiers.findDerivedClassifiers(javaLangString);
		JarToUML.logger.info("Found derived classifiers: " + derived);
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
		Model root = loadModelFromUri(pkServletDepsUri);
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
		JarToUML.logger.info("Loading UML model from: " + pkServletDepsUri);
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
		JarToUML.logger.info("Name list: " + names);
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
		JarToUML.logger.info("Loading UML model from: " + pkServletDepsUri);
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
		JarToUML.logger.info("Found annotations: " + javaLangString.getEAnnotations());
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
		JarToUML.logger.info("Loading UML model from: " + pkServletDepsUri);
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
		JarToUML.logger.info("Found annotations: " + javaLangString.getEAnnotations());
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
		IProject project = getProject(javatestProject);
		IJavaProject jproject = JarToUML.getJavaProject(project.getFullPath());
		IProject projectref = getProject(javatestReferredProject);
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
			// Create ambienttalk jars in Java test project
			//
			IFile atFile = copyFileToTestProject(atJar);
			IFile antlrFile = copyFileToTestProject(antlrJar);
			IFile getoptFile = copyFileToTestProject(getoptJar);
			//
			// Create platformkit servlet war in Java test project
			//
			IFile pksFile = copyFileToTestProject(pkServletWar);
			//
			// test run on Java test project
			//
			testRunProject(false);
			testRunProject(true);
			//
			// test run on ambienttalk jars
			//
			Model atModel = testRunJar(false, new IFile[]{atFile}, new IFile[]{antlrFile,getoptFile});
			Model atRefModel = loadModelFromUri(atModelUri);
			assertEquals(atModel.eResource(), atRefModel.eResource());
			Model atDepsModel = testRunJar(true, new IFile[]{atFile}, new IFile[]{antlrFile,getoptFile});
			Model atRefDepsModel = loadModelFromUri(atDepsModelUri);
			JarToUML.logger.info(atDepsModel.eResource().getContents().toString());
			JarToUML.logger.info(atRefDepsModel.eResource().getContents().toString());
			assertEquals(atDepsModel.eResource(), atRefDepsModel.eResource());
			//
			// test run on platformkit servlet war
			//
			Model pksDepsModel = testRunJar(true, new IFile[]{pksFile}, new IFile[]{});
			Model pksRefDepsModel = loadModelFromUri(pkServletDepsUri);
			JarToUML.logger.info(pksDepsModel.eResource().getContents().toString());
			JarToUML.logger.info(pksRefDepsModel.eResource().getContents().toString());
			assertEquals(pksDepsModel.eResource(), pksRefDepsModel.eResource());
		} catch (CoreException e) {
			handle(e);
		} catch (IOException e) {
			handle(e);
		} catch (InterruptedException e) {
			handle(e);
		}
	}

	/**
	 * Test run on Java test project.
	 * @param depsOnly
	 * @return The generated model.
	 * @throws JavaModelException
	 * @throws IOException
	 */
	private Model testRunProject(boolean depsOnly) throws JavaModelException, IOException {
		IProject project = getProject(javatestProject);
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
		return model;
	}

	/**
	 * Test run on jar files.
	 * @param depsOnly
	 * @param jarFiles
	 * @param cpJarFiles
	 * @return The generated model.
	 * @throws IOException
	 */
	private Model testRunJar(boolean depsOnly, IFile[] jarFiles, IFile[] cpJarFiles) throws IOException {
		JarToUML jar2uml = new JarToUML();
		for (IFile file : jarFiles) {
			jar2uml.addJar(jarFile(file));
		}
		for (IFile file : cpJarFiles) {
			jar2uml.addCpJar(jarFile(file));
		}
		jar2uml.setIncludeFeatures(true);
		jar2uml.setIncludeInstructionReferences(true);
		jar2uml.setDependenciesOnly(depsOnly);
		String outFileName = jarFiles[0].getFullPath().removeFileExtension().lastSegment();
		if (depsOnly) {
			outFileName += ".deps";
		}
		jar2uml.setOutputFile("platform:/resource/" + javatestProject + "/" + outFileName + ".uml");
		jar2uml.setOutputModelName(outFileName);
		assertFalse(jar2uml.isRunComplete());
		jar2uml.run();
		assertTrue(jar2uml.isRunComplete());
		Model model = jar2uml.getModel();
		validateModel(model);
		model.eResource().save(Collections.EMPTY_MAP);
		return model;
	}

}
