package be.ac.vub.jar2uml.ui;

import java.util.logging.Handler;
import java.util.logging.Logger;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.ui.logging.ConsoleStreamHandler;

public class JarToUMLPlugin extends AbstractUIPlugin {

	/**
	 * Keep track of the singleton.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static JarToUMLPlugin plugin;

    private static MessageConsole console = null;
    private static MessageConsoleStream consoleStream = null;
    private static IConsoleManager consoleMgr = null; 
    private static final String JAR2UML_CONSOLE = "be.ac.vub.jar2uml.ui.console"; 

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

	public JarToUMLPlugin() {
		// Remember the static instance.
		//
		plugin = this;
        if (console == null) {
            initConsole();
        }
	}

	private void initConsole () {
        console = findConsole(JAR2UML_CONSOLE);
        consoleStream = console.newMessageStream();
        activateConsole();
        consoleStream.println("Jar2UML Console initiated");
        Handler handler = new ConsoleStreamHandler(consoleStream);
        Logger.getLogger(JarToUML.LOGGER).addHandler(handler);
    }

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
}
