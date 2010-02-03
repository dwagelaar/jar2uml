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
package be.ac.vub.jar2uml.ui;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.ui.logging.ConsoleStreamHandler;
import be.ac.vub.jar2uml.ui.preferences.PreferenceConstants;

/**
 * Jar2UML plug-in class.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLPlugin extends AbstractUIPlugin {

	private static final String JAR2UML_CONSOLE = "be.ac.vub.jar2uml.ui.console"; //$NON-NLS-1$

	/**
	 * Keep track of the singleton.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static JarToUMLPlugin plugin;

	private static MessageConsole console;
	private static MessageConsoleStream consoleStream;
	private static IConsoleManager consoleMgr; 
	private static Handler handler;

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the singleton instance.
	 * @generated
	 */
	public static JarToUMLPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Creates a new {@link JarToUMLPlugin}.
	 */
	public JarToUMLPlugin() {
		plugin = this;
		if (console == null) {
			initConsole();
		}
	}

	/**
	 * @return The active shell.
	 */
	public Shell getShell() {
		return PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}

	/**
	 * Reports an exception/error in the log and on the screen.
	 * @param e the exception to report.
	 */
	public void report(Throwable e) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new ErrorDialogRunnable(e));
	}

	/**
	 * Logs a message.
	 * @param message the log message.
	 * @param level the log level (OK, INFO, WARNING, ERROR)
	 * @param exception the related exception, if any.
	 */
	public IStatus log(String message, int level, Throwable exception) {
		IStatus st = new Status(
				level, 
				getBundle().getSymbolicName(), 
				IStatus.OK, 
				message, 
				exception);
		getLog().log(st);
		return st;
	}

	/**
	 * Initialises (finds and activates) the message console.
	 */
	private void initConsole () {
		console = findConsole(JAR2UML_CONSOLE);
		consoleStream = console.newMessageStream();
		activateConsole();
		consoleStream.println(JarToUML.getString("JarToUMLPlugin.consoleInit")); //$NON-NLS-1$
		handler = new ConsoleStreamHandler(consoleStream);
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
	}

	/**
	 * Finds the console with the given name.
	 * @param name
	 * @return The found console, or a new console if none found.
	 */
	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		consoleMgr = plugin.getConsoleManager();
		IConsole[] existing = consoleMgr.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		consoleMgr.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}

	/**
	 * Activates the Jar2UML console in the active workbench window.
	 */
	private void activateConsole () {
		IWorkbenchPage page = null;
		IWorkbenchWindow window = getPlugin().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			page = window.getActivePage();
		}
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		try {
			if (page != null) {
				IConsoleView view = (IConsoleView) page.showView(id);
				view.display(console);      
			}
		} catch (org.eclipse.ui.PartInitException pex) {
			pex.printStackTrace();
		}
	}

	/**
	 * @param imageFilePath
	 * @return The ImageDescriptor object for imageFilePath
	 * @see #imageDescriptorFromPlugin(String, String)
	 */
	public ImageDescriptor getImageDescriptor(String imageFilePath) {
		return imageDescriptorFromPlugin(getBundle().getSymbolicName(), imageFilePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IPreferenceStore prefStore = getPreferenceStore();
		if (prefStore != null) {
			String logLevel = prefStore.getString(PreferenceConstants.P_LOG_LEVEL);
			logger.setLevel(Level.parse(logLevel));
			logger.info(String.format(JarToUML.getString("logLevelSetTo"), logger.getLevel())); //$NON-NLS-1$
		} else {
			logger.warning(JarToUML.getString("JarToUMLPlugin.cannotSetLogLevel")); //$NON-NLS-1$
		}
	}

	/**
	 * @return the logging handler that outputs to the Jar2UML console.
	 */
	public static Handler getHandler() {
		return handler;
	}

}
