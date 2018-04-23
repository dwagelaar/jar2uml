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
package org.eclipselabs.jar2uml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.JavaClass;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipselabs.jar2uml.ui.JarToUMLPlugin;

/**
 * Main class for the jar to UML converter. Start conversion by invoking {@link #run()}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public final class JarToUML extends JarToUMLRunnable {

	public static final String EANNOTATION = "Jar2UML"; //$NON-NLS-1$

	public static final String MAJOR_BYTECODE_FORMAT_VERSION = "majorBytecodeFormatVersion";
	public static final String MINOR_BYTECODE_FORMAT_VERSION = "minorBytecodeFormatVersion";
	public static final String PREVERIFIED = "preverified";

	private static final int WORK_CREATE_MODEL = 1;
	private static final int WORK_PARSE_CLASSES = 100;
	private static final int WORK_ADD_CLASSIFIERS = 200;
	private static final int WORK_ADD_PROPERTIES = 400;
	private static final int WORK_INFERRED_TAGS = 1;
	private static final int WORK_REMOVE_EMPTY = 1;
	private static final int WORK_ADD_METADATA = 1;
	private static final int WORK_TOTAL = WORK_CREATE_MODEL + WORK_PARSE_CLASSES + WORK_ADD_CLASSIFIERS + WORK_ADD_PROPERTIES + WORK_INFERRED_TAGS + WORK_REMOVE_EMPTY + WORK_ADD_METADATA;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final JarToUML jarToUML = new JarToUML();
			jarToUML.setFilter(new JavaAPIFilter());
			jarToUML.addJar(new JarFile(args[0]));
			jarToUML.setOutputFile(args[1]);
			jarToUML.setOutputModelName(args[2]);
			jarToUML.run();
			if (jarToUML.isRunComplete()) {
				jarToUML.saveModel();
			}
		} catch (final Exception e) {
			JarToUMLResources.report(e);
			JarToUMLResources.logger.severe(JarToUMLResources.getString("JarToUML.usage")); //$NON-NLS-1$
		}
	}

	/**
	 * @param path
	 * @return The Java project for the given path, or null
	 */
	public static IJavaProject getJavaProject(IPath path) {
		final IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		final IProject project = resource.getProject();
		return model.getJavaProject(project.getName());
	}

	/**
	 * Retrieves the project references for javaProject and stores them in refs
	 * @param javaProject
	 * @param refs the project references for javaProject
	 */
	public static void findJavaProjectReferences(IJavaProject javaProject, Set<IJavaProject> refs) {
		try {
			for (final IClasspathEntry cpe : javaProject.getResolvedClasspath(true)) {
				IPath cpePath;
				switch (cpe.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					cpePath = cpe.getPath();
					final IJavaProject ref = getJavaProject(cpePath);
					refs.add(ref);
					break;
				}
			}
		} catch (final JavaModelException e) {
			JarToUMLPlugin.getPlugin().report(e);
		}
	}

	/**
	 * @return A new ResourceSet with support for UML models outside Eclipse
	 */
	public static ResourceSet createResourceSet() {
		final ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI,
				UMLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().
		put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		return resourceSet;
	}

	/**
	 * @param javaClass
	 * @return True if the name of javaClass does not parse as an Integer.
	 */
	public static boolean isNamedClass(JavaClass javaClass) {
		final String javaClassName = javaClass.getClassName();
		final String leafName = javaClassName.substring(javaClassName.lastIndexOf('$') + 1);
		try {
			Integer.parseInt(leafName);
			return false;
		} catch (final NumberFormatException e) {
			//everything allright, not an anonymous class
		}
		return true;
	}

	/**
	 * @param flags
	 * @return The UML representation of flags.
	 */
	public static VisibilityKind toUMLVisibility(AccessFlags flags) {
		if (flags.isPublic()) {
			return VisibilityKind.PUBLIC_LITERAL;
		} else if (flags.isProtected()) {
			return VisibilityKind.PROTECTED_LITERAL;
		} else if (flags.isPrivate()) {
			return VisibilityKind.PRIVATE_LITERAL;
		} else {
			return VisibilityKind.PACKAGE_LITERAL;
		}
	}

	/**
	 * @param element
	 * @return The qualified name of element
	 */
	public static String qualifiedName(final NamedElement element) {
		final String qName = element.getQualifiedName();
		if (qName == null) {
			return "<unnamed>";
		}
		final int sepIndex = qName.indexOf("::");
		if (sepIndex > 0) {
			return qName.substring(sepIndex + 2);
		} else {
			return qName;
		}
	}

	/**
	 * @param elements
	 * @return A {@link List} of the qualified names of elements.
	 */
	public static List<String> getNameList(Collection<? extends NamedElement> elements) {
		final List<String> list = new ArrayList<String>();
		for (final NamedElement e : elements) {
			list.add(qualifiedName(e));
		}
		return list;
	}

	/**
	 * Creates a key-value entry under a "Jar2UML" {@link EAnnotation}.
	 * @param element
	 * @param key
	 * @param value
	 */
	public static final void annotate(final Element element, final String key, final String value) {
		EAnnotation ann = element.getEAnnotation(EANNOTATION);
		if (ann == null) {
			ann = element.createEAnnotation(EANNOTATION);
		}
		final EMap<String, String> details = ann.getDetails();
		details.put(key, value);
	}

	/**
	 * Removes a key-value entry under a "Jar2UML" {@link EAnnotation}.
	 * Removes the "Jar2UML" annotation if no key-value pairs left.
	 * @param element
	 * @param key
	 */
	public static final void deannotate(final Element element, final String key) {
		final EAnnotation ann = element.getEAnnotation(EANNOTATION);
		if (ann != null) {
			final EMap<String, String> details = ann.getDetails();
			if (details.containsKey(key)) {
				details.removeKey(key);
			}
			if (details.isEmpty()) {
				element.getEAnnotations().remove(ann);
			}
		}
	}

	/**
	 * @param element
	 * @param key
	 * @return The value of the element annotation with the given key, or <code>null</code>.
	 */
	public static final String getAnnotationValue(final Element element, final String key) {
		final EAnnotation ann = element.getEAnnotation(EANNOTATION);
		if (ann != null) {
			final EMap<String, String> details = ann.getDetails();
			if (details.containsKey(key)) {
				return details.get(key);
			}
		}
		return null;
	}

	/**
	 * Finds the {@link Comment} owned by element that contains the comment string.
	 * The {@link Comment} is created if necessary.
	 * @param element
	 * @param comment
	 * @return the {@link Comment} owned by element containing the comment string
	 */
	public static Comment getOwnedComment(Element element, String comment) {
		for (final Comment c : element.getOwnedComments()) {
			if (comment.equals(c.getBody())) {
				return c;
			}
		}
		final Comment c = element.createOwnedComment();
		c.setBody(comment);
		return c;
	}

	private final FindReferredTypesSwitch findReferredTypes = new FindReferredTypesSwitch();

	private Model model;
	private final List<JarFile> jars = new ArrayList<JarFile>();
	private final List<IContainer> paths = new ArrayList<IContainer>();
	private final List<JarFile> cpJars = new ArrayList<JarFile>();
	private final List<IContainer> cpPaths = new ArrayList<IContainer>();
	private final List<JavaClass> parsedClasses = new ArrayList<JavaClass>();
	private final List<JavaClass> parsedCpClasses = new ArrayList<JavaClass>();
	private Filter filter;
	private String outputFile = "api.uml"; //$NON-NLS-1$
	private String outputModelName = "api"; //$NON-NLS-1$
	private boolean includeInstructionReferences = false;
	private boolean includeFeatures = true;
	private boolean dependenciesOnly = false;
	private boolean includeComment = true;
	private boolean updateExistingFile;

	/**
	 * Performs the actual jar to UML conversion.
	 */
	@Override
	protected void runWithMonitor(final IProgressMonitor monitor) {
		try {
			assert getOutputFile() != null : JarToUMLResources.getString("JarToUML.nullOutputFile"); //$NON-NLS-1$
			beginTask(monitor, String.format(
					JarToUMLResources.getString("JarToUML.startingFor"),
					getOutputFile()), WORK_TOTAL);  //$NON-NLS-1$
			//
			// 1
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.creatingUML")); //$NON-NLS-1$
			if (getModel() == null) {
				final ResourceSet resourceSet = createResourceSet();
				final Resource res;
				if (isUpdateExistingFile()) {
					res = resourceSet.getResource(URI.createURI(getOutputFile()), true);
					if (res == null) {
						throw new IOException(String.format(JarToUMLResources.getString("JarToUML.nullRes"), getOutputFile())); //$NON-NLS-1$
					}
					setModel((Model) UML2Util.load(resourceSet, URI.createURI(getOutputFile()), UMLPackage.eINSTANCE.getModel()));
				} else {
					res = resourceSet.createResource(URI.createURI(getOutputFile()));
					if (res == null) {
						throw new IOException(String.format(JarToUMLResources.getString("JarToUML.nullRes"), getOutputFile())); //$NON-NLS-1$
					}
				}
				if (getModel() == null) {
					final Model newModel = UMLFactory.eINSTANCE.createModel();
					res.getContents().add(newModel);
					newModel.setName(getOutputModelName());
					setModel(newModel);
				}
			}
			final Model model = getModel();
			assert model != null;
			worked(monitor, JarToUMLResources.getString("JarToUML.createdUML")); //$NON-NLS-1$
			//
			// 2
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.parsing")); //$NON-NLS-1$
			final Filter filter = getFilter();
			final ParseClasses parseClasses = new ParseClasses(filter, monitor, WORK_PARSE_CLASSES);
			final List<JavaClass> parsedClasses = getParsedClasses();
			final List<JavaClass> parsedCpClasses = getParsedCpClasses();
			parseClasses.beginTask(
					JarToUMLResources.getString("JarToUML.parsing"),
					ParseClasses.getJarWork(getJars()) + ParseClasses.getJarWork(getCpJars()) + ParseClasses.getPathWork(getPaths()) + ParseClasses.getPathWork(getCpPaths())); //$NON-NLS-1$
			List<JarFile> jars = getJars();
			List<IContainer> paths = getPaths();
			List<JarFile> cpJars = getCpJars();
			List<IContainer> cpPaths = getCpPaths();
			//promote classpath entries to main entries if no main entries exist
			if (jars.isEmpty() && paths.isEmpty()) {
				jars = cpJars;
				paths = cpPaths;
				cpJars = Collections.emptyList();
				cpPaths = Collections.emptyList();
			}
			for (final JarFile jar : jars) {
				parseClasses.parseClasses(jar, parsedClasses, parsedCpClasses);
				checkCancelled(monitor);
			}
			for (final IContainer path : paths) {
				parseClasses.parseClasses(path, parsedClasses);
				checkCancelled(monitor);
			}
			for (final JarFile jar : cpJars) {
				parseClasses.parseClasses(jar, parsedCpClasses, parsedCpClasses);
				checkCancelled(monitor);
			}
			for (final IContainer path : cpPaths) {
				parseClasses.parseClasses(path, parsedCpClasses);
				checkCancelled(monitor);
			}
			worked(null, JarToUMLResources.getString("JarToUML.parsed")); //$NON-NLS-1$
			//
			// 3
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.addingClassifiers")); //$NON-NLS-1$
			final boolean includeFeatures = isIncludeFeatures();
			final boolean includeInstructionReferences = isIncludeInstructionReferences();
			final AddClassifiers addClassifiers = new AddClassifiers(filter, monitor, WORK_ADD_CLASSIFIERS,	model, includeFeatures,	includeInstructionReferences);
			addClassifiers.beginTask(
					JarToUMLResources.getString("JarToUML.addingClassifiers"),
					parsedClasses.size() + parsedCpClasses.size()); //$NON-NLS-1$
			addClassifiers.addAllClassifiers(parsedClasses);
			final List<JavaClass> skippedClasses = addClassifiers.addClassifiersClosure(parsedCpClasses);
			parsedCpClasses.removeAll(skippedClasses);
			worked(null, JarToUMLResources.getString("JarToUML.addedClassifiers")); //$NON-NLS-1$
			//
			// 4
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.addingProperties")); //$NON-NLS-1$
			final AddProperties addProperties = new AddProperties(filter, monitor, WORK_ADD_PROPERTIES, model, includeFeatures, includeInstructionReferences);
			addProperties.beginTask(
					JarToUMLResources.getString("JarToUML.addingProperties"),
					parsedClasses.size() + parsedCpClasses.size()); //$NON-NLS-1$
			addProperties.addAllProperties(parsedClasses);
			addProperties.addAllProperties(parsedCpClasses);
			worked(null, JarToUMLResources.getString("JarToUML.addedProperties")); //$NON-NLS-1$
			//
			// 5
			//
			final MarkInferredClassifiers markInferredClassifiers = new MarkInferredClassifiers(filter,	monitor, WORK_INFERRED_TAGS, model);
			final Set<Classifier> containedClassifiers = markInferredClassifiers.findContainedClassifiers(getParsedClasses());
			containedClassifiers.addAll(markInferredClassifiers.findContainedClassifiers(getParsedCpClasses()));
			final RemoveFromModel removeFromModel = new RemoveFromModel(filter, monitor, WORK_REMOVE_EMPTY, model);
			if (isDependenciesOnly()) {
				subTask(monitor, JarToUMLResources.getString("JarToUML.removingClassifiers")); //$NON-NLS-1$
				final Set<Classifier> inferredClassifiers = markInferredClassifiers.findInferredClassifiers(containedClassifiers);
				final Set<Type> referredTypes = findReferredTypes.findAllReferredTypes(inferredClassifiers);
				final Set<Type> containerTypes = FindReferredTypesSwitch.findContainerTypes(referredTypes);
				// also retain container types of referred types, otherwise we still get dangling refs.
				referredTypes.addAll(containerTypes);
				final Set<Classifier> removeClassifiers = new HashSet<Classifier>(containedClassifiers);
				if (removeClassifiers.removeAll(referredTypes)) {
					containedClassifiers.retainAll(referredTypes);
					JarToUMLResources.logger.warning(String.format(
							JarToUMLResources.getString("JarToUML.cyclicDepsFound"),
							getNameList(containedClassifiers)));
					// Keep referred classifiers, but strip their properties
					removeFromModel.removeAllProperties(containedClassifiers);
				}
				// Remove all classifiers before tagging
				removeFromModel.removeAllClassifiers(removeClassifiers);
				// Tag contained classifiers as "inferred" by the inferred classifiers
				markInferredClassifiers.addAllInferredTags(inferredClassifiers);
				worked(monitor, JarToUMLResources.getString("JarToUML.removedClassifiers")); //$NON-NLS-1$
			} else {
				subTask(monitor, JarToUMLResources.getString("JarToUML.addingInferred")); //$NON-NLS-1$
				markInferredClassifiers.addAllInferredTags(containedClassifiers);
				worked(monitor, JarToUMLResources.getString("JarToUML.addedInferred")); //$NON-NLS-1$
			}
			//
			// 6
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.removingEmpty")); //$NON-NLS-1$
			removeFromModel.removeEmptyPackages(model);
			worked(monitor, JarToUMLResources.getString("JarToUML.removedEmpty")); //$NON-NLS-1$
			//
			// 7
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.addingMetadata")); //$NON-NLS-1$
			if (isIncludeComment()) {
				getOwnedComment(model, String.format(
						JarToUMLResources.getString("JarToUML.generatedBy"),
						JarToUMLPlugin.getPlugin().getBundle().getVersion(),
						getInputList())); //$NON-NLS-1$
			}
			annotate(model, MAJOR_BYTECODE_FORMAT_VERSION, String.valueOf(parseClasses.getMajorFormatVersion())); //$NON-NLS-1$
			annotate(model, MINOR_BYTECODE_FORMAT_VERSION, String.valueOf(parseClasses.getMinorFormatVersion())); //$NON-NLS-1$
			annotate(model, PREVERIFIED, String.valueOf(addProperties.isPreverified())); //$NON-NLS-1$
			worked(monitor, JarToUMLResources.getString("JarToUML.addedMetadata"));
		} catch (final IOException e) {
			throw new JarToUMLException(e);
		} catch (final CoreException e) {
			throw new JarToUMLException(e);
		}
	}

	/**
	 * @return A comma-separated list of all Jar and Path inputs
	 */
	private String getInputList() {
		StringBuffer b = null;
		List<JarFile> jars = getJars();
		List<IContainer> paths = getPaths();
		//promote classpath entries to main entries if no main entries exist
		if (jars.isEmpty() && paths.isEmpty()) {
			jars = getCpJars();
			paths = getCpPaths();
		}
		for (final JarFile jar : jars) {
			if (b == null) {
				b = new StringBuffer();
			} else {
				b.append(", ");
			}
			final String jarName = jar.getName();
			b.append(jarName.substring(jarName.lastIndexOf('/') + 1));
		}
		for (final IContainer path : paths) {
			if (b == null) {
				b = new StringBuffer();
			} else {
				b.append(", ");
			}
			b.append(path.getFullPath());
		}
		if (b != null) {
			return b.toString();
		}
		return "";
	}

	/**
	 * Adds jar to the collection of jars to process.
	 * @param jar
	 */
	public void addJar(JarFile jar) {
		if (!this.jars.contains(jar)) {
			this.jars.add(jar);
		}
	}

	/**
	 * Removes jar from the collection of jars to process.
	 * @param jar
	 */
	public void removeJar(JarFile jar) {
		this.jars.remove(jar);
	}

	/**
	 * Clears the collection of jars to process.
	 */
	public void clearJars() {
		this.jars.clear();
	}

	/**
	 * @return The collection of jars to process.
	 */
	public List<JarFile> getJars() {
		return jars;
	}

	/**
	 * @return The generated UML model.
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Sets the UML model to store generated elements in.
	 * Overrides {@link #setOutputFile(String)} and {@link #setOutputModelName(String)}.
	 * @param model
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * @return The Java element filter to apply.
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Sets the Java element filter to apply.
	 * @param filter
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * @return The output file location (understandable to EMF).
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Sets the output file location (understandable to EMF).
	 * @param outputFile
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @return The name of the UML Model root element.
	 */
	public String getOutputModelName() {
		return outputModelName;
	}

	/**
	 * Sets the name of the UML Model root element.
	 * @param outputModelName
	 */
	public void setOutputModelName(String outputModelName) {
		this.outputModelName = outputModelName;
	}

	/**
	 * @return Whether or not to include Java elements that are only
	 * referred to by bytecode instructions. Defaults to false.
	 */
	public boolean isIncludeInstructionReferences() {
		return includeInstructionReferences;
	}

	/**
	 * Sets whether or not to include Java elements that are only
	 * referred to by bytecode instructions. Defaults to false.
	 * @param includeInstructionReferences
	 */
	public void setIncludeInstructionReferences(boolean includeInstructionReferences) {
		this.includeInstructionReferences = includeInstructionReferences;
	}

	/**
	 * Whether or not to include classifier operations and attributes. Defaults to true.
	 * @return the includeFeatures
	 */
	public boolean isIncludeFeatures() {
		return includeFeatures;
	}

	/**
	 * Whether or not to include classifier operations and attributes. Defaults to true.
	 * @param includeFeatures the includeFeatures to set
	 */
	public void setIncludeFeatures(boolean includeFeatures) {
		this.includeFeatures = includeFeatures;
	}

	/**
	 * If true, includes only the dependencies instead of the contained elements.
	 * @return the dependenciesOnly
	 */
	public boolean isDependenciesOnly() {
		return dependenciesOnly;
	}

	/**
	 * If true, includes only the dependencies instead of the contained elements.
	 * @param dependenciesOnly the dependenciesOnly to set
	 */
	public void setDependenciesOnly(boolean dependenciesOnly) {
		this.dependenciesOnly = dependenciesOnly;
	}

	/**
	 * @return the paths
	 */
	public List<IContainer> getPaths() {
		return paths;
	}

	/**
	 * Empties the list of paths
	 */
	public void clearPaths() {
		this.paths.clear();
	}

	/**
	 * @param path the path to add
	 */
	public void addPath(IContainer path) {
		if (!this.paths.contains(path)) {
			this.paths.add(path);
		}
	}

	/**
	 * @param path the path to remove
	 */
	public void removePath(IContainer path) {
		this.paths.remove(path);
	}

	/**
	 * Adds all relevant class file paths for javaProject
	 * @param javaProject
	 * @param includeWorkspaceReferences Include referenced projects and jar files in workspace
	 * @throws JavaModelException
	 * @throws IOException
	 */
	public void addPaths(IJavaProject javaProject, boolean includeWorkspaceReferences) throws JavaModelException, IOException {
		for (final IClasspathEntry cpe : javaProject.getResolvedClasspath(true)) {
			IPath cpePath;
			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_SOURCE:
				cpePath = cpe.getOutputLocation();
				if (cpePath == null) {
					cpePath = javaProject.getOutputLocation();
				}
				final IContainer container = (IContainer)
						ResourcesPlugin.getWorkspace().getRoot().findMember(cpePath);
				addPath(container);
				break;
			case IClasspathEntry.CPE_LIBRARY:
				cpePath = cpe.getPath();
				final IResource resource =
						ResourcesPlugin.getWorkspace().getRoot().findMember(cpePath);
				if ((resource != null) &&
						(includeWorkspaceReferences
								|| javaProject.getProject().equals(resource.getProject()))) {
					if (resource instanceof IFile) {
						addCpJar(new JarFile(resource.getLocation().toFile()));
					} else if (resource instanceof IContainer) {
						addCpPath((IContainer) resource);
					} else {
						throw new IOException(String.format(
								JarToUMLResources.getString("JarToUML.unexpectedResourceKind"),
								resource)); //$NON-NLS-1$
					}
				}
				break;
			}
		}
		if (includeWorkspaceReferences) {
			final Set<IJavaProject> refs = new HashSet<IJavaProject>();
			findJavaProjectReferences(javaProject, refs);
			for (final IJavaProject ref : refs) {
				addPaths(ref, includeWorkspaceReferences);
			}
		}
	}

	/**
	 * The jar files for which the class files should
	 * only be reverse engineered as they are referenced
	 * by the main class files.
	 * @return the jars on the classpath
	 */
	public List<JarFile> getCpJars() {
		return cpJars;
	}

	/**
	 * Adds cpJar to the classpath jars.
	 * @param cpJar
	 */
	public void addCpJar(JarFile cpJar) {
		if (!this.cpJars.contains(cpJar)) {
			this.cpJars.add(cpJar);
		}
	}

	/**
	 * Removes cpJar from the classpath jars.
	 * @param cpJar
	 */
	public void removeCpJar(JarFile cpJar) {
		this.cpJars.remove(cpJar);
	}

	/**
	 * Clears the classpath jars.
	 */
	public void clearCpJars() {
		this.cpJars.clear();
	}

	/**
	 * The paths for which the class files should
	 * only be reverse engineered as they are referenced
	 * by the main class files.
	 * @return the paths on the classpath
	 */
	public List<IContainer> getCpPaths() {
		return cpPaths;
	}

	/**
	 * Adds cpPath to the classpath paths.
	 * @param cpPath
	 */
	public void addCpPath(IContainer cpPath) {
		if (!this.cpPaths.contains(cpPath)) {
			this.cpPaths.add(cpPath);
		}
	}

	/**
	 * Remove cpPath from the classpath paths.
	 * @param cpPath
	 */
	public void removeCpPath(IContainer cpPath) {
		this.cpPaths.remove(cpPath);
	}

	/**
	 * Clears the classpath paths.
	 */
	public void clearCpPaths() {
		this.cpPaths.clear();
	}

	/**
	 * @return the parsed classes
	 */
	public List<JavaClass> getParsedClasses() {
		return parsedClasses;
	}

	/**
	 * @return the parsed classpath classes
	 * @see #getCpJars()
	 * @see #getCpPaths()
	 */
	public List<JavaClass> getParsedCpClasses() {
		return parsedCpClasses;
	}

	/**
	 * Whether or not to include a generator comment. Defaults to true.
	 * @return the includeComment
	 */
	public boolean isIncludeComment() {
		return includeComment;
	}

	/**
	 * Whether or not to include a generator comment. Defaults to true.
	 * @param includeComment the includeComment to set
	 */
	public void setIncludeComment(boolean includeComment) {
		this.includeComment = includeComment;
	}

	/**
	 * @return the updateExistingFile
	 */
	public boolean isUpdateExistingFile() {
		return updateExistingFile;
	}

	/**
	 * @param updateExistingFile the updateExistingFile to set
	 */
	public void setUpdateExistingFile(boolean updateExistingFile) {
		this.updateExistingFile = updateExistingFile;
	}

	/**
	 * Saves the UML model with default options.
	 * 
	 * @throws IOException
	 */
	public void saveModel() throws IOException {
		final Map<String, String> options = new HashMap<>();
		options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);
		getModel().eResource().save(options);
	}

}
