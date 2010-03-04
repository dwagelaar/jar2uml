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
package be.ac.vub.jar2uml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;

import be.ac.vub.jar2uml.ui.JarToUMLPlugin;

/**
 * Main class for the jar to UML converter. Start conversion by invoking {@link #run()}. 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUML implements Runnable {

	public static final String LOGGER = "be.ac.vub.jar2uml"; //$NON-NLS-1$
	public static final String EANNOTATION = "Jar2UML"; //$NON-NLS-1$

	protected static Logger logger = Logger.getLogger(LOGGER);

	protected static Pattern classFileName = Pattern.compile(".+\\.class$"); //$NON-NLS-1$
	protected static Pattern jarFileName = Pattern.compile(".+\\.(zip|(j|w|e|s|r)ar)$"); //$NON-NLS-1$

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JarToUML jarToUML = new JarToUML();
			jarToUML.setFilter(new JavaAPIFilter());
			jarToUML.addJar(new JarFile(args[0]));
			jarToUML.setOutputFile(args[1]);
			jarToUML.setOutputModelName(args[2]);
			jarToUML.run();
			if (jarToUML.isRunComplete()) {
				jarToUML.getModel().eResource().save(Collections.EMPTY_MAP);
			}
		} catch (Exception e) {
			logger.severe(JarToUMLResources.getString("JarToUML.usage")); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	/**
	 * @param path
	 * @return The Java project for the given path, or null
	 */
	public static IJavaProject getJavaProject(IPath path) {
		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		IProject project = resource.getProject();
		return model.getJavaProject(project.getName());
	}

	/**
	 * Retrieves the project references for javaProject and stores them in refs
	 * @param javaProject
	 * @param refs the project references for javaProject
	 */
	public static void findJavaProjectReferences(IJavaProject javaProject, Set<IJavaProject> refs) {
		try {
			for (IClasspathEntry cpe : javaProject.getResolvedClasspath(true)) {
				IPath cpePath;
				switch (cpe.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					cpePath = cpe.getPath();
					IJavaProject ref = getJavaProject(cpePath);
					refs.add(ref);
					break;
				}
			}
		} catch (JavaModelException e) {
			JarToUMLPlugin.getPlugin().report(e);
		}
	}

	/**
	 * @return A new ResourceSet with support for UML models outside Eclipse
	 */
	public static ResourceSet createResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI,
				UMLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().
		put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		return resourceSet;
	}

	/**
	 * Finds all .class files within parent
	 * @param parent
	 * @param cfs the list of found class files
	 * @throws CoreException
	 */
	public static void findClassFilesIn(IContainer parent, List<IFile> cfs)
	throws CoreException {
		for (IResource r : parent.members()) {
			switch (r.getType()) {
			case IResource.FILE:
				IFile file = (IFile) r;
				if (file.getFileExtension().equals("class")) { //$NON-NLS-1$
					cfs.add(file);
				}
				break;
			case IResource.FOLDER:
			case IResource.PROJECT:
				findClassFilesIn((IContainer)r, cfs);
				break;
			}
		}
	}

	/**
	 * @param classifier
	 * @return All classifiers that are derivatives (i.e. array types) of classifier.
	 */
	public static Collection<Classifier> findDerivedClassifiers(Classifier classifier) {
		Assert.assertNotNull(classifier);
		final String name = classifier.getName();
		Assert.assertNotNull(name);
		final List<Classifier> derived = new ArrayList<Classifier>();
		final Element owner = classifier.getOwner();
		Assert.assertNotNull(owner);
		for (Element e : owner.getOwnedElements()) {
			if ((e instanceof Classifier) && (e != classifier)) {
				Classifier c = (Classifier) e;
				String cname = c.getName();
				Assert.assertNotNull(cname);
				if (cname.startsWith(name)) {
					cname = cname.substring(name.length());
					cname = cname.replace('[', ' ');
					cname = cname.replace(']', ' ');
					cname = cname.trim();
					if (cname.length() == 0) {
						derived.add(c);
					}
				}
			}
		}
		return derived;
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
		} catch (NumberFormatException e) {
			//everything allright, not an anonymous class
		}
		return true;
	}

	/**
	 * @param code
	 * @return True if the code has been preverified for CLDC execution, i.e. it has a StackMap attribute
	 */
	public static boolean isPreverified(Code code) {
		if (code == null) {
			return false;
		}
		for (Attribute att : code.getAttributes()) {
			if (att instanceof StackMap) {
				return true;
			}
		}
		return false;
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
		} else {
			return VisibilityKind.PRIVATE_LITERAL;
		}
	}

	/**
	 * @param element
	 * @return The qualified name of element
	 */
	public static String qualifiedName(final NamedElement element) {
		final String qName = element.getQualifiedName();
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
		List<String> list = new ArrayList<String>();
		for (NamedElement e : elements) {
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
	 * Adds a tag to indicate element has been parsed
	 * from a classpath entry.
	 * @param element
	 */
	public static final void addClasspathTag(final Element element) {
		annotate(element, "classpath", "true");
	}

//	/**
//	 * Adds a tag to indicate element to be removed later.
//	 * @param element
//	 */
//	public static final void addRemoveTag(final Element element) {
//		annotate(element, "remove", "true");
//	}
//
//	/**
//	 * Removes the tag to indicate element to be removed later.
//	 * @param element
//	 */
//	public static final void clearRemoveTag(final Element element) {
//		deannotate(element, "remove");
//	}
//
//	/**
//	 * Adds a tag to indicate element to be removed later.
//	 * @param element
//	 */
//	public static final boolean hasRemoveTag(final Element element) {
//		return "true".equals(getAnnotationValue(element, "remove"));
//	}

	protected FindContainedClassifierSwitch findContainedClassifier = new FindContainedClassifierSwitch();
	protected RemoveClassifierSwitch removeClassifier = new RemoveClassifierSwitch();
	protected RemoveClassifierPropertiesSwitch removeClassifierProperties = new RemoveClassifierPropertiesSwitch();
	protected ReplaceByClassifierSwitch replaceByClassifier = new ReplaceByClassifierSwitch();
	protected TypeToClassifierSwitch typeToClassifier = new TypeToClassifierSwitch();
	protected FixClassifierSwitch fixClassifier = new FixClassifierSwitch();
	protected AddClassifierInterfaceSwitch addClassifierInterface = new AddClassifierInterfaceSwitch();
	protected AddClassifierPropertySwitch addClassifierProperty = new AddClassifierPropertySwitch(typeToClassifier);
	protected AddClassifierOperationSwitch addClassifierOperation = new AddClassifierOperationSwitch(typeToClassifier);
	protected AddInstructionReferencesVisitor addInstructionReferences = 
		new AddInstructionReferencesVisitor(
				typeToClassifier);
	protected AddInstructionDependenciesVisitor addInstructionDependencies = 
		new AddInstructionDependenciesVisitor(
				typeToClassifier,
				addClassifierProperty,
				addClassifierOperation);
	protected AddInferredTagSwitch addInferredTags = new AddInferredTagSwitch();
	protected FindReferredTypesSwitch findReferredTypes = new FindReferredTypesSwitch();

	private Model model = null;
	private List<JarFile> jars = new ArrayList<JarFile>();
	private List<IContainer> paths = new ArrayList<IContainer>();
	private List<JarFile> cpJars = new ArrayList<JarFile>();
	private List<IContainer> cpPaths = new ArrayList<IContainer>();
	private List<JavaClass> parsedClasses = new ArrayList<JavaClass>();
	private List<JavaClass> parsedCpClasses = new ArrayList<JavaClass>();
	private Filter filter = null;
	private String outputFile = "api.uml"; //$NON-NLS-1$
	private String outputModelName = "api"; //$NON-NLS-1$
	private IProgressMonitor monitor = null;
	private boolean includeInstructionReferences = false;
	private boolean includeFeatures = true;
	private boolean dependenciesOnly = false;
	private int majorFormatVersion;
	private int minorFormatVersion;
	private boolean preverified;
	private boolean runComplete = false;
	private long jobStartTime;

	/**
	 * Performs the actual jar to UML conversion.
	 */
	public void run() {
		try {
			final IProgressMonitor monitor = getMonitor();
			setRunComplete(false);
			Assert.assertNotNull(getOutputFile());
			beginTask(monitor, String.format(
					JarToUMLResources.getString("JarToUML.startingFor"),
					getOutputFile()), 7);  //$NON-NLS-1$
			//
			// 1
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.creatingUML")); //$NON-NLS-1$
			final ResourceSet resourceSet = createResourceSet();
			final Resource res = resourceSet.createResource(URI.createURI(getOutputFile()));
			Assert.assertNotNull(res);
			setModel(UMLFactory.eINSTANCE.createModel());
			res.getContents().add(getModel());
			getModel().setName(getOutputModelName());
			typeToClassifier.setRoot(getModel());
			worked(monitor, JarToUMLResources.getString("JarToUML.createdUML")); //$NON-NLS-1$
			//
			// 2
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.parsing")); //$NON-NLS-1$
			for (JarFile jar : getJars()) {
				parseClasses(jar, getParsedClasses());
				checkCancelled(monitor);
			}
			for (IContainer path : getPaths()) {
				parseClasses(path, getParsedClasses());
				checkCancelled(monitor);
			}
			for (JarFile jar : getCpJars()) {
				parseClasses(jar, getParsedCpClasses());
				checkCancelled(monitor);
			}
			for (IContainer path : getCpPaths()) {
				parseClasses(path, getParsedCpClasses());
				checkCancelled(monitor);
			}
			worked(monitor, JarToUMLResources.getString("JarToUML.parsed")); //$NON-NLS-1$
			//
			// 3
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.addingClassifiers")); //$NON-NLS-1$
			addAllClassifiers(getParsedClasses());
			List<JavaClass> skippedClasses = addClassifiersClosure(getParsedCpClasses());
			getParsedCpClasses().removeAll(skippedClasses);
			worked(monitor, JarToUMLResources.getString("JarToUML.addedClassifiers")); //$NON-NLS-1$
			//
			// 4
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.addingProperties")); //$NON-NLS-1$
			addAllProperties(getParsedClasses());
			addAllProperties(getParsedCpClasses());
			worked(monitor, JarToUMLResources.getString("JarToUML.addedProperties")); //$NON-NLS-1$
			//
			// 5
			//
			final Set<Classifier> containedClassifiers = findContainedClassifiers(getParsedClasses());
			containedClassifiers.addAll(findContainedClassifiers(getParsedCpClasses()));
			if (isDependenciesOnly()) {
				subTask(monitor, JarToUMLResources.getString("JarToUML.removingClassifiers")); //$NON-NLS-1$
				final Set<Classifier> inferredClassifiers = findInferredClassifiers(containedClassifiers);
				final Set<Type> referredTypes = findAllReferredTypes(inferredClassifiers);
				final Set<Classifier> removeClassifiers = new HashSet<Classifier>(containedClassifiers);
				if (removeClassifiers.removeAll(referredTypes)) {
					containedClassifiers.retainAll(referredTypes);
					logger.warning(String.format(
							JarToUMLResources.getString("JarToUML.cyclicDepsFound"),
							getNameList(containedClassifiers)));
					// Keep referred classifiers, but strip their properties
					removeAllProperties(containedClassifiers);
					// Tag contained classifiers as "inferred" by the inferred classifiers
					addAllInferredTags(inferredClassifiers);
				}
				removeAllClassifiers(removeClassifiers);
				worked(monitor, JarToUMLResources.getString("JarToUML.removedClassifiers")); //$NON-NLS-1$
			} else {
				subTask(monitor, JarToUMLResources.getString("JarToUML.addingInferred")); //$NON-NLS-1$
				addAllInferredTags(containedClassifiers);
				worked(monitor, JarToUMLResources.getString("JarToUML.addedInferred")); //$NON-NLS-1$
			}
			//
			// 6
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.removingEmpty")); //$NON-NLS-1$
			removeEmptyPackages(getModel());
			worked(monitor, JarToUMLResources.getString("JarToUML.removedEmpty")); //$NON-NLS-1$
			//
			// 7
			//
			subTask(monitor, JarToUMLResources.getString("JarToUML.addingMetadata")); //$NON-NLS-1$
			Comment comment = getModel().createOwnedComment();
			comment.setBody(String.format(
					JarToUMLResources.getString("JarToUML.generatedBy"), 
					JarToUMLPlugin.getPlugin().getBundle().getVersion(),
					getInputList())); //$NON-NLS-1$
			EAnnotation ann = getModel().createEAnnotation("Jar2UML"); //$NON-NLS-1$
			EMap<String,String> details = ann.getDetails();
			details.put("majorBytecodeFormatVersion", String.valueOf(getMajorFormatVersion())); //$NON-NLS-1$
			details.put("minorBytecodeFormatVersion", String.valueOf(getMinorFormatVersion())); //$NON-NLS-1$
			details.put("preverified", String.valueOf(isPreverified())); //$NON-NLS-1$
			worked(monitor, JarToUMLResources.getString("JarToUML.addedMetadata"));
			setRunComplete(true);
		} catch (OperationCanceledException e) {
			logger.info(JarToUMLResources.getString("JarToUML.cancelled")); //$NON-NLS-1$
		} catch (IOException e) {
			report(e);
		} catch (CoreException e) {
			report(e);
		} catch (JarToUMLException e) {
			report(e);
		} finally {
			done(monitor, JarToUMLResources.getString("JarToUML.finished")); //$NON-NLS-1$
		}
	}

	/**
	 * Starts a new task with the progress monitor, if not null.
	 * @param monitor The progress monitor.
	 * @param name
	 * @param totalWork
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	protected void beginTask(IProgressMonitor monitor, String name, int totalWork) {
		if (name != null) {
			logger.info(name);
		}
		if (monitor != null) {
			setJobStartTime(System.currentTimeMillis());
			monitor.beginTask(name, totalWork);
		}
	}

	/**
	 * Logs and starts a new task on the progress monitor
	 * @param monitor
	 * @param message
	 */
	protected void subTask(IProgressMonitor monitor, String message) {
		if (message != null) {
			logger.info(message);
		}
		if (monitor != null) {
			monitor.subTask(message);
		}
	}

	/**
	 * Increases the progressmonitor by 1, if not null.
	 * @param monitor
	 * @throws OperationCanceledException if user pressed cancel button.
	 */
	protected void worked(IProgressMonitor monitor, String message) 
	throws OperationCanceledException {
		if (message != null) {
			final long time = System.currentTimeMillis()-getJobStartTime();
			logger.info(String.format(
					JarToUMLResources.getString("JarToUML.logAt"), 
					message, time, time, time)); //$NON-NLS-1$
		}
		if (monitor != null) {
			monitor.worked(1);
			checkCancelled(monitor);
		}
	}

	/**
	 * Finishes progress monitor task.
	 * @param monitor
	 * @param message
	 */
	protected void done(IProgressMonitor monitor, String message) {
		if (message != null) {
			logger.info(message);
		}
		if (monitor != null) {
			monitor.done();
		}
	}

	/**
	 * Handles cancelled progress monitor
	 * @param monitor
	 * @throws OperationCanceledException
	 */
	protected void checkCancelled(IProgressMonitor monitor) throws OperationCanceledException {
		if ((monitor != null) && monitor.isCanceled()) {
			throw new OperationCanceledException(JarToUMLResources.getString("operationCancelledByUser")); //$NON-NLS-1$
		}
	}

	/**
	 * Reports e via the logger
	 * @param e
	 */
	protected void report(Exception e) {
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		logger.severe(e.getLocalizedMessage());
		logger.severe(stackTrace.toString());
	}

	/**
	 * @return A comma-separated list of all Jar and Path inputs
	 */
	private String getInputList() {
		StringBuffer b = null;
		for (JarFile jar : getJars()) {
			if (b == null) {
				b = new StringBuffer();
			} else {
				b.append(", ");	
			}
			String jarName = jar.getName();
			b.append(jarName.substring(jarName.lastIndexOf('/') + 1));	
		}
		for (IContainer path : getPaths()) {
			if (b == null) {
				b = new StringBuffer();
			} else {
				b.append(", ");	
			}
			b.append(path.getFullPath());
		}
		return b.toString();
	}

	/**
	 * @param expr The Java Jar file entry expression.
	 * @return True if the Jar entry should be included in the UML model.
	 */
	protected boolean filter(final String expr) {
		final Filter filter = getFilter();
		return (filter == null) || (filter.filter(expr));
	}

	/**
	 * @param javaClass The parsed Java class of interface.
	 * @return True if the class/interface should be included in the UML model.
	 */
	protected boolean filter(final JavaClass javaClass) {
		final Filter filter = getFilter();
		return (filter == null) || (filter.filter(javaClass));
	}

	/**
	 * @param flags The access modifier flags (public/protected/private) of a Java element.
	 * @return True if the given access modifier level should be included in the UML model.
	 */
	protected boolean filter(final AccessFlags flags) {
		final Filter filter = getFilter();
		return (filter == null) || (filter.filter(flags));
	}

	/**
	 * Parses all classes in jar and adds them to parsedClasses.
	 * @param jar
	 * @param parsedClasses
	 * @throws IOException
	 */
	protected void parseClasses(JarFile jar, Collection<JavaClass> parsedClasses) throws IOException {
		Assert.assertNotNull(jar);
		for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (classFileName.matcher(name).matches()) {
				if (!filter(entry.getName())) {
					continue;
				}
				InputStream input = jar.getInputStream(entry);
				ClassParser parser = new ClassParser(input, entry.getName());
				JavaClass javaClass = parser.parse();
				setMajorFormatVersion(javaClass.getMajor());
				setMinorFormatVersion(javaClass.getMinor());
				input.close();
				parsedClasses.add(javaClass);
			} else if (jarFileName.matcher(name).matches()) {
				InputStream input = jar.getInputStream(entry);
				JarInputStream nestedJar = new JarInputStream(input);
				// switch to classpath classes collection
				parseClasses(nestedJar, getParsedCpClasses());
				nestedJar.close();
			}
			checkCancelled(monitor);
		}
	}

	/**
	 * Parses all classes in jar and adds them to parsedClasses.
	 * @param jar
	 * @param parsedClasses
	 * @throws IOException
	 */
	protected void parseClasses(JarInputStream jar, Collection<JavaClass> parsedClasses) throws IOException {
		Assert.assertNotNull(jar);
		for (JarEntry entry = jar.getNextJarEntry(); entry != null; entry = jar.getNextJarEntry()) {
			String name = entry.getName();
			if (classFileName.matcher(name).matches()) {
				if (!filter(entry.getName())) {
					continue;
				}
				ClassParser parser = new ClassParser(jar, entry.getName());
				JavaClass javaClass = parser.parse();
				setMajorFormatVersion(javaClass.getMajor());
				setMinorFormatVersion(javaClass.getMinor());
				parsedClasses.add(javaClass);
			} else if (jarFileName.matcher(name).matches()) {
				JarInputStream nestedJar = new JarInputStream(jar);
				// switch to classpath classes collection
				parseClasses(nestedJar, getParsedCpClasses());
				// do NOT close input stream!
			}
			jar.closeEntry();
			checkCancelled(monitor);
		}
	}

	/**
	 * Parses all classes in container and adds them to parsedClasses.
	 * @param container
	 * @param parsedClasses
	 * @throws IOException
	 * @throws CoreException
	 */
	protected void parseClasses(IContainer container, Collection<JavaClass> parsedClasses) throws IOException, CoreException {
		Assert.assertNotNull(container);
		List<IFile> classFiles = new ArrayList<IFile>();
		findClassFilesIn(container, classFiles);
		for (IFile classFile : classFiles) {
			IPath filePath = classFile.getLocation();
			String filename = filePath.toString().substring(container.getLocation().toString().length());
			if (!filter(filename)) {
				continue;
			}
			InputStream input = classFile.getContents();
			ClassParser parser = new ClassParser(input, filename);
			JavaClass javaClass = parser.parse();
			setMajorFormatVersion(javaClass.getMajor());
			setMinorFormatVersion(javaClass.getMinor());
			input.close();
			parsedClasses.add(javaClass);
			checkCancelled(monitor);
		}
	}

	/**
	 * Adds all classifiers in parsedClasses to the UML model. Does not add classifier properties.
	 * @param parsedClasses
	 * @throws IOException
	 * @throws JarToUMLException 
	 */
	protected void addAllClassifiers(Collection<JavaClass> parsedClasses) throws IOException, JarToUMLException {
		for (JavaClass javaClass : parsedClasses) {
			addClassifier(javaClass, false);
			checkCancelled(monitor);
		}
	}

	/**
	 * Adds the closure of all referenced classifiers in parsedClasses to the UML model. Does not add classifier properties.
	 * @param parsedClasses
	 * @return The entries in parsedClasses that have not been added.
	 * @throws IOException
	 * @throws JarToUMLException 
	 */
	protected List<JavaClass> addClassifiersClosure(Collection<JavaClass> parsedClasses) throws IOException, JarToUMLException {
		final List<JavaClass> processClasses = new ArrayList<JavaClass>(parsedClasses);
		final Set<JavaClass> addedClasses = new HashSet<JavaClass>();
		do {
			processClasses.removeAll(addedClasses);
			addedClasses.clear();
			for (JavaClass javaClass : processClasses) {
				if (addClassifier(javaClass, true)) {
					addedClasses.add(javaClass);
				}
				checkCancelled(monitor);
			}
		} while (!addedClasses.isEmpty());
		return processClasses;
	}

	/**
	 * Adds the properties of all classifiers in parsedClasses to the classifiers in the UML model.
	 * @param parsedClasses
	 * @throws IOException
	 * @throws JarToUMLException 
	 */
	protected void addAllProperties(Collection<JavaClass> parsedClasses) throws IOException, JarToUMLException {
		for (JavaClass javaClass : parsedClasses) {
			addClassifierProperties(javaClass);
			checkCancelled(monitor);
		}
	}

	/**
	 * Removes all classifiers in removeClassifiers from the UML model.
	 * @param removeClassifiers
	 * @throws IOException
	 */
	protected void removeAllClassifiers(Collection<? extends Classifier> removeClassifiers) throws IOException {
		for (Classifier classifier : removeClassifiers) {
			removeClassifier(classifier);
			checkCancelled(monitor);
		}
	}

	/**
	 * Removes all properties of classifiers in removeClassifiers from the UML model.
	 * @param removeClassifiers
	 * @throws IOException
	 */
	protected void removeAllProperties(Collection<? extends Classifier> removeClassifiers) throws IOException {
		for (Classifier classifier : removeClassifiers) {
			removeProperties(classifier);
			checkCancelled(monitor);
		}
	}

	/**
	 * Adds inferred tags to all elements not contained in containedClassifiers.
	 * @param containedClassifiers
	 */
	protected void addAllInferredTags(Set<? extends Classifier> containedClassifiers) {
		addInferredTags.setContainedClassifiers(containedClassifiers);
		addInferredTags.doSwitch(getModel());
	}

	/**
	 * @param referredFrom
	 * @return All {@link Type}s referenced from elements in referredFrom.
	 */
	protected Set<Type> findAllReferredTypes(Collection<? extends Element> referredFrom) {
		findReferredTypes.resetReferencedTypes();
		for (Element element : referredFrom) {
			findReferredTypes.doSwitch(element);
		}
		logger.fine(JarToUMLResources.getString("JarToUML.foundReferredTypes")); //$NON-NLS-1$
		return findReferredTypes.getReferencedTypes();
	}

	/**
	 * Logs the skipping of javaClass.
	 * @param javaClass
	 */
	private void logSkippedFiltered(JavaClass javaClass) {
		logger.fine(String.format(
				JarToUMLResources.getString("JarToUML.skippedFiltered"), 
				javaClass.getClassName())); //$NON-NLS-1$
	}

	/**
	 * @return All {@link Classifier}s corresponding to elements contained in parsedClasses, including derived classifiers.
	 * @param parsedClasses
	 */
	protected Set<Classifier> findContainedClassifiers(Collection<JavaClass> parsedClasses) {
		final Set<Classifier> containedClassifiers = new HashSet<Classifier>();
		for (JavaClass javaClass : parsedClasses) {
			addContainedClassifier(javaClass, containedClassifiers);
		}
		logger.fine(JarToUMLResources.getString("JarToUML.foundContainedClassifiers")); //$NON-NLS-1$
		return containedClassifiers;
	}

	/**
	 * @return All {@link Classifier}s not in containedClassifiers.
	 * @param containedClassifiers
	 */
	protected Set<Classifier> findInferredClassifiers(Collection<? extends Classifier> containedClassifiers) {
		final Set<Classifier> inferredClassifiers = new HashSet<Classifier>();
		addInferredClassifiers(getModel(), containedClassifiers, inferredClassifiers);
		logger.fine(JarToUMLResources.getString("JarToUML.foundInferredClassifiers")); //$NON-NLS-1$
		return inferredClassifiers;
	}

	/**
	 * Adds all {@link Classifier}s under container not in containedClassifiers to inferredClassifiers.
	 * @param container
	 * @param containedClassifiers
	 * @param inferredClassifiers
	 */
	private void addInferredClassifiers(Element container, Collection<? extends Classifier> containedClassifiers, Set<Classifier> inferredClassifiers) {
		for (Element e : container.getOwnedElements()) {
			if (e instanceof Classifier) {
				if (!containedClassifiers.contains(e)) {
					inferredClassifiers.add((Classifier) e);
					logger.finer(String.format(
							JarToUMLResources.getString("JarToUML.addedInferredClassifier"), 
							qualifiedName((Classifier) e))); //$NON-NLS-1$
				}
				addInferredClassifiers(e, containedClassifiers, inferredClassifiers);
			} else if (e instanceof Package) {
				addInferredClassifiers(e, containedClassifiers, inferredClassifiers);
			}
		}
	}

	/**
	 * Adds all {@link Classifier}s for javaClass to containedClassifiers, including derived classifiers.
	 * @param javaClass
	 * @param containedClassifiers
	 */
	private void addContainedClassifier(JavaClass javaClass, Collection<Classifier> containedClassifiers) {
		if (!filter(javaClass)) {
			logSkippedFiltered(javaClass);
			return;
		}
		Classifier classifier = findContainedClassifier.findClassifier(
				getModel(), javaClass.getClassName(), null);
		containedClassifiers.add(classifier);
		logger.finer(String.format(
				JarToUMLResources.getString("JarToUML.addedContainedClassifier"), 
				qualifiedName(classifier))); //$NON-NLS-1$
		Collection<Classifier> derived = findDerivedClassifiers(classifier);
		containedClassifiers.addAll(derived);
	}

	/**
	 * Adds a classifier to the UML model that represents javaClass. Does not add classifier properties.
	 * @param javaClass The BCEL class representation to convert.
	 * @param isCp whether to treat javaClass as a classpath class.
	 * @return <code>true</code> iff javaClass was added.
	 * @throws JarToUMLException 
	 */
	private boolean addClassifier(JavaClass javaClass, boolean isCp) throws JarToUMLException {
		final String className = javaClass.getClassName();
		if (!filter(javaClass)) {
			logSkippedFiltered(javaClass);
			return false;
		}
		logger.finest(className);
		Classifier classifier;
		if (isCp) {
			classifier = findContainedClassifier.findClassifier(
					getModel(), className, null);
			if (classifier == null) {
				// classifier was not referenced
				return false;
			}
		} else {
			classifier = findContainedClassifier.findClassifier(
					getModel(), className, javaClass.isInterface() ? 
							UMLPackage.eINSTANCE.getInterface() : UMLPackage.eINSTANCE.getClass_());
		}
		Assert.assertNotNull(classifier);
		// replace by instance of correct meta-class, if necessary
		fixClassifier.setJavaClass(javaClass);
		classifier = fixClassifier.doSwitch(classifier);
		// add tag to classpath classifiers
		if (isCp) {
			addClasspathTag(classifier);
		}
		// add realizations/generalizations with correct types in 1st pass, as replacing already referenced types is not possible
		addReferencedInterfaces(javaClass);
		addReferencedGenerals(javaClass);
		// add realizations/generalizations in 1st pass, as class hierarchy is needed in 2nd pass
		addInterfaceRealizations(classifier, javaClass);
		addGeneralizations(classifier, javaClass);
		// add referred types in 1st pass, as replacing already referenced types is not possible
		if (isIncludeFeatures()) {
			addPropertyTypes(classifier, javaClass);
			addOperationReferences(classifier, javaClass);
		}
		// correct referred types in 1st pass, as replacing already referenced types is not possible
		if (isIncludeInstructionReferences()) {
			addOpCodeReferences(classifier, javaClass);
		}
		return true;
	}

	/**
	 * Adds the properties of the javaClass to the corresponding classifier in the UML model.
	 * @param javaClass The BCEL class representation to convert.
	 * @throws JarToUMLException 
	 */
	private void addClassifierProperties(JavaClass javaClass) throws JarToUMLException {
		final String className = javaClass.getClassName();
		if (!filter(javaClass)) {
			logSkippedFiltered(javaClass);
			return;
		}
		logger.finest(className);
		final Classifier classifier = findContainedClassifier.findClassifier(
				getModel(), className, null);
		if (isIncludeFeatures()) {
			addProperties(classifier, javaClass);
			addOperations(classifier, javaClass);
		}
	}

	/**
	 * Remove classifier from the UML model. Also removes contained classifiers.
	 * @param classifier
	 */
	private void removeClassifier(Classifier classifier) {
		Assert.assertNotNull(classifier);
		final Element owner = classifier.getOwner();
		if (owner != null) {
			// null owner means already removed
			removeClassifier.setClassifier(classifier);
			removeClassifier.doSwitch(owner);
		}
	}

	/**
	 * Remove features of classifier from the UML model.
	 * @param classifier
	 */
	private void removeProperties(Classifier classifier) {
		Assert.assertNotNull(classifier);
		removeClassifierProperties.doSwitch(classifier);
	}

	/**
	 * Recursively removes empty packages from fromPackage.
	 * @param fromPackage
	 */
	private void removeEmptyPackages(Package fromPackage) {
		for (Iterator<PackageableElement> it = fromPackage.getPackagedElements().iterator(); it.hasNext();) {
			checkCancelled(monitor);
			PackageableElement o = it.next();
			if (o instanceof Package) {
				Package pack = (Package) o;
				removeEmptyPackages(pack);
				if (pack.getPackagedElements().isEmpty()) {
					logger.finer(String.format(
							JarToUMLResources.getString("JarToUML.removing"), 
							qualifiedName(pack), 
							pack.eClass().getName())); //$NON-NLS-1$
					it.remove();
				}
			}
		}
	}

	/**
	 * Adds interfaces implemented by javaClass to the UML model. Used in 1st pass.
	 * @param javaClass the Java class file to convert.
	 */
	private void addReferencedInterfaces(JavaClass javaClass) {
		String interfaces[] = javaClass.getInterfaceNames();
		for (int i = 0; i < interfaces.length; i++) {
			Classifier iface = findContainedClassifier.findClassifier(
					getModel(), interfaces[i], UMLPackage.eINSTANCE.getInterface());
			if (!(iface instanceof Interface)) {
				replaceByClassifier.setClassifier(iface);
				replaceByClassifier.setMetaClass(UMLPackage.eINSTANCE.getInterface());
				iface = (Classifier) replaceByClassifier.doSwitch(iface.getOwner());
			}
			iface.setIsLeaf(false);
		}
	}

	/**
	 * Adds interface realizations to classifier for each interface implemented
	 * by javaClass. Used in 1st pass.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addInterfaceRealizations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		String interfaces[] = javaClass.getInterfaceNames();
		for (int i = 0; i < interfaces.length; i++) {
			Classifier iface = findContainedClassifier.findClassifier(
					getModel(), interfaces[i], null);
			Assert.assertTrue(iface instanceof Interface);
			addClassifierInterface.setIface((Interface) iface);
			addClassifierInterface.doSwitch(classifier);
		}
	}

	/**
	 * Adds superclass of javaClass to the UML model. Used in 1st pass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addReferencedGenerals(JavaClass javaClass) {
		if (!"java.lang.Object".equals(javaClass.getClassName())) { //$NON-NLS-1$
			Classifier superClass = findContainedClassifier.findClassifier(
					getModel(), javaClass.getSuperclassName(), UMLPackage.eINSTANCE.getClass_());
			if (!(superClass instanceof Class)) {
				replaceByClassifier.setClassifier(superClass);
				replaceByClassifier.setMetaClass(UMLPackage.eINSTANCE.getClass_());
				superClass = (Classifier) replaceByClassifier.doSwitch(superClass.getOwner());
			}
		}
	}

	/**
	 * Adds generalizations to classifier for each superclass
	 * of javaClass. Used in 1st pass.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addGeneralizations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		if (classifier instanceof Interface) {
			return;
		}
		if (!classifier.getQualifiedName().endsWith("java::lang::Object")) { //$NON-NLS-1$
			Classifier superClass = findContainedClassifier.findClassifier(
					getModel(), javaClass.getSuperclassName(), UMLPackage.eINSTANCE.getClass_());
			if (superClass != null) {
				Assert.assertTrue(superClass instanceof Class);
				classifier.createGeneralization(superClass);
			}
		}
	}

	/**
	 * Adds a property to classifier for each javaClass field.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	private void addProperties(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		Assert.assertNotNull(classifier);
		Field[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (!filter(fields[i])) {
				continue;
			}
			logger.finest(fields[i].getSignature());
			addClassifierProperty.setPropertyName(fields[i].getName());
			addClassifierProperty.setBCELPropertyType(fields[i].getType());
			if (addClassifierProperty.getPropertyType() == null) {
				throw new JarToUMLException(String.format(
						JarToUMLResources.getString("JarToUML.typeNotFoundFor"), 
						javaClass.getClassName(),
						fields[i].getName(),
						fields[i].getType().getSignature())); //$NON-NLS-1$
			}
			Property prop = (Property) addClassifierProperty.doSwitch(classifier);
			prop.setVisibility(toUMLVisibility(fields[i]));
			prop.setIsStatic(fields[i].isStatic());
			prop.setIsReadOnly(fields[i].isFinal());
			prop.setIsLeaf(fields[i].isFinal());
		}
	}

	/**
	 * Adds property types to the model for each javaClass field.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	private void addPropertyTypes(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		Assert.assertNotNull(classifier);
		Field[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (!filter(fields[i])) {
				continue;
			}
			logger.finest(fields[i].getSignature());
			addClassifierProperty.setPropertyName(fields[i].getName());
			addClassifierProperty.setBCELPropertyType(fields[i].getType());
			if (addClassifierProperty.getPropertyType() == null) {
				throw new JarToUMLException(String.format(
						JarToUMLResources.getString("JarToUML.typeNotFoundFor"), 
						javaClass.getClassName(),
						fields[i].getName(),
						fields[i].getType().getSignature())); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Adds an operation to classifier for each javaClass method.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	private void addOperations(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		Assert.assertNotNull(classifier);
		Method[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (getFilter() != null) {
				if (!getFilter().filter(methods[i])) {
					continue;
				}
			}
			logger.finest(methods[i].getSignature());
			org.apache.bcel.generic.Type[] types = methods[i].getArgumentTypes();
			addClassifierOperation.setOperationName(methods[i].getName());
			addClassifierOperation.setBCELArgumentTypes(types);
			addClassifierOperation.setBCELReturnType(methods[i].getReturnType());
			Operation op = (Operation) addClassifierOperation.doSwitch(classifier);
			op.setVisibility(toUMLVisibility(methods[i]));
			op.setIsAbstract(methods[i].isAbstract());
			op.setIsStatic(methods[i].isStatic());
			op.setIsLeaf(methods[i].isFinal());
			if (isIncludeInstructionReferences()) {
				addOpCode(classifier, methods[i]);
			}
			if (isPreverified(methods[i].getCode())) {
				setPreverified(true);
			}
		}
	}

	/**
	 * Adds referenced types to the model for each javaClass method.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	private void addOperationReferences(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		Assert.assertNotNull(classifier);
		Method[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (getFilter() != null) {
				if (!getFilter().filter(methods[i])) {
					continue;
				}
			}
			logger.finest(methods[i].getSignature());
			org.apache.bcel.generic.Type[] types = methods[i].getArgumentTypes();
			addClassifierOperation.setOperationName(methods[i].getName());
			addClassifierOperation.setBCELArgumentTypes(types);
			addClassifierOperation.setBCELReturnType(methods[i].getReturnType());
		}
	}

	/**
	 * Adds fields/methods referenced by the bytecode instructions of method
	 * to the UML model. Used in 2nd pass.
	 * @param instrContext The classifier on which the method is defined.
	 * @param method The method for which to convert the references.
	 * @throws JarToUMLException 
	 */
	private void addOpCode(Classifier instrContext, Method method) throws JarToUMLException {
		if (method.getCode() == null) {
			return;
		}
		addInstructionDependencies.setInstrContext(instrContext);
		addInstructionDependencies.setCp(method.getConstantPool());
		InstructionList instrList = new InstructionList(method.getCode().getCode());
		Instruction[] instr = instrList.getInstructions();
		for (int i = 0; i < instr.length; i++) {
			instr[i].accept(addInstructionDependencies);
			final Exception e = addInstructionDependencies.getException();
			if (e != null) {
				throw new JarToUMLException(e);
			}
		}
	}

	/**
	 * Adds the classifiers referenced by the bytecode instructions of javaClass
	 * to the UML model. Used in 1st pass.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addOpCodeReferences(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		Method[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (getFilter() != null) {
				if (!getFilter().filter(methods[i])) {
					continue;
				}
			}
			logger.finest(methods[i].getSignature());
			addOpCodeRefs(classifier, methods[i]);
		}
	}

	/**
	 * Adds the classifiers referenced by the bytecode instructions of method
	 * to the UML model. Used in 1st pass.
	 * @param instrContext The classifier on which the method is defined.
	 * @param method The method for which to convert the references.
	 */
	private void addOpCodeRefs(Classifier instrContext, Method method) {
		if (method.getCode() == null) {
			return;
		}
		addInstructionReferences.setCp(method.getConstantPool());
		InstructionList instrList = new InstructionList(method.getCode().getCode());
		Instruction[] instr = instrList.getInstructions();
		for (int i = 0; i < instr.length; i++) {
			instr[i].accept(addInstructionReferences);
		}
	}

	/**
	 * Adds jar to the collection of jars to process.
	 * @param jar
	 */
	public void addJar(JarFile jar) {
		jars.add(jar);
	}

	/**
	 * Removes jar from the collection of jars to process.
	 * @param jar
	 */
	public void removeJar(JarFile jar) {
		jars.remove(jar);
	}

	/**
	 * Clears the collection of jars to process.
	 */
	public void clearJars() {
		jars.clear();
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
	 * @param model
	 */
	protected void setModel(Model model) {
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
	 * @return The progress monitor object used to check for cancellations.
	 */
	public IProgressMonitor getMonitor() {
		return monitor;
	}

	/**
	 * Sets the progress monitor object used to check for cancellations.
	 * @param monitor
	 */
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
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
	 * The class file format major version. 
	 * @return the majorFormatVersion
	 * @see <a href="http://en.wikipedia.org/wiki/Class_(file_format)">Class_(file_format)</a>
	 */
	public int getMajorFormatVersion() {
		return majorFormatVersion;
	}

	/**
	 * The class file format minor version. 
	 * @param majorFormatVersion the majorFormatVersion to set
	 * @see <a href="http://en.wikipedia.org/wiki/Class_(file_format)">Class_(file_format)</a>
	 */
	protected void setMajorFormatVersion(int majorFormatVersion) {
		this.majorFormatVersion = Math.max(this.majorFormatVersion, majorFormatVersion);
	}

	/**
	 * @return the minorFormatVersion
	 */
	public int getMinorFormatVersion() {
		return minorFormatVersion;
	}

	/**
	 * @param minorFormatVersion the minorFormatVersion to set
	 */
	protected void setMinorFormatVersion(int minorFormatVersion) {
		this.minorFormatVersion = Math.max(this.minorFormatVersion, minorFormatVersion);
	}

	/**
	 * Whether or not the bytecode has been preverified for execution on J2ME CLDC.
	 * @return the preverified
	 */
	public boolean isPreverified() {
		return preverified;
	}

	/**
	 * @param preverified the preverified to set
	 */
	protected void setPreverified(boolean preverified) {
		this.preverified = preverified;
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
		this.paths.add(path);
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
		for (IClasspathEntry cpe : javaProject.getResolvedClasspath(true)) {
			IPath cpePath;
			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_SOURCE:
				cpePath = cpe.getOutputLocation();
				if (cpePath == null) {
					cpePath = javaProject.getOutputLocation();
				}
				IContainer container = (IContainer) 
				ResourcesPlugin.getWorkspace().getRoot().findMember(cpePath);
				addPath(container);
				break;
			case IClasspathEntry.CPE_LIBRARY:
				cpePath = cpe.getPath();
				IResource resource = 
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
			Set<IJavaProject> refs = new HashSet<IJavaProject>();
			findJavaProjectReferences(javaProject, refs);
			for (IJavaProject ref : refs) {
				addPaths(ref, includeWorkspaceReferences);
			}
		}
	}

	/**
	 * @return the runComplete
	 */
	public boolean isRunComplete() {
		return runComplete;
	}

	/**
	 * @param runComplete the runComplete to set
	 */
	public void setRunComplete(boolean runComplete) {
		this.runComplete = runComplete;
	}

	/**
	 * @return the jobStartTime
	 */
	public long getJobStartTime() {
		return jobStartTime;
	}

	/**
	 * @param jobStartTime the jobStartTime to set
	 */
	protected void setJobStartTime(long jobStartTime) {
		this.jobStartTime = jobStartTime;
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
		this.cpJars.add(cpJar);
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
		this.cpPaths.add(cpPath);
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

}
