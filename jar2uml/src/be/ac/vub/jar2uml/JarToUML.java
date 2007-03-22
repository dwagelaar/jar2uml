package be.ac.vub.jar2uml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;

public class JarToUML implements Runnable {
		
	public static final String LOGGER = "be.ac.vub.jar2uml";
	
	private static ResourceSet resourceSet = new ResourceSetImpl();
	private static Logger logger = Logger.getLogger(LOGGER);
	private static FindContainedClassifierSwitch findContainedClassifier = new FindContainedClassifierSwitch();

	static {
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI,
		   UMLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().
	  		put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			logger.setLevel(Level.INFO);
			JarToUML jarToUML = new JarToUML();
			jarToUML.setFilter(new JavaAPIFilter());
			jarToUML.addJar(new JarFile(args[0]));
			jarToUML.setOutputFile(args[1]);
			jarToUML.setOutputModelName(args[2]);
			jarToUML.run();
		} catch (Exception e) {
			logger.severe("Usage: JarToUML <jarfile> <umlfile> <umlmodelname>");
			e.printStackTrace();
		}
	}

	private Model model = null;
	private List jars = new ArrayList();
	private TypeToClassifierSwitch typeToClassifier = null;
	private FixClassifierSwitch fixClassifier = new FixClassifierSwitch();
	private AddClassifierInterfaceSwitch addClassifierInterface = new AddClassifierInterfaceSwitch();
	private AddClassifierPropertySwitch addClassifierProperty = new AddClassifierPropertySwitch();
	private AddClassifierOperationSwitch addClassifierOperation = new AddClassifierOperationSwitch();
	private Filter filter = null;
	private String outputFile = "api.uml";
	private String outputModelName = "api";

	public JarToUML() {
	}

	public void run() {
		Assert.assertNotNull(getOutputFile());
		logger.info("Starting JarToUML for " + getOutputFile());
		Resource res = resourceSet.createResource(URI.createURI(getOutputFile()));
		Assert.assertNotNull(res);
		try {
			model = UMLFactory.eINSTANCE.createModel();
			res.getContents().add(model);
			model.setName(getOutputModelName());
			typeToClassifier = new TypeToClassifierSwitch(getModel());
			for (Iterator it = getJars(); it.hasNext();) {
				JarFile jar = (JarFile) it.next();
				addAllClassifiers(jar);
			}
			for (Iterator it = getJars(); it.hasNext();) {
				JarFile jar = (JarFile) it.next();
				addAllProperties(jar);
			}
			res.save(Collections.EMPTY_MAP);
		} catch (Exception e) {
			e.printStackTrace();
		}
		resourceSet.getResources().remove(res);
		logger.info("Finished JarToUML");
	}
	
	private void addAllClassifiers(JarFile jar) throws IOException {
		Assert.assertNotNull(jar);
		for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
			JarEntry entry = (JarEntry) entries.nextElement();
			if (entry.getName().endsWith(".class")) {
				if (getFilter() != null) {
					if (!getFilter().filter(entry.getName())) {
						continue;
					}
				}
				ClassParser parser = new ClassParser(jar.getInputStream(entry), entry.getName());
				JavaClass javaClass = parser.parse();
				addClassifier(javaClass);
			}
		}
	}
	
	private void addAllProperties(JarFile jar) throws IOException {
		Assert.assertNotNull(jar);
		for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
			JarEntry entry = (JarEntry) entries.nextElement();
			if (entry.getName().endsWith(".class")) {
				if (getFilter() != null) {
					if (!getFilter().filter(entry.getName())) {
						continue;
					}
				}
				ClassParser parser = new ClassParser(jar.getInputStream(entry), entry.getName());
				JavaClass javaClass = parser.parse();
				addClassifierProperties(javaClass);
			}
		}
	}
	
	public static Package findPackage(Package root, String packageName, boolean create) {
		Assert.assertNotNull(packageName);
		Package parent = root;
		String tail = tail(packageName, '.');
		if (tail.length() < packageName.length()) {
			String parentName = packageName.substring(0, packageName.length() - tail.length() - 1);
			parent = findPackage(root, parentName, create);
			packageName = tail;
		}
		if (parent == null) {
			return null;
		}
		for (Iterator it = parent.getNestedPackages().iterator(); it.hasNext();) {
			Package pack = (Package) it.next();
			if (packageName.equals(pack.getName())) {
				return pack;
			}
		}
		if (create) {
			return parent.createNestedPackage(packageName);
		} else {
			return null;
		}
	}
	
	private void addClassifier(JavaClass javaClass) {
		if (!isAPIClass(javaClass)) {
			logger.fine("Skipped non-API class: " + javaClass.getClassName());
			return;
		}
		logger.fine(javaClass.getClassName());
		Classifier classifier = 
			findClassifier(getModel(), javaClass.getClassName(), UMLPackage.eINSTANCE.getClass_());
		Assert.assertNotNull(classifier);
		fixClassifier.setJavaClass(javaClass);
		fixClassifier.doSwitch(classifier);
	}
	
	private void addClassifierProperties(JavaClass javaClass) {
		if (!isAPIClass(javaClass)) {
			return;
		}
		logger.fine(javaClass.getClassName());
		Classifier classifier = 
			findClassifier(getModel(), javaClass.getClassName(), null);
		addInterfaceRealizations(classifier, javaClass);
		addGeneralizations(classifier, javaClass);
		addProperties(classifier, javaClass);
		addOperations(classifier, javaClass);
	}
	
	private void addInterfaceRealizations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		String interfaces[] = javaClass.getInterfaceNames();
		for (int i = 0; i < interfaces.length; i++) {
			Classifier iface = findClassifier(getModel(), interfaces[i], UMLPackage.eINSTANCE.getInterface());
			if (iface instanceof Interface) {
				addClassifierInterface.setIface((Interface) iface);
				addClassifierInterface.doSwitch(classifier);
			}
		}
	}
	
	private void addGeneralizations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		if (!"java.lang.Object".equals(javaClass.getSuperclassName())) {
			Classifier superClass = findClassifier(getModel(), javaClass.getSuperclassName(), UMLPackage.eINSTANCE.getClass_());
			if (superClass != null) {
				classifier.createGeneralization(superClass);
			}
		}
	}
	
	private void addProperties(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		Field[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (isAPI(fields[i])) {
				logger.fine(fields[i].getSignature());
				Type type = (Type) typeToClassifier.doSwitch(fields[i].getType());
				if (type == null) {
					logger.warning("Type not found for " + 
							javaClass.getClassName() + "#" + 
							fields[i].getName() + " : " + 
							fields[i].getType().getSignature());
				}
				addClassifierProperty.setPropertyName(fields[i].getName());
				addClassifierProperty.setPropertyType(type);
				Property prop = (Property) addClassifierProperty.doSwitch(classifier);
				prop.setVisibility(toUMLVisibility(fields[i]));
				prop.setIsStatic(fields[i].isStatic());
				prop.setIsReadOnly(fields[i].isFinal());
			}
		}
	}
	
	private void addOperations(Classifier classifier, JavaClass javaClass) {
		Assert.assertNotNull(classifier);
		Method[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (isAPI(methods[i])) {
				logger.fine(methods[i].getSignature());
				org.apache.bcel.generic.Type[] types = methods[i].getArgumentTypes();
				addClassifierOperation.setOperationName(methods[i].getName());
				addClassifierOperation.setArgumentTypes(toUMLTypes(types));
				Operation op = (Operation) addClassifierOperation.doSwitch(classifier);
				Type type = (Type) typeToClassifier.doSwitch(methods[i].getReturnType());
				if (type != null) {
					op.setType(type);
				}
				op.setVisibility(toUMLVisibility(methods[i]));
				op.setIsAbstract(methods[i].isAbstract());
				op.setIsStatic(methods[i].isStatic());
			}
		}
	}
	
	private EList toUMLTypes(org.apache.bcel.generic.Type[] types) {
		EList umlTypes = new BasicEList();
		for (int i = 0; i < types.length; i++) {
			Type type = (Type) typeToClassifier.doSwitch(types[i]);
			if (type == null) {
				logger.warning("Type not found: " +	types[i].getSignature());
			}
			umlTypes.add(type);
		}
		return umlTypes;
	}
	
	public static String tail(String javaName, char delim) {
		int dotIndex = javaName.lastIndexOf(delim);
		if (dotIndex > -1) {
			return javaName.substring(dotIndex+1, javaName.length());
		} else {
			return javaName;
		}
	}
	
	public static PrimitiveType findPrimitiveType(Package root, String typeName, boolean create) {
		Assert.assertNotNull(typeName);
		String localTypeName = typeName;
		Package parent = root;
		String tail = tail(typeName, '.');
		if (tail.length() < typeName.length()) {
			String parentName = typeName.substring(0, typeName.length() - tail.length() - 1);
			parent = findPackage(root, parentName, create);
			localTypeName = tail;
		}
		if (parent == null) {
			return null;
		}
		for (Iterator it = parent.getPackagedElements().iterator(); it.hasNext();) {
			Object element = it.next();
			if (element instanceof PrimitiveType) {
				PrimitiveType type = (PrimitiveType) element;
				if (localTypeName.equals(type.getName())) {
					return type;
				}
			}
		}
		if (create) {
			return parent.createOwnedPrimitiveType(localTypeName);
		}
		return null;
	}
	
	/**
	 * @param root The model root to start searching at.
	 * @param className The fully qualified classifier name (e.g. "java.lang.Class$Inner")
	 * @param createAs If not null and classifier is not found, an instance of this meta-class is created.
	 * @return A {@link Classifier} with qualified name className. If createAs is not null
	 * and the classifier was not found, a new instance of the createAs meta-class is
	 * created and returned.
	 */
	public static Classifier findClassifier(Package root, String className, EClass createAs) {
		Assert.assertNotNull(className);
		String localClassName = className;
		Package parent = root;
		String tail = tail(className, '.');
		if (tail.length() < className.length()) {
			String parentName = className.substring(0, className.length() - tail.length() - 1);
			parent = findPackage(root, parentName, createAs != null);
			localClassName = tail;
		}
		if (parent == null) {
			return null;
		}
		Classifier parentClass = null;
		tail = tail(className, '$');
		if (tail.length() < className.length()) {
			String parentName = className.substring(0, className.length() - tail.length() - 1);
			parentClass = findClassifier(root, parentName, createAs);
			localClassName = tail;
		}
		findContainedClassifier.setClassifierName(localClassName);
		if (createAs != null) {
			findContainedClassifier.setMetaClass(createAs);
			findContainedClassifier.setCreate(true);
		} else {
			findContainedClassifier.setCreate(false);
		}
		if (parentClass != null) {
			return (Classifier) findContainedClassifier.doSwitch(parentClass);
		} else {
			return (Classifier) findContainedClassifier.doSwitch(parent);
		}
	}
	
	public static boolean isAPIClass(JavaClass javaClass) {
		String leafName = tail(javaClass.getClassName(), '$');
		try {
			Integer.parseInt(leafName);
			return false;
		} catch (NumberFormatException e) {
			//everything allright, not an anonymous class
		}
		return isAPI(javaClass);
	}
	
	public static boolean isAPI(AccessFlags flags) {
		return (flags.isPublic() || flags.isProtected());
	}
	
	public static VisibilityKind toUMLVisibility(AccessFlags flags) {
		if (flags.isPublic()) {
			return VisibilityKind.PUBLIC_LITERAL;
		} else if (flags.isProtected()) {
			return VisibilityKind.PROTECTED_LITERAL;
		} else {
			return VisibilityKind.PRIVATE_LITERAL;
		}
	}

	
	public void addJar(JarFile jar) {
		jars.add(jar);
	}
	
	public void removeJar(JarFile jar) {
		jars.remove(jar);
	}
	
	public void clearJars() {
		jars.clear();
	}
	
	public Iterator getJars() {
		return jars.iterator();
	}

	public Model getModel() {
		return model;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getOutputModelName() {
		return outputModelName;
	}

	public void setOutputModelName(String outputModelName) {
		this.outputModelName = outputModelName;
	}

}
