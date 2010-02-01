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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;
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
		
	public static final String LOGGER = "be.ac.vub.jar2uml";
	
	protected static Logger logger = Logger.getLogger(LOGGER);

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
			logger.severe("Usage: JarToUML <jarfile> <umlfile> <umlmodelname>");
			e.printStackTrace();
		}
	}

	protected FindContainedClassifierSwitch findContainedClassifier = new FindContainedClassifierSwitch();
	protected RemoveClassifierSwitch removeClassifier = new RemoveClassifierSwitch();
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
	protected AddInferredTagSwitch addInferredTagSwitch = new AddInferredTagSwitch();

	private Model model = null;
	private List<JarFile> jars = new ArrayList<JarFile>();
	private List<IContainer> paths = new ArrayList<IContainer>();
	private Filter filter = null;
	private String outputFile = "api.uml";
	private String outputModelName = "api";
	private IProgressMonitor monitor = null;
	private boolean includeInstructionReferences = false;
	private boolean includeFeatures = true;
	private boolean dependenciesOnly = false;
	private int majorFormatVersion;
	private int minorFormatVersion;
	private boolean preverified;
	private boolean runComplete = false;

	public JarToUML() {
		logger.setLevel(Level.ALL);
	}

	/**
	 * Performs the actual jar to UML conversion.
	 */
	public void run() {
		setRunComplete(false);
		List<JavaClass> parsedClasses = new ArrayList<JavaClass>();
		Assert.assertNotNull(getOutputFile());
		logger.info("Starting JarToUML for " + getOutputFile());
		ResourceSet resourceSet = createResourceSet();
		Resource res = resourceSet.createResource(URI.createURI(getOutputFile()));
		Assert.assertNotNull(res);
		try {
			setModel(UMLFactory.eINSTANCE.createModel());
			res.getContents().add(getModel());
			getModel().setName(getOutputModelName());
			typeToClassifier.setRoot(getModel());
			for (JarFile jar : getJars()) {
				parseClasses(jar, parsedClasses);
				checkMonitor(monitor);
			}
			for (IContainer path : getPaths()) {
				parseClasses(path, parsedClasses);
				checkMonitor(monitor);
			}
			addAllClassifiers(parsedClasses);
			addAllProperties(parsedClasses);
			if (dependenciesOnly) {
				removeAllClassifiers(parsedClasses);
			} else {
				addAllInferredTags(parsedClasses);
			}
			removeEmptyPackages(getModel());
			Comment comment = getModel().createOwnedComment();
			comment.setBody(
					"Generated by Jar2UML " + 
					JarToUMLPlugin.getPlugin().getBundle().getVersion() +
					" from " + getInputList()); //NON-NLS-1
			EAnnotation ann = getModel().createEAnnotation("Jar2UML");
			EMap<String,String> details = ann.getDetails();
			details.put("majorBytecodeFormatVersion", String.valueOf(getMajorFormatVersion()));
			details.put("minorBytecodeFormatVersion", String.valueOf(getMinorFormatVersion()));
			details.put("preverified", String.valueOf(isPreverified()));
			setRunComplete(true);
		} catch (OperationCanceledException e) {
			logger.info("JarToUML run cancelled");
		} catch (IOException e) {
			report(e);
		} catch (CoreException e) {
			report(e);
		}
		logger.info("Finished JarToUML");
	}
	
	/**
	 * Parses all classes in jar and adds them to parsedClasses.
	 * @param jar
	 * @param parsedClasses
	 * @throws IOException
	 */
	private void parseClasses(JarFile jar, Collection<JavaClass> parsedClasses) throws IOException {
		Assert.assertNotNull(jar);
		for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			if (entry.getName().endsWith(".class")) {
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
			}
			checkMonitor(monitor);
		}
	}

	/**
	 * Parses all classes in container and adds them to parsedClasses.
	 * @param container
	 * @param parsedClasses
	 * @throws IOException
	 * @throws CoreException
	 */
	private void parseClasses(IContainer container, Collection<JavaClass> parsedClasses) throws IOException, CoreException {
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
			checkMonitor(monitor);
		}
	}
	
	/**
	 * Handles cancelled progress monitor
	 * @param monitor
	 * @throws OperationCanceledException
	 */
	private void checkMonitor(IProgressMonitor monitor) throws OperationCanceledException {
        if ((monitor != null) && monitor.isCanceled()) {
            throw new OperationCanceledException("Operation cancelled by user");
        }
	}

	/**
	 * Reports e via the logger
	 * @param e
	 */
	private void report(Exception e) {
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		logger.severe(e.getLocalizedMessage());
		logger.severe(stackTrace.toString());
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
	 * Adds all classifiers in parsedClasses to the UML model. Does not add classifier properties.
	 * @param parsedClasses
	 * @throws IOException
	 */
	private void addAllClassifiers(Collection<JavaClass> parsedClasses) throws IOException {
		for (JavaClass javaClass : parsedClasses) {
			addClassifier(javaClass);
			checkMonitor(monitor);
		}
	}
	
	/**
	 * @param expr The Java Jar file entry expression.
	 * @return True if the Jar entry should be included in the UML model.
	 */
	private boolean filter(final String expr) {
		final Filter filter = getFilter();
		return (filter == null) || (filter.filter(expr));
	}
	
	/**
	 * @param javaClass The parsed Java class of interface.
	 * @return True if the class/interface should be included in the UML model.
	 */
	private boolean filter(final JavaClass javaClass) {
		final Filter filter = getFilter();
		return (filter == null) || (filter.filter(javaClass));
	}
	
	/**
	 * @param flags The access modifier flags (public/protected/private) of a Java element.
	 * @return True if the given access modifier level should be included in the UML model.
	 */
	private boolean filter(final AccessFlags flags) {
		final Filter filter = getFilter();
		return (filter == null) || (filter.filter(flags));
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
				if (file.getFileExtension().equals("class")) {
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
	 * Adds the properties of all classifiers in parsedClasses to the classifiers in the UML model.
	 * @param parsedClasses
	 * @throws IOException
	 */
	private void addAllProperties(Collection<JavaClass> parsedClasses) throws IOException {
		for (JavaClass javaClass : parsedClasses) {
			addClassifierProperties(javaClass);
			checkMonitor(monitor);
		}
	}

	/**
	 * Removes all classifiers in parsedClasses from the UML model.
	 * @param parsedClasses
	 * @throws IOException
	 */
	private void removeAllClassifiers(Collection<JavaClass> parsedClasses) throws IOException {
		for (JavaClass javaClass : parsedClasses) {
			removeClassifier(javaClass);
			checkMonitor(monitor);
		}
	}

	/**
	 * Adds a classifier to the UML model that represents javaClass. Does not add classifier properties.
	 * @param javaClass The BCEL class representation to convert.
	 */
	private void addClassifier(JavaClass javaClass) {
		final String className = javaClass.getClassName();
		if (!filter(javaClass)) {
			logger.fine("Skipped non-API class: " + className);
			return;
		}
		logger.fine(className);
		final Classifier classifier = findContainedClassifier. 
			findClassifier(getModel(), className, UMLPackage.eINSTANCE.getClass_());
		Assert.assertNotNull(classifier);
		fixClassifier.setJavaClass(javaClass);
		fixClassifier.doSwitch(classifier);
		addReferencedInterfaces(javaClass);
		addReferencedGenerals(javaClass);
		// add realizations/generalizations in 1st pass, since inheritance hierarchy is needed in 2nd pass
		addInterfaceRealizations(classifier, javaClass);
		addGeneralizations(classifier, javaClass);
		if (isIncludeInstructionReferences()) {
			addOpCodeReferences(classifier, javaClass);
		}
	}
	
	/**
	 * Adds the properties of the javaClass to the corresponding classifier in the UML model.
	 * @param javaClass The BCEL class representation to convert.
	 */
	private void addClassifierProperties(JavaClass javaClass) {
		final String className = javaClass.getClassName();
		if (!filter(javaClass)) {
			logger.fine("Skipped non-API class: " + className);
			return;
		}
		logger.fine(className);
		final Classifier classifier = findContainedClassifier.
			findClassifier(getModel(), className, null);
		if (isIncludeFeatures()) {
			addProperties(classifier, javaClass);
			addOperations(classifier, javaClass);
		}
	}

	/**
	 * Remove the classifier corresponding to javaClass from the UML model.
	 * @param javaClass
	 */
	private void removeClassifier(JavaClass javaClass) {
		final String className = javaClass.getClassName();
		if (!filter(javaClass)) {
			logger.fine("Skipped non-API class: " + className);
			return;
		}
		logger.fine(className);
		final Classifier classifier = findContainedClassifier.
			findClassifier(getModel(), className, null);
		if (classifier != null) {
			removeClassifier(classifier);
		}
	}
	
	/**
	 * Remove classifier from the UML model. Also removes derived and contained
	 * classifiers.
	 * @param classifier
	 */
	private void removeClassifier(Classifier classifier) {
		Assert.assertNotNull(classifier);
		final List<Classifier> derived = findDerivedClassifiers(classifier);
		final Element owner = classifier.getOwner();
		Assert.assertNotNull(owner);
		for (Iterator<Classifier> it = derived.iterator(); it.hasNext();) {
			removeClassifier.setClassifier(it.next());
			removeClassifier.doSwitch(owner);
		}
		removeClassifier.setClassifier(classifier);
		removeClassifier.doSwitch(owner);
	}
	
	/**
	 * Recursively removes empty packages from fromPackage.
	 * @param fromPackage
	 */
	private void removeEmptyPackages(Package fromPackage) {
		for (Iterator<PackageableElement> it = fromPackage.getPackagedElements().iterator(); it.hasNext();) {
			checkMonitor(monitor);
			PackageableElement o = it.next();
			if (o instanceof Package) {
				Package pack = (Package) o;
				removeEmptyPackages(pack);
				if (pack.getPackagedElements().isEmpty()) {
					it.remove();
					logger.fine("Removed " + pack.getQualifiedName() + " : " + 
							pack.eClass().getName());
				}
			}
		}
	}

	/**
	 * Adds interfaces implemented by javaClass to the UML model. Used in 1st pass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addReferencedInterfaces(JavaClass javaClass) {
		String interfaces[] = javaClass.getInterfaceNames();
		for (int i = 0; i < interfaces.length; i++) {
			Classifier iface = findContainedClassifier.
				findClassifier(getModel(), interfaces[i], UMLPackage.eINSTANCE.getInterface());
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
	 * by javaClass. Used in 2nd pass.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addInterfaceRealizations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		String interfaces[] = javaClass.getInterfaceNames();
		for (int i = 0; i < interfaces.length; i++) {
			Classifier iface = findContainedClassifier.
				findClassifier(getModel(), interfaces[i], UMLPackage.eINSTANCE.getInterface());
			Assert.assertTrue(iface instanceof Interface);
			addClassifierInterface.setIface((Interface) iface);
			addClassifierInterface.doSwitch(classifier);
		}
	}
	
	/**
	 * Adds superclasses of javaClass to the UML model. Used in 1st pass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addReferencedGenerals(JavaClass javaClass) {
		if (!"java.lang.Object".equals(javaClass.getSuperclassName())) {
			Classifier superClass = findContainedClassifier.
				findClassifier(getModel(), javaClass.getSuperclassName(), UMLPackage.eINSTANCE.getClass_());
			if (superClass != null) {
				if (!(superClass instanceof Class)) {
					replaceByClassifier.setClassifier(superClass);
					replaceByClassifier.setMetaClass(UMLPackage.eINSTANCE.getClass_());
					superClass = (Classifier) replaceByClassifier.doSwitch(superClass.getOwner());
				}
			}
		}
	}

	/**
	 * Adds generalizations to classifier for each superclass
	 * of javaClass. Used in 2nd pass.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addGeneralizations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		if (classifier instanceof Interface) {
			return;
		}
		if (!classifier.getQualifiedName().endsWith("java::lang::Object")) {
			Classifier superClass = findContainedClassifier.
				findClassifier(getModel(), javaClass.getSuperclassName(), UMLPackage.eINSTANCE.getClass_());
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
	 */
	private void addProperties(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		Field[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (!filter(fields[i])) {
				continue;
			}
			logger.fine(fields[i].getSignature());
			addClassifierProperty.setPropertyName(fields[i].getName());
			addClassifierProperty.setBCELPropertyType(fields[i].getType());
			if (addClassifierProperty.getPropertyType() == null) {
				logger.warning("Type not found for " + 
						javaClass.getClassName() + "#" + 
						fields[i].getName() + " : " + 
						fields[i].getType().getSignature());
			}
			Property prop = (Property) addClassifierProperty.doSwitch(classifier);
			prop.setVisibility(toUMLVisibility(fields[i]));
			prop.setIsStatic(fields[i].isStatic());
			prop.setIsReadOnly(fields[i].isFinal());
			prop.setIsLeaf(fields[i].isFinal());
		}
	}
	
	/**
	 * Adds an operation to classifier for each javaClass method.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 */
	private void addOperations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		Method[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (getFilter() != null) {
				if (!getFilter().filter(methods[i])) {
					continue;
				}
			}
			logger.fine(methods[i].getSignature());
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
	 * Adds fields/methods referenced by the bytecode instructions of method
	 * to the UML model. Used in 2nd pass.
	 * @param instrContext The classifier on which the method is defined.
	 * @param method The method for which to convert the references.
	 */
	private void addOpCode(Classifier instrContext, Method method) {
		if (method.getCode() == null) {
			return;
		}
		addInstructionDependencies.setInstrContext(instrContext);
		addInstructionDependencies.setCp(method.getConstantPool());
		InstructionList instrList = new InstructionList(method.getCode().getCode());
		Instruction[] instr = instrList.getInstructions();
		for (int i = 0; i < instr.length; i++) {
			instr[i].accept(addInstructionDependencies);
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
			logger.fine(methods[i].getSignature());
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
	 * Adds inferred tags to all elements not contained in parsedClasses.
	 * @param parsedClasses
	 */
	private void addAllInferredTags(Collection<JavaClass> parsedClasses) {
		final Set<Classifier> containedClassifiers = new HashSet<Classifier>();
		for (JavaClass javaClass : parsedClasses) {
			containedClassifiers.add(findContainedClassifier. 
				findClassifier(getModel(), javaClass.getClassName(), null));
		}
		addInferredTagSwitch.setContainedClassifiers(containedClassifiers);
		addInferredTagSwitch.doSwitch(getModel());
	}

	/**
	 * Adds a tag to indicate it has been inferred
	 * from class file references.
	 * @param element The element to add the tag to.
	 */
	protected void addInferredTag(Element element) {
		final EAnnotation ann = element.createEAnnotation("Jar2UML");
		final EMap<String, String> details = ann.getDetails();
		details.put("inferred", "true");
	}
	
	/**
	 * Removes the tag to indicate it has been inferred
	 * from class file references.
	 * @param element The element to remove the tag from.
	 */
	protected void removeInferredTag(Element element) {
		final EAnnotation ann = element.getEAnnotation("Jar2UML");
		if (ann != null) {
			final EMap<String, String> details = ann.getDetails();
			if (details.containsKey("inferred")) {
				details.removeKey("inferred");
			}
			if (details.isEmpty()) {
				element.getEAnnotations().remove(ann);
			}
		}
	}
	
	/**
	 * @param classifier
	 * @return All classifiers that are derivatives (i.e. array types) of classifier.
	 */
	public static List<Classifier> findDerivedClassifiers(Classifier classifier) {
		Assert.assertNotNull(classifier);
		final String name = classifier.getName();
		Assert.assertNotNull(name);
		final List<Classifier> derived = new ArrayList<Classifier>();
		final Element owner = classifier.getOwner();
		Assert.assertNotNull(owner);
		for (Iterator<Element> owned = owner.getOwnedElements().iterator(); owned.hasNext();) {
			Element e = owned.next();
			if (e instanceof Classifier) {
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
		//addConstantPoolEntries.setFilter(filter);
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
	 * {@link http://en.wikipedia.org/wiki/Class_%28file_format%29}
	 */
	public int getMajorFormatVersion() {
		return majorFormatVersion;
	}

	/**
	 * The class file format minor version. 
	 * @param majorFormatVersion the majorFormatVersion to set
	 * {@link http://en.wikipedia.org/wiki/Class_%28file_format%29}
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
					addJar(new JarFile(resource.getLocation().toFile()));
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

}
