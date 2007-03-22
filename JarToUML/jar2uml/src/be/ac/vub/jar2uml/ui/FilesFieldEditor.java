/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package be.ac.vub.jar2uml.ui;

import java.io.File;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * A field editor for a file path type preference. A standard file 
 * dialog appears when the user presses the change button.
 */
public class FilesFieldEditor extends StringButtonFieldEditor {

    /**
     * List of legal file extension suffixes, or <code>null</code>
     * for system defaults.
     */
    private String[] extensions = null;

    /**
     * Indicates whether the path must be absolute;
     * <code>false</code> by default.
     */
    private boolean enforceAbsolute = false;

    /**
     * Creates a new file field editor 
     */
    protected FilesFieldEditor() {
    }

    /**
     * Creates a file field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public FilesFieldEditor(String name, String labelText, Composite parent) {
        this(name, labelText, false, parent);
    }

    /**
     * Creates a file field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param enforceAbsolute <code>true</code> if the file path
     *  must be absolute, and <code>false</code> otherwise
     * @param parent the parent of the field editor's control
     */
    public FilesFieldEditor(String name, String labelText,
            boolean enforceAbsolute, Composite parent) {
        init(name, labelText);
        this.enforceAbsolute = enforceAbsolute;
        setErrorMessage(JFaceResources
                .getString("FileFieldEditor.errorMessage"));//$NON-NLS-1$
        setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
        setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
        createControl(parent);
    }

    /* (non-Javadoc)
     * Method declared on StringButtonFieldEditor.
     * Opens the file chooser dialog and returns the selected file.
     */
    protected String changePressed() {
    	String firstFile = getTextControl().getText();
    	StringTokenizer files = new StringTokenizer(getTextControl().getText(), ";");
    	if (files.hasMoreTokens()) {
    		firstFile = files.nextToken();
    	}
        File f = new File(firstFile);
        if (!f.exists()) {
			f = null;
		}
        File[] d = getFile(f);
        if (d == null) {
			return null;
		}
        StringBuffer paths = new StringBuffer();
        for (int i = 0; i < d.length; i++) {
        	if (d[i] != null) {
        		if (paths.length() > 0) {
        			paths.append(';');
        		}
            	paths.append(d[i].getAbsolutePath());
        	}
        }

        return paths.toString();
    }

    /* (non-Javadoc)
     * Method declared on StringFieldEditor.
     * Checks whether the text input field specifies an existing file.
     */
    protected boolean checkState() {

        String msg = null;

        String path = getTextControl().getText();
        if (path != null) {
			path = path.trim();
		} else {
			path = "";//$NON-NLS-1$
		}
        if (path.length() == 0) {
            if (!isEmptyStringAllowed()) {
				msg = getErrorMessage();
			}
        } else {
        	StringTokenizer files = new StringTokenizer(path, ";");
        	while (files.hasMoreTokens()) {
                File file = new File(files.nextToken());
                if (file.isFile()) {
                    if (enforceAbsolute && !file.isAbsolute()) {
    					msg = JFaceResources
                                .getString("FileFieldEditor.errorMessage2");//$NON-NLS-1$
    				}
                } else {
                    msg = getErrorMessage();
                }
        	}
        }

        if (msg != null) { // error
            showErrorMessage(msg);
            return false;
        }

        // OK!
        clearErrorMessage();
        return true;
    }

    /**
     * Helper to open the file chooser dialog.
     * @param startingDirectory the directory to open the dialog on.
     * @return File The File the user selected or <code>null</code> if they
     * do not.
     */
    private File[] getFile(File startingDirectory) {

        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN + SWT.MULTI);
        if (startingDirectory != null) {
			dialog.setFileName(startingDirectory.getPath());
		}
        if (extensions != null) {
			dialog.setFilterExtensions(extensions);
		}
        String file = dialog.open();
        if (file != null) {
        	String path = dialog.getFilterPath();
        	String[] filenames = dialog.getFileNames();
        	File[] files = new File[filenames.length];
        	for (int i = 0; i < filenames.length; i++) {
                file = filenames[i].trim();
                if (file.length() > 0) {
                	files[i] = new File(path, file);
    			}
        	}
        	return files;
        }

        return null;
    }

    /**
     * Sets this file field editor's file extension filter.
     *
     * @param extensions a list of file extension, or <code>null</code> 
     * to set the filter to the system's default value
     */
    public void setFileExtensions(String[] extensions) {
        this.extensions = extensions;
    }
}
