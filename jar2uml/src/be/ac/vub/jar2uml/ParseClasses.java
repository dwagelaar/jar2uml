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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Parses Java class files into {@link JavaClass} instances.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class ParseClasses extends JarToUMLOperation {

	private static Pattern classFileName = Pattern.compile("^[a-zA-Z_0-9/]+\\.class$"); //$NON-NLS-1$
	private static Pattern jarFileName = Pattern.compile(".+\\.(zip|(j|w|e|s|r)ar)$"); //$NON-NLS-1$

	/**
	 * Creates a new {@link ParseClasses}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 */
	public ParseClasses(Filter filter, IProgressMonitor monitor) {
		super(filter, monitor);
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

	private int majorFormatVersion;
	private int minorFormatVersion;

	/**
	 * Parses all classes in jar and adds them to parsedClasses or parsedCpClasses.
	 * @param jar The jar file to parse class files from.
	 * @param parsedClasses Collection of classes directly parsed from jar.
	 * @param parsedCpClasses Collection of classes parsed from nested jars in jar.
	 * @throws IOException
	 */
	public void parseClasses(JarFile jar, Collection<JavaClass> parsedClasses, 
			Collection<JavaClass> parsedCpClasses) throws IOException {
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
				parseClasses(nestedJar, parsedCpClasses, parsedCpClasses);
				nestedJar.close();
			}
			checkCancelled();
		}
	}

	/**
	 * Parses all classes in jar and adds them to parsedClasses or parsedCpClasses.
	 * @param jar The jar file to parse class files from.
	 * @param parsedClasses Collection of classes directly parsed from jar.
	 * @param parsedCpClasses Collection of classes parsed from nested jars in jar.
	 * @throws IOException
	 */
	public void parseClasses(JarInputStream jar, Collection<JavaClass> parsedClasses, 
			Collection<JavaClass> parsedCpClasses) throws IOException {
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
				parseClasses(nestedJar, parsedCpClasses, parsedCpClasses);
				// do NOT close input stream!
			}
			jar.closeEntry();
			checkCancelled();
		}
	}

	/**
	 * Parses all classes in container and adds them to parsedClasses.
	 * @param container The Eclipse workspace container to parse class files from.
	 * @param parsedClasses Collection of classes directly parsed from container.
	 * @throws IOException
	 * @throws CoreException
	 */
	public void parseClasses(IContainer container, Collection<JavaClass> parsedClasses) throws IOException, CoreException {
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
			checkCancelled();
		}
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

}
