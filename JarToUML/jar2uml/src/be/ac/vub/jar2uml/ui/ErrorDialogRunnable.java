/*******************************************************************************
 * Copyright (c) 2006-2010 INRIA, Vrije Universiteit Brussel. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     INRIA - Initial API and implementation
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *     
 * $Id: ErrorDialogRunnable.java 7666 2009-02-10 20:54:49Z dwagelaa $
 * 
 ******************************************************************************/
package be.ac.vub.jar2uml.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;

public class ErrorDialogRunnable implements Runnable {

	private IStatus st;

	/**
	 * Creates a new {@link ErrorDialogRunnable}.
	 * @param e The error to report.
	 */
	public ErrorDialogRunnable(Throwable e) {
		String message;
		if (e.getMessage() == null) {
			message = e.getClass().getName(); 
		} else {
			message = e.getMessage();
		}
		st = JarToUMLPlugin.getPlugin().log(message, IStatus.ERROR, e);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ErrorDialog dlg = new ErrorDialog(
				JarToUMLPlugin.getPlugin().getShell(),
				"Error",
				st.getMessage(),
				st,
				IStatus.ERROR);
		dlg.open();
	}

}
